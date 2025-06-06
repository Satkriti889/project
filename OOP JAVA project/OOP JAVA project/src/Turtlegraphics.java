import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import uk.ac.leedsbeckett.oop.LBUGraphics;

public class Turtlegraphics extends LBUGraphics {

    private int turtleAngle = 90;
    private boolean penDown = false;
    private Color penColor = Color.RED;
    private BufferedImage loadedImage = null;
    private int penWidth = 1;

    private final ArrayList<String> commandHistory = new ArrayList<>();

    private int startX;
    private int startY;

    private boolean commandsSaved = false;
    private boolean isLoadingFromFile = false;
    private boolean isImageSaved = true;

    private File lastSavedImageFile = null;
    private File lastSavedCommandsFile = null;

    public Turtlegraphics() {
        JFrame mainFrame = new JFrame("Turtle Graphics");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new FlowLayout());

        mainFrame.add(this);
        mainFrame.pack();
        mainFrame.setVisible(true);

        startX = getWidth() / 2;
        startY = getHeight() / 2;

        setPenColour(penColor);
        penWidth(penWidth);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (loadedImage != null) {
            g.drawImage(loadedImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public void setPenColour(Color c) {
        super.setPenColour(c);
        penColor = c;
    }

    public void penWidth(int width) {
        this.penWidth = width;
        setStroke(width);
    }

    @Override
    public void processCommand(String input) {
        if (input == null || input.trim().isEmpty()) return;

        String trimmedInput = input.trim();
        String lowerCmd = trimmedInput.toLowerCase();

        boolean suppressShow = lowerCmd.equals("save") || lowerCmd.equals("savecommands") || lowerCmd.equals("clear");

        if (!isLoadingFromFile && !suppressShow) {
            commandHistory.add(trimmedInput);
            System.out.println(trimmedInput);
        } else if (lowerCmd.equals("save")) {
            System.out.println(trimmedInput);
        }

        String[] parts = trimmedInput.split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "pendown":
                drawOn();
                penDown = true;
                isImageSaved = false;
                loadedImage = null;
                repaint();
                break;

            case "penup":
                drawOff();
                penDown = false;
                break;

            case "move":
            case "forward":
                if (parts.length < 2) {
                    showErrorDialog("Missing parameter for command: " + command);
                    System.out.println("Invalid move/forward command.");
                    break;
                }
                try {
                    int dist = Integer.parseInt(parts[1]);
                    if (dist < 0) {
                        showErrorDialog("Distance cannot be negative.");
                    } else {
                        double rad = Math.toRadians(turtleAngle);
                        double newX = startX + dist * Math.cos(rad);
                        double newY = startY - dist * Math.sin(rad);

                        int width = getWidth();
                        int height = getHeight();

                        if (newX < 0 || newX > width || newY < 0 || newY > height) {
                            showErrorDialog("Movement out of bounds.");
                        } else {
                            forward(dist);
                            startX = (int)newX;
                            startY = (int)newY;

                            isImageSaved = false;
                            repaint();
                        }
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid number for move/forward distance.");
                }
                break;

            case "reverse":
                int revDist = 100;
                if (parts.length > 1) {
                    try {
                        revDist = Integer.parseInt(parts[1]);
                        if (revDist < 0) {
                            showErrorDialog("Negative distance not allowed.");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        showErrorDialog("Invalid number for reverse distance.");
                        revDist = 100;
                    }
                }
                left(180);
                forward(revDist);
                left(180);
                isImageSaved = false;

                double rad = Math.toRadians(turtleAngle);
                startX = (int)(startX - revDist * Math.cos(rad));
                startY = (int)(startY + revDist * Math.sin(rad));

                repaint();
                break;

            case "left":
                int leftAngle = 90;
                if (parts.length > 1) {
                    try {
                        leftAngle = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        showErrorDialog("Invalid number for left turn angle.");
                    }
                }
                left(leftAngle);
                turtleAngle = (turtleAngle - leftAngle + 360) % 360;
                isImageSaved = false;
                repaint();
                break;

            case "right":
                int rightAngle = 90;
                if (parts.length > 1) {
                    try {
                        rightAngle = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        showErrorDialog("Invalid number for right turn angle.");
                    }
                }
                right(rightAngle);
                turtleAngle = (turtleAngle + rightAngle) % 360;
                isImageSaved = false;
                repaint();
                break;

            case "red":
                setPenColour(Color.RED);
                break;

            case "green":
                setPenColour(Color.GREEN);
                break;

            case "blue":
                setPenColour(Color.BLUE);
                break;

            case "yellow":
                setPenColour(Color.YELLOW);
                break;

            case "clear":
                if (!isImageSaved) {
                    int option = JOptionPane.showConfirmDialog(this,
                            "You have unsaved changes. Are you sure you want to clear?",
                            "Unsaved Changes",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (option != JOptionPane.YES_OPTION) {
                        System.out.println("Clear canceled. Please save your work.");
                        break;
                    }
                }
                clear();
                isImageSaved = true;
                repaint();
                break;

            case "reset":
                reset();
                turtleAngle = 90;
                penColor = Color.RED;
                setPenColour(penColor);
                penWidth = 1;
                penWidth(penWidth);
                startX = getWidth() / 2;
                startY = getHeight() / 2;
                isImageSaved = true;
                repaint();
                break;

            case "about":
                super.about();
                String myName = "Satkriti Khadka";
                String message = "This program is created by: " + myName;
                JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
                System.out.println(message);
                break;

            case "save":
                saveImage();
                isImageSaved = true;
                break;

            case "load":
                if (lastSavedImageFile != null && lastSavedImageFile.exists()) {
                    loadImage(lastSavedImageFile);
                } else {
                    loadImage();
                }
                break;

            case "savecommands":
                saveCommandHistory();
                commandsSaved = true;
                break;

            case "loadcommands":
                if (lastSavedCommandsFile != null && lastSavedCommandsFile.exists()) {
                    loadCommandsFromFile(lastSavedCommandsFile);
                } else {
                    loadCommandsFromDialog();
                }
                break;

            case "square":
                if (parts.length < 2) {
                    showErrorDialog("Square command requires a length.");
                    break;
                }
                try {
                    int length = Integer.parseInt(parts[1]);
                    if (length < 0) {
                        showErrorDialog("Length cannot be negative.");
                        break;
                    }
                    drawSquare(length);
                    isImageSaved = false;
                    repaint();
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid number for square length.");
                }
                break;

            case "pencolour":
                if (parts.length != 4) {
                    showErrorDialog("pencolour command requires 3 RGB values.");
                    break;
                }
                try {
                    int r = Integer.parseInt(parts[1]);
                    int g = Integer.parseInt(parts[2]);
                    int b = Integer.parseInt(parts[3]);

                    if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                        showErrorDialog("RGB values must be between 0 and 255.");
                        break;
                    }

                    Color newColor = new Color(r, g, b);
                    setPenColour(newColor);
                } catch (Exception e) {
                    showErrorDialog("Invalid RGB values.");
                }
                break;

            case "penwidth":
                if (parts.length < 2) {
                    showErrorDialog("penwidth command requires a width value.");
                    break;
                }
                try {
                    int width = Integer.parseInt(parts[1]);
                    if (width <= 0) {
                        showErrorDialog("Width must be positive.");
                    } else {
                        penWidth(width);
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid number for pen width.");
                }
                break;

            case "triangle":
                if (parts.length < 2) {
                    showErrorDialog("Triangle command requires size or sides.");
                    break;
                }
                String args = trimmedInput.substring(command.length()).trim();

                if (args.chars().filter(ch -> ch == ',').count() == 2) {
                    drawCustomTriangle(args);
                } else {
                    drawEquilateralTriangle(args);
                }
                break;

            case "square_spiral":
                if (parts.length < 3) {
                    showErrorDialog("square_spiral command requires turns and initial length.");
                    break;
                }
                try {
                    int turns = Integer.parseInt(parts[1]);
                    int initialLength = Integer.parseInt(parts[2]);
                    drawSquareSpiral(turns, initialLength);
                    isImageSaved = false;
                    repaint();
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid numbers for square_spiral command.");
                }
                break;

            default:
                showErrorDialog("Unknown command: " + input);
                System.out.println("Unknown command: " + input);
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void drawSquare(int length) {
        for (int i = 0; i < 4; i++) {
            forward(length);
            right(90);
        }
    }

    private void drawEquilateralTriangle(String param) {
        try {
            int size = Integer.parseInt(param);
            for (int i = 0; i < 3; i++) {
                forward(size);
                right(120);
            }
        } catch (Exception e) {
        }
    }

    private void drawCustomTriangle(String param) {
        try {
            String[] sides = param.split(",");
            if (sides.length != 3) throw new NumberFormatException();

            int a = Integer.parseInt(sides[0].trim());
            int b = Integer.parseInt(sides[1].trim());
            int c = Integer.parseInt(sides[2].trim());
            if (a + b <= c || a + c <= b || b + c <= a) {
                return;
            }
            forward(a);
            double angleC = Math.toDegrees(Math.acos((a * a + b * b - c * c) / (2.0 * a * b)));
            right((int) Math.round(180 - angleC));
            forward(b);
            double angleA = Math.toDegrees(Math.acos((b * b + c * c - a * a) / (2.0 * b * c)));
            right((int) Math.round(180 - angleA));
            forward(c);
        } catch (Exception e) {
        }
    }

    private void drawSquareSpiral(int turns, int initialLength) {
        int length = initialLength;
        for (int i = 0; i < turns; i++) {
            forward(length);
            right(90);
            length += initialLength;
        }
    }

    private void saveCommandHistory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Commands As");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.setFileFilter(txtFilter);

        if(lastSavedCommandsFile != null && lastSavedCommandsFile.getParentFile() != null) {
            fileChooser.setCurrentDirectory(lastSavedCommandsFile.getParentFile());
        }

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                fileToSave = new File(path + ".txt");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (String cmd : commandHistory) {
                    writer.write(cmd);
                    writer.newLine();
                }
                System.out.println("Commands saved to " + fileToSave.getAbsolutePath());

                lastSavedCommandsFile = fileToSave;
                commandsSaved = true;
            } catch (IOException e) {
                showErrorDialog("Error saving commands: " + e.getMessage());
            }
        }
    }

    private void loadCommandsFromDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Commands File");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.setFileFilter(txtFilter);

        if(lastSavedCommandsFile != null && lastSavedCommandsFile.getParentFile() != null) {
            fileChooser.setCurrentDirectory(lastSavedCommandsFile.getParentFile());
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }

        int userSelection = fileChooser.showOpenDialog(this);
        if(userSelection == JFileChooser.APPROVE_OPTION) {
            File commandsFile = fileChooser.getSelectedFile();
            loadCommandsFromFile(commandsFile);
        }
    }

    private void loadCommandsFromFile(File commandsFile) {
        if(commandsFile == null || !commandsFile.exists()) {
            showErrorDialog("Selected commands file does not exist.");
            return;
        }

        isLoadingFromFile = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(commandsFile))) {
            commandHistory.clear();

            clear();
            drawOff();

            loadedImage = null;

            turtleAngle = 90;
            penDown = false;
            penColor = Color.RED;
            setPenColour(penColor);
            penWidth = 1;
            penWidth(penWidth);

            startX = getWidth() / 2;
            startY = getHeight() / 2;

            repaint();

            String line;
            while ((line = reader.readLine()) != null) {
                commandHistory.add(line);
                processCommand(line);
            }

            lastSavedCommandsFile = commandsFile;
        } catch (IOException e) {
            showErrorDialog("Failed to load commands: " + e.getMessage());
        } finally {
            isLoadingFromFile = false;
            repaint();
        }
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Image");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Image (*.png)", "png");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.setFileFilter(pngFilter);

