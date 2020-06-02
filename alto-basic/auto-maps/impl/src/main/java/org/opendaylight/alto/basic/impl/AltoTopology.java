/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AltoTopology {
    private static final Logger LOG = LoggerFactory.getLogger(AltoTopology.class);
    private DirectedGraph<Long, String> graph;
    private DijkstraDistance<Long, String> distance;
    private DijkstraShortestPath<Long, String> shortestPath;
    private Map<String, Long> firstHop;

    public AltoTopology() {
        graph = new DirectedSparseGraph<>();
        firstHop = new LinkedHashMap<>();
    }

    public void buildShortestPath() {
        shortestPath = new DijkstraShortestPath<>(graph, true);
    }

    public void buildDistance() {
        LOG.debug("Building distance computer via Dijkstra:");
        LOG.debug("Graph: {}", graph);
        distance = new DijkstraDistance<>(graph, true);
    }

    public boolean addNode(Long nodeId) {
        return graph.addVertex(nodeId);
    }

    public boolean addLink(String linkId, Long sourceId, Long destId) {
        return graph.addEdge(linkId, sourceId, destId);
    }

    public boolean addPrefix(String prefix, Long originId) {
        boolean success = true;
        if (!graph.containsVertex(originId)) {
            success = graph.addVertex(originId);
        }
        firstHop.put(prefix, originId);
        return success;
    }

    public boolean containsNode(Long nodeId) {
        return graph.containsVertex(nodeId);
    }

    public int getDistance(Long sourceId, Long destId) {
        int d = 0;
        try {
            LOG.debug("Computing distance from {} to {} ...", sourceId, destId);
            d = distance.getDistance(sourceId, destId).intValue();
            LOG.debug("Distance from {} to {} is {}", sourceId, destId, d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }

    public int getDistance(String sourcePrefix, String destPrefix) {
        Long sourceId = firstHop.get(sourcePrefix);
        if (sourceId == null) {
            return 0;
        }
        Long destId = firstHop.get(destPrefix);
        if (destId == null) {
            return 0;
        }
        return getDistance(sourceId, destId);
    }

    public int getDistance(List<String> sourcePrefixes, List<String> destPrefixes) {
        int numOfFlows = 0;
        int cost = 0;
        for (String sourcePrefix : sourcePrefixes) {
            for (String destPrefix : destPrefixes) {
                cost = cost + getDistance(sourcePrefix, destPrefix);
                numOfFlows++;
            }
        }
        if (numOfFlows > 0) {
            return cost / numOfFlows;
        }
        return 0;
    }

    public List<String> getPathVector(Long sourceId, Long destId) {
        return shortestPath.getPath(sourceId, destId);
    }

}
