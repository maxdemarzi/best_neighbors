package com.maxdemarzi;

import org.neo4j.graphdb.Node;

import java.util.List;

public class NodesAndCostResult implements Comparable<NodesAndCostResult> {
    public List<Node> nodes;
    public Double cost;

    public NodesAndCostResult(List<Node> nodes, Double cost) {
        this.nodes = nodes;
        this.cost = cost;
    }

    @Override
    public int compareTo(NodesAndCostResult result) {
        return this.cost.compareTo(result.cost);
    }
}
