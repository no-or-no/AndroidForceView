package top.amot.library.view.force;

/**
 * <p>Created by Z.pan on 2016/12/31.</p>
 */
final class ForceAlgorithmChain {

    private ForceAlgorithm[] algorithms;
    private int cursor = -1;

    ForceAlgorithm next() {
        if (algorithms == null || cursor >= algorithms.length - 1) {
            return null;
        }
        return algorithms[++cursor];
    }

    void add(ForceAlgorithm... algorithms) {
        if (algorithms == null || algorithms.length == 0) {
            return;
        }
        if (this.algorithms == null) {
            this.algorithms = algorithms;
        } else {
            int oldLen = this.algorithms.length;
            int addLen = algorithms.length;
            ForceAlgorithm[] merge = new ForceAlgorithm[oldLen + addLen];
            System.arraycopy(this.algorithms, 0, merge, 0, oldLen);
            System.arraycopy(algorithms, 0, merge, oldLen, addLen);
            this.algorithms = merge;
        }
    }

    void init() {
        cursor = -1;
    }
}