        if(lastSavedImageFile != null && lastSavedImageFile.getParentFile() != null) {
            fileChooser.setCurrentDirectory(lastSavedImageFile.getParentFile());
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }

        int userSelection = fileChooser.showOpenDialog(this);
        if(userSelection == JFileChooser.APPROVE_OPTION) {
            File inputFile = fileChooser.getSelectedFile();
            loadImage(inputFile);
        }
    }

    private void loadImage(File inputFile) {
        if(inputFile == null || !inputFile.exists()) {
            showErrorDialog("Selected image file does not exist.");
            return;
        }
        try {
            loadedImage = ImageIO.read(inputFile);
            if (loadedImage == null) {
                showErrorDialog("Failed to load image. The file may be corrupted.");
                return;
            }

            clear();
            drawOff();
            turtleAngle = 90;

            startX = getWidth() / 2;
            startY = getHeight() / 2;

            isImageSaved = true;
            repaint();

            lastSavedImageFile = inputFile;
            System.out.println("Image loaded from " + inputFile.getAbsolutePath());
        } catch (IOException e) {
            showErrorDialog("Error loading image: " + e.getMessage());
        }
    }

    private void saveImage() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            showErrorDialog("Cannot save image: Invalid canvas size.");
            return;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        paint(g);
        g.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image As");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Image (*.png)", "png");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.setFileFilter(pngFilter);

        if(lastSavedImageFile != null && lastSavedImageFile.getParentFile() != null) {
            fileChooser.setCurrentDirectory(lastSavedImageFile.getParentFile());
        }

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png")) {
                fileToSave = new File(path + ".png");
            }

            try {
                ImageIO.write(image, "png", fileToSave);
                System.out.println("Image saved as " + fileToSave.getAbsolutePath());

                lastSavedImageFile = fileToSave;
            } catch (IOException e) {
                showErrorDialog("Error saving image: " + e.getMessage());
            }
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void about() {
        super.about();
        String myName = "Satkriti Khadka";
        String message = "This program is created by: " + myName;
        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
        System.out.println(message);
    }
}
