package com.maxdemarzi;

import com.maxdemarzi.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class Procedures {


    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;


    @Procedure(name = "com.maxdemarzi.best_neighbors", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.best_neighbors(Node start, Number n, Number hops)")
    public Stream<NodesAndCostResult> bestNeighbors(@Name("start") Node start, @Name("n") Number n, @Name("hops") Number hops) throws IOException {
        HashMap<Node, Pair<ArrayList<Node>, Double>> nodePathMap = new HashMap<>();

        // First Hop
        for(Relationship r : start.getRelationships(RelationshipTypes.to)) {
            Node next = r.getOtherNode(start);
            if(nodePathMap.containsKey(next)){
                Double weight = nodePathMap.get(next).other();
                Double cost = ((Number)r.getProperty("count", 0.0)).doubleValue();
                if (weight > cost) {
                    nodePathMap.put(next, Pair.of(new ArrayList<Node>() {{
                        add(start);
                        add(next);
                    }}, cost));
                }
            } else {
                Double cost = ((Number)r.getProperty("count", 0.0)).doubleValue();
                nodePathMap.put(next, Pair.of(new ArrayList<Node>(){{add(start); add(next); }}, cost));
            }
        }

        for(int i = 1; i < hops.intValue(); i++) {
            nextHop(n, nodePathMap);
        }

        ArrayList<Entry<Node, Pair<ArrayList<Node>, Double>>> list = new ArrayList<>(nodePathMap.entrySet());
        list.sort(Comparator.comparing(o -> o.getValue().other()));

        return list.subList(0, Math.min(list.size(), n.intValue())).stream().map(x -> new NodesAndCostResult(x.getValue().first(), x.getValue().other()));
    }

    private void nextHop(@Name("n") Number n, HashMap<Node, Pair<ArrayList<Node>, Double>> nodePathMap) {
        if(nodePathMap.size() < n.intValue()) {
            ArrayList<Entry<Node, Pair<ArrayList<Node>, Double>>> list = new ArrayList<>(nodePathMap.entrySet());

            for(Entry<Node, Pair<ArrayList<Node>, Double>> entry : list) {
                ArrayList<Node> nodes = entry.getValue().first();
                Double cost = entry.getValue().other();
                Node from = nodes.get(nodes.size() - 1);
                for(Relationship r : from.getRelationships(RelationshipTypes.to)) {
                    Node next = r.getOtherNode(from);
                    Double nextCost = ((Number)r.getProperty("count", 0.0)).doubleValue();
                    if (nodePathMap.containsKey(next)) {
                        Pair<ArrayList<Node>, Double> found = nodePathMap.get(next);
                        if(found.other() > cost + nextCost) {
                            nodePathMap.put(next, Pair.of(new ArrayList<Node>(){{addAll(nodes); add(next);}}, cost + nextCost));
                        }
                    } else {
                        nodePathMap.put(next, Pair.of(new ArrayList<Node>(){{addAll(nodes); add(next);}}, cost + nextCost));
                    }
                }
            }
        }
    }
}
