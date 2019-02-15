package top.amot.forceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.SparseIntArray;

import java.util.List;

public class ForceDrawer {

    private static final int DEFAULT_TEXT_SIZE_SP = 13;
    private static final int LOW = -1;

    private Paint paint;
    private Paint textPaint;
    private Paint linkPaint;
    private Paint linkTextPaint;

    private Path arrowPath = new Path();
    private SparseIntArray colors;

    private Context context;
    private float padding = 5f;
    private float textBaseline;
    private float textHeight;
    private float linkTextBaseline;

    private Node selectedNode;

    ForceDrawer(Context context) {
        this.context = context;

        colors = new SparseIntArray();
        colors.put(0, 0xFFFD6D59);
        colors.put(1, 0xFFFDB40A);
        colors.put(2, 0xFF6CC354);
        colors.put(3, 0xFF508CD8);
        colors.put(4, 0xFF443294);
        colors.put(LOW, 0xFF984387);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp2px(DEFAULT_TEXT_SIZE_SP));
        textPaint.setColor(Color.WHITE);

        linkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        linkTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linkTextPaint.setTextAlign(Paint.Align.CENTER);
        linkTextPaint.setTextSize(sp2px(DEFAULT_TEXT_SIZE_SP));

        measureText();

    }

    void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    void drawLinks(Canvas canvas, Link[] links) {
        if (links == null || links.length == 0) {
            return;
        }

        linkPaint.setColor(Color.GRAY);
        linkTextPaint.setColor(Color.GRAY);
        for (int i = 0; i < links.length; i++) {
            drawLink(canvas, links[i]);
        }
    }

    void drawLinks(Canvas canvas, List<Link> links) {
        if (links == null || links.isEmpty()) {
            return;
        }
        linkPaint.setColor(Color.GRAY);
        linkTextPaint.setColor(Color.GRAY);
        for (int i = 0; i < links.size(); i++) {
            drawLink(canvas, links.get(i));
        }
    }

    void drawLink(Canvas canvas, Link link) {
        if (link == null) {
            return;
        }

        float startX = (float) link.source.x;
        float startY = (float) link.source.y;
        float stopX = (float) link.target.x;
        float stopY = (float) link.target.y;

        int color = Color.GRAY;
        if (selectedNode != null && selectedNode == link.source || selectedNode == link.target) {
            color = getColor(link.source.level);
        }

        linkPaint.setColor(color);
        linkTextPaint.setColor(color);

        canvas.drawLine(startX, startY, stopX, stopY, linkPaint);

        // draw arrow
        float length = link.length();
        float ratio = (length - link.target.radius) / length;
        float ax1 = (stopX - startX) * ratio + startX;
        float ay1 = (stopY - startY) * ratio + startY;

        ratio = (length - link.target.radius - dp2px(10)) / length;
        float ax3 = (stopX - startX) * ratio + startX;
        float ay3 = (stopY - startY) * ratio + startY;

        ratio = (length - link.target.radius - dp2px(12)) / length;
        float ax24 = (stopX - startX) * ratio + startX;
        float ay24 = (stopY - startY) * ratio + startY;

        float l = dp2px(5);
        float dx = (stopY - startY) / length * l;
        float dy = (startX - stopX) / length * l;

        float ax2 = ax24 - dx;
        float ay2 = ay24 - dy;
        float ax4 = ax24 + dx;
        float ay4 = ay24 + dy;

        arrowPath.reset();
        arrowPath.moveTo(ax1, ay1);
        arrowPath.lineTo(ax2, ay2);
        arrowPath.lineTo(ax3, ay3);
        arrowPath.lineTo(ax4, ay4);
        arrowPath.close();

        canvas.drawPath(arrowPath, linkPaint);

        // draw link text
        String linkText = link.text;
        if (linkText != null && linkText.trim().length() > 0) {
            ratio = (length - link.target.radius - length / 5f) / length;
            float textX = (stopX - startX) * ratio + startX;
            float textY = (stopY - startY) * ratio + startY;
            canvas.drawText(linkText, textX, textY - linkTextBaseline, linkTextPaint);
        }
    }

    void drawNodes(Canvas canvas, Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return;
        }

        for (int i = 0; i < nodes.length; i++) {
            drawNode(canvas, nodes[i]);
        }
    }

    void drawNodes(Canvas canvas, List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            drawNode(canvas, nodes.get(i));
        }
    }

    void drawNode(Canvas canvas, Node node) {
        if (node == null) {
            return;
        }

        float cx = (float) node.x;
        float cy = (float) node.y;

        paint.setColor(getColor(node.level));
        canvas.drawCircle(cx, cy, node.radius, paint);

        String text = node.text;
        if (text == null) {
            text = "";
        }
        double w = Math.sqrt(4 * node.radius * node.radius - textHeight * textHeight) - padding * 2;
        float textWidth = textPaint.measureText(text);
        float n;
        if (w >= textWidth) {
            canvas.drawText(text, cx, cy - textBaseline, textPaint);
        } else {
            float th = textHeight * 2;
            w = Math.sqrt(4 * node.radius * node.radius - th * th) - padding * 2;

            n = (float) w / textWidth;
            int end = (int) (text.length() * n);
            if (end < text.length() - 1) {
                canvas.drawText(text.substring(0, end), cx, cy - textHeight * 0.5f - textBaseline, textPaint);
                String t;
                if (textWidth > 2 * w) {
                    t = text.substring(end, 2 * end - 1) + "...";
                } else {
                    t = text.substring(end);
                }
                canvas.drawText(t, cx, cy + textHeight * 0.5f - textBaseline, textPaint);
            } else {
                canvas.drawText(text, cx, cy - textBaseline, textPaint);
            }
        }
    }

    private void measureText() {
        Paint.FontMetrics fontMetrics;

        fontMetrics = textPaint.getFontMetrics();
        textBaseline = (fontMetrics.bottom + fontMetrics.top) * 0.5f;
        textHeight = fontMetrics.bottom - fontMetrics.top;

        fontMetrics = linkTextPaint.getFontMetrics();
        linkTextBaseline = (fontMetrics.bottom + fontMetrics.top) * 0.5f;
    }

    private int getColor(int level) {
        if (level >= colors.size() - 1 || level < 0) {
            level = LOW;
        }
        return colors.get(level);
    }

    private int sp2px(double sp) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    private int dp2px(double dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
