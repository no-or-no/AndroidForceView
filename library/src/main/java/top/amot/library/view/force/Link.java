package top.amot.library.view.force;

/**
 * <p>Created by Z.pan on 2016/12/31.</p>
 */
public class Link {

    String text;

    Node source;
    Node target;
    int index = -1;

    public Link() {
    }

    public Link(Node source, Node target, String text) {
        this.source = source;
        this.target = target;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    float length() {
        float dx = source.x - target.x;
        float dy = source.y - target.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
