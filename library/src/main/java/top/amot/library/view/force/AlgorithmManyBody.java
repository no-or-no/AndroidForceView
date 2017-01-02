package top.amot.library.view.force;

import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.List;

import static top.amot.library.view.force.ForceAlgorithm.Utils.jiggle;

/**
 * <p>Created by Z.pan on 2017/1/1.</p>
 */
class AlgorithmManyBody implements ForceAlgorithm {

    private List<Node> nodes;
    private float[] strengths;
    private float alpha;
    private float theta2 = 0.81f;
    private float distanceMin2 = 1;
    private float distanceMax2 = Float.POSITIVE_INFINITY;

    @Override
    public void force(float alpha) {
        this.alpha = alpha;
        if (nodes != null) {
            QuadTree quadTree = quadTree(nodes);
            quadTree.accumulate();
            for (Node node : nodes) {
                quadTree.visit(node);
            }
        }
    }

    @Override
    public void initialize(@NonNull List<Node> nodes) {
        this.nodes = nodes;
        if (nodes.isEmpty()) {
            return;
        }

        strengths = new float[nodes.size()];
        for (Node node : nodes) {
            strengths[node.index] = getStrength(node);
        }

    }

    private float getStrength(Node node) {
        return -30f;
    }

    private QuadTree quadTree(List<Node> nodes) {
        QuadTree quadTree = new QuadTree(0, 0, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
        if (nodes != null && !nodes.isEmpty()) {
            quadTree.addAll(nodes);
        }
        return quadTree;
    }

    private class QuadTree {

        float x, y;
        float x0, y0; // top-left
        float x1, y1; // bottom-right
        Quad root;

        QuadTree(float x, float y, float x0, float y0, float x1, float y1) {
            this.x = x;
            this.y = y;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        void add(Node node) {
            // ignore invalid points
            if (node == null) {
                return;
            }

            Quad leaf = new Quad(node);

            // If the tree is empty, initialize the root as a leaf.
            if (root == null) {
                root = leaf;
                return;
            }

            float x0 = this.x0;
            float y0 = this.y0;
            float x1 = this.x1;
            float y1 = this.y1;

            // Find the existing leaf for the new point, or add it.
            Quad quad = root;
            Quad[] parent = null;
            int i = 0;
            while (quad.children != null) {
                float xm = (x0 + x1) / 2;
                float ym = (y0 + y1) / 2;

                int isRight, isBottom;
                if (node.x >= xm) {
                    isRight = 1;
                    x0 = xm;
                } else {
                    isRight = 0;
                    x1 = xm;
                }
                if (node.y >= ym) {
                    isBottom = 1;
                    y0 = ym;
                } else {
                    isBottom = 0;
                    y1 = ym;
                }

                parent = quad.children;
                i = isBottom << 1 | isRight;
                quad = quad.children[i];
                if (quad == null) {
                    parent[i] = leaf;
                    return;
                }
            }

            // Is the new point is exactly coincident with the existing point?
            float xp = quad.data.x;
            float yp = quad.data.y;
            if (node.x == xp && node.y == yp) {
                leaf.next = quad;
                if (parent != null) {
                    parent[i] = leaf;
                } else {
                    root = leaf;
                }
                return;
            }

            // Otherwise, split the leaf node until the old and new point are separated.
            int isRight, isBottom, j;
            do {
                if (parent != null) {
                    parent[i] = new Quad();
                    parent[i].children = new Quad[4];
                    parent = parent[i].children;
                } else {
                    root.children = new Quad[4];
                    parent = root.children;
                }

                float xm = (x0 + x1) / 2;
                float ym = (y0 + y1) / 2;

                if (node.x >= xm) {
                    isRight = 1;
                    x0 = xm;
                } else {
                    isRight = 0;
                    x1 = xm;
                }
                if (node.y >= ym) {
                    isBottom = 1;
                    y0 = ym;
                } else {
                    isBottom = 0;
                    y1 = ym;
                }

                i = isBottom << 1 | isRight;
                j = (yp >= ym ? 1 : 0) << 1 | (xp >= xm ? 1 : 0);
            } while (i == j);
            parent[j] = new Quad(quad.data);
            parent[i] = leaf;
        }

        void addAll(List<Node> nodes) {
            if (nodes == null || nodes.isEmpty()) {
                return;
            }

            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;

            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                float x = node.x;
                float y = node.y;

                if (x == 0 || y == 0) {
                    continue;
                }

                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }

            if (maxX < minX) {
                minX = this.x0;
                maxX = this.x1;
            }
            if (maxY < minY) {
                minY = this.y0;
                maxY = this.y1;
            }

            cover(minX, minY);
            cover(maxX, maxY);

            for (Node node : nodes) {
                add(node);
            }
        }

        void cover(float x, float y) {
            if (Float.isNaN(x) || Float.isNaN(y)) {
                return;
            }
            if (Float.isNaN(x0)) {
                x1 = (x0 = (float) Math.floor(x)) + 1;
                y1 = (y0 = (float) Math.floor(y)) + 1;
            } else if (x0 > x || x > x1 || y0 > y || y > y1) {
                float z = x1 - x0;
                int isTop = (y < (y0 + y1) / 2) ? 1 : 0;
                int isLeft = (x < (x0 + x1) / 2) ? 1 : 0;
                int i = isTop << 1 | isLeft;

                Quad quad = root;
                Quad[] parent = null;
                switch (i) {
                    case 0:
                        do {
                            parent = new Quad[4];
                            parent[i] = quad;
                            quad = new Quad();
                            quad.children = parent;

                            z *= 2;
                            x1 = x0 + z;
                            y1 = y0 + z;
                        } while (x > x1 || y > y1);
                        break;
                    case 1:
                        do {
                            parent = new Quad[4];
                            parent[i] = quad;
                            quad = new Quad();
                            quad.children = parent;

                            z *= 2;
                            x0 = x1 - z;
                            y1 = y0 + z;
                        } while (x0 > x || y > y1);
                        break;
                    case 2:
                        do {
                            parent = new Quad[4];
                            parent[i] = quad;
                            quad = new Quad();
                            quad.children = parent;

                            z *= 2;
                            x1 = x0 + z;
                            y0 = y1 - z;
                        } while (x > x1 || y0 > y);
                        break;
                    case 3:
                        do {
                            parent = new Quad[4];
                            parent[i] = quad;
                            quad = new Quad();
                            quad.children = parent;

                            z *= 2;
                            x0 = x1 - z;
                            y0 = y1 - z;
                        } while (x0 > x || y0 > y);
                        break;
                }

                if (root != null) {
                    root = quad;
                }
            }
        }

        void accumulate() {
            ArrayDeque<Quad> stack = new ArrayDeque<>();
            ArrayDeque<Quad> next = new ArrayDeque<>();
            if (root != null) {
                stack.push(root.xy(x0, y0, x1, y1));
            }
            Quad q;
            while (!stack.isEmpty()) {
                q = stack.pop();
                if (q.children != null) {
                    Quad child;
                    float x0 = q.x0, y0 = q.y0;
                    float x1 = q.x1, y1 = q.y1;
                    float xm = (x0 + x1) / 2, ym = (y0 + y1) / 2;
                    if ((child = q.children[0]) != null) {
                        stack.push(child.xy(x0, y0, xm, ym));
                    }
                    if ((child = q.children[1]) != null) {
                        stack.push(child.xy(xm, y0, x1, ym));
                    }
                    if ((child = q.children[2]) != null) {
                        stack.push(child.xy(x0, ym, xm, y1));
                    }
                    if ((child = q.children[3]) != null) {
                        stack.push(child.xy(xm, ym, x1, y1));
                    }
                }
//                Log.e("TAG", "push");
                next.push(q);
            }
            while (!next.isEmpty()) {
                q = next.pop();
                int strength = 0;
                float x$$1 = 0, y$$1 = 0;
                Quad _q;
                if (q.children != null) {
                    for (int i = 0; i < 4; i++) {
                        if ((_q = q.children[i]) != null) {
                            int c = _q.strength;
                            strength += c;
                            x$$1 += c * _q.x;
                            y$$1 += c * _q.y;
                        }
                    }
                    q.x = x$$1 / strength;
                    q.y = y$$1 / strength;
                } else {
                    _q = q;
                    _q.x = _q.data.x;
                    _q.y = _q.data.y;
                    do {
                        strength += strengths[_q.data.index];
                    } while ((_q = _q.next) != null);
                }
                q.strength = strength;
            }
        }

        void visit(Node node) {
            ArrayDeque<Quad> stack = new ArrayDeque<>();
            if (root != null) {
                stack.push(root.xy(x0, y0, x1, y1));
            }
            Quad q;
            while (!stack.isEmpty()) {
                q = stack.pop();
                float x0 = q.x0, y0 = q.y0;
                float x1 = q.x1, y1 = q.y1;
                Quad[] children = q.children;
                if (!apply(q, node) && children != null) {
                    float xm = (x0 + x1) / 2;
                    float ym = (y0 + y1) / 2;
                    Quad child;
                    if ((child = children[3]) != null) {
                        stack.push(child.xy(xm, ym, x1, y1));
                    }
                    if ((child = children[2]) != null) {
                        stack.push(child.xy(x0, ym, xm, y1));
                    }
                    if ((child = children[1]) != null) {
                        stack.push(child.xy(xm, y0, x1, ym));
                    }
                    if ((child = children[0]) != null) {
                        stack.push(child.xy(x0, y0, xm, ym));
                    }
                }
            }
        }

        boolean apply(Quad q, Node node) {
            if (q.strength == 0) {
                return true;
            }
            float x$$1 = q.x - node.x;
            float y$$1 = q.y - node.y;
            float w = q.x1 - q.x0;
            float l = x$$1 * x$$1 + y$$1 * y$$1;

            // Apply the Barnes-Hut approximation if possible.
            // Limit forces for very close nodes; randomize direction if coincident.
            if (w * w / theta2 < l) {
                if (l < distanceMax2) {
                    if (x$$1 == 0) {
                        x$$1 = jiggle();
                        l += x$$1 * x$$1;
                    }
                    if (y$$1 == 0) {
                        y$$1 = jiggle();
                        l += y$$1 * y$$1;
                    }
                    if (l < distanceMin2) {
                        l = (float) Math.sqrt(distanceMin2 * l);
                    }
                    node.vx += x$$1 * q.strength * alpha / l;
                    node.vy += y$$1 * q.strength * alpha / l;
                }
                return true;
            }

            // Limit forces for very close nodes; randomize direction if coincident.
            if (q.data != node || q.next != null) {
                if (x$$1 == 0) {
                    x$$1 = jiggle();
                    l += x$$1 * x$$1;
                }
                if (y$$1 == 0) {
                    y$$1 = jiggle();
                    l += y$$1 * y$$1;
                }
                if (l < distanceMin2) {
                    l = (float) Math.sqrt(distanceMin2 * l);
                }
            }

            do {
                if (q.data != node && q.data != null) {
                    w = strengths[q.data.index] * alpha / l;
                    node.vx += x$$1 * w;
                    node.vy += y$$1 * w;
                }
            } while ((q = q.next) != null);

            return false;
        }
    }

    private class Quad {
        Node data;
        Quad next;
        Quad[] children;

        float x, y;
        float x0, y0;
        float x1, y1;
        int strength;

        Quad () {
        }

        Quad(Node data) {
            this.data = data;
        }

        Quad xy(float x0, float y0, float x1, float y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            return this;
        }
    }

}
