package top.amot.library.view.force;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * SurfaceView-based implement of Force layout.
 * <p>
 * Created by Z.pan on 2016/11/2.
 */
public class ForceSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback, ForceListener {

    private final SurfaceHolder holder;
    private HandlerThread thread;
    private Handler handler;
    private Force force;
    private ForceDrawer drawer;
    private ScaleGestureDetector scaleDetector;

    private List<FLink> targetLinks = new ArrayList<>();
    private List<FLink> sourceLinks = new ArrayList<>();
    private List<FNode> selectedNodes = new ArrayList<>();
    private FNode node;

    private float touchSlop;
    private float translateX, translateY;
    private float scale = 1f;
    private int activePointerId = -1;
    private float x0, y0;
    private float downX, downY;

    private OnNodeClickListener onNodeClickListener;

    public OnNodeClickListener getOnNodeClickListener() {
        return onNodeClickListener;
    }

    public void setOnNodeClickListener(OnNodeClickListener onNodeClickListener) {
        this.onNodeClickListener = onNodeClickListener;
    }

    public ForceSurfaceView(Context context) {
        this(context, null);
    }

    public ForceSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForceSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        holder = getHolder();
        holder.addCallback(this);
        force = new Force(this);
        drawer = new ForceDrawer(context);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    }

    public void setData(ArrayList<FNode> nodes, ArrayList<FLink> links) {
        force.setNodes(nodes)
                .setLinks(links)
                .start();
    }

    /** 绘图，非 UI 线程 */
    private void doDraw(Canvas canvas) {
        ArrayList<FNode> nodes = force.nodes;
        ArrayList<FLink> links = force.links;

        canvas.save();

        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);

        drawer.drawLinks(canvas, links, node);
        drawer.drawNodes(canvas, nodes, false);
        if (!targetLinks.isEmpty()) {
            drawer.drawLinks(canvas, targetLinks, node);
        }
        if (!sourceLinks.isEmpty()) {
            drawer.drawLinks(canvas, sourceLinks, node);
        }
        drawer.drawNodes(canvas, selectedNodes, false);
        drawer.drawNode(canvas, node, true);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        float x;
        float y;
        int pointerIndex;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                x0 = downX = x = event.getX();
                y0 = downY = y = event.getY();
                node = force.getNode(
                        x - translateX,
                        y - translateY,
                        scale);
                if (node != null) {
                    node.selected = 1;
                    node.setDragState(FNode.DRAG_START);
                    ArrayList<FLink> links = force.links;
                    for (int i = 0, size = links.size(); i < size; i++) {
                        FLink link = links.get(i);
                        if (link.source == node) {
                            selectedNodes.add(link.target);
                            targetLinks.add(link);
                        } else if (link.target == node) {
                            selectedNodes.add(link.source);
                            sourceLinks.add(link);
                        }
                    }
                    refresh();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(activePointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                if (Math.abs((x - x0) * (x - y0)) > touchSlop * touchSlop) {
                    if (node != null) {
                        node.x = node.px = (x - translateX) / scale;
                        node.y = node.py = (y - translateY) / scale;
                        force.resume();
                    } else {
                        if (!scaleDetector.isInProgress()) {
                            translateX += x - downX;
                            translateY += y - downY;
                            refresh();
                        }
                    }
                }
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                activePointerId = -1;
                if (node != null) {
                    node.selected = 0;
                    node.setDragState(FNode.DRAG_END);
                    refresh();
                    x = event.getX();
                    y = event.getY();
                    if (Math.abs((x - x0) * (y - y0)) < touchSlop * touchSlop) {
                        if (onNodeClickListener != null) {
                            onNodeClickListener.onNodeClick(node);
                        }
                    }
                    node = null;
                }
                targetLinks.clear();
                sourceLinks.clear();
                selectedNodes.clear();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                node = null;
                targetLinks.clear();
                sourceLinks.clear();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    downX = event.getX(newPointerIndex);
                    downY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }
        return true;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int w = getWidth();
        int h = getHeight();
        force.setSize(w, h)
                .setStrength(0.7f)
                .setFriction(0.6f)
                .setDistance(150)
                .setCharge(-320f)
                .setGravity(0.1f)
                .setTheta(0.8f)
                .setAlpha(0.2f)
                .start();
        thread = new HandlerThread("ForceSurfaceThread");
        thread.start();
        handler = new Handler(thread.getLooper(), this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // no op
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        force.endTickTask();
        thread.quit();
        thread = null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Canvas canvas = null;
        try {
            synchronized (holder) {
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                doDraw(canvas);
            }
        } catch (Exception e) {
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
        return true;
    }

    @Override
    public void refresh() {
        handler.sendEmptyMessage(0);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.isInProgress()) {
                float factor = detector.getScaleFactor();

                float pScale = scale;

                scale *= factor;
                scale = Math.max(0.1f, Math.min(scale, 5.0f));

                if (!((pScale == 0.1 && scale == 0.1) || (pScale == 5 && scale == 5))) {
                    float focusX = detector.getFocusX();
                    float focusY = detector.getFocusY();
                    translateX += (focusX - translateX) * (1 - factor);
                    translateY += (focusY - translateY) * (1 - factor);
                }

                refresh();
            }

            return true;
        }
    }
}
