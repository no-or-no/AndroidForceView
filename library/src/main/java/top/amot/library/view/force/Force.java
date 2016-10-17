package top.amot.library.view.force;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Z.Pan on 2016/10/9.
 */

class Force {

    private static final long PERIOD_MILLIS = 17;
    private static final int DEFAULT_LINK_DISTANCE = 20;
    private static final float DEFAULT_LINK_STRENGTH = 0.1f;
    private static final float DEFAULT_CHARGE = -30f;
    private static final float DEFAULT_FRICTION = 0.9f;
    private static final float DEFAULT_GRAVITY = 0.1f;
    private static final float DEFAULT_THETA = 0.8f;
    private static final float DEFAULT_ALPHA = 0.1f;

    private ForceListener listener;

    List<FNode> nodes;
    List<FLink> links;

    private int width;
    private int height;
    private int distance = DEFAULT_LINK_DISTANCE;
    private float strength = DEFAULT_LINK_STRENGTH;
    private float charge = DEFAULT_CHARGE;
    private float friction = DEFAULT_FRICTION;
    private float gravity = DEFAULT_GRAVITY;
    private float theta = DEFAULT_THETA;
    private float alpha = DEFAULT_ALPHA;

    private ForceHandler handler;
    private Timer timer;
    private TimerTask task;

    private float minX, minY, maxX, maxY;

    Force(ForceListener listener) {
        this.listener = listener;
        handler = new ForceHandler(this, Looper.getMainLooper());
    }

    Force setNodes(List<FNode> nodes) {
//        this.nodes = Collections.synchronizedList(nodes);
        this.nodes = nodes;
        return this;
    }

    Force setLinks(List<FLink> links) {
//        this.links = Collections.synchronizedList(links);
        this.links = links;
        return this;
    }

    Force setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    Force setDistance(int distance) {
        this.distance = distance;
        return this;
    }

    Force setStrength(float strength) {
        this.strength = strength;
        return this;
    }

    Force setFriction(float friction) {
        this.friction = friction;
        return this;
    }

    Force setCharge(float charge) {
        this.charge = charge;
        return this;
    }

    Force setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    Force setTheta(float theta) {
        this.theta = theta;
        return this;
    }

    Force setAlpha(float alpha) {
        if (alpha < 0) {
            alpha = 0;
        }

        this.alpha = alpha;

        startTickTask();

        return this;
    }

    Force start() {
        int nodeCount = 0;
        int linkCount = 0;

        if (nodes != null) {
            nodeCount = nodes.size();
        }

        if (links != null) {
            linkCount = links.size();
        }

        for (int i = 0; i < nodeCount; i++) {
            FNode node = nodes.get(i);
            node.weight = 0;
        }
        for (int i = 0; i < linkCount; i++) {
            FLink link = links.get(i);
            link.source.weight++;
            link.target.weight++;
        }
        for (int i = 0; i < nodeCount; i++) {
            FNode node = nodes.get(i);
            node.px = node.x = getRandomPosition(width);
            node.py = node.y = getRandomPosition(height);
        }

        return resume();
    }

    Force stop() {
        return setAlpha(0);
    }

    Force resume() {
        return setAlpha(DEFAULT_ALPHA);
    }

    private float getRandomPosition(int max) {
        return (float) (Math.random() * max);
    }

    FNode getNode(float x, float y, float scale) {
        if (nodes == null) {
            return null;
        }

        for (int i = nodes.size() - 1; i >= 0; i--) {
            FNode node = nodes.get(i);
            if (node.isInside(x, y, scale)) {
                return node;
            }
        }

        return null;
    }

    private float linkDistance(FLink link) {
        return distance + link.source.getRadius() + link.target.getRadius();
    }

    private void tick() {
        if ((alpha *= 0.99) < 0.005) {
            endTickTask();
            return;
        }

        if (nodes == null || links == null) {
            return;
        }

        int nodeCount = nodes.size();
        int linkCount = links.size();

        for (int i = 0; i < linkCount; i++) {
            FLink link = links.get(i);
            FNode sourceNode = link.source;
            FNode targetNode = link.target;
            float dx = targetNode.x - sourceNode.x;
            float dy = targetNode.y - sourceNode.y;
            double d = dx * dx + dy * dy;
            if (d > 0) {
                d = Math.sqrt(d);

                d = alpha * linkStrength(link) * (d - linkDistance(link)) / d;
                dx *= d;
                dy *= d;

                float k = sourceNode.weight * 1.0f / (targetNode.weight + sourceNode.weight);
                targetNode.x -= dx * k;
                targetNode.y -= dy * k;

                k = 1 - k;
                sourceNode.x += dx * k;
                sourceNode.y += dy * k;
            }
        }

        float k = alpha * gravity;
        if (k != 0) {
            int w = width / 2;
            int h = height / 2;
            for (int i = 0; i < nodeCount; i++) {
                FNode node = nodes.get(i);
                node.x += (w - node.x) * k;
                node.y += (h - node.y) * k;
            }
        }

        if (charge != 0) {
            QuadTree quadTree = getQuadTree(nodes);
            forceAccumulate(quadTree.root);
            for (int i = 0; i < nodeCount; i++) {
                FNode node = nodes.get(i);
                if (!node.isStable()) {
                    visitQuadTree(quadTree.root, node, minX, minY, maxX, maxY);
                }
            }
        }

        for (int i = 0; i < nodeCount; i++) {
            FNode node = nodes.get(i);
            if (node.isStable()) {
                node.x = node.px;
                node.y = node.py;
            } else {
                node.x -= (node.px - (node.px = node.x)) * friction;
                node.y -= (node.py - (node.py = node.y)) * friction;
            }
        }

        if (listener != null) {
            listener.refresh();
        }

    }

