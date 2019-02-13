package top.amot.androidforceview;

import androidx.appcompat.app.AppCompatActivity;
import top.amot.forceview.ForceView;
import top.amot.forceview.Link;
import top.amot.forceview.Node;
import top.amot.forceview.Simulation;
import top.amot.forceview.force.ForceCenter;
import top.amot.forceview.force.ForceLink;
import top.amot.forceview.force.ForceManyBody;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Link> links = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();

        Node node = new Node();
        node.index = 0;

        Simulation simulation = new Simulation.Builder()
                .links(links)
                .nodes(nodes)
                .force(ForceManyBody.NAME, new ForceManyBody())
                .force(ForceLink.NAME, new ForceLink())
                .force(ForceCenter.NAME, new ForceCenter())
                .build();

        ForceView forceView = new ForceView(this);
        forceView.setSimulation(simulation);

        setContentView(forceView);
    }
}
