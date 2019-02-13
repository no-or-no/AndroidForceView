package top.amot.forceview;

import java.util.ArrayDeque;
import java.util.ArrayList;

public final class QuadTree {

    Force.NodeCalculation x, y;
    double x0, y0; // top-left
    double x1, y1; // bottom-right

    TreeNode root;

    public static QuadTree create(Node[] nodes, Force.NodeCalculation x, Force.NodeCalculation y) {
        QuadTree tree = new QuadTree(x, y, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        if (nodes != null) {
            tree.addAll(nodes);
        }
        return tree;
    }

    private QuadTree(Force.NodeCalculation x, Force.NodeCalculation y, double x0, double y0, double x1, double y1) {
        this.x = x;
        this.y = y;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public QuadTree addAll(Node[] nodes) {
        double[] xz = new double[nodes.length], yz = new double[nodes.length];
        double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY;
        double x, y;
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            x = node.x;
            y = node.y;
            xz[i] = x;
            yz[i] = y;
            if (x < x0) x0 = x;
            if (x > x1) x1 = x;
            if (y < y0) y0 = y;
            if (y > y1) y1 = y;
        }

        if (x1 < x0) {
            x0 = this.x0;
            x1 = this.x1;
        }
        if (y1 < y0) {
            y0 = this.y0;
            y1 = this.y1;
        }

        cover(x0, y0).cover(x1, y1);

        for (int i = 0; i < nodes.length; i++) {
            add(xz[i], yz[i], nodes[i]);
        }

        return this;
    }

    public QuadTree add(double x, double y, Node d) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return this;
        }

        TreeNode node = root;
        TreeNode leaf = TreeNode.createLeaf(d);
        if (node == null) {
            root = leaf;
            return this;
        }

        double x0 = this.x0, y0 = this.y0;
        double x1 = this.x1, y1 = this.y1;

        int right, bottom;
        double xm, ym;
        TreeNode parent = null;
        int i = 0, j = 0;
        while (!node.isLeaf()) {
            xm = (x0 + x1) / 2;
            ym = (y0 + y1) / 2;

            if (x >= xm) {
                right = 1;
                x0 = xm;
            } else {
                right = 0;
                x1 = xm;
            }

            if (y >= ym) {
                bottom = 1;
                y0 = ym;
            } else {
                bottom = 0;
                y1 = ym;
            }

            parent = node;
            i = bottom << 1 | right;
            node = node.quadrants[i];
            if (node == null) {
                parent.quadrants[i] = leaf;
                return this;
            }
        }

        double xp = x(node.data), yp = y(node.data);
        if (x == xp && y == yp) {
            leaf.next = node;
            if (parent != null) {
                parent.quadrants[i] = leaf;
            } else {
                root = leaf;
            }
            return this;
        }

        do {
            if (parent != null) {
                parent = parent.quadrants[i] = TreeNode.createInternal();
            } else {
                parent = root = TreeNode.createInternal();
            }

            xm = (x0 + x1) / 2;
            ym = (y0 + y1) / 2;

            if (x >= xm) {
                right = 1;
                x0 = xm;
            } else {
                right = 0;
                x1 = xm;
            }

            if (y >= ym) {
                bottom = 1;
                y0 = ym;
            } else {
                bottom = 0;
                y1 = ym;
            }

            i = bottom << 1 | right;
            j = (yp >= ym ? 1 : 0) << 1 | (xp >= xm ? 1 : 0);
        } while (i == j);

        parent.quadrants[j] = node;
        parent.quadrants[i] = leaf;

