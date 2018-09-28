/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.spring.BeanUtils;
import org.opennms.features.enlinkd.service.api.EnhancedLinkdService;
import org.opennms.features.enlinkd.service.api.Node;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsTopologyException;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * <p>
 * Linkd class.
 * </p>
 * 
 * @author ranger
 * @version $Id: $
 */
public class EnhancedLinkd extends AbstractServiceDaemon {
    private final static Logger LOG = LoggerFactory.getLogger(EnhancedLinkd.class);
    /**
     * The log4j category used to log messages.
     */
    private static final String LOG_PREFIX = "enlinkd";

    /**
     * scheduler thread
     */
    private LegacyScheduler m_scheduler;

    /**
     * The DB connection read and write handler
     */
    private EnhancedLinkdService m_queryMgr;

    /**
     * Linkd Configuration Initialization
     */

    private EnhancedLinkdConfig m_linkdConfig;

    /**
     * List that contains Linkable Nodes.
     */
    private Map<Integer, List<NodeDiscovery>> m_nodes = new HashMap<Integer, List<NodeDiscovery>>();

    /**
     * Event handler
     */
    private volatile EventForwarder m_eventForwarder;

    @Autowired
    private LocationAwareSnmpClient m_locationAwareSnmpClient;

    private DiscoveryCdpTopology m_discoveryCdpTopology;
    private DiscoveryBridgeDomains m_discoveryBridgeDomains;

    private volatile Set<Integer> m_bridgecollectionsscheduled = new HashSet<>();
    /**
     * <p>
     * Constructor for EnhancedLinkd.
     * </p>
     */
    public EnhancedLinkd() {
        super(LOG_PREFIX);
    }

    /**
     * <p>
     * onInit
     * </p>
     */
    protected void onInit() {
        BeanUtils.assertAutowiring(this);

        Assert.state(m_eventForwarder != null,
                     "must set the eventForwarder property");

        LOG.debug("init: Loading nodes.....");
        List<Node> nodes = m_queryMgr.getSnmpNodeList();
        Assert.notNull(m_nodes);
        LOG.debug("init: Nodes loaded.");
        LOG.debug("init: Loading Bridge Topology.....");
        m_queryMgr.loadBridgeTopology();
        LOG.debug("init: Bridge Topology loaded.");

        scheduleCollection(nodes);

        if (m_linkdConfig.useBridgeDiscovery()) {
            scheduleDiscoveryBridgeDomain();
        }

        if (m_linkdConfig.useCdpDiscovery()) {
            scheduleDiscoveryCdpTopology();
        }

    }
    
    public void scheduleDiscoveryCdpTopology() {
         try {
            m_discoveryCdpTopology = DiscoveryCdpTopology.createAndRegister(this);
        } catch (OnmsTopologyException e) {
            LOG.error("OnmsTopologyException: cannote schedule: {} {} {}", e.getMessage(),e.getId(),e.getProtocol());
            return;
        }
         LOG.debug("scheduleDiscoveryCdpTopology: Scheduling {}",
                   m_discoveryCdpTopology.getInfo());
         m_discoveryCdpTopology.setScheduler(m_scheduler);
         m_discoveryCdpTopology.schedule();
    }

    public void scheduleDiscoveryBridgeDomain() {
            m_discoveryBridgeDomains=
                    new DiscoveryBridgeDomains(this);
            LOG.debug("scheduleDiscoveryBridgeDomain: Scheduling {}",
                     m_discoveryBridgeDomains.getInfo());
            m_discoveryBridgeDomains.setScheduler(m_scheduler);
            m_discoveryBridgeDomains.schedule();
    }

    private void scheduleCollection(List<Node> nodes) {
        synchronized (nodes) {
            for (final Node node : nodes) {
                scheduleCollectionForNode(node);
            }
        }
    }

    /**
     * This method schedules a {@link SnmpCollection} for node for each
     * package. Also schedule discovery link on package when not still
     * activated.
     * 
     * @param node
     */
    private void scheduleCollectionForNode(final Node node) {

        List<NodeDiscovery> colls = new ArrayList<>();
        
        if (m_linkdConfig.useLldpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Lldp Discovery: {}",
                    node);
            colls.add(new NodeDiscoveryLldp(this, node));
        }
        
