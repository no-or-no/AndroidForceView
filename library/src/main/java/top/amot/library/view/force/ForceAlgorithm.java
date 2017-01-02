package top.amot.library.view.force;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * <p>Created by Z.pan on 2016/12/31.</p>
 */
public interface ForceAlgorithm {
    void force(float alpha);
    void initialize(@NonNull List<Node> nodes);

    class Utils {
        protected static float jiggle() {
            return (float) ((Math.random() - 0.5) * 1e-6);
        }
    }
}
