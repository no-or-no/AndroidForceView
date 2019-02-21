package top.amot.androidforceview;

import androidx.appcompat.app.AppCompatActivity;
import top.amot.forceview.ForceView;
import top.amot.forceview.Link;
import top.amot.forceview.Node;
import top.amot.forceview.Simulation;
import top.amot.forceview.force.ForceCenter;
import top.amot.forceview.force.ForceCollide;
import top.amot.forceview.force.ForceLink;
import top.amot.forceview.force.ForceManyBody;
import top.amot.forceview.force.ForceRadial;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private List<Node> nodes = new ArrayList<>();
    private List<Link> links = new ArrayList<>();

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initNodesAndLinks();

        Simulation simulation = new Simulation.Builder()
                .links(links)
                .nodes(nodes)
                .force(ForceLink.NAME, new ForceLink().distance(link -> 350).strength(link -> 2))
                .force(ForceCenter.NAME, new ForceCenter())
                .force(ForceManyBody.NAME, new ForceManyBody().strength(node -> (node.level + 1) * -3000))
                .force(ForceRadial.NAME, new ForceRadial().strength(node -> 1))
                .force(ForceCollide.NAME, new ForceCollide().strength(10).radius(node -> node.level * 10))
                .build();

        ForceView forceView = new ForceView(this);
        forceView.setSimulation(simulation);
        forceView.setOnNodeClickListener(node -> {
            show(node.text);
        });

        setContentView(forceView);
    }

    private void initNodesAndLinks() {
        nodes.add(new Node("A", 0)); // 0
        nodes.add(new Node("B", 1)); // 1
        nodes.add(new Node("C", 1)); // 2
        nodes.add(new Node("D", 1)); // 3
        nodes.add(new Node("E", 1)); // 4
        nodes.add(new Node("F", 2)); // 5
        nodes.add(new Node("G", 2)); // 6
        nodes.add(new Node("H", 2)); // 7
        nodes.add(new Node("I", 2)); // 8
        nodes.add(new Node("J", 2)); // 9

        links.add(new Link(nodes.get(0), nodes.get(1), "A-B"));
        links.add(new Link(nodes.get(0), nodes.get(2), "A-C"));
        links.add(new Link(nodes.get(0), nodes.get(3), "A-D"));
        links.add(new Link(nodes.get(0), nodes.get(4), "A-E"));
        links.add(new Link(nodes.get(1), nodes.get(5), "B-F"));
        links.add(new Link(nodes.get(2), nodes.get(6), "C-G"));
        links.add(new Link(nodes.get(3), nodes.get(7), "D-H"));
        links.add(new Link(nodes.get(4), nodes.get(8), "E-I"));
        links.add(new Link(nodes.get(4), nodes.get(9), "E-J"));

        Random r = new Random();
        for (int i = 5; i < 10; i++) {
            Node node = nodes.get(i);
            int n = r.nextInt(10) + 10;
            for (int j = 0; j < n; j++) {
                Node child = new Node(node.text + (j + 1), 3);
                nodes.add(child);
                links.add(new Link(node, child, node.text + "-" + child.text));
            }
        }

        int last = nodes.size() - 1;

        for (int i = last; i > last - 10; i--) {
            Node node = nodes.get(r.nextInt(nodes.size()));
            int n = r.nextInt(10);
            for (int j = 0; j < n; j++) {
                Node child = new Node(node.text + (j + 1), node.level + 1);
                nodes.add(child);
                links.add(new Link(node, child, node.text + "-" + child.text));
            }
        }

        Log.e("MainActivity", "nodes count: " + nodes.size() + ", links count:" + links.size());
    }

    private void show(String text) {
        if (toast == null) {
            toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }
}
