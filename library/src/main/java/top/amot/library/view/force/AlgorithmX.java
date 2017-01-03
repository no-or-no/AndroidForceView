package top.amot.library.view.force;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * <p>Created by Z.pan on 2017/1/3.</p>
 */
class AlgorithmX implements ForceAlgorithm {

    private List<Node> nodes;
    private float[] strengths;
    private float[] xz;

    @Override
    public void force(float alpha) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.vx += (xz[i] - node.x) * strengths[i] * alpha;
        }
    }

    @Override
    public void initialize(@NonNull List<Node> nodes) {
        this.nodes = nodes;
        strengths = new float[nodes.size()];
        xz = new float[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            xz[i] = node.x;
            strengths[i] = xz[i] == 0 ? 0 : getStrength(node);
        }
    }

    private float getStrength(Node node) {
        return 0.1f;
    }
}
