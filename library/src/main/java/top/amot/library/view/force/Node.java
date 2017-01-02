package top.amot.library.view.force;

/**
 * <p>Created by Z.pan on 2016/12/31.</p>
 */
public class Node {

    String text;
    int level;
    private Object data;

    float radius = 50;
    float x, y;
    float fx, fy;
    float vx, vy;
    int index = -1;

    public Node() {
    }

    public Node(String text, int level) {
        this.text = text;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 给定一个坐标 (x, y)，判断该坐标是否在节点所在范围内。用来判断是否点击了该节点。
     * @param x x坐标
     * @param y y坐标
     * @param scale 缩放比例
     * @return true 表示 (x, y) 在该节点内部
     */
    boolean isInside(float x, float y, float scale) {
        float left = (this.x - radius) * scale;
        float top = (this.y - radius) * scale;
        float right = (this.x + radius) * scale;
        float bottom = (this.y + radius) * scale;
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    @Override
    public String toString() {
        return "Node{" +
                "text='" + text + '\'' +
                ", level=" + level +
                ", x=" + x +
                ", y=" + y +
                ", fx=" + fx +
                ", fy=" + fy +
                ", vx=" + vx +
                ", vy=" + vy +
                ", index=" + index +
                '}';
    }
}
