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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AltoTopology {
    private static final Logger LOG = LoggerFactory.getLogger(AltoTopology.class);
    private Map<Long, DirectedGraph<Long, String>> multigraph;
    private DijkstraShortestPath<Long, String> shortestPath;
    private Map<String, FirstHops> multiFirstHops;
    private Map<Long, DijkstraDistance<Long, String>> multidistance;
    private Map<Long, Set<Long>> areaBorderRouters;
    private static final int MAX_HOP_COUNT = 64;

    public AltoTopology() {
        multigraph = new LinkedHashMap<>();
        multiFirstHops = new LinkedHashMap<>();
        areaBorderRouters = new LinkedHashMap<>();
    }

    public void buildShortestPath() throws Exception {
        throw new Exception("Not implemented yet");
    }

    public void buildDistance() {
        LOG.debug("Building distance computer via Dijkstra:");
        // LOG.debug("Graph: {}", graph);
        // distance = new DijkstraDistance<>(graph, true);
        multidistance = new LinkedHashMap<>();
        for (Long areaId : multigraph.keySet()) {
            multidistance.put(areaId, new DijkstraDistance<>(multigraph.get(areaId), true));
        }
    }

    public boolean addNode(Long areaId, Long nodeId) {
        if (!multigraph.containsKey(areaId)) {
            multigraph.put(areaId, new DirectedSparseGraph<>());
        }
        return multigraph.get(areaId).addVertex(nodeId);
    }

    public boolean addLink(String linkId, Long areaId, Long sourceId, Long destId) {
        if (!multigraph.containsKey(areaId)) {
            multigraph.put(areaId, new DirectedSparseGraph<>());
        }
        return multigraph.get(areaId).addEdge(linkId, sourceId, destId);
    }

    private boolean checkAndAddNode(Long areaId, Long originId) {
        boolean success = true;
        if (!multigraph.containsKey(areaId)) {
            multigraph.put(areaId, new DirectedSparseGraph<>());
        }
        if (!multigraph.get(areaId).containsVertex(originId)) {
            success = multigraph.get(areaId).addVertex(originId);
        }
        return success;
    }

    public boolean addIntraPrefix(String prefix, Long areaId, Long originId) {
        boolean success = checkAndAddNode(areaId, originId);
        if (success) {
            if (!multiFirstHops.containsKey(prefix)) {
                multiFirstHops.put(prefix, new FirstHops());
            }
            multiFirstHops.get(prefix).intraAreaId = areaId;
            multiFirstHops.get(prefix).intraOriginId = originId;
        }
        return success;
    }

    public boolean addInterPrefix(String prefix, Long areaId, Long originId, Long metric) {
        boolean success = checkAndAddNode(areaId, originId);
        if (success) {
            if (!multiFirstHops.containsKey(prefix)) {
                multiFirstHops.put(prefix, new FirstHops());
            }
            if (!multiFirstHops.get(prefix).interOriginIds.containsKey(areaId)) {
                multiFirstHops.get(prefix).interOriginIds.put(areaId, new LinkedHashMap<>());
            }
            multiFirstHops.get(prefix).interOriginIds.get(areaId).put(originId, metric);

            if (!areaBorderRouters.containsKey(originId)) {
                areaBorderRouters.put(originId, new HashSet<>());
            }
            areaBorderRouters.get(originId).add(areaId);
        }
        return success;
    }

    public boolean containsNode(Long areaId, Long nodeId) {
        return multigraph.containsKey(areaId) && multigraph.get(areaId).containsVertex(nodeId);
    }

    public int getDistance(Long areaId, Long sourceId, Long destId) {
        int d = 0;
        try {
            LOG.debug("Computing distance from {} to {} ...", sourceId, destId);
            d = multidistance.get(areaId).getDistance(sourceId, destId).intValue();
            LOG.debug("Distance from {} to {} is {}", sourceId, destId, d);
        } catch (Exception e) {
            // e.printStackTrace();
            LOG.debug("Fail to compute distance");
        }
        return d;
    }

    public int getDistance(String sourcePrefix, String destPrefix) {
        FirstHops sourceOrigins = multiFirstHops.get(sourcePrefix);
        if (sourceOrigins == null || sourceOrigins.intraOriginId == null || sourceOrigins.intraAreaId == null) {
            return MAX_HOP_COUNT;
        }
        FirstHops destOrigins = multiFirstHops.get(destPrefix);
        if (destOrigins == null || destOrigins.intraOriginId == null || destOrigins.intraAreaId == null) {
            return MAX_HOP_COUNT;
        }
        Long currentAreaId = sourceOrigins.intraAreaId;
        Long currentSourceId = sourceOrigins.intraOriginId;
        int totalCost = 0;
        int _cnt = 0;
        while (currentAreaId != destOrigins.intraAreaId) {
            if (_cnt > MAX_HOP_COUNT) {
                return MAX_HOP_COUNT;
            }
            Map<Long, Long> destInterOrigins = destOrigins.interOriginIds.get(currentAreaId);
            if (destInterOrigins == null) {
                return MAX_HOP_COUNT;
            }
            int cost = MAX_HOP_COUNT;
            Long currentAbrId = null;
            for (Long abrId : destInterOrigins.keySet()) {
                int currentCost = getDistance(currentAreaId, sourceOrigins.intraOriginId, abrId) + destInterOrigins.get(abrId).intValue();
                if (currentCost < cost) {
                    cost = currentCost;
                    currentAbrId = abrId;
                }
            }
            if (currentAbrId == null) {
                return cost;
            }
            Set<Long> interAreas = areaBorderRouters.get(currentAbrId);
            if (interAreas == null) {
                return cost;
            }
            Long nextAreaId = null;
            for (Long _nextAreaId : interAreas) {
                if (_nextAreaId != currentAreaId) {
                    nextAreaId = _nextAreaId;
                    break;
                }
            }
            if (nextAreaId == null) {
                return cost;
            }
            currentAreaId = nextAreaId;
            currentSourceId = currentAbrId;
            totalCost = totalCost + cost;
            _cnt++;
        }
        return totalCost + getDistance(currentAreaId, currentSourceId, destOrigins.intraOriginId) + 1;
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

    public List<String> getPathVector(Long sourceId, Long destId) throws Exception {
        throw new Exception("Not implemented yet");
        // return shortestPath.getPath(sourceId, destId);
    }

    private class FirstHops {
        public Long intraAreaId;
        public Long intraOriginId;
        public Map<Long, Map<Long, Long>> interOriginIds;

        public FirstHops() {
            interOriginIds = new LinkedHashMap<>();
        }
    }
}
