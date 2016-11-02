package top.amot.library.view.force;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;

/**
 * Created by Z.pan on 2016/11/2.
 */

public final class ForceDrawer {

    private static final int[] colors = {
            Color.parseColor("#f09d24"),
            Color.parseColor("#d95c8a"),
            Color.parseColor("#13a1e1"),
            Color.parseColor("#8bc34a"),
            Color.parseColor("#8d6e63")
    };

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path arrowPath = new Path();

    private Context context;
    private float padding = 5f;
    private float textBaseline;
    private float textHeight;
    private float linkTextBaseline;

    ForceDrawer(Context context) {
        this.context = context;
        initPaint();
        measureText();
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

    void drawLinks(Canvas canvas, List<FLink> links, FNode selectedNode) {
        if (links == null) {
            return;
        }

        linkPaint.setColor(Color.GRAY);
        linkTextPaint.setColor(Color.GRAY);
        for (int i = 0; i < links.size(); i++) {
            FLink link = links.get(i);
            drawLink(canvas, link, selectedNode);
        }
    }

    void drawLink(Canvas canvas, FLink link, FNode selectedNode) {
        if (link == null) {
            return;
        }

        float startX = link.source.snapshotX; //x;
        float startY = link.source.snapshotY; //y;
        float stopX = link.target.snapshotX; //x;
        float stopY = link.target.snapshotY; //y;

        int color = Color.GRAY;
        if (selectedNode != null && selectedNode == link.source || selectedNode == link.target) {
            color = getColor(link.source.getLevel());
        }

        linkPaint.setColor(color);
        linkTextPaint.setColor(color);

        canvas.drawLine(startX, startY, stopX, stopY, linkPaint);

        // draw arrow
        double distance = link.getNodeDistance();
        float ratio = (float) ((distance - link.target.getRadius()) / distance);
        float ax1 = (stopX - startX) * ratio + startX;
        float ay1 = (stopY - startY) * ratio + startY;

        ratio = (float) ((distance - link.target.getRadius() - dp2px(10)) / distance);
        float ax3 = (stopX - startX) * ratio + startX;
        float ay3 = (stopY - startY) * ratio + startY;

        ratio = (float) ((distance - link.target.getRadius() - dp2px(12)) / distance);
        float ax24 = (stopX - startX) * ratio + startX;
        float ay24 = (stopY - startY) * ratio + startY;

        float l = dp2px(5);
        float dx = (stopY - startY) / (float) distance * l;
        float dy = (startX - stopX) / (float) distance * l;

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
            ratio = (float) ((distance - link.target.getRadius() - distance / 5f) / distance);
            float textX = (stopX - startX) * ratio + startX;
            float textY = (stopY - startY) * ratio + startY;
            canvas.drawText(linkText, textX, textY - linkTextBaseline, linkTextPaint);
        }
    }

    void drawNodes(Canvas canvas, List<FNode> nodes, boolean drawStroke) {
        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            FNode node = nodes.get(i);
            drawNode(canvas, node, drawStroke);
        }
    }

    void drawNode(Canvas canvas, FNode node, boolean drawStroke) {
        if (node == null) {
            return;
        }

        float cx = node.snapshotX; //x;
        float cy = node.snapshotY; //y;

        resetPaintColor(node.getLevel());
        canvas.drawCircle(cx, cy, node.getRadius(), paint);

        String text = node.getText();
        double w = Math.sqrt(4 * node.getRadius() * node.getRadius() - textHeight * textHeight) - padding * 2;
        float textWidth = textPaint.measureText(text);
        float n;
        if (w >= textWidth) {
            canvas.drawText(text, cx, cy - textBaseline, textPaint);
        } else {
            float th = textHeight * 2;
            w = Math.sqrt(4 * node.getRadius() * node.getRadius() - th * th) - padding * 2;

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
            int strokeColor = getColor(node.getLevel());
            strokeColor = Color.argb(128, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor));
            strokePaint.setColor(strokeColor);
            canvas.drawCircle(cx, cy, node.getRadius() + 5, strokePaint);
        }
    }

    private void resetPaintColor(int level) {
        int color = getColor(level);
        paint.setColor(color);
        textPaint.setColor(Color.WHITE);
//        textPaint.setAlpha(192);
        strokePaint.setColor(color);
    }

    private int getColor(int level) {
        return colors[level % colors.length];
    }

    private int dp2px(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
