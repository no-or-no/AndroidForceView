package top.amot.forceview.force;

import androidx.annotation.NonNull;
import top.amot.forceview.Force;
import top.amot.forceview.Link;
import top.amot.forceview.Node;
import top.amot.forceview.Simulation;

public class ForceLink extends Force.DefaultImpl {

    public static final String NAME = "Link";

    private static final int ITERATIONS = 1;
    private static final double DEFAULT_DISTANCE = 30;

    private Link[] links;
    private double[] distances;
    private double[] strengths;
    private double[] bias;
    private double[] count;

    private LinkCalculation strengthCalculation;
    private LinkCalculation distanceCalculation;

    @Override
    public void initialize(@NonNull Simulation simulation) {
        super.initialize(simulation);
        links(simulation.getLinks());
        initialize();
    }

    @Override
    public void apply(double alpha) {
        for (int k = 0; k < ITERATIONS; k++) {
            for (int i = 0; i < links.length; i++) {
                Link link = links[i];
                Node source = link.source;
                Node target = link.target;
                double x = target.x + target.vx - source.x - source.vx;
                double y = target.y + target.vy - source.y - source.vy;
                if (x == 0) {
                    x = jiggle();
                }
                if (y == 0) {
                    y = jiggle();
                }
                double l = Math.sqrt(x * x + y * y);
                l = (l - distances[i]) / l * alpha * strengths[i];
                x *= l;
                y *= l;
                double b = bias[i];
                target.vx -= x * b;
                target.vy -= y * b;
                b = 1 - b;
                source.vx += x * b;
                source.vy += y * b;
            }
        }
    }

    public ForceLink strength(LinkCalculation c) {
        strengthCalculation = c;
        initializeStrength();
        return this;
    }

    public ForceLink distance(LinkCalculation c) {
        distanceCalculation = c;
        initializeDistance();
        return this;
    }

    public ForceLink links(Link[] links) {
        if (links == null) {
            this.links = new Link[0];
        } else {
            this.links = links;
        }
        return this;
    }

    private void initialize() {
        if (nodes == null) {
            return;
        }
        count = new double[nodes.length];
        for (int i = 0; i < links.length; i++) {
            Link link = links[i];
            link.index = i;
            count[link.source.index] += 1;
            count[link.target.index] += 1;
        }

        bias = new double[links.length];
        for (int i = 0; i < links.length; i++) {
            Link link = links[i];
            bias[i] = count[link.source.index] / (count[link.source.index] + count[link.target.index]);
        }

        strengths = new double[links.length];
        initializeStrength();

        distances = new double[links.length];
        initializeDistance();
    }

    private void initializeStrength() {
        if (nodes == null) {
            return;
        }
        for (int i = 0; i < links.length; i++) {
            strengths[i] = strength(i);
        }
    }

    private void initializeDistance() {
        if (nodes == null) {
            return;
        }
        for (int i = 0; i < links.length; i++) {
            distances[i] = distance(i);
        }
    }

    private double strength(int i) {
        Link link = links[i];
        if (strengthCalculation != null) {
            return strengthCalculation.calculate(link);
        }
        return 1 / Math.min(count[link.source.index], count[link.target.index]);
    }

    private double distance(int i) {
        Link link = links[i];
        double distance = -1;
        if (distanceCalculation != null) {
            distance = distanceCalculation.calculate(link);
        }
        if (distance < 0) {
            return DEFAULT_DISTANCE;
        } else {
            return distance;
        }
    }
}
