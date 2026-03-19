package manager;

import java.awt.*;

import common.SharedCanvasPainter;
import remote.WhiteboardServer;
import util.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

/**
 * ManagerUI represents the main whiteboard interface for the manager.
 * It supports drawing, broadcasting changes, user management, chat,
 * and saving/loading of whiteboard content.
 */
public class ManagerUI {

    /** Canvas listener handling manager drawing interactions. */
    static ManagerCanvasListener listener;

    /** Canvas dimensions for image export. */
    int width;
    int height;

    /** Current file path used for Save/Save As operations. */
    private String filePath = null;

    /** Shared canvas component for drawing and rendering. */
    static SharedCanvasPainter canvas;

    /** Reference to the whiteboard server for broadcasting events. */
    private final WhiteboardServer server;

    /** Main application frame. */
    public JFrame frmManagerBoard;

    /** UI list of currently connected users. */
    public JList<String> list;

    /** Used to track canvas rendering offsets when frame is moved. */
    public static int currX, currY;

    /** Text area used for displaying chat messages. */
    public static JTextArea chatBox;

    /** Text field used to enter and send chat messages. */
    private JTextField msgField;

    /** Username of the manager. */
    private final String username;

    /**
     * Constructs and initializes the Manager whiteboard interface.
     * @param username the manager's display name
     * @param server the server instance used for broadcasting and coordination
     */
    public ManagerUI(String username, WhiteboardServer server) {
        this.username = username;
        this.server = server;
        initialize();
    }

    /**
     * Initializes the entire UI layout and binds all listeners and components.
     */
    private void initialize() {
        frmManagerBoard = new JFrame();
        frmManagerBoard.setTitle("Whiteboard (Manager)");
        frmManagerBoard.setBounds(100, 100, 1142, 720);
        frmManagerBoard.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frmManagerBoard.getContentPane().setLayout(null);

        listener = new ManagerCanvasListener(frmManagerBoard, canvas, username, server);
        canvas = new SharedCanvasPainter(() -> listener.getRecord());
        listener.setCanvas(canvas);

        canvas.setBounds(110, 0, 822, 692);
        canvas.setBorder(null);
        canvas.setBackground(Color.WHITE);
        canvas.setLayout(null);
        width = canvas.getWidth();
        height = canvas.getHeight();
        frmManagerBoard.getContentPane().add(canvas);
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);

        JPanel panelTool = new JPanel();
        panelTool.setBounds(0, 46, 108, 545);
        panelTool.setLayout(null);
        frmManagerBoard.getContentPane().add(panelTool);

        // Menu dropdown: New, Save, Save As, Open, Exit
        JComboBox<String> menu = new JComboBox<>();
        menu.setBounds(6, 24, 102, 27);
        menu.setModel(new DefaultComboBoxModel<>(new String[]{"New", "Save", "Save as", "Open", "Exit"}));
        menu.addActionListener(_ -> {
            Object selected = menu.getSelectedItem();
            if (selected != null) {
                handleMenuSelection(selected.toString());
            }
        });
        panelTool.add(menu);

        // Drawing tools
        addIconButton(panelTool, "/icon/line.png", "Line", 144);
        addIconButton(panelTool, "/icon/rectangle.png", "Rectangle", 192);
        addIconButton(panelTool, "/icon/triangle.png", "Triangle", 240);
        addIconButton(panelTool, "/icon/oval.png", "Oval", 288);
        addIconButton(panelTool, "/icon/freestyle.png", "Freestyle", 336);
        addIconButton(panelTool, "/icon/eraser.png", "Eraser", 384);

        // Stroke size control
        JLabel strokeSizeLabel = new JLabel("Stroke Size:");
        strokeSizeLabel.setBounds(16, 74, 90, 16);
        panelTool.add(strokeSizeLabel);

        JSlider strokeSlider = new JSlider(1, 30, 5);
        strokeSlider.setBounds(6, 92, 90, 40);
        strokeSlider.setPaintTicks(true);
        strokeSlider.setPaintLabels(true);
        strokeSlider.setMajorTickSpacing(10);
        strokeSlider.setMinorTickSpacing(1);
        panelTool.add(strokeSlider);

