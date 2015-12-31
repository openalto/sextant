/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import org.opendaylight.alto.basic.endpointcostservice.suportservice.exception.ReadDataFailedException;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkFlowCapableNodeService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkPortStatisticsService;
import org.opendaylight.alto.basic.endpointcostservice.helper.DataStoreHelper;
import org.opendaylight.alto.basic.endpointcostservice.util.InstanceIdentifierUtils;
import org.opendaylight.alto.basic.endpointcostservice.util.NameConverter;
import org.opendaylight.alto.basic.endpointcostservice.util.NetworkServiceConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkFlowCapableNodeImpl implements NetworkFlowCapableNodeService{
    private static final Logger log = LoggerFactory
            .getLogger(NetworkFlowCapableNodeImpl.class);
    private DataBroker dataBroker;
    private NetworkPortStatisticsService portStatistics;

    public NetworkFlowCapableNodeImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.portStatistics = new NetworkPortStatisticsServiceImpl(dataBroker);
    }

    @Override
    public void addFlowCapableNode(FlowCapableNode node) {}

    @Override
    public void deleteFlowCapableNode(FlowCapableNode node) {}

    @Override
    public FlowCapableNode getFlowCapableNode(String nodeId) {
        log.info("Reading flow capable node for " + nodeId);
        try {
            return DataStoreHelper.readOperational(dataBroker,
                    InstanceIdentifierUtils.flowCapableNode(nodeId));
        } catch (ReadDataFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FlowCapableNodeConnector getFlowCapableNodeConnector(String tpId) {
        log.info("Reading flow capable node connector for " + tpId);
        try {
            return DataStoreHelper.readOperational(dataBroker,
                    InstanceIdentifierUtils.flowCapableNodeConnector(tpId));
        } catch (ReadDataFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FlowCapableNodeConnectorStatistics getFlowCapableNodeConnectorStatistics(String tpId) {
        log.info("Reading flow capable node connector statistics data for + " + tpId);
        try {
            return DataStoreHelper.readOperational(dataBroker,
                    InstanceIdentifierUtils.flowCapableNodeConnectorStatisticsData(tpId))
                    .getFlowCapableNodeConnectorStatistics();
        } catch (ReadDataFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long getConsumedBandwidth(String tpId) {
        FlowCapableNodeConnector nodeConnector = getFlowCapableNodeConnector(tpId);
        return getConsumedBandwidth(tpId, isHalfDuplex(nodeConnector));
    }

    @Override
    public Long getAvailableBandwidth(String tpId) {
        return getAvailableBandwidth(tpId, null);
    }

    @Override
    public Long getAvailableBandwidth(String tpId, Long meterId) {
        FlowCapableNodeConnector nodeConnector = getFlowCapableNodeConnector(tpId);
        Long capacity = getCapacity(nodeConnector, readMeter(tpId, meterId));
        Long consumedBandwidth = getConsumedBandwidth(tpId, isHalfDuplex(nodeConnector));
        log.info("capacity: " + capacity);
        log.info("consumedBandwidth: " + consumedBandwidth);
        if (capacity == null || consumedBandwidth == null) return null;
        return capacity - consumedBandwidth;
    }


    @Override
    public Long getCapacity(String tpId) {
        return getCapacity(tpId, null);
    }

    @Override
    public Long getCapacity(String tpId, Long meterId) {
        FlowCapableNodeConnector nodeConnector = getFlowCapableNodeConnector(tpId);
        return getCapacity(nodeConnector, readMeter(tpId, meterId));
    }

    private long getConsumedBandwidth(String tpId, boolean isHalfDuplex) {
        long transmitted = portStatistics.getCurrentTxSpeed(tpId, NetworkPortStatisticsService.Metric.BITSPERSECOND)
                / 1000;
        long received = portStatistics.getCurrentRxSpeed(tpId, NetworkPortStatisticsService.Metric.BITSPERSECOND)
                / 1000;
        if (isHalfDuplex) {
            return transmitted + received;
        } else {
            return transmitted;
        }
    }
    private boolean isHalfDuplex(FlowCapableNodeConnector nodeConnector) {
        if (nodeConnector == null) return false;
        boolean[] portFeatures = nodeConnector.getCurrentFeature().getValue();
        return portFeatures[NetworkServiceConstants.PORT_FEATURES.get(NetworkServiceConstants.TEN_MB_HD)]
                || portFeatures[NetworkServiceConstants.PORT_FEATURES.get(NetworkServiceConstants.HUNDRED_MD_HD)]
                || portFeatures[NetworkServiceConstants.PORT_FEATURES.get(NetworkServiceConstants.ONE_GB_HD)];
    }

    private Meter readMeter(String tpId, long meterId) {
        String nodeId = NameConverter.extractNodeId(tpId);
        try {
            return DataStoreHelper.readOperational(this.dataBroker,
                    InstanceIdentifierUtils.flowCapableNodeMeter(nodeId, meterId));
        } catch (ReadDataFailedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            return null;
        }
        return null;
    }
    private Long getCapacity(FlowCapableNodeConnector nodeConnector, Meter meter) {
        if (nodeConnector == null) return null;
        long currentSpeed = nodeConnector.getCurrentSpeed();
        if (meter == null) return currentSpeed;
        long bandRate = -1;
        for (MeterBandHeader band : meter.getMeterBandHeaders().getMeterBandHeader()) {
            if (bandRate > band.getBandRate() && bandRate < currentSpeed) {
                bandRate = band.getBandRate();
            }
        }
        return (bandRate == -1) ? currentSpeed : bandRate;
    }
}
