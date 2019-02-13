package top.amot.forceview.force;

import androidx.annotation.NonNull;
import top.amot.forceview.Force;
import top.amot.forceview.Node;
import top.amot.forceview.QuadTree;

/**
 * A positive strength causes nodes to attract each other, similar to gravity, while a negative
 * value causes nodes to repel each other, similar to electrostatic charge.
 */
public class ForceManyBody extends Force.DefaultImpl {

    public static final String NAME = "Charge";

    private static final double DEFAULT_STRENGTH = -30;
    private static final double DEFAULT_THETA2 = 0.81;

    private double distanceMin2 = 1;
    private double distanceMax2 = Double.POSITIVE_INFINITY;

    private double[] strengths;
    private NodeCalculation strengthCalculation;
    private double theta2 = DEFAULT_THETA2;
    private double alpha;

    private Node node;

    @Override
    public void initialize(@NonNull Node[] nodes) {
        super.initialize(nodes);
        initialize();
    }

    @Override
    public void apply(double alpha) {
        this.alpha = alpha;
        QuadTree tree = QuadTree.create(nodes, ForceManyBody::x, ForceManyBody::y).visitAfter(this::accumulate);
        for (Node node : nodes) {
            this.node = node;
            tree.visit(this::applyVisit);
        }
    }

    public ForceManyBody strength(NodeCalculation c) {
        strengthCalculation = c;
        return this;
    }

    public ForceManyBody theta(double theta) {
        theta2 = theta * theta;
        return this;
    }

    public ForceManyBody distanceMin(double distanceMin) {
        distanceMin2 = distanceMin * distanceMin;
        return this;
    }

    public ForceManyBody distanceMax(double distanceMax) {
        distanceMax2 = distanceMax * distanceMax;
        return this;
    }

    private void initialize() {
        if (nodes == null) {
            return;
        }
        strengths = new double[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            strengths[node.index] = strength(i);
        }
    }

    private double strength(int i) {
        if (strengthCalculation == null) {
            return DEFAULT_STRENGTH;
        }
        return strengthCalculation.calculate(nodes[i]);
    }

    private boolean accumulate(QuadTree.TreeNode node, double x0, double y0, double x1, double y1) {
        double strength = 0, weight = 0;
        double x = 0, y = 0;
        QuadTree.TreeNode no;
        if (!node.isLeaf()) {
            for (int i = 0; i < 4; i++) {
                double c;
                if ((no = node.quadrants[i]) != null && (c = Math.abs(no.strength)) > 0) {
                    strength += no.strength;
                    weight += c;
                    x += c * no.x;
                    y += c * no.y;
                }
            }
            node.x = x / weight;
            node.y = y / weight;
        } else {
            no = node;
            no.x = no.data.x;
            no.y = no.data.y;
            do {
                strength += strengths[no.data.index];
            } while ((no = no.next) != null);
        }
        node.strength = strength;

        return false;
    }

    private boolean applyVisit(QuadTree.TreeNode node, double x0, double y0, double x1, double y1) {
        if (node.strength == 0) {
            return true;
        }

        double x = node.x - this.node.x, y = node.y - this.node.y;
        double w = x1 - x0;
        double l = x * x + y * y;

        if (w * w / theta2 < l) {
            if (l < distanceMax2) {
                if (x == 0) {
                    x = Force.jiggle();
                    l += x * x;
                }
                if (y == 0) {
                    y = Force.jiggle();
                    l += y * y;
                }
                if (l < distanceMin2) {
                    l = Math.sqrt(distanceMin2 * l);
                }
                this.node.vx += x * node.strength * alpha / l;
                this.node.vy += y * node.strength * alpha / l;
            }
            return true;
        } else if (!node.isLeaf() || l >= distanceMax2) {
            return false;
        }

        if (node.data != this.node || node.next != null) {
            if (x == 0) {
                x = Force.jiggle();
                l += x * x;
            }
            if (y == 0) {
                y = Force.jiggle();
                l += y * y;
            }
            if (l < distanceMin2) {
                l = Math.sqrt(distanceMin2 * l);
            }
        }

        do {
            if (node.data != this.node) {
                w = strengths[node.data.index] * alpha / l;
                this.node.vx += x * w;
                this.node.vy += y * w;
            }
        } while ((node = node.next) != null);

        return false;
    }

    private static double x(Node node) {
        if (node != null) {
            return node.x;
        }
        return 0;
    }

    private static double y(Node node) {
        if (node != null) {
            return node.y;
        }
        return 0;
    }
}
