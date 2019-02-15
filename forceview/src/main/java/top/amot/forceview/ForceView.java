package top.amot.forceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import top.amot.forceview.force.ForceCenter;
import top.amot.forceview.force.ForceLink;

public class ForceView extends SurfaceView implements SurfaceHolder.Callback, Simulation.Callback {

    private OnNodeClickListener onNodeClickListener;

    private SurfaceHolder holder;
    private Simulation simulation;
    private AtomicBoolean isDrawing = new AtomicBoolean(false);

    private ForceDrawer drawer;
    private ScaleGestureDetector scaleDetector;
    private float translateX, translateY;
    private float scale = 1f;
    private int touchSlop;

    private Node selectedNode;
    private List<Link> targetLinks = new ArrayList<>();
    private List<Link> sourceLinks = new ArrayList<>();
    private List<Node> selectedNodes = new ArrayList<>();

    private int activePointerId = -1;
    private float downX, downY;
    private float x0, y0;

    public ForceView(Context context) {
        this(context, null);
    }

    public ForceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        drawer = new ForceDrawer(context);
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
        scaleDetector = new ScaleGestureDetector(getContext(), new ForceView.ScaleListener());
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        simulation.setCallback(this);
    }

    public void setOnNodeClickListener(OnNodeClickListener listener) {
        onNodeClickListener = listener;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w("--force", "surfaceCreated");
        isDrawing.set(true);
        simulation.restart();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.w("--force", "surfaceChanged: width=" + width + ", height=" + height);
        Force force = simulation.getForce(ForceCenter.NAME);
        if (force != null) {
            ((ForceCenter) force).x(width * 0.5).y(height * 0.5);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w("--force", "surfaceDestroyed");
        isDrawing.set(false);
        simulation.stop();
    }

    @Override
    public void onTick() {
        Log.w("--force", "onTick: isDrawing=" + isDrawing.get());
        if (isDrawing.get()) {
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                drawForce(canvas);
            } catch (Exception e) {
                Log.w("ForceView", e);
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void drawForce(Canvas canvas) {
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);

        canvas.drawColor(Color.WHITE);
        drawer.setSelectedNode(selectedNode);
        drawer.drawLinks(canvas, simulation.getLinks());
        drawer.drawNodes(canvas, simulation.getNodes());
        drawer.drawLinks(canvas, targetLinks);
        drawer.drawLinks(canvas, sourceLinks);
        drawer.drawNodes(canvas, selectedNodes);

        canvas.restore();
    }

    @Override
    public void onEnd() {
        Log.w("--force", "onEnd");
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
                selectedNode = simulation.find(x - translateX, y - translateY, Node.RADIUS * scale);
                if (selectedNode != null) {
                    Link[] links = simulation.getLinks();
                    for (Link link : links) {
                        if (link.source == selectedNode) {
                            selectedNodes.add(link.target);
                            targetLinks.add(link);
                        } else if (link.target == selectedNode) {
                            selectedNodes.add(link.source);
                            sourceLinks.add(link);
                        }
                    }
                    selectedNode.fx = selectedNode.x;
                    selectedNode.fy = selectedNode.y;
                    simulation.restart();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(activePointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                if (Math.abs((x - x0) * (x - y0)) > touchSlop * touchSlop) {
                    if (selectedNode != null) {
                        selectedNode.fx = (x - translateX) / scale;
                        selectedNode.fy = (y - translateY) / scale;
                        simulation.restart();
                    } else {
                        if (!scaleDetector.isInProgress()) {
                            translateX += x - downX;
                            translateY += y - downY;
                            invalidate();
                        }
                    }
                }
                downX = x;
                downY = y;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                activePointerId = -1;
                if (selectedNode != null) {
                    //simulation.setAlphaTarget(0);
                    selectedNode.fx = 0;
                    selectedNode.fy = 0;

                    invalidate();
                    x = event.getX();
                    y = event.getY();
                    if (Math.abs((x - x0) * (y - y0)) < touchSlop * touchSlop) {
                        if (onNodeClickListener != null) {
                            onNodeClickListener.onNodeClick(selectedNode);
                        }
                    }
                    selectedNode = null;
                }
                targetLinks.clear();
                sourceLinks.clear();
                selectedNodes.clear();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                selectedNode = null;
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
    protected void onDetachedFromWindow() {
        if (simulation != null) {
            simulation.destroy();
        }
        super.onDetachedFromWindow();
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

                onTick();
            }

            return true;
        }
    }
}
