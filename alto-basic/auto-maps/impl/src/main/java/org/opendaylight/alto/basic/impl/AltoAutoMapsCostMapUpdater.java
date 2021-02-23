/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import com.google.common.base.Optional;
import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.CostMapConfig;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.cost.map.config.general.params.Bgp;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.cost.map.config.general.params.bgp.BgpParams;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.cost.map.config.general.params.bgp.bgp.params.AlternativeBgpRib;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.Map;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.Meta;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.map.DstCostsBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.map.dst.costs.cost.TypeNumericalBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMap;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.dependent.vtags.DependentVtagsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.LinkstateAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.LinkstateSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.OspfRouteType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.ProtocolId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.bgp.rib.rib.loc.rib.tables.routes.LinkstateRoutesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.LinkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.NodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.PrefixCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.node._case.NodeDescriptors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.prefix._case.PrefixDescriptors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.path.attribute.link.state.attribute.PrefixAttributesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.routes.LinkstateRoutes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.routes.linkstate.routes.LinkstateRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.routes.linkstate.routes.linkstate.route.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.CRouterIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.c.router.identifier.IsisNodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.c.router.identifier.IsisPseudonodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.c.router.identifier.OspfNodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.c.router.identifier.OspfPseudonodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.BgpRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.RibId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.bgp.rib.Rib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.bgp.rib.RibKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.bgp.rib.rib.LocRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.rib.TablesKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class AltoAutoMapsCostMapUpdater implements DataTreeChangeListener<Tables>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(AltoAutoMapsCostMapUpdater.class);

    private final DataBroker dataBroker;
    private ListenerRegistration<?> registration;
    private String contextId;
    private CostMapConfig costMapConfig;
    private InstanceIdentifier<Tables> tableIID;
    private AltoTopology topology;

    public AltoAutoMapsCostMapUpdater(String contextId, CostMapConfig costMapConfig, final DataBroker dataBroker) {
        this.contextId = contextId;
        this.costMapConfig = costMapConfig;
        this.dataBroker = dataBroker;
        this.tableIID = getConfiguredLinkStateTable(costMapConfig);
        registerBGPListener();
    }

    private void registerBGPListener() {
        if (tableIID != null) {
            registration = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                    LogicalDatastoreType.OPERATIONAL, tableIID), this);
            LOG.info("Listening on BGP Link State Routing Table:", tableIID);
        } else {
            LOG.info("No routing table to listen");
        }
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Tables>> changes) {
        updateCostMap();
    }

    private InstanceIdentifier<Tables> getConfiguredLinkStateTable(CostMapConfig costMapConfig) {
        if (costMapConfig.getGeneralParams() instanceof Bgp) {
            BgpParams params = ((Bgp) costMapConfig.getGeneralParams()).getBgpParams();
            List<AlternativeBgpRib> bgpRibs = params.getAlternativeBgpRib();
            if (bgpRibs == null || bgpRibs.size() < 1) {
                // TODO: read default BGP RIB from config of the dependent network map
                LOG.error("MUST have at least one bgp rib configured");
            } else {
                AlternativeBgpRib defaultBgpRib = bgpRibs.get(0);
                RibId ribId = defaultBgpRib.getRibId();
                return InstanceIdentifier.builder(BgpRib.class)
                        .child(Rib.class, new RibKey(ribId))
                        .child(LocRib.class)
                        .child(Tables.class,
                                new TablesKey(LinkstateAddressFamily.class, LinkstateSubsequentAddressFamily.class))
                        .build();
            }
        } else {
            LOG.error("Unsupported configuration for bgp-ls");
        }
        return null;
    }

    private void updateCostMap() {
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();
        LOG.info("Putting auto generated cost-map into manual map config...");
        Resource dependentNetworkMap = ResourcepoolUtils.readResource(contextId,
                costMapConfig.getDependentNetworkMap(), rwx);
        if (dependentNetworkMap == null) {
            LOG.error("Cannot get meta info of the dependent network map");
            return;
        }
        ResourceNetworkMap networkMap = ManualMapsUtils
                .readResourceNetworkMap(dependentNetworkMap.getResourceId(), rwx);
        Meta meta = new MetaBuilder()
                .setDependentVtags(Arrays.asList(new DependentVtagsBuilder().setResourceId(networkMap.getResourceId())
                        .setTag(dependentNetworkMap.getDefaultTag()).build()))
                .build();
        LOG.debug("Computing cost map by hop count...");
        List<Map> costMap = computeCostMapByHopcount(networkMap);
        LOG.debug("Writing computed cost map: " + costMap.toString());
        ManualMapsUtils.createResourceCostMap(contextId, costMapConfig.getResourceId().getValue(), meta, costMap, rwx);
        LOG.debug(String.format("New cost map [%s, %s] written", contextId, costMapConfig.getResourceId().getValue()));
        rwx.submit();
    }

    private List<Map> computeCostMapByHopcount(ResourceNetworkMap networkMap) {
        LOG.debug("Loading PIDs from dependent network map...");
        java.util.Map<String, List<String>> pids = loadPids(networkMap);
        LOG.debug("Loading topology graph from network information...");
        loadTopology();
        LOG.debug("Building distance (hop count) cache from topology graph...");
        topology.buildDistance();
        LOG.debug("Computing hop count for each pair of PIDs");
        List<Map> costMap = new LinkedList<>();
        for (String srcPid : pids.keySet()) {
            List<DstCosts> dstCosts = new LinkedList<>();
            List<String> srcPrefixes = pids.getOrDefault(srcPid, new LinkedList<>());
            for (String dstPid : pids.keySet()) {
                if (dstPid.equals(srcPid)) {
                    continue;
                }
                List<String> dstPrefixes = pids.getOrDefault(dstPid, new LinkedList<>());
                LOG.debug("Computing cost (hop count) from {} to {}", srcPrefixes, dstPrefixes);
                dstCosts.add(new DstCostsBuilder()
                        .setDst(new PidName(dstPid))
                        .setCost(new TypeNumericalBuilder()
                                .setNumericalCostValue(
                                        BigDecimal.valueOf(topology.getDistance(srcPrefixes, dstPrefixes)))
                                .build())
                        .build());
            }
            costMap.add(new MapBuilder()
                    .setSrc(new PidName(srcPid))
                    .setDstCosts(dstCosts)
                    .build());
        }
        return costMap;
    }

    private java.util.Map<String, List<String>> loadPids(ResourceNetworkMap networkMap) {
        java.util.Map<String, List<String>> pids = new LinkedHashMap<>();
        for (org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.Map pid : networkMap.getMap()) {
            String pidName = pid.getPid().getValue();
            List<String> prefixes = new LinkedList<>();
            for (EndpointAddressGroup addressGroup : pid.getEndpointAddressGroup()) {
                if (addressGroup.getAddressType().getEnumeration().getName().equals("ipv4")) {
                    for (IpPrefix prefix : addressGroup.getEndpointPrefix()) {
                        prefixes.add(prefix.getIpv4Prefix().getValue());
                    }
                } else if (addressGroup.getAddressType().getEnumeration().getName().equals("ipv6")) {
                    for (IpPrefix prefix : addressGroup.getEndpointPrefix()) {
                        prefixes.add(prefix.getIpv6Prefix().getValue());
                    }
                }
            }
            pids.put(pidName, prefixes);
        }
        return pids;
    }

    private void loadTopology() {
        final ReadTransaction rx = dataBroker.newReadOnlyTransaction();
        try {
            Optional<Tables> optional = rx.read(LogicalDatastoreType.OPERATIONAL, tableIID).get();
            if (optional.isPresent()) {
                Tables table = optional.get();
                if (table.getRoutes() instanceof LinkstateRoutesCase) {
                    loadTopologyFromLinkstate(((LinkstateRoutesCase) table.getRoutes()).getLinkstateRoutes());
                } else {
                    LOG.error("Unsupported route type");
                }
            } else {
                LOG.error("BGP local rib not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getNodeId(CRouterIdentifier cRouterIdentifier) {
        Long nodeId = null;
        if (cRouterIdentifier instanceof OspfNodeCase) {
            nodeId = ((OspfNodeCase) cRouterIdentifier).getOspfNode().getOspfRouterId();
        } else if (cRouterIdentifier instanceof OspfPseudonodeCase) {
            nodeId = ((OspfPseudonodeCase) cRouterIdentifier).getOspfPseudonode().getOspfRouterId();
        } else if (cRouterIdentifier instanceof IsisNodeCase) {
            nodeId = new BigInteger(1, ((IsisNodeCase) cRouterIdentifier)
                    .getIsisNode().getIsoSystemId().getValue()).longValue();
        } else if (cRouterIdentifier instanceof IsisPseudonodeCase) {
            nodeId = new BigInteger(1, ((IsisPseudonodeCase) cRouterIdentifier)
                    .getIsisPseudonode().getIsIsRouterIdentifier().getIsoSystemId().getValue()).longValue();
        } else {
            LOG.warn("Protocol not supported yet");
        }
        return nodeId;
    }

    private void loadTopologyFromLinkstate(LinkstateRoutes routes) {
        topology = new AltoTopology();
        for (LinkstateRoute route : routes.getLinkstateRoute()) {
            if (route.getProtocolId() == ProtocolId.Ospf ||
                    route.getProtocolId() == ProtocolId.IsisLevel1 ||
                    route.getProtocolId() == ProtocolId.IsisLevel2) {
                if (route.getObjectType() instanceof NodeCase) {
                    // add new vertex to graph
                    NodeDescriptors nodeDesc = ((NodeCase) route.getObjectType()).getNodeDescriptors();
                    Long nodeId = getNodeId(nodeDesc.getCRouterIdentifier());
                    Long areaId = nodeDesc.getAreaId().getValue();
                    if (nodeId != null && !topology.containsNode(areaId, nodeId)) {
                        LOG.debug("Add new node [areaId={}, nodeId={}] to topology", areaId, nodeId);
                        topology.addNode(areaId, nodeId);
                    }
                } else if (route.getObjectType() instanceof PrefixCase) {
                    // map a prefix to a vertex
                    PrefixDescriptors prefixDesc = ((PrefixCase) route.getObjectType()).getPrefixDescriptors();
                    IpPrefix prefix = prefixDesc.getIpReachabilityInformation();
                    String prefixStr = prefix.getIpv4Prefix() != null ?
                            prefix.getIpv4Prefix().getValue() : prefix.getIpv6Prefix().getValue();
                    Long originId = getNodeId(((PrefixCase) route.getObjectType()).getAdvertisingNodeDescriptors().getCRouterIdentifier());
                    Long areaId = ((PrefixCase) route.getObjectType()).getAdvertisingNodeDescriptors().getAreaId().getValue();
                    Long metric = ((PrefixAttributesCase) route.getAttributes().getAugmentation(Attributes1.class)
                            .getLinkStateAttribute()).getPrefixAttributes().getPrefixMetric().getValue();
                    if (route.getProtocolId() == ProtocolId.IsisLevel1 ||
                            (prefixDesc.getOspfRouteType() != null &&
                                    prefixDesc.getOspfRouteType().equals(OspfRouteType.IntraArea))) {
                        LOG.debug("Add new intra first hop for [prefix={}, areaId={}, nodeId={}] to topology", prefixStr, areaId, originId);
                        topology.addIntraPrefix(prefixStr, areaId, originId);
                    } else if (route.getProtocolId() == ProtocolId.IsisLevel2 ||
                            (prefixDesc.getOspfRouteType() != null &&
                                    prefixDesc.getOspfRouteType().equals(OspfRouteType.InterArea))) {
                        LOG.debug("Add new inter first hop [prefix={}, areaId={}, nodeId={}, metric={}] to topology", prefixStr, areaId, originId, metric);
                        topology.addInterPrefix(prefixStr, areaId, originId, metric);
                    } else {
                        LOG.debug("Linkstate route type not supported yet");
                    }
                } else if (route.getObjectType() instanceof LinkCase) {
                    // add new edge to graph
                    String linkId = String.valueOf(route.getRouteKey());
                    LinkCase link = (LinkCase) route.getObjectType();
                    Long sourceId = getNodeId(link.getLocalNodeDescriptors().getCRouterIdentifier());
                    Long sourceAreaId = link.getLocalNodeDescriptors().getAreaId().getValue();
                    Long destId = getNodeId(link.getRemoteNodeDescriptors().getCRouterIdentifier());
                    Long destAreaId = link.getRemoteNodeDescriptors().getAreaId().getValue();
                    if (sourceAreaId.equals(destAreaId)) {
                        LOG.debug("Add new link [areaId={}, sourceId={}, destId={}] to topology", sourceAreaId, sourceId, destId);
                        topology.addLink(linkId, sourceAreaId, sourceId, destId);
                    }
                }
            } else {
                LOG.warn("Protocol not supported yet");
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (registration != null) {
            registration.close();
        }
        LOG.info("BGP LS updater closed");
    }
}
