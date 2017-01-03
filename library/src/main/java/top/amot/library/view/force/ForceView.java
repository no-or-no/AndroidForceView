package top.amot.library.view.force;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Z.pan on 2016/12/31.</p>
 */
public class ForceView extends View implements ForceListener {

    private OnNodeClickListener onNodeClickListener;

    private Simulation simulation = new Simulation.Builder().build();
    private ScaleGestureDetector scaleDetector;
    private ForceDrawer drawer;

    private float touchSlop;
    private float translateX, translateY;
    private float scale = 1f;

    List<Node> nodes;
    List<Link> links;
    private List<Link> targetLinks = new ArrayList<>();
    private List<Link> sourceLinks = new ArrayList<>();
    private List<Node> selectedNodes = new ArrayList<>();
    private Node selectedNode;

    private int activePointerId = -1;
    private float downX, downY;
    private float x0, y0;

    public void setOnNodeClickListener(OnNodeClickListener onNodeClickListener) {
        this.onNodeClickListener = onNodeClickListener;
    }

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
        scaleDetector = new ScaleGestureDetector(getContext(), new ForceView.ScaleListener());
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        drawer = new ForceDrawer(getContext());
    }

    public void setSimulation(final Simulation simulation) {
        this.simulation = simulation;
        this.nodes = simulation.getNodes();
        this.links = simulation.getLinks();
        simulation.setForceListener(ForceView.this);
        simulation.addForceAlgorithms(
                new AlgorithmManyBody().setStrength(-15),
                new AlgorithmLink(simulation.getLinks()).setStrength(2),
                new AlgorithmCenter(720 / 2, 1280 / 2)
//                new AlgorithmX(),
//                new AlgorithmY()
        );

        simulation.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Log.e("TAG", "ondraw ");

        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);

        drawer.setSelectedNode(selectedNode);
        drawer.drawLinks(canvas, links);
        drawer.drawNodes(canvas, nodes);
        drawer.drawLinks(canvas, targetLinks);
        drawer.drawLinks(canvas, sourceLinks);
        drawer.drawNodes(canvas, selectedNodes);
        drawer.drawNode(canvas, selectedNode, true);

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
                selectedNode = simulation.find(x - translateX, y - translateY, scale);
                if (selectedNode != null) {
                    List<Link> links = simulation.getLinks();
                    for (int i = 0; i < links.size(); i++) {
                        Link link = links.get(i);
                        if (link.source == selectedNode) {
                            selectedNodes.add(link.target);
                            targetLinks.add(link);
                        } else if (link.target == selectedNode) {
                            selectedNodes.add(link.source);
                            sourceLinks.add(link);
                        }
                    }

                    //invalidate();
                    selectedNode.fx = selectedNode.x;
                    selectedNode.fy = selectedNode.y;
                    simulation.setAlphaTarget(0.3f).start();
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
                        simulation.start();
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
                    simulation.setAlphaTarget(0);
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
            simulation.stop();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void refresh() {
        postInvalidate();
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

                invalidate();
            }

            return true;
        }
    }
}
