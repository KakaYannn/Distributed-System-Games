package common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * AbstractCanvasListener provides shared drawing behavior for both manager and guest users.
 * It handles mouse and action events for tools including Line, Oval, Rectangle, Triangle, Freestyle, Eraser, and Text.
 * Subclasses must implement sendDrawMessage() to define how drawing instructions are sent to the remote counterpart.
 */
public abstract class AbstractCanvasListener implements MouseListener, MouseMotionListener, ActionListener {

    /** The parent frame used for dialog and UI context. */
    protected final JFrame frame;

    /** The canvas where drawing components are rendered. May be initialized later via setCanvas(). */
    protected JPanel canvas;

    /** The username of the local user (manager or guest). */
    protected final String username;

    /** Current stroke size for drawing. */
    protected int stroke = 5;

    /** Current color for drawing. */
    protected Color color = Color.BLACK;

    /** Current selected tool type (e.g., "Line", "Freestyle", etc.). */
    protected Object toolType = "Line";

    /** The local list of drawing instructions (strings in custom format). */
    protected final ArrayList<String> recordList = new ArrayList<>();

    /** Coordinates used for drawing actions. */
    protected int startX, startY, endX, endY;

    /**
     * Constructs a canvas listener with the specified UI components and user identity.
     * @param frame the parent window frame
     * @param canvas the drawing canvas (may be null and later set via setCanvas)
     * @param username the local username
     */
    public AbstractCanvasListener(JFrame frame, JPanel canvas, String username) {
        this.frame = frame;
        this.canvas = canvas;
        this.username = username;
    }

    /** Returns the local list of drawing records. */
    public synchronized ArrayList<String> getRecord() {
        return recordList;
    }

    /** Clears the local drawing record list. */
    public void clearRecord() {
        recordList.clear();
    }

    /** Sets the current stroke size. */
    public void setStroke(int t) {
        this.stroke = t;
    }

    /** Converts a Color object to its "R G B" string representation. */
    protected String getColor(Color color) {
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
    }

    /**
     * Abstract method that must be implemented to handle how drawing instructions
     * are sent to the remote party (e.g., via RMI).
     * @param record the drawing command in string format
     */
    protected abstract void sendDrawMessage(String record);

    /**
     * Adds a new record to the local drawing list.
     * @param record the drawing instruction
     */
    public synchronized void update(String record) {
        recordList.add(record);
    }

    /** Handles tool button actions and color selection. */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Color".equals(e.getActionCommand())) {
            Color chosen = JColorChooser.showDialog(frame, "Choose Color", color);
            if (chosen != null) {
                color = chosen;
            }
        } else {
            this.toolType = e.getActionCommand();
            frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Handles mouse press events to initiate drawing. */
    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        if (toolType.equals("Freestyle") || toolType.equals("Eraser")) {
            drawSegment(startX, startY, startX, startY, toolType.equals("Eraser"));
        }
    }

    /** Handles mouse release events to complete drawing shapes or trigger text input. */
    @Override
    public void mouseReleased(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();

        if (toolType.equals("Text")) {
            JTextField textField = new JTextField();
            textField.setBounds(endX, endY, 150, 25);
            canvas.add(textField);
            textField.requestFocus();
            textField.addActionListener(_ -> {
                String input = textField.getText().trim();
                if (!input.isEmpty()) {
                    String rgb = getColor(color);
                    String record = "Text " + stroke + " " + rgb + " " + endX + " " + endY + " :::" + input;
                    sendDrawMessage(record);
                }
                canvas.remove(textField);
                canvas.repaint();
            });
            return;
        }

        if (toolType.equals("Line") || toolType.equals("Oval") || toolType.equals("Rectangle") || toolType.equals("Triangle")) {
            String rgb = getColor(color);
            String record = toolType + " " + stroke + " " + rgb + " " + startX + " " + startY + " " + endX + " " + endY;
            sendDrawMessage(record);
        }
    }

    /** Handles dragging behavior for freestyle and eraser tools. */
    @Override
    public void mouseDragged(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();
        if (toolType.equals("Freestyle") || toolType.equals("Eraser")) {
            drawSegment(startX, startY, endX, endY, toolType.equals("Eraser"));
            startX = endX;
            startY = endY;
        }
    }

    /**
     * Sends a line segment drawing instruction based on current tool and color.
     * Used during dragging for freestyle and erasing.
     */
    private void drawSegment(int x1, int y1, int x2, int y2, boolean isErase) {
        Color drawColor = isErase ? Color.WHITE : color;
        String rgb = getColor(drawColor);
        String rec = "Line " + stroke + " " + rgb + " " + x1 + " " + y1 + " " + x2 + " " + y2;
        sendDrawMessage(rec);
    }

    // Empty implementations for unused mouse events
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}

    /** Sets the canvas component reference after construction, if needed. */
    public void setCanvas(JPanel canvas) {
        this.canvas = canvas;
    }
}
