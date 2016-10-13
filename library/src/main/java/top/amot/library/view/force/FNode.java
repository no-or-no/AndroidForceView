package top.amot.library.view.force;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Z.Pan on 2016/10/8.
 */

public class FNode {

    public static final int ROOT_NODE_LEVEL = 0;
    public static final short DRAG_START = 2;
    public static final short DRAG = 4;
    public static final short DRAG_END = 6;

    private String text;
    private Object obj;
    private int level;

    float x, y;
    float px, py;
    int weight;
    private float radius = 50f;
    private short state;

    public FNode(String text) {
        this(text, 50f, ROOT_NODE_LEVEL);
    }

    public FNode(String text, float radius, int level) {
        this.text = text;
        this.radius = radius;
        this.level = level;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getText() {
        return text;
    }

    public Object getObj() {
        return obj;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    float getRadius() {
        return radius;
    }

    boolean isRootNode() {
        return level == ROOT_NODE_LEVEL;
    }

    boolean isInside(float x, float y) {
        float left = this.x - radius;
        float top = this.y - radius;
        float right = this.x + radius;
        float bottom = this.y + radius;
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    boolean isStable() {
        return state != 0;
    }

    void setDragState(@State short state) {
        switch (state) {
            case DRAG_START:
                this.state |= state;
                break;
            case DRAG_END:
                this.state &= ~state;
                break;
        }
    }

    @ShortDef({DRAG_START, DRAG, DRAG_END})
    @Retention(SOURCE)
    public @interface State {}

    @Retention(SOURCE)
    @Target({ANNOTATION_TYPE})
    public @interface ShortDef {
        short[] value() default {};
        boolean flag() default false;
    }

}
