package top.amot.androidforceview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

import top.amot.library.view.force.FLink;
import top.amot.library.view.force.FNode;
import top.amot.library.view.force.ForceView;

public class MainActivity extends AppCompatActivity {

    ForceView fview;
    private ArrayList<FNode> nodes;
    private ArrayList<FLink> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fview = (ForceView) findViewById(R.id.fview);

        initData();
        fview.setData(nodes, links);
    }

    private void initData() {
        nodes = new ArrayList<>();
        links = new ArrayList<>();

        nodes.add(new FNode("0", 80f, 1));
        nodes.add(new FNode("1", 60f, 2));
        nodes.add(new FNode("2", 60f, 2));
        nodes.add(new FNode("3", 60f, 2));
        nodes.add(new FNode("4", 60f, 2));
        nodes.add(new FNode("5", 60f, 2));
        nodes.add(new FNode("6", 60f, 2));
        nodes.add(new FNode("7", 60f, 2));
        nodes.add(new FNode("8", 60f, 2));
        nodes.add(new FNode("9", 60f, 2));
        nodes.add(new FNode("10", 60f, 2));

        for (int i = 1; i <= 10; i++) {
            links.add(new FLink(nodes.get(0), nodes.get(i)));
        }

        for (int i = 1; i <= 10; i++) {
            nodes.add(new FNode("" + i + "-1", 40f, 3));
            nodes.add(new FNode("" + i + "-2", 40f, 3));
            nodes.add(new FNode("" + i + "-3", 40f, 3));
            nodes.add(new FNode("" + i + "-4", 40f, 3));
            nodes.add(new FNode("" + i + "-5", 40f, 3));
        }

        for (int i = 1, j = 11; i <= 10; i++) {
            for (int n = 1; n <= 5; n++) {
                links.add(new FLink(nodes.get(i), nodes.get(j++)));
            }
        }

        FNode node0 = nodes.get(0);
        FNode node1 = nodes.get(1);
        FNode node2 = nodes.get(2);
        FNode node3 = nodes.get(3);
        FNode node4 = nodes.get(4);
        for (int i = 1; i <= 10; i++) {
            FNode node = new FNode("0-" + i, 60f, 2);
            nodes.add(node);
            links.add(new FLink(node0, node));
        }

        for (int i = 1; i <= 7; i++) {
            FNode node = new FNode("1-" + i, 40f, 3);
            nodes.add(node);
            links.add(new FLink(node1, node));
        }
        for (int i = 1; i <= 3; i++) {
            FNode node = new FNode("2-" + i, 40f, 3);
            nodes.add(node);
            links.add(new FLink(node2, node));
        }
        for (int i = 1; i <= 15; i++) {
            FNode node = new FNode("3-" + i, 40f, 3);
            nodes.add(node);
            links.add(new FLink(node3, node));
        }
        for (int i = 1; i <= 12; i++) {
            FNode node = new FNode("4-" + i, 40f, 3);
            nodes.add(node);
            links.add(new FLink(node4, node));
        }

    }
}
