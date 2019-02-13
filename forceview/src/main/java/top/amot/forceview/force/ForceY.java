package top.amot.forceview.force;

import androidx.annotation.NonNull;
import top.amot.forceview.Force;
import top.amot.forceview.Node;

public class ForceY extends Force.DefaultImpl {

    public static final String NAME = "Y";

    private static final double DEFAULT_STRENGTH = 0.1;

    private double[] strengths;
    private double[] yz;
    private NodeCalculation yCalculation;
    private NodeCalculation strengthCalculation;

    @Override
    public void initialize(@NonNull Node[] nodes) {
        super.initialize(nodes);
        initialize();
    }

    @Override
    public void apply(double alpha) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            node.vy += (yz[i] - node.y) * strengths[i] * alpha;
        }
    }

    public ForceY y(NodeCalculation c) {
        yCalculation = c;
        return this;
    }

    public ForceY strength(NodeCalculation c) {
        strengthCalculation = c;
        return this;
    }

    private void initialize() {
        if (nodes == null) {
            return;
        }
        int size = nodes.length;
        strengths = new double[size];
        yz = new double[size];
        for (int i = 0; i < size; i++) {
            yz[i] = y(i);
            strengths[i] = strength(i);
        }
    }

    private double y(int i) {
        if (yCalculation == null) {
            return 0;
        }
        return yCalculation.calculate(nodes[i]);
    }

    private double strength(int i) {
        if (strengthCalculation == null) {
            return DEFAULT_STRENGTH;
        }
        return strengthCalculation.calculate(nodes[i]);
    }

}
