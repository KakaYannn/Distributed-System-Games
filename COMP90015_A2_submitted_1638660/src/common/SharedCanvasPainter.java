package common;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * SharedCanvasPainter is a reusable JPanel component that renders drawing records
 * provided by a dynamic record supplier. It supports rendering shapes such as lines,
 * ovals, rectangles, triangles, and text using a custom string-based instruction format.
 */
public class SharedCanvasPainter extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Supplies the current list of drawing records to be rendered on the canvas. */
    private final Supplier<ArrayList<String>> recordSupplier;

    /**
     * Constructs a SharedCanvasPainter with a given record supplier.
     * @param recordSupplier a function that returns the current drawing record list
     */
    public SharedCanvasPainter(Supplier<ArrayList<String>> recordSupplier) {
        this.recordSupplier = recordSupplier;
    }

    /**
     * Called automatically by Swing when the panel needs to be redrawn.
     * Delegates to the draw() method with the latest drawing records.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        ArrayList<String> drawing = recordSupplier.get();
        synchronized (drawing) {
            draw(g2d, drawing);
        }
    }

    /**
     * Parses and renders the given list of drawing commands using Graphics2D.
     * Supported shapes include Line, Oval, Rectangle, Triangle, and Text.
     * @param g the graphics context to render with
     * @param recordList the list of drawing instructions
     */
    public void draw(Graphics2D g, ArrayList<String> recordList) {
        List<String> safeCopy = new ArrayList<>(recordList);
        for (String line : safeCopy) {
            if (line == null || line.trim().isEmpty()) continue;

            int textStartIndex = line.indexOf(":::");
            String[] record = (textStartIndex == -1)
                    ? line.split(" ")
                    : line.substring(0, textStartIndex).trim().split(" ");
            if (record.length < 2) continue;

            String type = record[0];
            int stroke, red, green, blue, startX, startY, endX = 0, endY = 0;

            try {
                stroke = Integer.parseInt(record[1]);
                red = Integer.parseInt(record[2]);
                green = Integer.parseInt(record[3]);
                blue = Integer.parseInt(record[4]);
                startX = Integer.parseInt(record[5]);
                startY = Integer.parseInt(record[6]);

                g.setColor(new Color(red, green, blue));
                g.setStroke(new BasicStroke(stroke));

                switch (type) {
                    case "Line":
                    case "Oval":
                    case "Rectangle":
                    case "Triangle":
                        if (record.length < 9) continue;
                        endX = Integer.parseInt(record[7]);
                        endY = Integer.parseInt(record[8]);
                        break;
                    case "Text":
                        if (textStartIndex == -1) continue;
                        break;
                    default:
                        continue;
                }

                switch (type) {
                    case "Line":
                        g.drawLine(startX, startY, endX, endY);
                        break;
                    case "Oval":
                        g.drawOval(Math.min(startX, endX), Math.min(startY, endY),
                                Math.abs(startX - endX), Math.abs(startY - endY));
                        break;
                    case "Rectangle":
                        g.drawRect(Math.min(startX, endX), Math.min(startY, endY),
                                Math.abs(endX - startX), Math.abs(endY - startY));
                        break;
                    case "Triangle":
                        g.drawLine(startX, startY, endX, endY);
                        g.drawLine(startX, endY, endX, endY);
                        g.drawLine(startX, startY, startX, endY);
                        break;
                    case "Text":
                        String text = line.substring(textStartIndex + 3).trim();
                        g.setFont(new Font(null, Font.PLAIN, stroke + 10));
                        g.drawString(text, startX, startY);
                        break;
                }
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                System.out.println("Failed to parse draw record: " + line);
            }
        }
    }
}
