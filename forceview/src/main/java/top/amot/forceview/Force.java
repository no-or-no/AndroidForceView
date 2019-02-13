package top.amot.forceview;

import androidx.annotation.CallSuper;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

public interface Force {

    static double jiggle() {
        return (Math.random() - 0.5) * 1e-6;
    }

    /**
     * optionally implement force.initialize to receive the simulation’s array of nodes.
     * @param nodes
     */
    void initialize(@NonNull Node[] nodes);

    /**
     * apply this force to modify nodes’ positions or velocities.
     * @param alpha from 0 (inclusive) to 1 (inclusive), default 1
     */
    void apply(@FloatRange(from = 0, to = 1) double alpha);

    abstract class DefaultImpl implements Force {
        protected Node[] nodes;

        @CallSuper
        @Override
        public void initialize(@NonNull Node[] nodes) {
            this.nodes = nodes;
        }

    }

    interface NodeCalculation {
        double calculate(Node node);
    }

    interface LinkCalculation {
        double calculate(Link link);
    }

    class ConstantCalculation implements NodeCalculation, LinkCalculation {
        double value;

        public ConstantCalculation(double value) {
            this.value = value;
        }

        @Override
        public double calculate(Node node) {
            return value;
        }

        @Override
        public double calculate(Link link) {
            return value;
        }

    }
}
