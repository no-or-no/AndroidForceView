package top.amot.library.view.force;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * <p>Created by Z.pan on 2017/1/3.</p>
 */
class AlgorithmY implements ForceAlgorithm {

    private List<Node> nodes;
    private float[] strengths;
    private float[] yz;

    @Override
    public void force(float alpha) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.vy += (yz[i] - node.y) * strengths[i] * alpha;
        }
    }

    @Override
    public void initialize(@NonNull List<Node> nodes) {
        this.nodes = nodes;
        strengths = new float[nodes.size()];
        yz = new float[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            yz[i] = node.y;
            strengths[i] = yz[i] == 0 ? 0 : getStrength(node);
        }
    }

    private float getStrength(Node node) {
        return 0.1f;
    }
}
