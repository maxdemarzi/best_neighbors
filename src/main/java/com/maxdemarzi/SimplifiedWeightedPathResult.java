package com.maxdemarzi;

import org.neo4j.graphdb.Node;

import java.util.List;

public class SimplifiedWeightedPathResult implements Comparable<SimplifiedWeightedPathResult> {
    public List<Node> nodes;
    public Double cost;

    public SimplifiedWeightedPathResult(List<Node> nodes, Double cost) {
        this.nodes = nodes;
        this.cost = cost;
    }

    @Override
    public int compareTo(SimplifiedWeightedPathResult result) {
        return this.cost.compareTo(result.cost);
    }
}