        strokeSlider.addChangeListener(_ -> {
            int value = strokeSlider.getValue();
            listener.setStroke(value);
        });

        // Text tool
        JButton btnText = new JButton("text");
        btnText.setActionCommand("Text");
        btnText.setBounds(18, 432, 62, 36);
        panelTool.add(btnText);
        btnText.addActionListener(listener);

        // Color picker
        addIconButton(panelTool, "/icon/color.png", "Color", 503);

        // Room member list
        this.list = new JList<>();
        String[] nameList = {username};
        this.list.setListData(nameList);
        JScrollPane scrollPane = new JScrollPane(this.list);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(935, 57, 201, 165);
        scrollPane.setViewportView(this.list);
        frmManagerBoard.getContentPane().add(scrollPane);

        JLabel lblRoom = new JLabel("Room member: ");
        lblRoom.setBounds(938, 31, 137, 14);
        frmManagerBoard.getContentPane().add(lblRoom);

        // Kick user button
        JButton btnKickOut = new JButton("Kick out!");
        btnKickOut.setBounds(990, 221, 100, 29);
        btnKickOut.addActionListener(_ -> handleKick());
        frmManagerBoard.getContentPane().add(btnKickOut);

        // Chat components
        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);
        JScrollPane scrollPaneChat = new JScrollPane(chatBox);
        scrollPaneChat.setBounds(935, 255, 201, 336);
        frmManagerBoard.getContentPane().add(scrollPaneChat);

        msgField = new JTextField();
        msgField.setBounds(935, 603, 155, 68);
        frmManagerBoard.getContentPane().add(msgField);

        ImageIcon sent = loadIcon("/icon/sent.png");
        if (sent != null) {
            JButton btnSent = new JButton(new ImageIcon(sent.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
            btnSent.setBounds(1091, 603, 45, 65);
            btnSent.addActionListener(_ -> handleSendMessage());
            frmManagerBoard.getContentPane().add(btnSent);
        }

        frmManagerBoard.setVisible(true);
        frmManagerBoard.setResizable(false);

        frmManagerBoard.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        Point canvasOnScreen = canvas.getLocationOnScreen();
                        currX = canvasOnScreen.x;
                        currY = canvasOnScreen.y;
                    } catch (IllegalComponentStateException ex) {
                        // Happens if canvas isn't yet visible
                        System.out.println("Canvas not visible yet. Skipping position update.");
                    }
                });
            }
        });

        frmManagerBoard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        frmManagerBoard,
                        "Are you sure you want to close the whiteboard?\nAll guests will be disconnected.",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    shutdownWhiteboard();
                }
            }
        });
    }

    /** Loads an icon resource from the classpath. */
    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Missing icon resource: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    /** Adds a labeled icon button to the tool panel. */
    private void addIconButton(JPanel panel, String iconPath, String action, int yOffset) {
        ImageIcon icon = loadIcon(iconPath);
        if (icon != null) {
            JButton button = new JButton(new ImageIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
            button.setActionCommand(action);
            button.setBounds(18, yOffset, 62, 36);
            button.addActionListener(listener);
            panel.add(button);
        }
    }

    /** Handles kicking a selected guest from the whiteboard session. */
    private void handleKick() {
        String user = list.getSelectedValue();
        if (user == null) {
            JOptionPane.showMessageDialog(frmManagerBoard, "Please select a user to kick.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (username.equals(user)) {
            JOptionPane.showMessageDialog(frmManagerBoard, "You cannot kick yourself out.", "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            server.kickGuest(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frmManagerBoard, "Failed to kick user '" + user + "'.\nReason: " + ex.getMessage(), "Kick Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Sends a chat message from the manager to all guests. */
    private void handleSendMessage() {
        String rawMsg = msgField.getText();
        if (rawMsg == null || rawMsg.trim().isEmpty()) return;

        Message msg = new Message("chat", username);
        msg.data.put("message", rawMsg);

        try {
            server.broadcast(msg.toJson());
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    frmManagerBoard,
                    "Failed to send message.\n" + ex.getMessage(),
                    "Broadcast Error",
                    JOptionPane.ERROR_MESSAGE
            ));
        }

        msgField.setText("");
    }

    /** Executes logic for menu item selections like Save/Open/New/Exit. */
    private void handleMenuSelection(String selection) {
        switch (selection) {
            case "New":
                canvas.removeAll();
                canvas.updateUI();
                listener.clearRecord();
                filePath = null;
                try {
                    server.broadcast(new Message("new").toJson());
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            frmManagerBoard,
                            "Failed to broadcast 'New' message.\n" + ex.getMessage(),
                            "Broadcast Error",
                            JOptionPane.ERROR_MESSAGE
                    ));
                }
                break;
            case "Save":
                if (filePath == null) {
                    new SaveWindow(this).frameSave.setVisible(true);
                } else {
                    saveFile(filePath);
                }
                break;
            case "Save as":
                new SaveAsWindow(this).frameSaveAs.setVisible(true);
                break;
            case "Open":
                new OpenFileWindow(this).frameOpen.setVisible(true);
                break;
            case "Exit":
                WindowEvent closingEvent = new WindowEvent(frmManagerBoard, WindowEvent.WINDOW_CLOSING);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
                break;
        }
    }

    /** Serializes and saves whiteboard records as JSON. */
    public void saveFile(String path) {
        List<Message> messageList = new ArrayList<>();
        for (String rec : listener.getRecord()) {
            Message msg = new Message("draw", username);
            msg.data.put("record", rec);
            messageList.add(msg);
        }
        if (!path.endsWith(".json")) path += ".json";
        try (Writer writer = new FileWriter(path)) {
            new Gson().toJson(messageList, writer);
            this.filePath = path;
            System.out.println("JSON whiteboard saved: " + path);
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    frmManagerBoard,
                    "Failed to save whiteboard to file." + e.getMessage(),
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    /** Saves the current canvas as an image file (e.g., .png or .jpg). */
    public void saveImg(String path, String format) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG = image.createGraphics();
        tempG.setColor(Color.WHITE);
        tempG.fillRect(0, 0, width, height);
        ArrayList<String> recordList = listener.getRecord();
        canvas.draw(tempG, recordList);

        File outputFile = new File(path + format);
        try {
            boolean success = ImageIO.write(image, format.replace(".", ""), outputFile);
            if (!success) throw new IOException("ImageIO.write returned false.");
            System.out.println("Whiteboard image saved: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    frmManagerBoard,
                    "Failed to save image to:\n" + outputFile.getAbsolutePath() + "\n\n" + e.getMessage(),
                    "Save Image Error",
                    JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    /** Opens a JSON save file and synchronizes it with the whiteboard. */
    public void openFile(String path) {
        canvas.removeAll();
        canvas.repaint();
        listener.clearRecord();
        try (Reader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<Message>>() {}.getType();
            List<Message> messages = new Gson().fromJson(reader, listType);
            for (Message msg : messages) {
                if ("draw".equals(msg.type)) {
                    listener.update((String) msg.data.get("record"));
                }
            }
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            JOptionPane.showMessageDialog(frmManagerBoard, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            server.broadcast(new Message("new").toJson());
            Message batchMsg = new Message("batch", username);
            batchMsg.data.put("records", String.join("\n", listener.getRecord()));
            server.broadcast(batchMsg.toJson());
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    frmManagerBoard,
                    "Failed to sync whiteboard to guests.\n" + e.getMessage(),
                    "Broadcast Failed",
                    JOptionPane.ERROR_MESSAGE
            ));
        }

        canvas.repaint();
    }

    /** Broadcasts shutdown message and exits the application. */
    private void shutdownWhiteboard() {
        Message shutdown = new Message("shutdown", "System");
        shutdown.data.put("message", "The host has closed the whiteboard.");
        try {
            server.broadcast(shutdown.toJson());
            Thread.sleep(1000);
        } catch (IOException | InterruptedException ex) {
            System.out.println("Failed to broadcast shutdown message.");
        }
        System.exit(0);
    }
}
