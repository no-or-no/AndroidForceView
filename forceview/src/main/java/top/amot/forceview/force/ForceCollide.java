package top.amot.forceview.force;

import androidx.annotation.NonNull;
import top.amot.forceview.Force;
import top.amot.forceview.Node;
import top.amot.forceview.QuadTree;

public class ForceCollide extends Force.DefaultImpl {

    public static final String NAME = "Collide";
    private static final double DEFAULT_RADIUS = 1;
    private static final double DEFAULT_STRENGTH = 1;
    private static final int DEFAULT_ITERATIONS = 1;

    private NodeCalculation radius;
    private double strength = DEFAULT_STRENGTH;
    private int iterations = DEFAULT_ITERATIONS;
    private double[] radii;
    private double ri, xi, yi, ri2;

    private Node node;

    @Override
    public void initialize(@NonNull Node[] nodes) {
        super.initialize(nodes);
        initialize();
    }

    @Override
    public void apply(double alpha) {
        QuadTree tree;
        for (int k = 0; k < iterations; k++) {
            tree = QuadTree.create(nodes, ForceCollide::x, ForceCollide::y).visitAfter(this::prepare);
            for (int i = 0; i < nodes.length; i++) {
                node = nodes[i];
                ri = radii[node.index];
                ri2 = ri * ri;
                xi = node.x + node.vx;
                yi = node.y + node.vy;
                tree.visit(this::applyVisit);
            }
        }
    }

    public ForceCollide radius(NodeCalculation c) {
        radius = c;
        return this;
    }

    public ForceCollide iterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public ForceCollide strength(double strength) {
        this.strength = strength;
        return this;
    }

    private void initialize() {
        if (nodes == null) {
            return;
        }
        radii = new double[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            radii[node.index] = radius(node);
        }
    }

    private boolean prepare(QuadTree.TreeNode node, double x0, double y0, double x1, double y1) {
        if (node.data != null) {
            node.r = radii[node.data.index];
            return false;
        }
        node.r = 0;
        for (int i = 0; i < 4; i++) {
            if (node.quadrants[i] != null && node.quadrants[i].r > node.r) {
                node.r = node.quadrants[i].r;
            }
        }
        return false;
    }

    private boolean applyVisit(QuadTree.TreeNode node, double x0, double y0, double x1, double y1) {
        Node data = node.data;
        double rj = node.r, r = ri + rj;
        if (data != null) {
            if (data.index > node.index) {
                double x = xi - data.x - data.vx;
                double y = yi - data.y - data.vy;
                double l = x * x + y * y;
                if (l < r * r) {
                    if (x == 0) {
                        x = Force.jiggle();
                        l += x * x;
                    }
                    if (y == 0) {
                        y = Force.jiggle();
                        l += y * y;
                    }
                    l = Math.sqrt(l);
                    l = (r - l) / l * strength;

                    this.node.vx += (x *= l) * (r = (rj *= rj) / (ri2 + rj));
                    this.node.vy += (y *= l) * r;
                    data.vx -= x * (r = 1 - r);
                    data.vy -= y * r;
                }
            }
            return false;
        }
        return x0 > xi + r || x1 < xi - r || y0 > yi + r || y1 < yi - r;
    }

    private double radius(Node node) {
        if (radius == null) {
            return DEFAULT_RADIUS;
        }

        double r = radius.calculate(node);
        if (r < 0) {
            return -r;
        }
        if (r == 0) {
            return  DEFAULT_RADIUS;
        }
        return r;
    }

    private static double x(Node node) {
        if (node != null) {
            return node.x + node.vx;
        }
        return 0;
    }

    private static double y(Node node) {
        if (node != null) {
            return node.y + node.vy;
        }
        return 0;
    }

}
