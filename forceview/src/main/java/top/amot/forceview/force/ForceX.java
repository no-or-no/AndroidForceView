package top.amot.forceview.force;

import androidx.annotation.NonNull;
import top.amot.forceview.Force;
import top.amot.forceview.Node;
import top.amot.forceview.Simulation;

public class ForceX extends Force.DefaultImpl {

    public static final String NAME = "X";

    private static final double DEFAULT_STRENGTH = 0.1;

    private double[] strengths;
    private double[] xz;
    private NodeCalculation xCalculation;
    private NodeCalculation strengthCalculation;

    @Override
    public void initialize(@NonNull Simulation simulation) {
        super.initialize(simulation);
        initialize();
    }

    @Override
    public void apply(double alpha) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            node.vx += (xz[i] - node.x) * strengths[i] * alpha;
        }
    }

    public ForceX x(NodeCalculation c) {
        xCalculation = c;
        return this;
    }

    public ForceX strength(NodeCalculation c) {
        strengthCalculation = c;
        return this;
    }

    private void initialize() {
        if (nodes == null) {
            return;
        }
        strengths = new double[nodes.length];
        xz = new double[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            xz[i] = x(i);
            strengths[i] = strength(i);
        }
    }

    private double x(int i) {
        if (xCalculation == null) {
            return 0;
        }
        return xCalculation.calculate(nodes[i]);
    }

    private double strength(int i) {
        if (strengthCalculation == null) {
            return DEFAULT_STRENGTH;
        }
        return strengthCalculation.calculate(nodes[i]);
    }

}
