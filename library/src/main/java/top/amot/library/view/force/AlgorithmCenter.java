package top.amot.library.view.force;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * <p>Created by Z.pan on 2017/1/2.</p>
 */
public class AlgorithmCenter implements ForceAlgorithm {

    private List<Node> nodes;
    private float x, y;

    public AlgorithmCenter(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void force(float alpha) {
        float sx = 0, sy = 0;
        for (Node node : nodes) {
            sx += node.x;
            sy += node.y;
        }

        sx = sx / nodes.size() - x;
        sy = sy / nodes.size() - y;
        for (Node node : nodes) {
            node.x -= sx;
            node.y -= sy;
        }
    }

    @Override
    public void initialize(@NonNull List<Node> nodes) {
        this.nodes = nodes;
    }
}
