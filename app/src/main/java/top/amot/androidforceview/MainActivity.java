package top.amot.androidforceview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import top.amot.library.view.force.ForceView;
import top.amot.library.view.force.Link;
import top.amot.library.view.force.Node;
import top.amot.library.view.force.OnNodeClickListener;
import top.amot.library.view.force.Simulation;

public class MainActivity extends AppCompatActivity {

    private ForceView forceView;
    private Toast toast;
    private List<Node> nodes;
    private List<Link> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        forceView = (ForceView) findViewById(R.id.force_view);

        initNodes();
        initLinks();

        Simulation simulation = new Simulation.Builder()
                .nodes(nodes)
                .links(links)
                .build();

        forceView.setSimulation(simulation);
        forceView.setOnNodeClickListener(new OnNodeClickListener() {
            @Override
            public void onNodeClick(Node node) {
                show("" + node.getText());
            }
        });

    }

    private void initNodes() {
        nodes = new ArrayList<>();

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
    }

    private void initLinks() {
        links = new ArrayList<>();

        links.add(new Link(nodes.get(0), nodes.get(1), "A-B"));
        links.add(new Link(nodes.get(0), nodes.get(2), "A-C"));
        links.add(new Link(nodes.get(0), nodes.get(3), "A-D"));
        links.add(new Link(nodes.get(0), nodes.get(4), "A-E"));
        links.add(new Link(nodes.get(1), nodes.get(5), "B-F"));
        links.add(new Link(nodes.get(2), nodes.get(6), "C-G"));
        links.add(new Link(nodes.get(3), nodes.get(7), "D-H"));
        links.add(new Link(nodes.get(4), nodes.get(8), "E-I"));
        links.add(new Link(nodes.get(4), nodes.get(9), "E-J"));
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
