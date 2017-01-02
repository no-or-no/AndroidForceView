package top.amot.library.view.force;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.SparseIntArray;

import java.util.List;

import top.amot.library.R;

/**
 * Created by Z.pan on 2016/11/2.
 */

final class ForceDrawer {

    private static final int LOW = -1;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path arrowPath = new Path();
    private SparseIntArray colors = new SparseIntArray();

    private Context context;
    private float padding = 5f;
    private float textBaseline;
    private float textHeight;
    private float linkTextBaseline;

    private Node selectedNode;

    ForceDrawer(Context context) {
        this.context = context;
        initPaint();
        measureText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors.put(0, context.getColor(R.color.level_1));
            colors.put(1, context.getColor(R.color.level_2));
            colors.put(2, context.getColor(R.color.level_3));
            colors.put(3, context.getColor(R.color.level_4));
            colors.put(4, context.getColor(R.color.level_5));
            colors.put(LOW, context.getColor(R.color.level_low));
        } else {
            colors.put(0, context.getResources().getColor(R.color.level_1));
            colors.put(1, context.getResources().getColor(R.color.level_2));
            colors.put(2, context.getResources().getColor(R.color.level_3));
            colors.put(3, context.getResources().getColor(R.color.level_4));
            colors.put(4, context.getResources().getColor(R.color.level_5));
            colors.put(LOW, context.getResources().getColor(R.color.level_low));
        }

    }

    void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    void drawLinks(Canvas canvas, List<Link> links) {
        if (links == null || links.isEmpty()) {
            return;
        }

        linkPaint.setColor(Color.GRAY);
        linkTextPaint.setColor(Color.GRAY);
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            drawLink(canvas, link);
        }
    }

    void drawLink(Canvas canvas, Link link) {
        if (link == null) {
            return;
        }

        float startX = link.source.x;
        float startY = link.source.y;
        float stopX = link.target.x;
        float stopY = link.target.y;

        int color = Color.GRAY;
        if (selectedNode != null && selectedNode == link.source || selectedNode == link.target) {
            color = getColor(link.source.level);
        }

        linkPaint.setColor(color);
        linkTextPaint.setColor(color);

        canvas.drawLine(startX, startY, stopX, stopY, linkPaint);

        // draw arrow
        float length = link.length();
        float ratio = (float) ((length - link.target.radius) / length);
        float ax1 = (stopX - startX) * ratio + startX;
        float ay1 = (stopY - startY) * ratio + startY;

        ratio = (float) ((length - link.target.radius - dp2px(10)) / length);
        float ax3 = (stopX - startX) * ratio + startX;
        float ay3 = (stopY - startY) * ratio + startY;

        ratio = (float) ((length - link.target.radius - dp2px(12)) / length);
        float ax24 = (stopX - startX) * ratio + startX;
        float ay24 = (stopY - startY) * ratio + startY;

        float l = dp2px(5);
        float dx = (stopY - startY) / (float) length * l;
        float dy = (startX - stopX) / (float) length * l;

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
        String linkText = link.getText();
        if (linkText != null && linkText.trim().length() > 0) {
            ratio = (float) ((length - link.target.radius - length / 5f) / length);
            float textX = (stopX - startX) * ratio + startX;
            float textY = (stopY - startY) * ratio + startY;
            canvas.drawText(linkText, textX, textY - linkTextBaseline, linkTextPaint);
        }
    }

    void drawNodes(Canvas canvas, List<Node> nodes) {
        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            drawNode(canvas, node, false);
        }
    }

    void drawNode(Canvas canvas, Node node, boolean drawStroke) {
        if (node == null) {
            return;
        }

        float cx = node.x; //x;
        float cy = node.y; //y;

        int color = getColor(node.level);
        paint.setColor(color);
        textPaint.setColor(Color.WHITE);
//        textPaint.setAlpha(192);
        strokePaint.setColor(color);
        canvas.drawCircle(cx, cy, node.radius, paint);

        String text = node.getText();
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
        if (drawStroke) {
            int strokeColor = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
            strokePaint.setColor(strokeColor);
            canvas.drawCircle(cx, cy, node.radius + 5, strokePaint);
        }
    }

    private void initPaint() {
        paint.setColor(Color.LTGRAY);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStrokeWidth(5f);
        strokePaint.setStyle(Paint.Style.STROKE);

        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp2px(13));
        textPaint.setColor(Color.BLUE);

        linkPaint.setAntiAlias(true);

        linkTextPaint.setAntiAlias(true);
        linkTextPaint.setTextAlign(Paint.Align.CENTER);
        linkTextPaint.setTextSize(dp2px(13));
    }

    private void measureText() {
        Paint.FontMetrics fontMetrics;

        fontMetrics= textPaint.getFontMetrics();
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

    private int dp2px(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