    private float linkStrength(FLink link) {
        float k = 1;
        if (link != null) {
//            int sl = link.source.getLevel();
//            int tl = link.target.getLevel();
//            if(sl > 0 && tl > 0) {
//                    k = (sl + tl) * 0.5f;
//            }
        }
        return strength * k;
    }

    private QuadTree getQuadTree(List<FNode> nodes) {
        if (nodes == null) {
            return null;
        }

        int nodeCount = nodes.size();

        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        maxY = Float.MIN_VALUE;
        for (int i = 0; i < nodeCount; i++) {
            FNode node = nodes.get(i);
            minX = Math.min(minX, node.x);
            minY = Math.min(minY, node.y);
            maxX = Math.max(maxX, node.x);
            maxY = Math.max(maxY, node.y);
        }

        float dx = maxX - minX;
        float dy = maxY - minY;
        if (dx > dy) {
            maxY = minY + dx;
        } else {
            maxX = minX + dy;
        }

        QuadTree quadTree = new QuadTree();

        int i = -1;
        while (++i < nodeCount) {
            quadTree.insert(quadTree.root, nodes.get(i), minX, minY, maxX, maxY);
        }

        return quadTree;
    }

    private void forceAccumulate(QuadTree.Node root) {
        int cx = 0, cy = 0;
        root.charge = 0;
        if (!root.isLeaf) {
            QuadTree.Node[] children = root.children;
            int count = children.length;
            int i = -1;
            while (++i < count) {
                QuadTree.Node node = children[i];
                if (node == null) {
                    continue;
                }

                forceAccumulate(node);
                root.charge += node.charge;
                cx += node.charge * node.cx;
                cy += node.charge * node.cy;
            }
        }

        if (root.point != null) {
            if (!root.isLeaf) {
                root.point.x += Math.random() - 0.5;
                root.point.y += Math.random() - 0.5;
            }
            float k = alpha * nodeCharge(root.point);
            root.pointCharge = k;
            root.charge += k;
            cx += k * root.point.x;
            cy += k * root.point.y;
        }

        root.cx = cx / root.charge;
        root.cy = cy / root.charge;
    }

    private float nodeCharge(FNode node) {
        if (node == null) {
            return charge;
        }
        int level = node.getLevel();
        if (level <= 1) {
            return charge;
        }
        return charge * node.getRadius() / level / 5f;
    }

    private boolean repulse(QuadTree.Node root, FNode node, float x1, float x2) {
        if (root.point != node) {
            float dx = root.cx - node.x;
            float dy = root.cy - node.y;
            float dw = x2 - x1;
            float dn = dx * dx + dy * dy;
            if ((dw * dw) / (theta * theta) < dn) {
                if (dn < Float.POSITIVE_INFINITY) {
                    float k = root.charge / dn;
                    node.px -= dx * k;
                    node.py -= dy * k;
                }
                return true;
            }
            if (root.point != null && dn > 0 && dn < Float.POSITIVE_INFINITY) {
                float k = root.pointCharge / dn;
                node.px -= dx * k;
                node.py -= dy * k;
            }
        }
        return root.charge == 0;
    }

    private void visitQuadTree(QuadTree.Node root, FNode node, float x1, float y1, float x2, float y2) {
        if (!repulse(root, node, x1, x2)) {
            float sx = (x1 + x2) * 0.5f;
            float sy = (y1 + y2) * 0.5f;
            QuadTree.Node[] children = root.children;
            if (children[0] != null) {
                visitQuadTree(children[0], node, x1, y1, sx, sy);
            }
            if (children[1] != null) {
                visitQuadTree(children[1], node, sx, y1, x2, sy);
            }
            if (children[2] != null) {
                visitQuadTree(children[2], node, x1, sy, sx, y2);
            }
            if (children[3] != null) {
                visitQuadTree(children[3], node, sx, sy, x2, y2);
            }
        }
    }

    private void startTickTask() {
        if (timer == null || task == null) {
            /* execute tick() method once per PERIOD_MILLIS ms. */
            timer = new Timer(true);
            task = new TickTask();
            timer.schedule(task, 0, PERIOD_MILLIS);
        }
    }

    private void endTickTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private static class ForceHandler extends Handler {
        private WeakReference<Force> forceReference;

        ForceHandler(Force force, Looper looper) {
            super(looper);
            forceReference = new WeakReference<Force>(force);
        }


        @Override
        public void handleMessage(Message msg) {
            Force force = forceReference.get();
            if (force != null) {
                force.tick();
            }
        }
    }

    private class TickTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
//            tick();
        }
    }

}