        if (m_linkdConfig.useCdpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Cdp Discovery: {}",
                    node);
             colls.add(new NodeDiscoveryCdp(this, node));       
        }
        
        if (m_linkdConfig.useBridgeDiscovery()) {
                LOG.debug("getSnmpCollections: adding IpNetToMedia Discovery: {}",
                    node);
                colls.add(new NodeDiscoveryIpNetToMedia(this, node));
                
                LOG.debug("getSnmpCollections: adding Bridge Discovery: {}",
                    node);
                colls.add(new NodeDiscoveryBridge(this, node));
        }

        if (m_linkdConfig.useOspfDiscovery()) {
            LOG.debug("getSnmpCollections: adding Ospf Discovery: {}",
                    node);
                colls.add(new NodeDiscoveryOspf(this, node));
        }

        if (m_linkdConfig.useIsisDiscovery()) {
            LOG.debug("getSnmpCollections: adding Is-Is Discovery: {}",
                    node);
                colls.add(new NodeDiscoveryIsis(this, node));
        }
       
        for (final NodeDiscovery coll : colls ){
            LOG.debug("ScheduleCollectionForNode: Scheduling {}",
                coll.getInfo());
            coll.setScheduler(m_scheduler);
            coll.schedule();
        }
        
        synchronized (m_nodes) {
            LOG.debug("scheduleNodeCollection: adding node {} to the collection", node);
            m_nodes.put(node.getNodeId(),colls);
        }
    }

    public DiscoveryBridgeTopology getNodeBridgeDiscoveryTopology(BroadcastDomain domain) {
        return new DiscoveryBridgeTopology(this,domain);
    }
    /**
     * <p>
     * onStart
     * </p>
     */
    protected synchronized void onStart() {

        // start the scheduler
        //
        m_scheduler.start();
        
        // Set the status of the service as running.
        //

    }

    /**
     * <p>
     * onStop
     * </p>
     */
    protected synchronized void onStop() {
              // Stop the scheduler
        m_scheduler.stop();
        m_scheduler = null;
    }

    /**
     * <p>
     * onPause
     * </p>
     */
    protected synchronized void onPause() {
        m_scheduler.pause();
    }

    /**
     * <p>
     * onResume
     * </p>
     */
    protected synchronized void onResume() {
        m_scheduler.resume();
    }

    public boolean scheduleNodeCollection(int nodeid) {

        LOG.debug("scheduleNodeCollection: Loading node {} from database",
                  nodeid);
        Node node = m_queryMgr.getSnmpNode(nodeid);
        if (node == null) {
            LOG.warn("scheduleNodeCollection: Failed to get linkable node from database with ID {}. Exiting",
                           nodeid);
            return false;
        }
        scheduleCollectionForNode(node);
        return true;
    }

    public boolean runSingleSnmpCollection(final int nodeId) {
        boolean allready = true;
        if (!m_nodes.containsKey(nodeId)) {
            return false;
        }
        for (final NodeDiscovery snmpColl : m_nodes.get(nodeId)) {
            if (!snmpColl.isReady()) {
                allready = false;
                continue;
            }
            snmpColl.runDiscovery();
        }
        return allready;
    }

    public void runDiscoveryBridgeDomains() {
        if (m_discoveryBridgeDomains != null) {
            m_discoveryBridgeDomains.runDiscovery();
        }
    }

    public void runDiscoveryCdpTopology() {
        if (m_discoveryCdpTopology != null) {
            m_discoveryCdpTopology.runDiscovery();
        }
    }
    
    public DiscoveryBridgeDomains getDiscoveryBridgeDomains() {
        return m_discoveryBridgeDomains;
    }
    
    public DiscoveryCdpTopology getDiscoveryCdpTopology() {
        return m_discoveryCdpTopology;
    }

    public void scheduleNodeBridgeTopologyDiscovery(BroadcastDomain domain, Map<Integer,Set<BridgeForwardingTableEntry>> updateBfpMap) {
        final DiscoveryBridgeTopology bridgediscovery = getNodeBridgeDiscoveryTopology(domain);
        for (Integer bridgeid: updateBfpMap.keySet()) {
            bridgediscovery.addUpdatedBFT(bridgeid, updateBfpMap.get(bridgeid));
        }
        LOG.debug("scheduleBridgeTopologyDiscovery: Scheduling {}",
                    bridgediscovery.getInfo());
        bridgediscovery.setScheduler(m_scheduler);
        bridgediscovery.schedule();
    }

    void wakeUpNodeCollection(int nodeid) {

        if (!m_nodes.containsKey(nodeid)) {
            LOG.warn("wakeUpNodeCollection: node not found during scheduling with ID {}",
                           nodeid);
            scheduleNodeCollection(nodeid);
            return;
        } 
        m_nodes.get(nodeid).stream().forEach(collection -> collection.wakeUp());
    }

    void deleteNode(int nodeid) {
        LOG.info("deleteNode: deleting LinkableNode for node {}",
                        nodeid);

        if (m_nodes.containsKey(nodeid)) {
            m_nodes.remove(nodeid).stream().forEach(coll -> coll.suspend());
        }
        try {
            m_queryMgr.delete(nodeid);
        } catch (BridgeTopologyException e) {
            LOG.error("deleteNode: {}", e.getMessage());
        }

    }

    void rescheduleNodeCollection(int nodeid) {
        LOG.info("rescheduleNodeCollection: suspend collection LinkableNode for node {}",
                nodeid);        
        if (m_nodes.containsKey(nodeid)) {
            m_nodes.remove(nodeid).stream().forEach(coll -> coll.suspend());
        } 
        
        scheduleNodeCollection(nodeid);
        
    	
    }
    
    void suspendNodeCollection(int nodeid) {
        LOG.info("suspendNodeCollection: suspend collection LinkableNode for node {}",
                        nodeid);   
        if (m_nodes.containsKey(nodeid)) {
            m_nodes.get(nodeid).stream().forEach(coll -> coll.suspend());
        } 
    }

    public EnhancedLinkdService getQueryManager() {
        return m_queryMgr;
    }

    /**
     * <p>
     * setQueryManager
     * </p>
     * 
     * @param queryMgr
     *            a {@link org.opennms.features.enlinkd.persistence.api.linkd.EnhancedLinkdService} object.
     */
    public void setQueryManager(EnhancedLinkdService queryMgr) {
        m_queryMgr = queryMgr;
    }

    /**
     * <p>
     * getScheduler
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.enlinkd.scheduler.Scheduler} object.
     */
    public LegacyScheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>
     * setScheduler
     * </p>
     * 
     * @param scheduler
     *            a {@link org.opennms.netmgt.enlinkd.scheduler.Scheduler}
     *            object.
     */
    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>
     * getLinkdConfig
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.config.LinkdConfig} object.
     */
    public EnhancedLinkdConfig getLinkdConfig() {
        return m_linkdConfig;
    }

    /**
     * <p>
     * setLinkdConfig
     * </p>
     * 
     * @param config
     *            a {@link org.opennms.netmgt.config.LinkdConfig} object.
     */
    public void setLinkdConfig(final EnhancedLinkdConfig config) {
        m_linkdConfig = config;
    }

    /**
     * @return the eventForwarder
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * @param eventForwarder
     *            the eventForwarder to set
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        this.m_eventForwarder = eventForwarder;
    }

	public String getSource() {
		return "enlinkd";
	}

    public SnmpAgentConfig getSnmpAgentConfig(InetAddress ipaddr, String location) {
    	return SnmpPeerFactory.getInstance().getAgentConfig(ipaddr, location);
    }

    public long getInitialSleepTime() {
    	return m_linkdConfig.getInitialSleepTime();
    }

    public long getRescanInterval() {
            return m_linkdConfig.getRescanInterval(); 
    }
    public long getBridgeTopologyInterval() {
        return m_linkdConfig.getBridgeTopologyInterval();
    }
    
    public int getDiscoveryBridgeThreads() {
        return m_linkdConfig.getDiscoveryBridgeThreads();
    }

    public LocationAwareSnmpClient getLocationAwareSnmpClient() {
        return m_locationAwareSnmpClient;
    }
    
    public int getMaxbft() {
    	return m_linkdConfig.getMaxBft();
    }
    
    public synchronized boolean collectBft(int nodeid) {
    	if (getQueryManager().getUpdateBftMap().size()+m_bridgecollectionsscheduled.size() >= m_linkdConfig.getMaxBft() )
    		return false;
    	synchronized (m_bridgecollectionsscheduled) {
        	m_bridgecollectionsscheduled.add(nodeid);
		}
    	return true;
    }
    
    public synchronized void collectedBft(int nodeid) {
    	synchronized (m_bridgecollectionsscheduled) {
        	m_bridgecollectionsscheduled.remove(nodeid);
		}
    }

}