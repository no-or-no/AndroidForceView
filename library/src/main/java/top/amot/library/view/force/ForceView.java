package top.amot.library.view.force;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Force layout from D3.js
 *
 * Created by Z.Pan on 2016/10/8.
 */
public class ForceView extends View implements ForceListener {

    private static final int[] colors = {
            Color.parseColor("#e75988"),
            Color.parseColor("#d4d9ae"),
            Color.parseColor("#b9d6e5"),
            Color.parseColor("#e5b9ce")
    };

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Force force;
    private float textBaseline;

    private float touchSlop;
    private FNode node;
    private float downX, downY;
    private float dx, dy;
    private float x0, y0;
    private List<FNode> selectedNodes = new ArrayList<>();
    private List<FLink> selectedLinks = new ArrayList<>();

    public ForceView(Context context) {
        this(context, null);
    }

    public ForceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.LTGRAY);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStrokeWidth(2f);
        strokePaint.setStyle(Paint.Style.STROKE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(40f);
        textPaint.setColor(Color.BLUE);
        linkPaint.setAntiAlias(true);

        Paint.FontMetricsInt anInt = textPaint.getFontMetricsInt();
        textBaseline = (anInt.bottom + anInt.top) / 2f + 5f;

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        force = new Force(this)
                .setStrength(0.5f)
                .setFriction(0.9f)
                .setDistance(50)
                .setCharge(-5000f)
                .setGravity(0.1f)
                .setTheta(0.8f)
                .setAlpha(0.1f);

        post(new Runnable() {
            @Override
            public void run() {
                force.setSize(getWidth(), getHeight()).start();
            }
        });

    }

    public void setData(List<FNode> nodes, List<FLink> links) {
        force.setNodes(nodes).setLinks(links).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(dx, dy);

        linkPaint.setColor(Color.LTGRAY);
        drawLinks(canvas, force.links);

        if (!selectedLinks.isEmpty()) {
            linkPaint.setColor(Color.RED);
            drawLinks(canvas, selectedLinks);
        }

        drawNodes(canvas, force.nodes);

        if (!selectedNodes.isEmpty()) {
            strokePaint.setColor(Color.RED);
            textPaint.setColor(Color.RED);
            drawNodes(canvas, selectedNodes);
        }

        canvas.restore();

    }

    private void drawLinks(Canvas canvas, List<FLink> links) {
        if (links == null) {
            return;
        }
        for (int i = 0; i < links.size(); i++) {
            FLink link = links.get(i);
            float startX = link.source.x;
            float startY = link.source.y;
            float stopX = link.target.x;
            float stopY = link.target.y;
            canvas.drawLine(startX, startY, stopX, stopY, linkPaint);
        }
    }

    private void drawNodes(Canvas canvas, List<FNode> nodes) {
        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            FNode node = nodes.get(i);
            resetPaintColor(node.getLevel());
            float cx = node.x;
            float cy = node.y;
            canvas.drawCircle(cx, cy, node.getRadius(), paint);
            canvas.drawText(node.getText(), cx, cy - textBaseline, textPaint);
//            canvas.drawCircle(cx, cy, node.getRadius(), strokePaint);
        }
    }

    private void resetPaintColor(int level) {
        paint.setColor(colors[(level - 1) % colors.length]);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha(192);
//        strokePaint.setColor(colors[(level - 1) % colors.length]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x0 = downX = x;
                y0 = downY = y;
                node = force.getNode(x - dx, y - dy);
                if (node != null) {
                    selectedNodes.add(node);
                    for (int i = 0, size = force.links.size(); i < size; i++) {
                        FLink link = force.links.get(i);
                        if (link.source == node || link.target == node) {
                            if (link.source != node) {
                                selectedNodes.add(link.source);
                            } else if (link.target != node) {
                                selectedNodes.add(link.target);
                            }
                            selectedLinks.add(link);
                        }
                    }
                    node.setDragState(FNode.DRAG_START);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs((x - x0) * (x - y0)) > touchSlop) {
                    if (node != null) {
                        node.px = x - dx;
                        node.py = y - dy;
                        force.resume();
                    } else {
                        dx += x - downX;
                        dy += y - downY;
                        downX = x;
                        downY = y;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (node != null) {
                    node.setDragState(FNode.DRAG_END);
                    invalidate();
                    node = null;
                }
                if (!selectedNodes.isEmpty()) {
                    selectedNodes.clear();
                }
                if (!selectedLinks.isEmpty()) {
                    selectedLinks.clear();
                }
                break;
        }
        return true;
    }

    @Override
    public void refresh() {
        invalidate();
    }
}
