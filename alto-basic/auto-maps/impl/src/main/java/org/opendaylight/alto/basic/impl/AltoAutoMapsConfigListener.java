/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.ConfigContext;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.CostMapConfig;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.NetworkMapConfig;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.cost.map.config.CostType;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.cost.map.config.GeneralParams;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.Algorithm;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.Params;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.algorithm.FirstHopCluster;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.params.Bgp;
import org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.network.map.config.params.Openflow;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AltoAutoMapsConfigListener implements AutoCloseable {

    private DataBroker dataBroker;
    private static final Logger LOG = LoggerFactory.getLogger(AltoAutoMapsConfigListener.class);
    private final List<ListenerRegistration<?>> listenerRegs = new LinkedList<>();

    // FIXME: Should not be a fixed iid; replace it later
    // private static final String DUMMY_CONTEXT = "00000000-0000-0000-0000-000000000000";
    // private final InstanceIdentifier<ConfigContext> dummyConfigContext = InstanceIdentifier.builder(
    //         ConfigContext.class, new ConfigContextKey(new Uuid(DUMMY_CONTEXT))).build();

    private InstanceIdentifier<ConfigContext> configListIID = InstanceIdentifier.builder(ConfigContext.class).build();
    private java.util.Map<String, java.util.Map<String, AutoCloseable>> updaters = new LinkedHashMap<>();

    public void register(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        listenerRegs.add(dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, configListIID),
                changes -> onConfigContextChanged(changes)));
        listenerRegs.add(dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, configListIID.child(NetworkMapConfig.class)),
                changes -> onNetworkMapConfigured(changes)));
        listenerRegs.add(dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, configListIID.child(CostMapConfig.class)),
                changes -> onCostMapConfigured(changes)));
    }

    private void onConfigContextChanged(Collection<DataTreeModification<ConfigContext>> changes) {
        // TODO: Update new config context
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        for (DataTreeModification<ConfigContext> change : changes) {
            final DataObjectModification<ConfigContext> rootNode = change.getRootNode();
            final InstanceIdentifier<ConfigContext> identifier = change.getRootPath().getRootIdentifier();
            final String contextId = identifier.firstKeyOf(ConfigContext.class).getContextId().getValue();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    createConfigContext(contextId, rwx);
                    break;
                case DELETE:
                    break;
            }
        }
        rwx.submit();
    }

    private void createConfigContext(String contextId, ReadWriteTransaction rwx) {
        try {
            if (!ManualMapsUtils.contextExists(contextId, rwx)) {
                ManualMapsUtils.createContext(contextId, rwx);
                updaters.put(contextId, new LinkedHashMap<>());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Fail to create config context!", e);
        }
    }

    private void onNetworkMapConfigured(Collection<DataTreeModification<NetworkMapConfig>> changes) {
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        for (DataTreeModification<NetworkMapConfig> change : changes) {
            final DataObjectModification<NetworkMapConfig> rootNode = change.getRootNode();
            final InstanceIdentifier<NetworkMapConfig> identifier = change.getRootPath().getRootIdentifier();
            final String contextId = identifier.firstKeyOf(ConfigContext.class).getContextId().getValue();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    generateNetworkMapUpdater(contextId, rootNode.getDataAfter().getResourceId().getValue(),
                            rootNode.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    // TODO: Update configuration
                    break;
                case DELETE:
                    removeNetworkMapUpdater(contextId, rootNode.getDataBefore().getResourceId().getValue(), rwx);
                    break;
            }
        }

        rwx.submit();
    }

    private void onCostMapConfigured(Collection<DataTreeModification<CostMapConfig>> changes) {
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        for (DataTreeModification<CostMapConfig> change : changes) {
            final DataObjectModification<CostMapConfig> rootNode = change.getRootNode();
            final InstanceIdentifier<CostMapConfig> identifier = change.getRootPath().getRootIdentifier();
            final String contextId = identifier.firstKeyOf(ConfigContext.class).getContextId().getValue();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    generateCostMapUpdater(contextId, rootNode.getDataAfter().getResourceId().getValue(),
                            rootNode.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    // TODO: Update configuration
                    break;
                case DELETE:
                    // TODO
                    break;
            }
        }
        rwx.submit();
    }

    private void generateNetworkMapUpdater(String contextId, String resourceId, NetworkMapConfig networkMapConfig) {
        java.util.Map<String, AutoCloseable> contextUpdaters = getUpdaterOrCreate(contextId);
        if (contextUpdaters.containsKey(resourceId)) {
            // TODO: handle configuration updates
            LOG.warn("Updating configuration not supported yet");
            return;
        }
        Params params = networkMapConfig.getParams();
        Algorithm algorithm = networkMapConfig.getAlgorithm();
        if (params == null) {
            LOG.warn("Must set parameters");
        } else if (params instanceof Openflow) {
            LOG.info("creating updater for OpenFlow topology...");
            if (algorithm == null) {
                contextUpdaters.put(resourceId, new AltoAutoMapsOpenflowUpdater(contextId, networkMapConfig,
                        dataBroker));
            } else {
                LOG.warn("Algorithm not supported by OpenFlow network");
            }
        } else if (params instanceof Bgp) {
            if (algorithm == null) {
                LOG.warn("Must set algorithm");
            } else if (algorithm instanceof FirstHopCluster) {
                if (((FirstHopCluster) algorithm).getFirstHopClusterAlgorithm().isInspectIgp()) {
                    LOG.info("creating updater for BGP-LS topology");
                    contextUpdaters.put(resourceId, new AltoAutoMapsBgpLsUpdater(contextId, networkMapConfig,
                            dataBroker));
                } else {
                    LOG.info("creating updater for BGP IPv4 topology...");
                    contextUpdaters.put(resourceId, new AltoAutoMapsBgpIpv4Updater(contextId, networkMapConfig,
                            dataBroker));
                }
            } else {
                LOG.warn("Algorithm not supported by BGP network");
            }
        }
    }

    private void generateCostMapUpdater(String contextId, String resourceId, CostMapConfig costMapConfig) {
        java.util.Map<String, AutoCloseable> contextUpdaters = getUpdaterOrCreate(contextId);
        if (contextUpdaters.containsKey(resourceId)) {
            // TODO: handle configuration updates
            LOG.warn("Updating configuration not supported yet");
            return;
        }
        List<CostType> costTypes = costMapConfig.getCostType();
        if (costTypes == null || costTypes.isEmpty()) {
            LOG.warn("MUST set one cost type");
        }
        CostType costType = costTypes.get(0);
        String costMode = costType.getCostMode();
        CostMetric costMetric = costType.getCostMetric();
        if (costMode.equals("numerical")) {
            if (costMetric.getValue().equals("hopcount")) {
            } else if (costMetric.getValue().equals("routingcost")) {
                LOG.warn(String.format("Cost-type [%s, %s] not supported yet"), costMode, costMetric.getValue());
                return;
            } else {
                LOG.warn(String.format("Not supported cost metric: %s", costMetric.getValue()));
                return;
            }
        } else if (costMode.equals("ordinal")) {
            LOG.warn("Cost mode [ordinal] not supported yet");
            return;
        } else {
            LOG.warn(String.format("Not supported cost mode: %s", costMode));
            return;
        }
        GeneralParams params = costMapConfig.getGeneralParams();
        if (params instanceof org.opendaylight.yang.gen.v1.urn.alto.auto.maps.rev150105.config.context.cost.map.config.general.params.Bgp) {
            LOG.info("creating updater for BGP cost map");
            contextUpdaters.put(resourceId, new AltoAutoMapsCostMapUpdater(contextId, costMapConfig, dataBroker));
        }

    }

    private void removeNetworkMapUpdater(String contextId, String resourceId, WriteTransaction wx) {
        if (updaters.containsKey(contextId) && updaters.get(contextId).containsKey(resourceId)) {
            try {
                updaters.get(contextId).get(resourceId).close();
                updaters.get(contextId).remove(resourceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMap> networkMapIID =
                ManualMapsUtils.getResourceNetworkMapIID(contextId, resourceId);
        removeMap(networkMapIID, wx);
    }

    private void removeMap(InstanceIdentifier<?> mapIID, final WriteTransaction wx) {
        wx.delete(LogicalDatastoreType.CONFIGURATION, mapIID);
    }

    private java.util.Map<String, AutoCloseable> getUpdaterOrCreate(String contextId) {
        if (!updaters.containsKey(contextId)) {
            updaters.put(contextId, new LinkedHashMap<>());
        }
        return updaters.get(contextId);
    }

    @Override
    public void close() throws Exception {
        for (String contextId : updaters.keySet()) {
            for (String resourceId : updaters.get(contextId).keySet()) {
                updaters.get(contextId).get(resourceId).close();
            }
        }
        for (ListenerRegistration<?> reg : listenerRegs) {
            reg.close();
        }
    }
}
