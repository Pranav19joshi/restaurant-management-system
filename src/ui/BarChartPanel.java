package ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BarChartPanel — Unit IV: Swing, paintComponent, Graphics2D
 * A reusable JPanel that draws a bar chart for any Map<String, Double>.
 * Call setData() to supply new data; the panel repaints itself automatically.
 */
public class BarChartPanel extends JPanel {

    private Map<String, Double> data = new LinkedHashMap<>();

    // Palette: one colour per bar, cycling through the list
    private static final Color[] BAR_COLORS = {
        new Color(99, 179, 237),   // sky blue
        new Color(154, 117, 235),  // purple
        new Color(72, 207, 173),   // teal
        new Color(252, 182, 72),   // amber
        new Color(248, 113, 113),  // red
        new Color(74, 222, 128),   // green
    };

    public BarChartPanel() {
        setBackground(new Color(28, 28, 40));
        setPreferredSize(new Dimension(500, 220));
    }

    /** Replace the chart data and trigger a repaint. */
    public void setData(Map<String, Double> data) {
        this.data = data != null ? data : new LinkedHashMap<>();
        repaint();
    }

    // Unit IV – paintComponent: draw bars using Graphics2D
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.setColor(new Color(120, 120, 140));
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padLeft  = 40;
        int padBot   = 45;
        int padTop   = 20;
        int padRight = 20;

        int chartW = getWidth()  - padLeft - padRight;
        int chartH = getHeight() - padBot  - padTop;

        double maxVal = data.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(1);

        int n = data.size();
        int gap = 12;
        int barWidth = (chartW - gap * (n + 1)) / n;
        if (barWidth < 6) barWidth = 6;

        // Y-axis grid lines + labels
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= 4; i++) {
            int y = padTop + chartH - (int)(chartH * i / 4.0);
            g2.setColor(new Color(60, 60, 80));
            g2.drawLine(padLeft, y, padLeft + chartW, y);
            g2.setColor(new Color(140, 140, 160));
            String lbl = String.format("%.0f", maxVal * i / 4);
            g2.drawString(lbl, 2, y + 4);
        }

        // Bars
        int colorIdx = 0;
        int x = padLeft + gap;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int barH = (int)((entry.getValue() / maxVal) * chartH);
            int y    = padTop + chartH - barH;

            Color barColor = BAR_COLORS[colorIdx % BAR_COLORS.length];
            g2.setColor(barColor);
            g2.fillRoundRect(x, y, barWidth, barH, 6, 6);

            // Value label on top of bar
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String valStr = String.format("%.0f", entry.getValue());
            int valX = x + (barWidth - g2.getFontMetrics().stringWidth(valStr)) / 2;
            if (barH > 16) g2.drawString(valStr, valX, y + 13);

            // Category label below bar
            g2.setColor(new Color(180, 180, 200));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String label = entry.getKey().length() > 10
                    ? entry.getKey().substring(0, 9) + "." : entry.getKey();
            int lblX = x + (barWidth - g2.getFontMetrics().stringWidth(label)) / 2;
            g2.drawString(label, lblX, padTop + chartH + 15);

            x += barWidth + gap;
            colorIdx++;
        }

        // X-axis line
        g2.setColor(new Color(80, 80, 100));
        g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);
    }
}
