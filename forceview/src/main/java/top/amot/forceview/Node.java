package top.amot.forceview;

public class Node {

    public static final float RADIUS = 50;

    // the node’s zero-based index into nodes
    public int index;
    // the node’s current x-position and y-position, the position is initialized in a phyllotaxis arrangement.
    public double x, y;
    // the node’s current x-velocity and y-velocity, the velocity is initialized to ⟨0,0⟩.
    public double vx, vy;
    // the node’s fixed x-position and y-position
    public double fx = Double.MAX_VALUE, fy = Double.MAX_VALUE;

    public String text;
    public int level;

    public float radius = RADIUS;

    public Node() {
    }

    public Node(String text, int level) {
        this.text = text;
        this.level = level;
    }

}
