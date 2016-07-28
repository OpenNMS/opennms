/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;

import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.DefaultTopologyProviderInfo;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SimpleMetaTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.providers.BridgeTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.providers.CdpTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.providers.EnhancedLinkdTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.providers.IsisTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.providers.LldpTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.providers.OspfTopologyProvider;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.FilterManager;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

public class TopologyFactory {
    
    private MetricRegistry metricRegistry;
    private NodeDao nodeDao;
    private SnmpInterfaceDao snmpInterfaceDao;
    private IpInterfaceDao ipInterfaceDao;
    private TopologyDao topologyDao;
    private FilterManager filterManager;
    private boolean addNodeWithoutLink;
    private LinkdHopCriteriaFactory linkdHopCriteriaFactory;
    private LldpElementDao lldpElementDao;
    private LldpLinkDao lldpLinkDao;
    private CdpLinkDao cdpLinkDao;
    private CdpElementDao cdpElementDao;
    private OspfLinkDao ospfLinkDao;
    private IsIsLinkDao isisLinkDao;
    private BridgeMacLinkDao bridgeMacLinkDao;
    private BridgeBridgeLinkDao bridgeBridgeLinkDao;
    private BridgeTopologyDao bridgeTopologyDao;
    private IpNetToMediaDao ipNetToMediaDao;
    private TransactionOperations transactionOperations;
    
    public GraphProvider createBridgeTopologyProvider() {
        BridgeTopologyProvider bridgeTopologyProvider = new BridgeTopologyProvider(metricRegistry);
        injectEnhancedLinkdProperties(bridgeTopologyProvider);
        bridgeTopologyProvider.setIpNetToMediaDao(ipNetToMediaDao);
        bridgeTopologyProvider.setBridgeBridgeLinkDao(bridgeBridgeLinkDao);
        bridgeTopologyProvider.setBridgeMacLinkDao(bridgeMacLinkDao);
        bridgeTopologyProvider.setBridgeTopologyDao(bridgeTopologyDao);
        bridgeTopologyProvider.setTopologyProviderInfo(new DefaultTopologyProviderInfo("Bridge", "Bridge"));
        return wrap(bridgeTopologyProvider);
    }

    public GraphProvider createCdpTopologyProvider() {
        CdpTopologyProvider cdpTopologyProvider = new CdpTopologyProvider(metricRegistry);
        injectEnhancedLinkdProperties(cdpTopologyProvider);
        cdpTopologyProvider.setCdpElementDao(cdpElementDao);
        cdpTopologyProvider.setCdpLinkDao(cdpLinkDao);
        cdpTopologyProvider.setTopologyProviderInfo(new DefaultTopologyProviderInfo("Cdp", "Cdp"));
        return wrap(cdpTopologyProvider);
    }

    public GraphProvider createIsisTopologyProvider() {
        IsisTopologyProvider isisTopologyProvider = new IsisTopologyProvider(metricRegistry);
        injectEnhancedLinkdProperties(isisTopologyProvider);
        isisTopologyProvider.setIsisLinkDao(isisLinkDao);
        isisTopologyProvider.setTopologyProviderInfo(new DefaultTopologyProviderInfo("Isis", "Isis"));
        return wrap(isisTopologyProvider);
    }

    public GraphProvider createLldpTopologyProvider() {
        LldpTopologyProvider lldpTopologyProvider = new LldpTopologyProvider(metricRegistry);
        injectEnhancedLinkdProperties(lldpTopologyProvider);
        lldpTopologyProvider.setLldpElementDao(lldpElementDao);
        lldpTopologyProvider.setLldpLinkDao(lldpLinkDao);
        lldpTopologyProvider.setTopologyProviderInfo(new DefaultTopologyProviderInfo("Lldp", "Lldp"));
        return wrap(lldpTopologyProvider);
    }

    public GraphProvider createOspfTopologyProvider() {
        OspfTopologyProvider ospfTopologyProvider = new OspfTopologyProvider(metricRegistry);
        injectEnhancedLinkdProperties(ospfTopologyProvider);
        ospfTopologyProvider.setOspfLinkDao(ospfLinkDao);
        ospfTopologyProvider.setTopologyProviderInfo(new DefaultTopologyProviderInfo("Ospf", "Ospf"));
        return wrap(ospfTopologyProvider);
    }

    public MetaTopologyProvider createMetaTopologyProvider() {
        ArrayList<GraphProvider> graphProviders = Lists.newArrayList(
                createBridgeTopologyProvider(),
                createCdpTopologyProvider(),
                createLldpTopologyProvider(),
                createIsisTopologyProvider(),
                createOspfTopologyProvider());
        SimpleMetaTopologyProvider metaTopologyProvider = new SimpleMetaTopologyProvider(graphProviders, graphProviders.get(0));
        return metaTopologyProvider;
    }

    private void injectEnhancedLinkdProperties(EnhancedLinkdTopologyProvider input) {
        input.setAddNodeWithoutLink(addNodeWithoutLink);
        input.setFilterManager(filterManager);
        input.setIpInterfaceDao(ipInterfaceDao);
        input.setLinkdHopCriteriaFactory(linkdHopCriteriaFactory);
        input.setNodeDao(nodeDao);
        input.setSnmpInterfaceDao(snmpInterfaceDao);
        input.setTopologyDao(topologyDao);
        input.setTransactionOperations(transactionOperations);
    }

    private GraphProvider wrap(GraphProvider input) {
        return new NodeACLVertexProvider(new VertexHopGraphProvider(input), nodeDao);
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        this.snmpInterfaceDao = snmpInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setTopologyDao(TopologyDao topologyDao) {
        this.topologyDao = topologyDao;
    }

    public void setFilterManager(FilterManager filterManager) {
        this.filterManager = filterManager;
    }

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) {
        this.addNodeWithoutLink = addNodeWithoutLink;
    }

    public void setLinkdHopCriteriaFactory(LinkdHopCriteriaFactory linkdHopCriteriaFactory) {
        this.linkdHopCriteriaFactory = linkdHopCriteriaFactory;
    }

    public void setLldpElementDao(LldpElementDao lldpElementDao) {
        this.lldpElementDao = lldpElementDao;
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        this.lldpLinkDao = lldpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        this.cdpLinkDao = cdpLinkDao;
    }

    public void setCdpElementDao(CdpElementDao cdpElementDao) {
        this.cdpElementDao = cdpElementDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        this.ospfLinkDao = ospfLinkDao;
    }

    public void setIsisLinkDao(IsIsLinkDao isisLinkDao) {
        this.isisLinkDao = isisLinkDao;
    }

    public void setBridgeMacLinkDao(BridgeMacLinkDao bridgeMacLinkDao) {
        this.bridgeMacLinkDao = bridgeMacLinkDao;
    }

    public void setBridgeBridgeLinkDao(BridgeBridgeLinkDao bridgeBridgeLinkDao) {
        this.bridgeBridgeLinkDao = bridgeBridgeLinkDao;
    }

    public void setBridgeTopologyDao(BridgeTopologyDao bridgeTopologyDao) {
        this.bridgeTopologyDao = bridgeTopologyDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        this.ipNetToMediaDao = ipNetToMediaDao;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }
}
