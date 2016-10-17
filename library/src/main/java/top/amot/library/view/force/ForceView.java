package top.amot.library.view.force;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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
    private static final String TAG = "ForceView";

    private static final int[] colors = {
            Color.parseColor("#f09d24"),
            Color.parseColor("#d95c8a"),
            Color.parseColor("#13a1e1"),
            Color.parseColor("#8BC34A")
    };

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Force force;
    private float textBaseline;

    private float touchSlop;
    private FNode node;
    private float downX, downY;
    private float pointerX, pointerY;
    private float translateX, translateY;
    private float scale = 1f;
    private float x0, y0;
    private boolean isScaling;
    private List<FNode> selectedNodes = new ArrayList<>();
    private List<FLink> selectedLinks = new ArrayList<>();
    private ScaleGestureDetector scaleDetector;

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
        strokePaint.setStrokeWidth(5f);
        strokePaint.setStyle(Paint.Style.STROKE);

        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(20f);
        textPaint.setColor(Color.BLUE);

        linkPaint.setAntiAlias(true);

        cPaint.setAntiAlias(true);
        cPaint.setColor(Color.RED);

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        Paint.FontMetricsInt anInt = textPaint.getFontMetricsInt();
        textBaseline = (anInt.bottom + anInt.top) / 2f + 5f;

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        force = new Force(this)
                .setStrength(0.5f)
                .setFriction(0.9f)
                .setDistance(50)
                .setCharge(-400f)
                .setGravity(0.1f)
                .setTheta(0.1f)
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

    private void drawNodes(Canvas canvas, List<FNode> nodes, boolean drawStroke) {
        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            FNode node = nodes.get(i);
            float cx = node.x;
            float cy = node.y;
            resetPaintColor(node.getLevel());
            canvas.drawCircle(cx, cy, node.getRadius(), paint);
            canvas.drawText(node.getText(), cx, cy - textBaseline, textPaint);
            if (drawStroke) {
                strokePaint.setColor(selectedColor);
                canvas.drawCircle(cx, cy, node.getRadius() + 5, strokePaint);
            }
        }
    }

    private void resetPaintColor(int level) {
        int color = getColor(level);
        paint.setColor(color);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha(192);
        strokePaint.setColor(color);
    }

    private int selectedColor;

    private int getColor(int level) {
        return colors[(level - 1) % colors.length];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale, pointerX, pointerY);

        linkPaint.setColor(Color.GRAY);
        drawLinks(canvas, force.links);

        if (!selectedLinks.isEmpty()) {
            linkPaint.setColor(selectedColor);
            drawLinks(canvas, selectedLinks);
        }

        drawNodes(canvas, force.nodes, false);

        if (!selectedNodes.isEmpty()) {
            drawNodes(canvas, selectedNodes, true);
        }

        canvas.restore();

    }

    private int activePointerId = -1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        float x;
        float y;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                x0 = downX = x = event.getX();
                y0 = downY = y = event.getY();
                if (!isScaling) {
                    node = force.getNode(
                            x + pointerX * (scale - 1) - translateX,
                            y + pointerY * (scale - 1) - translateY,
                            scale);
                    if (node != null) {
                        selectedColor = getColor(node.getLevel());
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
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(activePointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);
                if (Math.abs((x - x0) * (x - y0)) > touchSlop) {
                    if (node != null) {
                        node.px = (x + pointerX * (scale - 1) - translateX) / scale;
                        node.py = (y + pointerY * (scale - 1) - translateY) / scale;
                        force.resume();
                    } else {
                        translateX += x - downX;
                        translateY += y - downY;
                        downX = x;
                        downY = y;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                activePointerId = -1;
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
            case MotionEvent.ACTION_POINTER_DOWN: {
                isScaling = true;
                pointerX = (event.getX(1) + event.getX(0)) * 0.5f;
                pointerY = (event.getY(1) + event.getY(0)) * 0.5f;
            } break;

            case MotionEvent.ACTION_POINTER_UP: {
                isScaling = false;
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    downX = event.getX(newPointerIndex);
                    downY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
            } break;
        }
        return true;
    }

    @Override
    public void refresh() {
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));

            invalidate();
            return true;
        }
    }

}
