package guest;

import common.SharedCanvasPainter;
import remote.WhiteboardRemote;
import remote.WhiteboardServer;
import util.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * GuestUI is the main graphical interface for a guest user in the collaborative whiteboard system.
 * It manages drawing tools, canvas rendering, chat functionality, and user presence.
 */
public class GuestUI {

    /** Global static reference to the active GuestUI instance. */
    public static GuestUI gui;

    /** Guest's main whiteboard window. */
    public JFrame frmGuestBoard;

    /** JList to display currently connected users. */
    public JList<String> list;

    /** Tracks current window position for offset calculations in canvas rendering. */
    public static int currX, currY;

    /** Listener responsible for handling mouse and drawing events. */
    public static GuestCanvasListener listener;

    /** Shared canvas rendering component. */
    public static SharedCanvasPainter canvas;

    /** Input field for sending chat messages. */
    public static JTextField msgField;

    /** Text area for receiving chat messages. */
    public static JTextArea chatBox;

    /** RMI reference to the server. */
    public static WhiteboardServer server;

    /** RMI stub that receives remote messages from manager. */
    public static WhiteboardRemote remoteStub;

    /** Local guest username. */
    String username;

    /**
     * Constructs the guest interface and registers with the server.
     * @param server remote reference to the manager's server object
     * @param remoteStub guest's own RMI stub
     * @param username local username
     */
    public GuestUI(WhiteboardServer server, WhiteboardRemote remoteStub, String username) {
        GuestUI.server = server;
        GuestUI.remoteStub = remoteStub;
        GuestUI.gui = this;
        this.username = username;
        initialize();

        try {
            server.handleBegin(username);  // Trigger sync with server
        } catch (Exception e) {
            System.out.println("Failed to initialize whiteboard from server: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to initialize whiteboard from server.\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            });
        }
    }

    /**
     * Initializes the graphical layout, event bindings, drawing tools, chat panel, and window handlers.
     */
    private void initialize() {
        frmGuestBoard = new JFrame();
        frmGuestBoard.setTitle("Whiteboard (" + username + ")");
        frmGuestBoard.setBounds(100, 100, 1142, 720);
        frmGuestBoard.getContentPane().setLayout(null);

        // Drawing setup
        listener = new GuestCanvasListener(frmGuestBoard, canvas, username);
        canvas = new SharedCanvasPainter(() -> listener.getRecord());
        listener.setCanvas(canvas);

        canvas.setBounds(110, 0, 822, 692);
        canvas.setBorder(null);
        canvas.setBackground(Color.WHITE);
        canvas.setLayout(null);
        frmGuestBoard.getContentPane().add(canvas);

        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);

        // Tool panel
        JPanel panelTool = new JPanel();
        panelTool.setBounds(0, 46, 108, 545);
        panelTool.setLayout(null);
        frmGuestBoard.getContentPane().add(panelTool);

        // Tool buttons
        addIconButton(panelTool, "/icon/line.png", "Line", 132);
        addIconButton(panelTool, "/icon/rectangle.png", "Rectangle", 180);
        addIconButton(panelTool, "/icon/triangle.png", "Triangle", 228);
        addIconButton(panelTool, "/icon/oval.png", "Oval", 276);
        addIconButton(panelTool, "/icon/freestyle.png", "Freestyle", 324);
        addIconButton(panelTool, "/icon/eraser.png", "Eraser", 372);

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

        // Text tool button
        JButton btnText = new JButton("text");
        btnText.setActionCommand("Text");
        btnText.setBounds(18, 420, 62, 36);
        btnText.addActionListener(listener);
        panelTool.add(btnText);

        // Color tool
        addIconButton(panelTool, "/icon/color.png", "Color", 503);

        // User list
        this.list = new JList<>();
        String[] nameList = { username };
        this.list.setListData(nameList);
        JScrollPane scrollPane = new JScrollPane(this.list);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(935, 57, 201, 165);
        scrollPane.setViewportView(this.list);
        frmGuestBoard.getContentPane().add(scrollPane);

        JLabel lblRoom = new JLabel("Room member: ");
        lblRoom.setBounds(938, 31, 137, 14);
        frmGuestBoard.getContentPane().add(lblRoom);

        // Chat display
        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);

        JScrollPane scrollPaneChat = new JScrollPane(chatBox);
        scrollPaneChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneChat.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneChat.setBounds(935, 255, 201, 336);
        frmGuestBoard.getContentPane().add(scrollPaneChat);

        // Chat input
        msgField = new JTextField();
        msgField.setBounds(935, 603, 155, 68);
        msgField.setColumns(10);
        frmGuestBoard.getContentPane().add(msgField);

        // Send button
        ImageIcon sent = loadIcon("/icon/sent.png");
        if (sent != null) {
            JButton btnSent = new JButton(new ImageIcon(sent.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
            btnSent.setBounds(1091, 603, 45, 65);
            btnSent.addActionListener(_ -> handleSendMessage());
            frmGuestBoard.getContentPane().add(btnSent);
        }

        // Exit event binding
        frmGuestBoard.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frmGuestBoard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    server.removeClient(username); // Inform manager before closing
                } catch (IOException ex) {
                    System.out.println("Failed to notify server on exit: " + ex.getMessage());
                }
                System.exit(0);
            }
        });

        frmGuestBoard.setVisible(true);
        frmGuestBoard.setResizable(false);

        // Track window position for canvas origin offset
        frmGuestBoard.addComponentListener(new ComponentAdapter() {
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
    }

    /**
     * Adds an icon-based button to the provided panel with an associated tool action.
     * @param panel tool panel
     * @param iconPath resource path for the icon
     * @param action action command to bind
     * @param yOffset vertical placement on the panel
     */
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

    /**
     * Loads an icon from the given resource path.
     * @param path path to the icon image
     * @return loaded icon or null if missing
     */
    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Missing icon resource: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    /**
     * Sends the chat message entered in the input field to the server for broadcast.
     * Shows a dialog on failure and clears the field after sending.
     */
    private void handleSendMessage() {
        String rawMsg = msgField.getText();
        if (rawMsg == null || rawMsg.trim().isEmpty()) return;

        Message msg = new Message("chat");
        msg.sender = JoinWindow.username;
        msg.data.put("message", rawMsg);

        try {
            server.broadcast(msg.toJson());
        } catch (IOException ex) {
            System.out.println("Failed to send chat message: " + ex.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        frmGuestBoard,
                        "Lost connection to the manager. The whiteboard session will now close.",
                        "Manager Disconnected",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(0);
            });
        }
        msgField.setText("");
    }
}