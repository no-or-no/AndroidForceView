package top.amot.forceview.force;

import top.amot.forceview.Force;
import top.amot.forceview.Node;

public class ForceCenter extends Force.DefaultImpl {

    public static final String NAME = "Center";

    private double x, y;

    public ForceCenter() {}

    public ForceCenter(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void apply(double alpha) {
        double sx = 0, sy = 0;
        int n = nodes.length;

        for (Node node : nodes) {
            sx += node.x;
            sy += node.y;
        }

        sx = sx / n - x;
        sy = sy / n - y;

        for (Node node : nodes) {
            node.x -= sx;
            node.y -= sy;
        }
    }

    public ForceCenter x(double x) {
        this.x = x;
        return this;
    }

    public ForceCenter y(double y) {
        this.y = y;
        return this;
    }

}
