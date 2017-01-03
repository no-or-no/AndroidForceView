package top.amot.library.view.force;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Z.pan on 2016/12/31.
 */
public final class Simulation {
    private static final long PERIOD_MILLIS = 15;

    private int initialRadius;
    private float initialAngle;

    private float alpha;
    private float alphaMin;
    private float alphaDecay;
    private float alphaTarget;
    private float velocityDecay;

    private List<Node> nodes;
    private List<Link> links;

    private Timer timer;
    private Task task;

    private ForceAlgorithmChain chain;
    private ForceListener listener;

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Link> getLinks() {
        return links;
    }

    void start() {
        if (timer != null && task != null) {
            task.resume();
        } else {
            /* execute tick() method once per PERIOD_MILLIS ms. */
            timer = new Timer(true);
            task = new Task();
            timer.schedule(task, 0, PERIOD_MILLIS);
        }
    }

    void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    Simulation addForceAlgorithms(ForceAlgorithm... algorithms) {
        chain.add(algorithms);
        chain.init();

        ForceAlgorithm algorithm;
        while ((algorithm = chain.next()) != null) {
            algorithm.initialize(nodes);
        }

        return this;
    }

    Simulation setAlphaTarget(float alphaTarget) {
        this.alphaTarget = alphaTarget;
        return this;
    }

    Simulation setForceListener(ForceListener listener) {
        this.listener = listener;
        return this;
    }

    Node find(float x, float y, float scale) {
        List<Node> nodes = this.nodes;

        if (nodes == null) {
            return null;
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.isInside(x, y, scale)) {
                return node;
            }
        }

        return null;
    }

    private void tick() {
        alpha += (alphaTarget - alpha) * alphaDecay;

        // 算法链
        chain.init();
        ForceAlgorithm algorithm;
        while ((algorithm = chain.next()) != null) {
            algorithm.force(alpha);
        }

        for (Node node : nodes) {
            if (node.fx == 0) {
                node.x += node.vx *= velocityDecay;
            } else {
                node.x = node.fx;
                node.vx = 0;
            }
            if (node.fy == 0) {
                node.y += node.vy *= velocityDecay;
            } else {
                node.y = node.fy;
                node.vy = 0;
            }
        }

        if (listener != null) {
            listener.refresh();
        }
    }

    private void initializeNodes() {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.index = i;
            if (node.x == 0 || node.y == 0) {
                float radius = (float) (initialRadius * Math.sqrt(i));
                float angle = i * initialAngle;
                node.x = (float) (radius * Math.cos(angle));
                node.y = (float) (radius * Math.sin(angle));
            }
        }
    }

    private void step() {
        tick();
        if (alpha < alphaMin) {
            task.pause();
        }
    }

    private Simulation(Builder builder) {
        this.initialRadius = builder.initialRadius;
        this.initialAngle = builder.initialAngle;
        this.alpha = builder.alpha;
        this.alphaMin = builder.alphaMin;
        this.alphaDecay = builder.alphaDecay;
        this.alphaTarget = builder.alphaTarget;
        this.velocityDecay = builder.velocityDecay;

        nodes = builder.nodes;
        links = builder.links;

        chain = new ForceAlgorithmChain();

        initializeNodes();
    }

    public static class Builder {

        private int initialRadius;
        private float initialAngle;
        private float alpha;
        private float alphaMin;
        private float alphaDecay;
        private float alphaTarget;
        private float velocityDecay;

        private List<Node> nodes;
        private List<Link> links;

        public Builder() {
            this.initialRadius = 50;
            this.initialAngle = (float) (Math.PI * (3 - Math.sqrt(5)));
            this.alpha = 1;
            this.alphaMin = 0.001f;
            this.alphaDecay = (float) (1 - Math.pow(alphaMin, 1.0 / 300));
            this.alphaTarget = 0;
            this.velocityDecay = 0.6f;

            this.nodes(null);
            this.links(null);
        }

        public Builder nodes(List<Node> nodes) {
            this.nodes = nodes;
            if (nodes == null) {
                this.nodes = Collections.emptyList();
            }
            return this;
        }

        public Builder links(List<Link> links) {
            this.links = links;
            if (links == null) {
                this.links = Collections.emptyList();
            }
            return this;
        }

        public Simulation build() {
            return new Simulation(this);
        }
    }

    private class Task extends TimerTask {
        private volatile int lock = 0;

        @Override
        public void run() {
            if (lock == 0) {
                step();
            } else {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        void pause() {
            lock = 1;
        }

        void resume() {
            synchronized (this) {
                if (lock == 1) {
                    lock = 0;
                    notify();
                }
            }
        }
    }
}
