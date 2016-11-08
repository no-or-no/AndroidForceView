package top.amot.androidforceview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.Toast;

import java.util.ArrayList;

import top.amot.library.view.force.FLink;
import top.amot.library.view.force.FNode;
import top.amot.library.view.force.ForceSurfaceView;
import top.amot.library.view.force.ForceView;

public class MainActivity extends AppCompatActivity {

    ForceView fview;
    ForceSurfaceView fsView;
    private ArrayList<FNode> nodes;
    private ArrayList<FLink> links;
    private SparseArray<FNode> nodeMap;
    private Toast toast;

    private void show(String text) {
        if (toast == null) {
            toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nodes = new ArrayList<>();
        links = new ArrayList<>();
//        initData();
//        initData2();
        initData3();

        /*fview = (ForceView) findViewById(R.id.fview);
        fview.setData(nodes, links);
        fview.setOnNodeClickListener(new OnNodeClickListener() {
            @Override
            public void onNodeClick(FNode node) {
                show(node.getText());
            }
        });*/

        fsView = (ForceSurfaceView) findViewById(R.id.fsview);
        fsView.setData(nodes, links);

    }

    private void initData3() {
        nodes.add(new FNode("0", dp2px(30), 0));
        for (int i = 1; i <= 20; i++) {
            nodes.add(new FNode("" + i, dp2px(30), 1));
        }
        for (int i = 1; i <= 20; i++) {
            FLink link = new FLink(nodes.get(0), nodes.get(i));
            link.setText("0-" + i);
            links.add(link);
        }
        for (int i = 21; i <= 25; i++) {
            nodes.add(new FNode("" + i, dp2px(30), 2));
        }
        for (int i = 26; i <= 35; i++) {
            nodes.add(new FNode("" + i, dp2px(30), 3));
        }
        for (int i = 36; i <= 50; i++) {
            nodes.add(new FNode("" + i, dp2px(30), 4));
        }

        addLink3(7, 8, 8);
        addLink3(9, 24, 28);
        addLink3(10, 25, 26);
        addLink3(7, 21, 25);
        addLink3(8, 24, 26);
        addLink3(23, 26, 35);
        addLink3(26, 26, 35);
        addLink3(30, 36, 50);

    }

    private void initData2() {
        int n = 500;
        for (int i = 0; i < n; i++) {
            nodes.add(new FNode("节点" + i, 30f, 1));
        }
        for (int i = 0; i < n / 2; i++) {
            links.add(new FLink(nodes.get(i), nodes.get(n / 2 + i)));
        }

    }

    private void initData() {
        nodeMap = new SparseArray<>();

        FNode rootNode = new FNode("根节点0", dp2px(35), 0);
        nodeMap.put(0, rootNode);
        nodes.add(rootNode);

        int r = dp2px(30);
        for (int i = 1; i <= 50; i++) {
            FNode node = new FNode("节点-" + i, r, 1);
            nodeMap.put(i, node);
            nodes.add(node);

            FLink link = new FLink(rootNode, node);
            link.setText("关系-0-" + i);
            links.add(link);
            if (i % 3 == 0) {
                FLink link1 = new FLink(node, rootNode);
                link1.setText("关系-" + i + "-0");
                links.add(link1);
            }
        }

        for (int i = 51; i <= 200; i++) {
            FNode node = new FNode("" + i, r, 2);
            nodeMap.put(i, node);
            nodes.add(node);
        }

        addLink(15, 51, 60);
        addLink(23, 60, 80);
        addLink(24, 81, 103);
        addLink(41, 104, 109);
        addLink(44, 110, 130);
        addLink(50, 125, 200);


        for (int i = 201; i <= 240; i++) {
            FNode node = new FNode("" + i, r, 3);
            nodeMap.put(i, node);
            nodes.add(node);
        }
        for (int i = 241; i <= 600; i++) {
            FNode node = new FNode("" + i, r, 4);
            nodeMap.put(i, node);
            nodes.add(node);
        }

        addLink(137, 201, 220);
        addLink(50, 201, 220);
        addLink(144, 145, 145);
        addLink(145, 201, 220);
        addLink(151, 221, 300);
        addLink(205, 221, 230);
        addLink(37, 241, 260);
        addLink(121, 249, 270);
        addLink(23, 268, 275);
        addLink(249, 275, 600);



    }

    private void addLink(int n, int start, int end) {
        FNode n1 = nodeMap.get(n);
        for (; start <= end; start++) {
            FLink link = new FLink(n1, nodeMap.get(start));
            link.setText(n + "-" + start);
            links.add(link);
        }
    }
    private void addLink3(int n, int start, int end) {
        FNode n1 = nodes.get(n);
        for (; start <= end; start++) {
            FLink link = new FLink(n1, nodes.get(start));
            link.setText(n + "-" + start);
            links.add(link);
        }
    }

    public int dp2px(float dipValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
