package top.amot.library.view.force;

/**
 * Created by Z.Pan on 2016/10/9.
 */

public class FLink {

    final FNode source;
    final FNode target;
    private String text;

    public FLink(FNode source, FNode target) {
        this.source = source;
        this.target = target;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
