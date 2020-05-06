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
import org.opendaylight.alto.basic.manual.maps.MatchedIPPrefix;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.NetworkMapConfig;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.algorithm.FirstHopCluster;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.algorithm.first.hop.cluster.FirstHopClusterAlgorithm;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.params.Bgp;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.params.bgp.BgpParams;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.Map;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.AreaIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.DomainIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.LinkstateAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.LinkstateSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.ProtocolId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.bgp.rib.rib.loc.rib.tables.routes.LinkstateRoutesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.LinkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.PrefixCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.link._case.LocalNodeDescriptors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.link._case.RemoteNodeDescriptors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.object.type.prefix._case.AdvertisingNodeDescriptors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.linkstate.routes.linkstate.routes.LinkstateRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.CRouterIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.c.router.identifier.OspfNodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.linkstate.rev171207.node.identifier.c.router.identifier.ospf.node._case.OspfNode;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AltoAutoMapsBgpLsUpdater implements DataTreeChangeListener<Tables>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AltoAutoMapsBgpLsUpdater.class);

    private final DataBroker dataBroker;
    private ListenerRegistration<?> registration;
    private String contextId;
    private NetworkMapConfig networkMapConfig;
    private boolean inspectInternalLink;
    private InstanceIdentifier<Tables> tableIID;

    public AltoAutoMapsBgpLsUpdater(String contextId, NetworkMapConfig networkMapConfig, final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.contextId = contextId;
        this.networkMapConfig = networkMapConfig;
        this.tableIID = getConfiguredLinkStateTable(networkMapConfig);
        inspectInternalLink = ((FirstHopCluster) networkMapConfig.getAlgorithm()).getFirstHopClusterAlgorithm()
                .isInspectInternalLink();
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

    private InstanceIdentifier<Tables> getConfiguredLinkStateTable(NetworkMapConfig networkMapConfig) {
        if (networkMapConfig.getParams() instanceof Bgp) {
            BgpParams params = ((Bgp) networkMapConfig.getParams()).getBgpParams();
            RibId ribId = params.getBgpRib().get(0).getRibId();
            return InstanceIdentifier.builder(BgpRib.class)
                    .child(Rib.class, new RibKey(ribId))
                    .child(LocRib.class)
                    .child(Tables.class,
                            new TablesKey(LinkstateAddressFamily.class, LinkstateSubsequentAddressFamily.class))
                    .build();
        } else {
            LOG.error("Unsupported algorithm for bgp topology");
        }
        return null;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Tables>> changes) {
        updateNetworkMap();
    }

    private void updateNetworkMap() {
        final ReadWriteTransaction wrx = dataBroker.newReadWriteTransaction();
        List<Map> networkMap = computeNetworkMapByBgpLs();
        LOG.info("Putting auto generated network-map to manual map config...");
        ManualMapsUtils.createResourceNetworkMap(contextId, networkMapConfig.getResourceId().getValue(),
                networkMap, wrx);
        wrx.submit();
    }

    private List<Map> computeNetworkMapByBgpLs() {
        final ReadTransaction rx = dataBroker.newReadOnlyTransaction();
        List<Map> networkMap = new LinkedList<>();
        try {
            Optional<Tables> optional = rx.read(LogicalDatastoreType.OPERATIONAL, tableIID).get();
            if (optional.isPresent()) {
                Tables table = optional.get();
                if (table.getRoutes() instanceof LinkstateRoutesCase) {
                    java.util.Map<String, List<IpPrefix>> pids = getPIDClusters((LinkstateRoutesCase) table.getRoutes());
                    for (java.util.Map.Entry<String, List<IpPrefix>> entry : pids.entrySet()) {
                        String pidName = entry.getKey();
                        List<IpPrefix> prefixList = entry.getValue();
                        if ((prefixList == null) || prefixList.isEmpty()) {
                            continue;
                        }
                        networkMap.add(new MapBuilder()
                                .setPid(new PidName(pidName))
                                .setEndpointAddressGroup(Arrays.asList(new EndpointAddressGroup[]{
                                        new EndpointAddressGroupBuilder()
                                                .setAddressType(new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4))
                                                .setEndpointPrefix(prefixList)
                                                .build()
                                }))
                                .build());
                    }
                } else {
                    LOG.error("Unsupported route type");
                }
            } else {
                LOG.error("BGP Local RIB not found");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return networkMap;
    }

    private java.util.Map<String, List<IpPrefix>> getPIDClusters(LinkstateRoutesCase routesCase) {
        java.util.Map<String, List<IpPrefix>> pids = new LinkedHashMap<>();
        for (LinkstateRoute route : routesCase.getLinkstateRoutes().getLinkstateRoute()) {
            if (route.getProtocolId() == ProtocolId.Ospf) {
                if (route.getObjectType() instanceof PrefixCase) {
                    PrefixCase prefixCase = (PrefixCase) route.getObjectType();
                    IpPrefix prefix = prefixCase.getPrefixDescriptors().getIpReachabilityInformation();
                    AdvertisingNodeDescriptors nodeDesc = prefixCase.getAdvertisingNodeDescriptors();
                    String pidName = generatePidFromNodeDesc(nodeDesc.getAsNumber(), nodeDesc.getDomainId(),
                            nodeDesc.getAreaId(), nodeDesc.getCRouterIdentifier());
                    if (pidName != null && prefix != null) {
                        if (!pids.containsKey(pidName)) {
                            pids.put(pidName, new LinkedList<>());
                        }
                        boolean included = false;
                        for (IpPrefix p : pids.get(pidName)) {
                            if (matchPrefix(p, prefix)) {
                                included = true;
                                break;
                            }
                        }
                        if (!included) {
                            pids.get(pidName).add(prefix);
                        }
                    }
                }
            } else {
                LOG.debug("Protocol not supported yet");
            }
        }
        for (LinkstateRoute route : routesCase.getLinkstateRoutes().getLinkstateRoute()) {
            if (route.getProtocolId() == ProtocolId.Ospf) {
                if (route.getObjectType() instanceof LinkCase) {
                    LinkCase linkCase = (LinkCase) route.getObjectType();
                    Ipv4Address src = linkCase.getLinkDescriptors().getIpv4InterfaceAddress();
                    Ipv4Address dst = linkCase.getLinkDescriptors().getIpv4NeighborAddress();
                    LocalNodeDescriptors srcNodeDesc = linkCase.getLocalNodeDescriptors();
                    RemoteNodeDescriptors dstNodeDesc = linkCase.getRemoteNodeDescriptors();
                    String srcPidName = generatePidFromNodeDesc(srcNodeDesc.getAsNumber(), srcNodeDesc.getDomainId(),
                            srcNodeDesc.getAreaId(), srcNodeDesc.getCRouterIdentifier());
                    String dstPidName = generatePidFromNodeDesc(dstNodeDesc.getAsNumber(), dstNodeDesc.getDomainId(),
                            dstNodeDesc.getAreaId(), dstNodeDesc.getCRouterIdentifier());
                    if (srcPidName != null && src != null) {
                        if (!pids.containsKey(srcPidName)) {
                            pids.put(srcPidName, new LinkedList<>());
                        }
                        IpPrefix srcPrefix = new IpPrefix(new Ipv4Prefix(src.getValue() + "/32"));
                        List<IpPrefix> removes = new LinkedList<>();
                        for (IpPrefix p : pids.get(srcPidName)) {
                            if (p.equals(srcPrefix)) {
                                continue;
                            }
                            if (matchPrefix(srcPrefix, p)) {
                                removes.add(p);
                            }
                        }
                        LOG.debug("Removing internal subnet " + removes.toString() + " for " + srcPrefix.toString());
                        pids.get(srcPidName).removeAll(removes);
                        LOG.debug("After removed: " + pids.get(srcPidName).toString());
                        if (inspectInternalLink) {
                            pids.get(srcPidName).add(srcPrefix);
                        }
                    }
                    if (dstPidName != null && dst != null) {
                        if (!pids.containsKey(dstPidName)) {
                            pids.put(dstPidName, new LinkedList<>());
                        }
                        IpPrefix dstPrefix = new IpPrefix((new Ipv4Prefix(dst.getValue() + "/32")));
                        List<IpPrefix> removes = new LinkedList<>();
                        for (IpPrefix p : pids.get(dstPidName)) {
                            if (p.equals(dstPrefix)) {
                                continue;
                            }
                            if (matchPrefix(dstPrefix, p)) {
                                removes.add(p);
                            }
                        }
                        LOG.debug("Removing internal subnet " + removes.toString() + " for " + dstPrefix.toString());
                        pids.get(dstPidName).removeAll(removes);
                        LOG.debug("After removed: " + pids.get(dstPidName).toString());
                        if (inspectInternalLink) {
                            pids.get(dstPidName).add(dstPrefix);
                        }
                    }
                }
            } else {
                LOG.debug("Protocol not supported yet");
            }
        }
        return pids;
    }

    /* Return if p in prefix */
    private boolean matchPrefix(IpPrefix p, IpPrefix prefix) {
        String _pStr = p.getIpv4Prefix() != null ?
                p.getIpv4Prefix().getValue() : p.getIpv6Prefix().getValue();
        String _prefrixStr = prefix.getIpv4Prefix() != null ?
                prefix.getIpv4Prefix().getValue() : prefix.getIpv6Prefix().getValue();
        return new MatchedIPPrefix(_prefrixStr).match(new MatchedIPPrefix(_pStr));
    }

    private String generatePidFromNodeDesc(AsNumber asNumber, DomainIdentifier domainId, AreaIdentifier areaId,
                                           CRouterIdentifier cRouterId) {
        final String separator = ":";
        String pidName = "PID";
        pidName += (asNumber == null) ? "0" : asNumber.getValue().toString();
        pidName += separator + ((domainId == null) ? "0" : domainId.getValue().toString());
        pidName += separator + ((areaId == null) ? "0" : areaId.getValue().toString());
        if (cRouterId instanceof OspfNodeCase) {
            OspfNode ospfNode = ((OspfNodeCase) cRouterId).getOspfNode();
            pidName += separator + Integer.toHexString(ospfNode.getOspfRouterId().intValue());
        } else {
            LOG.debug("Node not support yet");
            return null;
        }
        return pidName;
    }

    @Override
    public void close() throws Exception {
        if (registration != null) {
            registration.close();
        }
        LOG.info("BGP IPv4 updater closed");
    }
}
