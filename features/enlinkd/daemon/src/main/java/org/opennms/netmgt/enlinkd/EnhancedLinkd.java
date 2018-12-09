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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
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
    
    public static EnlinkdOnmsTopologyUpdater createAndRegister(EnhancedLinkd linkd, ProtocolSupported proto) throws OnmsTopologyException {
        EnlinkdOnmsTopologyUpdater onmsTopologyUpdater = null;
        switch (proto) {
        case CDP:
            onmsTopologyUpdater = new CdpOnmsTopologyUpdater(
                                                             linkd.getEventForwarder(),
                                                             linkd.getOnmsTopologyDao(),
                                                             linkd.getCdpTopologyService(),
                                                             linkd.getQueryManager(),
                                                             linkd.getBridgeTopologyInterval(),
                                                             linkd.getInitialSleepTime()
                                                                     + linkd.getBridgeTopologyInterval());

            break;
        case BRIDGE:
            onmsTopologyUpdater = new BridgeOnmsTopologyUpdater(
                                                             linkd.getEventForwarder(),
                                                             linkd.getOnmsTopologyDao(),
                                                             linkd.getBridgeTopologyService(),
                                                             linkd.getQueryManager(),
                                                             linkd.getBridgeTopologyInterval(),
                                                             linkd.getInitialSleepTime()
                                                                     + linkd.getBridgeTopologyInterval());
            break;
        case ISIS:
            onmsTopologyUpdater = new IsisOnmsTopologyUpdater(
                                                             linkd.getEventForwarder(),
                                                             linkd.getOnmsTopologyDao(),
                                                             linkd.getIsisTopologyService(),
                                                             linkd.getQueryManager(),
                                                             linkd.getBridgeTopologyInterval(),
                                                             linkd.getInitialSleepTime()
                                                                     + linkd.getBridgeTopologyInterval());

            break;
        case LLDP:
            onmsTopologyUpdater = new LldpOnmsTopologyUpdater(
                                                             linkd.getEventForwarder(),
                                                             linkd.getOnmsTopologyDao(),
                                                             linkd.getLldpTopologyService(),
                                                             linkd.getQueryManager(),
                                                             linkd.getBridgeTopologyInterval(),
                                                             linkd.getInitialSleepTime()
                                                                     + linkd.getBridgeTopologyInterval());
            break;
        case OSPF:
            onmsTopologyUpdater = new OspfOnmsTopologyUpdater(
                                                             linkd.getEventForwarder(),
                                                             linkd.getOnmsTopologyDao(),
                                                             linkd.getOspfTopologyService(),
                                                             linkd.getQueryManager(),
                                                             linkd.getBridgeTopologyInterval(),
                                                             linkd.getInitialSleepTime()
                                                                     + linkd.getBridgeTopologyInterval());

            break;
        default: 
            return null;
            
        }
        linkd.getOnmsTopologyDao().register(onmsTopologyUpdater);
        return onmsTopologyUpdater;
    }

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
    private NodeTopologyService m_queryMgr;

    private BridgeTopologyService m_bridgeTopologyService;
    private CdpTopologyService m_cdpTopologyService;
    private IsisTopologyService m_isisTopologyService;
    private IpNetToMediaTopologyService m_ipNetToMediaTopologyService;
    private LldpTopologyService m_lldpTopologyService;
    private OspfTopologyService m_ospfTopologyService;

    private OnmsTopologyDao m_onmsTopologyDao;
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

    private BridgeOnmsTopologyUpdater m_bridgeTopologyUpdater;
    private CdpOnmsTopologyUpdater m_cdpTopologyUpdater;
    private LldpOnmsTopologyUpdater m_lldpTopologyUpdater;
    private IsisOnmsTopologyUpdater m_isisTopologyUpdater;
    private OspfOnmsTopologyUpdater m_ospfTopologyUpdater;
    
    private DiscoveryBridgeDomains m_discoveryBridgeDomains;

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

        createScheduler();

        LOG.debug("init: Loading nodes.....");
        List<NodeTopologyEntity> nodes = m_queryMgr.findAllSnmpNode();
        Assert.notNull(m_nodes);
        LOG.debug("init: Nodes loaded.");
        LOG.debug("init: Loading Bridge Topology.....");
        m_bridgeTopologyService.load();
        LOG.debug("init: Bridge Topology loaded.");

        scheduleCollection(nodes);

        if (m_linkdConfig.useBridgeDiscovery()) {
            scheduleDiscoveryBridgeDomain();
            m_bridgeTopologyUpdater = (BridgeOnmsTopologyUpdater) scheduleOnmsTopologyUpdater(ProtocolSupported.BRIDGE);
        }

        if (m_linkdConfig.useCdpDiscovery()) {
            m_cdpTopologyUpdater = (CdpOnmsTopologyUpdater)scheduleOnmsTopologyUpdater(ProtocolSupported.CDP);
        }

        if (m_linkdConfig.useLldpDiscovery()) {
            m_lldpTopologyUpdater = (LldpOnmsTopologyUpdater)scheduleOnmsTopologyUpdater(ProtocolSupported.LLDP);
        }

        if (m_linkdConfig.useIsisDiscovery()) {
            m_isisTopologyUpdater = (IsisOnmsTopologyUpdater)scheduleOnmsTopologyUpdater(ProtocolSupported.ISIS);
        }
        
        if (m_linkdConfig.useOspfDiscovery()) {
            m_ospfTopologyUpdater = (OspfOnmsTopologyUpdater)scheduleOnmsTopologyUpdater(ProtocolSupported.OSPF);
        }


    }

    private void createScheduler() {

        // Create a scheduler
        //
        try {
            LOG.info("init: Creating EnhancedLinkd scheduler");
            setScheduler(new LegacyScheduler("EnhancedLinkd", getLinkdConfig().getThreads()));
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create EnhancedLinkd scheduler", e);
            throw e;
        }
    }
    
    public EnlinkdOnmsTopologyUpdater scheduleOnmsTopologyUpdater(ProtocolSupported proto) {
        EnlinkdOnmsTopologyUpdater onmsTopologyUpdater = null;
         try {
             onmsTopologyUpdater = createAndRegister(this,proto);
        } catch (OnmsTopologyException e) {
            LOG.error("scheduleOnmsTopologyUpdater: cannote schedule: {} {} {}", e.getMessage(),e.getId(),e.getProtocol());
            return null;
        }
         LOG.info("scheduleOnmsTopologyUpdater: Scheduling {}",
                   onmsTopologyUpdater.getInfo());
         onmsTopologyUpdater.setScheduler(m_scheduler);
         onmsTopologyUpdater.schedule();
         return onmsTopologyUpdater;
    }

    public void scheduleDiscoveryBridgeDomain() {
            m_discoveryBridgeDomains=
                    new DiscoveryBridgeDomains(getEventForwarder(),
                                               getBridgeTopologyService(),
                                               getBridgeTopologyInterval(),
                                               getBridgeTopologyInterval()+getInitialSleepTime(),
                                               getDiscoveryBridgeThreads());
            LOG.info("scheduleDiscoveryBridgeDomain: Scheduling {}",
                     m_discoveryBridgeDomains.getInfo());
            m_discoveryBridgeDomains.setScheduler(m_scheduler);
            m_discoveryBridgeDomains.schedule();
    }

    private void scheduleCollection(List<NodeTopologyEntity> nodes) {
        synchronized (nodes) {
            for (final NodeTopologyEntity node : nodes) {
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
    private void scheduleCollectionForNode(final NodeTopologyEntity node) {

        List<NodeDiscovery> colls = new ArrayList<>();
        
        if (m_linkdConfig.useLldpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Lldp: {}",
                    node);
            colls.add(new NodeDiscoveryLldp(getEventForwarder(), 
                                            getLldpTopologyService(),
                                            getLocationAwareSnmpClient(), 
                                            getRescanInterval(),
                                            getInitialSleepTime(),
                                             node));
        }
        
        if (m_linkdConfig.useCdpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Cdp: {}",
                    node);
             colls.add(new NodeDiscoveryCdp(getEventForwarder(), 
                                            getCdpTopologyService(),
                                            getLocationAwareSnmpClient(), 
                                            getRescanInterval(),
                                            getInitialSleepTime(),
                                            node));       
        }
        
        if (m_linkdConfig.useBridgeDiscovery()) {
                LOG.debug("getSnmpCollections: adding IpNetToMedia: {}",
                    node);
                colls.add(new NodeDiscoveryIpNetToMedia(getEventForwarder(), 
                                                        getIpNetToMediaTopologyService(),
                                                        getLocationAwareSnmpClient(), 
                                                        getRescanInterval(),
                                                        getInitialSleepTime(),
                                                        node));
                
                LOG.debug("getSnmpCollections: adding Bridge: {}",
                    node);
                colls.add(new NodeDiscoveryBridge(getEventForwarder(), 
                                                  getBridgeTopologyService(),
                                                  getMaxbft(),
                                                  getLocationAwareSnmpClient(), 
                                                  getRescanInterval(),
                                                  getInitialSleepTime(),
                                                  node));
        }

        if (m_linkdConfig.useOspfDiscovery()) {
            LOG.debug("getSnmpCollections: adding Ospf: {}",
                    node);
                colls.add(new NodeDiscoveryOspf(getEventForwarder(), 
                                                getOspfTopologyService(),
                                                getLocationAwareSnmpClient(), 
                                                getRescanInterval(),
                                                getInitialSleepTime(),
                                                node));
        }

        if (m_linkdConfig.useIsisDiscovery()) {
            LOG.debug("getSnmpCollections: adding Is-Is: {}",
                    node);
                colls.add(new NodeDiscoveryIsis(getEventForwarder(), 
                        getIsisTopologyService(),
                        getLocationAwareSnmpClient(), 
                        getRescanInterval(),
                        getInitialSleepTime(), 
                        node));
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

        if (m_nodes.containsKey(nodeid)) {
            LOG.debug("scheduleNodeCollection: node:[{}], node Collection already Scheduled ",
                      nodeid);
            return false;
        }
        LOG.debug("scheduleNodeCollection: Loading node {} from database",
                  nodeid);
        NodeTopologyEntity node = m_queryMgr.getSnmpNode(nodeid);
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

    public void runTopologyUpdater(ProtocolSupported proto) {
        switch (proto) {
            case CDP:
            if (m_cdpTopologyUpdater != null) {
                m_cdpTopologyUpdater.runDiscovery();
            }
            break;
      
            case LLDP:
            if (m_lldpTopologyUpdater != null) {
                m_lldpTopologyUpdater.runDiscovery();
            }
            break;
            
            case ISIS:
            if (m_isisTopologyUpdater != null) {
                m_isisTopologyUpdater.runDiscovery();
            }
            break;
            
            case OSPF:
            if (m_ospfTopologyUpdater != null) {
                m_ospfTopologyUpdater.runDiscovery();
            }
            break;
            
            case BRIDGE:
                if (m_bridgeTopologyUpdater != null) {
                    m_bridgeTopologyUpdater.runDiscovery();
                }
                break;
            
            default:
                break;
            
        }
    }

    public DiscoveryBridgeDomains getDiscoveryBridgeDomains() {
        return m_discoveryBridgeDomains;
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
            m_bridgeTopologyService.delete(nodeid);
        } catch (BridgeTopologyException e) {
            LOG.error("deleteNode: {}", e.getMessage());
        }
        m_cdpTopologyService.delete(nodeid);
        m_isisTopologyService.delete(nodeid);
        m_lldpTopologyService.delete(nodeid);
        m_ospfTopologyService.delete(nodeid);
        m_ipNetToMediaTopologyService.delete(nodeid);
        //FIXME update with a delete topologyService

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

    public NodeTopologyService getQueryManager() {
        return m_queryMgr;
    }

    /**
     * <p>
     * setQueryManager
     * </p>
     * 
     * @param queryMgr
     *            a {@link org.opennms.features.NodeTopologyService.persistence.api.linkd.EnhancedLinkdService} object.
     */
    public void setQueryManager(NodeTopologyService queryMgr) {
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
    
    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }
    public void setBridgeTopologyService(
            BridgeTopologyService bridgeTopologyService) {
        m_bridgeTopologyService = bridgeTopologyService;
    }
    public CdpTopologyService getCdpTopologyService() {
        return m_cdpTopologyService;
    }
    public void setCdpTopologyService(CdpTopologyService cdpTopologyService) {
        m_cdpTopologyService = cdpTopologyService;
    }
    public IsisTopologyService getIsisTopologyService() {
        return m_isisTopologyService;
    }
    public void setIsisTopologyService(IsisTopologyService isisTopologyService) {
        m_isisTopologyService = isisTopologyService;
    }
    public LldpTopologyService getLldpTopologyService() {
        return m_lldpTopologyService;
    }
    public void setLldpTopologyService(LldpTopologyService lldpTopologyService) {
        m_lldpTopologyService = lldpTopologyService;
    }
    public OspfTopologyService getOspfTopologyService() {
        return m_ospfTopologyService;
    }
    public void setOspfTopologyService(OspfTopologyService ospfTopologyService) {
        m_ospfTopologyService = ospfTopologyService;
    }
    public IpNetToMediaTopologyService getIpNetToMediaTopologyService() {
        return m_ipNetToMediaTopologyService;
    }
    public void setIpNetToMediaTopologyService(
            IpNetToMediaTopologyService ipNetToMediaTopologyService) {
        m_ipNetToMediaTopologyService = ipNetToMediaTopologyService;
    }

    public CdpOnmsTopologyUpdater getCdpTopologyUpdater() {
        return m_cdpTopologyUpdater;
    }
    public LldpOnmsTopologyUpdater getLldpTopologyUpdater() {
        return m_lldpTopologyUpdater;
    }
    public IsisOnmsTopologyUpdater getIsisTopologyUpdater() {
        return m_isisTopologyUpdater;
    }
    public BridgeOnmsTopologyUpdater getBridgeTopologyUpdater() {
        return m_bridgeTopologyUpdater;
    }
    public OspfOnmsTopologyUpdater getOspfTopologyUpdater() {
        return m_ospfTopologyUpdater;
    }
    public OnmsTopologyDao getOnmsTopologyDao() {
        return m_onmsTopologyDao;
    }
    public void setOnmsTopologyDao(OnmsTopologyDao onmsTopologyDao) {
        m_onmsTopologyDao = onmsTopologyDao;
    }

}