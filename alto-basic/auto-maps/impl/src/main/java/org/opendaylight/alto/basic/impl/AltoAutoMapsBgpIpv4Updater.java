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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.ResourceNetworkMap;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.resource.network.map.algorithm.BgpSimpleAsCluster;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.Map;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev171207.bgp.rib.rib.loc.rib.tables.routes.Ipv4RoutesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev171207.ipv4.routes.ipv4.routes.Ipv4Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev171207.path.attributes.attributes.as.path.Segments;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.BgpRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.RibId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.bgp.rib.Rib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.bgp.rib.RibKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.bgp.rib.rib.LocRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.UnicastSubsequentAddressFamily;
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

public class AltoAutoMapsBgpIpv4Updater implements DataTreeChangeListener<Tables>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AltoAutoMapsBgpIpv4Updater.class);

    private final DataBroker dataBroker;
    private ListenerRegistration<?> registration;
    private String contextId;
    private ResourceNetworkMap networkMapConfig;
    private InstanceIdentifier<Tables> tableIID;

    public AltoAutoMapsBgpIpv4Updater(String contextId, ResourceNetworkMap networkMapConfig, final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.contextId = contextId;
        this.networkMapConfig = networkMapConfig;
        this.tableIID = getConfiguredIpv4Table(networkMapConfig);
        registerBGPListener();
    }

    private void registerBGPListener() {
        if (tableIID != null) {
            registration = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                    LogicalDatastoreType.OPERATIONAL, tableIID), this);
            LOG.info("Listening on BGP Ipv4 Routing Table:", tableIID);
        }
        LOG.info("No routing table to listen");
    }

    private void updateNetworkMap() {
        final ReadWriteTransaction wrx = dataBroker.newReadWriteTransaction();
        List<Map> networkMap = computeNetworkMapByBgpIpv4(networkMapConfig);
        LOG.info("Putting auto generated network-map to manual map config...");
        ManualMapsUtils.createResourceNetworkMap(contextId, networkMapConfig.getResourceId().getValue(),
                networkMap, wrx);
        wrx.submit();
    }

    private InstanceIdentifier<Tables> getConfiguredIpv4Table(ResourceNetworkMap networkMapConfig) {
        if (networkMapConfig.getAlgorithm() instanceof BgpSimpleAsCluster) {
            BgpSimpleAsCluster algorithm = (BgpSimpleAsCluster) networkMapConfig.getAlgorithm();
            RibId ribId = algorithm.getBgpSimpleAsParams().getBgpRib();
            return InstanceIdentifier.builder(BgpRib.class)
                    .child(Rib.class, new RibKey(ribId))
                    .child(LocRib.class)
                    .child(Tables.class, new TablesKey(Ipv4AddressFamily.class, UnicastSubsequentAddressFamily.class))
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

    private List<Map> computeNetworkMapByBgpIpv4(ResourceNetworkMap networkMapConfig) {
        final ReadTransaction rx = dataBroker.newReadOnlyTransaction();
        List<Map> networkMap = new LinkedList<>();
        try {
            Optional<Tables> optional = rx.read(LogicalDatastoreType.OPERATIONAL, tableIID).get();
            if (optional.isPresent()) {
                Tables table = optional.get();
                if (table.getRoutes() instanceof Ipv4RoutesCase) {
                    java.util.Map<String, List<IpPrefix>> pids = getPIDClusters((Ipv4RoutesCase) table.getRoutes());
                    for (java.util.Map.Entry<String, List<IpPrefix>> entry : pids.entrySet()) {
                        String pidName = entry.getKey();
                        List<IpPrefix> prefixList = entry.getValue();
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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return networkMap;
    }

    private java.util.Map<String, List<IpPrefix>> getPIDClusters(Ipv4RoutesCase routesCase) {
        java.util.Map<String, List<IpPrefix>> pids = new LinkedHashMap<>();
        for (Ipv4Route route : routesCase.getIpv4Routes().getIpv4Route()) {
            List<Segments> segments = route.getAttributes().getAsPath().getSegments();
            String pidName = "PID0";
            if (segments != null && !segments.isEmpty()) {
                List<AsNumber> asSequence = segments.get(segments.size() - 1).getAsSequence();
                if (asSequence != null && !asSequence.isEmpty()) {
                    pidName = "PID" + asSequence.get(asSequence.size() - 1).getValue().toString();
                } else {
                    List<AsNumber> asSet = segments.get(segments.size() - 1).getAsSet();
                    if (asSet != null && !asSet.isEmpty()) {
                        pidName = "PID" + String.join("-",
                                (String[]) Arrays.stream((AsNumber[]) asSet.toArray())
                                        .map(s -> s.getValue().toString())
                                        .toArray());
                    }
                }
            }
            if (!pids.containsKey(pidName)) {
                pids.put(pidName, new LinkedList<>());
            }
            pids.get(pidName).add(new IpPrefix(route.getPrefix()));
        }
        return pids;
    }

    @Override
    public void close() throws Exception {
        if (registration != null) {
            registration.close();
        }
        LOG.info("BGP IPv4 updater closed");
    }
}
