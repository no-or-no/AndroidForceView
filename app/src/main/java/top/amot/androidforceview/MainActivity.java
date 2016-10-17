package top.amot.androidforceview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

import top.amot.library.view.force.FLink;
import top.amot.library.view.force.FNode;
import top.amot.library.view.force.ForceView;

public class MainActivity extends AppCompatActivity {

    ForceView fview;
    private ArrayList<FNode> nodes;
    private ArrayList<FLink> links;

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

        fview = (ForceView) findViewById(R.id.fview);

        initData();
        fview.setData(nodes, links);
        fview.setOnNodeClickListener(new ForceView.OnNodeClickListener() {
            @Override
            public void onNodeClick(FNode node) {
                show(node.getText());
            }
        });
    }

    private void initData() {
        nodes = new ArrayList<>();
        links = new ArrayList<>();

        nodes.add(new FNode("0", 50f, 1));
        nodes.add(new FNode("1", 50f, 2));
        nodes.add(new FNode("2", 50f, 2));
//        nodes.add(new FNode("3", 50f, 2));
//        nodes.add(new FNode("4", 50f, 2));
//        nodes.add(new FNode("5", 50f, 2));
//        nodes.add(new FNode("6", 50f, 2));
//        nodes.add(new FNode("7", 50f, 2));
//        nodes.add(new FNode("8", 50f, 2));
//        nodes.add(new FNode("9", 50f, 2));
//        nodes.add(new FNode("10", 50f, 2));

        for (int i = 1; i <= 2; i++) {
            links.add(new FLink(nodes.get(0), nodes.get(i)));
        }

//        for (int i = 1; i <= 10; i++) {
//            nodes.add(new FNode("" + i + "-1", 50f, 3));
//            nodes.add(new FNode("" + i + "-2", 50f, 3));
//            nodes.add(new FNode("" + i + "-3", 50f, 3));
//            nodes.add(new FNode("" + i + "-4", 50f, 3));
//            nodes.add(new FNode("" + i + "-5", 50f, 3));
//        }
//
//        for (int i = 1, j = 11; i <= 10; i++) {
//            for (int n = 1; n <= 5; n++) {
//                links.add(new FLink(nodes.get(i), nodes.get(j++)));
//            }
//        }
//
//        FNode node0 = nodes.get(0);
//        FNode node1 = nodes.get(1);
//        FNode node2 = nodes.get(2);
//        FNode node3 = nodes.get(3);
//        FNode node4 = nodes.get(4);
//        for (int i = 1; i <= 10; i++) {
//            FNode node = new FNode("0-" + i, 50f, 2);
//            nodes.add(node);
//            links.add(new FLink(node0, node));
//        }
//
//        for (int i = 1; i <= 7; i++) {
//            FNode node = new FNode("1-" + i, 50f, 3);
//            nodes.add(node);
//            links.add(new FLink(node1, node));
//        }
//        for (int i = 1; i <= 3; i++) {
//            FNode node = new FNode("2-" + i, 50f, 3);
//            nodes.add(node);
//            links.add(new FLink(node2, node));
//        }
//
//        FNode node2_x1 = nodes.get(nodes.size() - 1);
//        FNode node2_x2 = nodes.get(nodes.size() - 2);
//        FNode node2_x3 = nodes.get(nodes.size() - 3);
//        for (int i = 1; i <= 7; i++) {
//            FNode node_1 = new FNode(node2_x1.getText() + "-" + i, 50f, 4);
//            FNode node_2 = new FNode(node2_x2.getText() + "-" + i, 50f, 4);
//            FNode node_3 = new FNode(node2_x3.getText() + "-" + i, 50f, 4);
//            nodes.add(node_1);
//            nodes.add(node_2);
//            nodes.add(node_3);
//            links.add(new FLink(node2_x1, node_1));
//            links.add(new FLink(node2_x2, node_2));
//            links.add(new FLink(node2_x3, node_3));
//        }
//
//        for (int i = 1; i <= 15; i++) {
//            FNode node = new FNode("3-" + i, 50f, 3);
//            nodes.add(node);
//            links.add(new FLink(node3, node));
//        }
//
//        FNode node3_x1 = nodes.get(nodes.size() - 1);
//        FNode node3_x2 = nodes.get(nodes.size() - 2);
//        FNode node3_x3 = nodes.get(nodes.size() - 3);
//        for (int i = 1; i <= 10; i++) {
//            FNode node_1 = new FNode(node3_x1.getText() + "-" + i, 50f, 4);
//            FNode node_2 = new FNode(node3_x2.getText() + "-" + i, 50f, 4);
//            FNode node_3 = new FNode(node3_x3.getText() + "-" + i, 50f, 4);
//            nodes.add(node_1);
//            nodes.add(node_2);
//            nodes.add(node_3);
//            links.add(new FLink(node3_x1, node_1));
//            links.add(new FLink(node3_x2, node_2));
//            links.add(new FLink(node3_x3, node_3));
//        }
//
//        for (int i = 1; i <= 13; i++) {
//            FNode node = new FNode("4-" + i, 50f, 3);
//            nodes.add(node);
//            links.add(new FLink(node4, node));
//        }
//
//        FNode node4_x = nodes.get(nodes.size() - 1);
//        for (int i = 1; i <= 6; i++) {
//            FNode node = new FNode(node4_x.getText() + "-" + i, 50f, 4);
//            nodes.add(node);
//            links.add(new FLink(node4_x, node));
//        }
//
//        FNode node5_x = nodes.get(nodes.size() - 1);
//        for (int i = 1; i <= 10; i++) {
//            FNode node = new FNode(node5_x.getText() + "-aaabc" + i, 50f, 5);
//            nodes.add(node);
//            links.add(new FLink(node5_x, node));
//        }



    }
}
