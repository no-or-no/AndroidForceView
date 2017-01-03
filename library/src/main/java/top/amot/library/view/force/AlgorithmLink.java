package top.amot.library.view.force;

import android.support.annotation.NonNull;

import java.util.List;

import static top.amot.library.view.force.ForceAlgorithm.Utils.jiggle;

/**
 * <p>Created by Z.pan on 2017/1/1.</p>
 */
class AlgorithmLink implements ForceAlgorithm {

    private List<Link> links;
    private float distance = 200f;
    private float strength;

    private int[] count;
    private float[] bias;
    private float[] strengths;
    private float[] distances;
    private int iterations = 1;

    AlgorithmLink(@NonNull List<Link> links) {
        this.links = links;
    }

    @Override
    public void force(float alpha) {
        for (int k = 0; k < iterations; k++) {
            for (int i = 0; i < links.size(); i++) {
                Link link = links.get(i);
                Node source = link.source;
                Node target = link.target;
                float x = target.x + target.vx - source.x - source.vx;
                float y = target.y + target.vy - source.y - source.vy;
                if (x == 0) {
                    x = jiggle();
                }
                if (y == 0) {
                    y = jiggle();
                }

                float l = (float) Math.sqrt(x * x + y * y);
                l = (l - distances[i]) / l * alpha * strengths[i];
                x *= l;
                y *= l;

                float b = bias[i];
                target.vx -= x * b;
                target.vy -= y * b;
                b = 1 - b;
                source.vx += x * b;
                source.vy += y * b;
            }
        }
    }

    @Override
    public void initialize(@NonNull List<Node> nodes) {
        if (nodes.isEmpty()) {
            return;
        }

        count = new int[nodes.size()];
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            link.index = i;
            if (link.source != null && link.source.index != -1) {
                count[link.source.index] += 1;
            }
            if (link.target != null && link.target.index != -1) {
                count[link.target.index] += 1;
            }
        }

        bias = new float[links.size()];
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            bias[i] = 1f * count[link.source.index] / (count[link.source.index] + count[link.target.index]);
        }

        strengths = new float[links.size()];
        distances = new float[links.size()];

        for (int i = 0; i < links.size(); i++) {
            strengths[i] = getStrength(links.get(i));
            distances[i] = getDistance(links.get(i));
        }
    }

    public AlgorithmLink setStrength(float strength) {
        this.strength = strength;
        return this;
    }

    public AlgorithmLink setDistance(float distance) {
        this.distance = distance;
        return this;
    }

    public AlgorithmLink setIterations(int n) {
        this.iterations = n;
        return this;
    }

    private float getStrength(Link link) {
        if (strength == 0) {
            return 1f / Math.min(count[link.source.index], count[link.target.index]);
        } else {
            return strength;
        }
    }

    private float getDistance(Link link) {
        return distance;
    }

}