        return this;
    }

    public QuadTree cover(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return this;
        }

        double x0 = this.x0, y0 = this.y0;
        double x1 = this.x1, y1 = this.y1;

        if (Double.isNaN(x0)) {
            x1 = (x0 = Math.floor(x)) + 1;
            y1 = (y0 = Math.floor(y)) + 1;
        } else if (x0 > x || x > x1 || y0 > y || y > y1) {
            double z = x1 - x0;
            TreeNode node = root;
            TreeNode parent;

            int i = (y < (y0 + y1) / 2 ? 1 : 0) << 1 | (x < (x0 + x1) / 2 ? 1 : 0);
            switch (i) {
                case 0:
                    do {
                        parent = TreeNode.createInternal();
                        parent.quadrants[i] = node;
                        node = parent;

                        z *= 2;
                        x1 = x0 + z;
                        y1 = y0 + z;
                    } while (x > x1 || y > y1);
                    break;
                case 1:
                    do {
                        parent = TreeNode.createInternal();
                        parent.quadrants[i] = node;
                        node = parent;

                        z *= 2;
                        x0 = x1 - z;
                        y1 = y0 + z;
                    } while (x0 > x || y > y1);
                    break;
                case 2:
                    do {
                        parent = TreeNode.createInternal();
                        parent.quadrants[i] = node;
                        node = parent;

                        z *= 2;
                        x1 = x0 + z;
                        y0 = y1 - z;
                    } while (x > x1 || y0 > y);
                    break;
                case 3:
                    do {
                        parent = TreeNode.createInternal();
                        parent.quadrants[i] = node;
                        node = parent;

                        z *= 2;
                        x0 = x1 - z;
                        y0 = y1 - z;
                    } while (x0 > x || y0 > y);
                    break;
            }

            if (root != null && !root.isLeaf()) {
                root = node;
            }
        } else {
            return this;
        }

        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;

        return this;
    }

    public QuadTree visit(VisitCallback callback) {
        ArrayDeque<Quad> quads = new ArrayDeque<>();
        TreeNode node = root;

        if (node != null) {
            quads.push(new Quad(node, x0, y0, x1, y1));
        }

        Quad q;
        TreeNode child;
        while ((q = quads.pop()) != null) {
            node = q.node;
            double x0 = q.x0, y0 = q.y0, x1 = q.x1, y1 = q.y1;
            if (!callback.visit(node, x0, y0, x1, y1) && !node.isLeaf()) {
                double xm = (x0 + x1) / 2, ym = (y0 + y1) / 2;
                if ((child = node.quadrants[3]) != null) {
                    quads.push(new Quad(child, xm, ym, x1, y1));
                }
                if ((child = node.quadrants[2]) != null) {
                    quads.push(new Quad(child, x0, ym, xm, y1));
                }
                if ((child = node.quadrants[1]) != null) {
                    quads.push(new Quad(child, xm, y0, x1, ym));
                }
                if ((child = node.quadrants[0]) != null) {
                    quads.push(new Quad(child, x0, y0, xm, ym));
                }
            }
        }
        return this;
    }

    public QuadTree visitAfter(VisitCallback callback) {
        ArrayDeque<Quad> quads = new ArrayDeque<>();
        ArrayDeque<Quad> next = new ArrayDeque<>();

        if (root != null) {
            quads.push(new Quad(root, x0, y0, x1, y1));
        }

        Quad q;
        TreeNode child;
        while ((q = quads.pop()) != null) {
            TreeNode node = q.node;
            if (!node.isLeaf()) {
                double x0 = q.x0, y0 = q.y0, x1 = q.x1, y1 = q.y1;
                double xm = (x0 + x1) / 2, ym = (y0 + y1) / 2;
                if ((child = node.quadrants[0]) != null) {
                    quads.push(new Quad(child, x0, y0, xm, ym));
                }
                if ((child = node.quadrants[1]) != null) {
                    quads.push(new Quad(child, xm, y0, x1, ym));
                }
                if ((child = node.quadrants[2]) != null) {
                    quads.push(new Quad(child, x0, ym, xm, y1));
                }
                if ((child = node.quadrants[3]) != null) {
                    quads.push(new Quad(child, xm, ym, x1, y1));
                }
            }
            next.push(q);
        }

        while ((q = next.pop()) != null) {
            callback.visit(q.node, q.x0, q.y0, q.x1, q.y1);
        }

        return this;
    }

    Node find(double x, double y, double radius) {
        Node data = null;
        ArrayList<Quad> quads = new ArrayList<>();
        TreeNode node = root;
        double x0 = this.x0, y0 = this.y0, x1, y1, x2, y2, x3 = this.x1, y3 = this.y1;

        if (node != null) {
            quads.add(new Quad(node, x0, y0, x3, y3));
        }

        if (radius <= 0) {
            radius = Double.POSITIVE_INFINITY;
        } else {
            x0 = x - radius;
            y0 = y - radius;
            x3 = x + radius;
            y3 = y + radius;
            radius *= radius;
        }

        Quad q;
        while ((q = quads.remove(quads.size() - 1)) != null) {
            if ((node = q.node) == null
                    || (x1 = q.x0) > x3
                    || (y1 = q.y0) > y3
                    || (x2 = q.x1) < x0
                    || (y2 = q.y1) < y0) {
                continue;
            }

            if (!node.isLeaf()) {
                double xm = (x1 + x2) / 2, ym = (y1 + y2) / 2;
                quads.add(new Quad(node.quadrants[3], xm, ym, x2, y2));
                quads.add(new Quad(node.quadrants[2], x1, ym, xm, y2));
                quads.add(new Quad(node.quadrants[1], xm, y1, x2, ym));
                quads.add(new Quad(node.quadrants[0], x1, y1, xm, ym));

                int i = (y >= ym ? 1 : 0) << 1 | (x >= xm ? 1 : 0);
                if (i > 0) {
                    q = quads.get(quads.size() - 1);
                    quads.set(quads.size() - 1, quads.get(quads.size() - 1 - i));
                    quads.set(quads.size() - 1 - i, q);
                }
            } else {
                double dx = x - x(node.data), dy = y - y(node.data);
                double d2 = dx * dx + dy * dy;
                if (d2 < radius) {
                    radius = d2;
                    double d = Math.sqrt(radius);
                    x0 = x - d;
                    y0 = y - d;
                    x3 = x + d;
                    y3 = y + d;
                    data = node.data;
                }
            }
        }

        return data;
    }

    int size() {
        final int[] size = {0};
        visit((node, x01, y01, x11, y11) -> {
            if (node.isLeaf()) {
                do {
                    size[0]++;
                } while ((node = node.next) != null);
            }
            return false;
        });
        return size[0];
    }

    private double x(Node node) {
        if (x != null) {
            return x.calculate(node);
        }
        return 0;
    }

    private double y(Node node) {
        if (y != null) {
            return y.calculate(node);
        }
        return 0;
    }

    public interface VisitCallback {
        /**
         * If the callback returns true for a given node, then the children of that node are not visited;
         * otherwise, all child nodes are visited. This can be used to quickly visit only parts of the tree.
         * <p>
         * ⟨x0, y0⟩ are the lower bounds of the node, and ⟨x1, y1⟩ are the upper bounds,
         * Assuming that positive x is right and positive y is down, as is typically the case in
         * Canvas, ⟨x0, y0⟩ is the top-left corner and ⟨x1, y1⟩ is the lower-right corner.
         */
        boolean visit(TreeNode node, double x0, double y0, double x1, double y1);
    }

    public static class TreeNode {

        public TreeNode[] quadrants;

        public Node data; // only leaf node has data
        public TreeNode next;

        public double strength;
        public double x, y;
        public double r;
        public int index;

        /** Leaf node */
        public static TreeNode createLeaf(Node data) {
            TreeNode node = new TreeNode();
            node.data = data;
            return node;
        }

        /**
         * Internal nodes of the quadtree are represented as four-element arrays in left-to-right,
         * top-to-bottom order:
         * <li>0 - the top-left quadrant, if any.</li>
         * <li>1 - the top-right quadrant, if any.</li>
         * <li>2 - the bottom-left quadrant, if any.</li>
         * <li>3 - the bottom-right quadrant, if any.</li>
         */
        public static TreeNode createInternal() {
            TreeNode node = new TreeNode();
            node.quadrants = new TreeNode[4];
            return node;
        }

        private TreeNode() {}

        public boolean isLeaf() {
            return quadrants == null;
        }
    }

    static class Quad {
        TreeNode node;
        double x0, y0;
        double x1, y1;

        Quad(TreeNode node, double x0, double y0, double x1, double y1) {
            this.node = node;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
    }
}
