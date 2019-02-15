package top.amot.forceview.force;

import androidx.annotation.NonNull;
import top.amot.forceview.Force;
import top.amot.forceview.Node;
import top.amot.forceview.Simulation;

public class ForceRadial extends Force.DefaultImpl {

    public static final String NAME = "Radial";

    private static final double DEFAULT_RADIUS = 1;

    private NodeCalculation radius, strength;
    private double x, y;
    private double[] radiuses;
    private double[] strengths;

    public ForceRadial() { }

    public ForceRadial(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void initialize(@NonNull Simulation simulation) {
        super.initialize(simulation);
        initialize();
    }

    @Override
    public void apply(double alpha) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            double dx = node.x - x, dy = node.y - y;
            dx = dx == 0 ? 1e-6 : dx;
            dy = dy == 0 ? 1e-6 : dy;
            double r = Math.sqrt(dx * dx + dy * dy);
            double k = (radiuses[i] - r) * strengths[i] * alpha / r;

            node.vx += dx * k;
            node.vy += dy * k;
        }
    }

    public ForceRadial radius(NodeCalculation c) {
        radius = c;
        return this;
    }

    public ForceRadial strength(NodeCalculation c) {
        strength = c;
        return this;
    }

    public ForceRadial x(double x) {
        this.x = x;
        return this;
    }

    public ForceRadial y(double y) {
        this.y = y;
        return this;
    }

    private void initialize() {
        if (nodes == null) {
            return;
        }
        strengths = new double[nodes.length];
        radiuses = new double[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            radiuses[i] = radius(nodes[i]);
            strengths[i] = strength(nodes[i]);
        }
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

    private double strength(Node node) {
        if (strength == null) {
            return 0;
        }
        return strength.calculate(node);
    }

}
