package top.amot.forceview;

public class Link {

    public Node source;
    public Node target;
    public int index = -1;

    public String text;

    public Link() {
    }

    public Link(Node source, Node target, String text) {
        this.source = source;
        this.target = target;
        this.text = text;
    }

    float length() {
        double dx = source.x - target.x;
        double dy = source.y - target.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
