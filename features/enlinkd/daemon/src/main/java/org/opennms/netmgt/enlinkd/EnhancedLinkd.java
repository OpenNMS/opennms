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


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;
import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Linkd class.
 * </p>
 * 
 * @author ranger
 * @version $Id: $
 */
public class EnhancedLinkd extends AbstractServiceDaemon implements ReloadableTopologyDaemon {

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

    /**
     * Linkd Configuration Initialization
     */

    private EnhancedLinkdConfig m_linkdConfig;

    /**
     * Map that contains Nodeid and List of NodeCollector.
     */
    private Map<Integer, List<NodeCollector>> m_nodes = new HashMap<Integer, List<NodeCollector>>();

    @Autowired
    private LocationAwareSnmpClient m_locationAwareSnmpClient;

    @Autowired
    private NodesOnmsTopologyUpdater m_nodesTopologyUpdater;
    @Autowired
    private BridgeOnmsTopologyUpdater m_bridgeTopologyUpdater;
    @Autowired
    private CdpOnmsTopologyUpdater m_cdpTopologyUpdater;
    @Autowired
    private LldpOnmsTopologyUpdater m_lldpTopologyUpdater;
    @Autowired
    private IsisOnmsTopologyUpdater m_isisTopologyUpdater;
    @Autowired
    private OspfOnmsTopologyUpdater m_ospfTopologyUpdater;
    @Autowired    
    private DiscoveryBridgeDomains m_discoveryBridgeDomains;
    @Autowired
    private UserDefinedLinkTopologyUpdater m_userDefinedLinkTopologyUpdater;

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

        // Create a scheduler
        //
        try {
            LOG.info("init: Creating EnhancedLinkd scheduler");
            m_scheduler = new LegacyScheduler("EnhancedLinkd", m_linkdConfig.getThreads());
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create EnhancedLinkd scheduler", e);
            throw e;
        }

        LOG.debug("init: Loading nodes.....");
        for (final Node node : m_queryMgr.findAllSnmpNode()) {
            m_nodes.put(node.getNodeId(), scheduleCollectionForNode(node));
        }
        LOG.debug("init: Nodes loaded.");
        LOG.debug("init: Loading Bridge Topology.....");
        m_bridgeTopologyService.load();
        LOG.debug("init: Bridge Topology loaded.");

        scheduleAndRegisterOnmsTopologyUpdater(m_nodesTopologyUpdater);
        scheduleAndRegisterOnmsTopologyUpdater(m_userDefinedLinkTopologyUpdater);

        if (m_linkdConfig.useBridgeDiscovery()) {
            scheduleDiscoveryBridgeDomain();
            scheduleAndRegisterOnmsTopologyUpdater(m_bridgeTopologyUpdater);
        }

        if (m_linkdConfig.useCdpDiscovery()) {
            scheduleAndRegisterOnmsTopologyUpdater(m_cdpTopologyUpdater);
            
        }

        if (m_linkdConfig.useLldpDiscovery()) {
            scheduleAndRegisterOnmsTopologyUpdater(m_lldpTopologyUpdater);
       }

        if (m_linkdConfig.useIsisDiscovery()) {
            scheduleAndRegisterOnmsTopologyUpdater(m_isisTopologyUpdater);
        }
        
        if (m_linkdConfig.useOspfDiscovery()) {
            scheduleAndRegisterOnmsTopologyUpdater(m_ospfTopologyUpdater);
        }

    }

    public void unscheduleAndUnregisterOnmsTopologyUpdater(TopologyUpdater onmsTopologyUpdater) {
        LOG.info("unscheduleOnmsTopologyUpdater: UnScheduling {}",
                   onmsTopologyUpdater.getInfo());
        onmsTopologyUpdater.unschedule();
        onmsTopologyUpdater.unregister();
    }

    public void scheduleAndRegisterOnmsTopologyUpdater(TopologyUpdater onmsTopologyUpdater) {
        onmsTopologyUpdater.setScheduler(m_scheduler);
        onmsTopologyUpdater.setPollInterval(m_linkdConfig.getTopologyInterval());
        onmsTopologyUpdater.setInitialSleepTime(0L);
        LOG.info("scheduleOnmsTopologyUpdater: Scheduling {}",
                 onmsTopologyUpdater.getInfo());
        onmsTopologyUpdater.schedule();
        onmsTopologyUpdater.register();
    }

    public void unscheduleDiscoveryBridgeDomain() {
    LOG.info("unscheduleDiscoveryBridgeDomain: Scheduling {}",
             m_discoveryBridgeDomains.getInfo());
             m_discoveryBridgeDomains.unschedule();
    }
    
    public void scheduleDiscoveryBridgeDomain() {
            m_discoveryBridgeDomains.setScheduler(m_scheduler);
            m_discoveryBridgeDomains.setPollInterval(m_linkdConfig.getBridgeTopologyInterval());
            m_discoveryBridgeDomains.setInitialSleepTime(m_linkdConfig.getBridgeTopologyInterval()+m_linkdConfig.getInitialSleepTime());
            m_discoveryBridgeDomains.setMaxthreads(m_linkdConfig.getDiscoveryBridgeThreads());
            LOG.info("scheduleDiscoveryBridgeDomain: Scheduling {}",
                     m_discoveryBridgeDomains.getInfo());
            m_discoveryBridgeDomains.schedule();
    }

    /**
     * This method schedules a {@link SnmpCollection} for node for each
     * package. Also schedule discovery link on package when not still
     * activated.
     * 
     * @param node
     */
    private List<NodeCollector> scheduleCollectionForNode(final Node node) {

        List<NodeCollector> colls = new ArrayList<>();
        
        if (m_linkdConfig.useLldpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Lldp: {}",
                    node);
            colls.add(new NodeDiscoveryLldp(m_lldpTopologyService,
                                            m_locationAwareSnmpClient, 
                                            m_linkdConfig.getRescanInterval(),
                                            m_linkdConfig.getInitialSleepTime(),
                                             node));
        }
        
        if (m_linkdConfig.useCdpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Cdp: {}",
                    node);
             colls.add(new NodeDiscoveryCdp(m_cdpTopologyService,
                                            m_locationAwareSnmpClient, 
                                            m_linkdConfig.getRescanInterval(),
                                            m_linkdConfig.getInitialSleepTime(),
                                            node));       
        }
        
        if (m_linkdConfig.useBridgeDiscovery()) {
                LOG.debug("getSnmpCollections: adding IpNetToMedia: {}",
                    node);
                colls.add(new NodeDiscoveryIpNetToMedia(m_ipNetToMediaTopologyService,
                                                        m_locationAwareSnmpClient, 
                                                        m_linkdConfig.getRescanInterval(),
                                                        m_linkdConfig.getInitialSleepTime(),
                                                        node));
                
                LOG.debug("getSnmpCollections: adding Bridge: {}",
                    node);
                colls.add(new NodeDiscoveryBridge(m_bridgeTopologyService,
                                                  m_linkdConfig.getMaxBft(),
                                                  m_locationAwareSnmpClient, 
                                                  m_linkdConfig.getRescanInterval(),
                                                  m_linkdConfig.getInitialSleepTime(),
                                                  node,
                                                  m_linkdConfig.disableBridgeVlanDiscovery()));
        }

        if (m_linkdConfig.useOspfDiscovery()) {
            LOG.debug("getSnmpCollections: adding Ospf: {}",
                    node);
                colls.add(new NodeDiscoveryOspf(m_ospfTopologyService,
                                                m_locationAwareSnmpClient, 
                                                m_linkdConfig.getRescanInterval(),
                                                m_linkdConfig.getInitialSleepTime(),
                                                node));
        }

        if (m_linkdConfig.useIsisDiscovery()) {
            LOG.debug("getSnmpCollections: adding Is-Is: {}",
                    node);
                colls.add(new NodeDiscoveryIsis(m_isisTopologyService,
                                                m_locationAwareSnmpClient, 
                                                m_linkdConfig.getRescanInterval(),
                                                m_linkdConfig.getInitialSleepTime(), 
                                                node));
        }
       
        for (final NodeCollector coll : colls ){
            LOG.debug("ScheduleCollectionForNode: Scheduling {}",
                coll.getInfo());
            coll.setScheduler(m_scheduler);
            coll.schedule();
        }
        
        return colls;
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
        Node node = m_queryMgr.getSnmpNode(nodeid);
        if (node == null) {
            LOG.warn("scheduleNodeCollection: Failed to get linkable node from database with ID {}. Exiting",
                           nodeid);
            return false;
        }
        synchronized (m_nodes) {
            m_nodes.put(node.getNodeId(), scheduleCollectionForNode(node));
        }
        return true;
    }

    public boolean runSingleSnmpCollection(final int nodeId) {
        boolean allready = true;
        if (!m_nodes.containsKey(nodeId)) {
            return false;
        }
        for (final NodeCollector snmpColl : m_nodes.get(nodeId)) {
            if (!snmpColl.isReady()) {
                allready = false;
                continue;
            }
            snmpColl.collect();
        }
        return allready;
    }

    public void runDiscoveryBridgeDomains() {
            m_discoveryBridgeDomains.runDiscovery();
    }

    public void forceTopologyUpdaterRun(ProtocolSupported proto) {
        switch (proto) {
        case CDP:
            if (m_linkdConfig.useCdpDiscovery()) {
                m_cdpTopologyUpdater.forceRun();
            }
            break;
  
        case LLDP:
            if (m_linkdConfig.useLldpDiscovery()) {
                m_lldpTopologyUpdater.forceRun();
            }
            break;
        
        case ISIS:
            if (m_linkdConfig.useIsisDiscovery()) {
                m_isisTopologyUpdater.forceRun();
            }
            break;
        
        case OSPF:
            if (m_linkdConfig.useOspfDiscovery()) {
                m_ospfTopologyUpdater.forceRun();
            }
            break;
        
        case BRIDGE:
            if (m_linkdConfig.useBridgeDiscovery()) {
                m_bridgeTopologyUpdater.forceRun();
            }
            break;

        case NODES:
            m_nodesTopologyUpdater.forceRun();
            break;

        case USERDEFINED:
            m_userDefinedLinkTopologyUpdater.forceRun();
            break;

        default:
            break;
        
    }

    }

    public void runTopologyUpdater(ProtocolSupported proto) {
        switch (proto) {
            case CDP:
                if (m_linkdConfig.useCdpDiscovery()) {
                    m_cdpTopologyUpdater.runDiscovery();
                }
                break;
      
            case LLDP:
                if (m_linkdConfig.useLldpDiscovery()) {
                    m_lldpTopologyUpdater.runDiscovery();
                }
                break;
            
            case ISIS:
                if (m_linkdConfig.useIsisDiscovery()) {
                    m_isisTopologyUpdater.runDiscovery();
                }
                break;
            
            case OSPF:
                if (m_linkdConfig.useOspfDiscovery()) {
                    m_ospfTopologyUpdater.runDiscovery();
                }
                break;
            
            case BRIDGE:
                if (m_linkdConfig.useBridgeDiscovery()) {
                    m_bridgeTopologyUpdater.runDiscovery();
                }
                break;

            case NODES:
                m_nodesTopologyUpdater.runDiscovery();
                break;

            case USERDEFINED:
                m_userDefinedLinkTopologyUpdater.runDiscovery();
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

    public void addNode(int intValue) {
        m_queryMgr.updatesAvailable();
    }

    void deleteNode(int nodeid) {
        LOG.info("deleteNode: deleting LinkableNode for node {}",
                        nodeid);
        unscheduleNodeCollection(nodeid);

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
        
        m_queryMgr.updatesAvailable();

    }

    void unscheduleNodeCollection(int nodeid) {
        synchronized (m_nodes) {
            if (m_nodes.containsKey(nodeid)) {
                m_nodes.remove(nodeid).stream().
                forEach(coll -> 
                    coll.unschedule());
            }        
        }
    }
    
    void rescheduleNodeCollection(int nodeid) {
        LOG.info("rescheduleNodeCollection: suspend collection LinkableNode for node {}",
                nodeid);        
        unscheduleNodeCollection(nodeid);
        
        scheduleNodeCollection(nodeid);            	
    }
    
    void suspendNodeCollection(int nodeid) {
        LOG.info("suspendNodeCollection: suspend collection LinkableNode for node {}",
                        nodeid);   
        synchronized (m_nodes) {
               if (m_nodes.containsKey(nodeid)) {
                   m_nodes.get(nodeid).stream().forEach(coll -> coll.suspend());
               } 
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
    public String getSource() {
        return "enlinkd";
    }
    public LocationAwareSnmpClient getLocationAwareSnmpClient() {
        return m_locationAwareSnmpClient;
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
    public NodesOnmsTopologyUpdater getNodesTopologyUpdater() {
        return m_nodesTopologyUpdater;
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

    public void reload() {
        LOG.info("reload: reload enlinkd daemon service");

        m_nodesTopologyUpdater.unschedule();
        m_nodesTopologyUpdater.unregister();
        NodesOnmsTopologyUpdater nodeupdater = NodesOnmsTopologyUpdater.clone(m_nodesTopologyUpdater);
        scheduleAndRegisterOnmsTopologyUpdater(nodeupdater);
        m_nodesTopologyUpdater = nodeupdater;

        if (m_linkdConfig.useOspfDiscovery()) {
            if (m_ospfTopologyUpdater.isRegistered()) {
                m_ospfTopologyUpdater.unschedule();
                m_ospfTopologyUpdater.unregister();
                OspfOnmsTopologyUpdater updater = OspfOnmsTopologyUpdater.clone(m_ospfTopologyUpdater);
                scheduleAndRegisterOnmsTopologyUpdater(updater);
                m_ospfTopologyUpdater = updater;
            } else {
                scheduleAndRegisterOnmsTopologyUpdater(m_ospfTopologyUpdater);
            }
        } else {
            unscheduleAndUnregisterOnmsTopologyUpdater(m_ospfTopologyUpdater);
        }

        if (m_linkdConfig.useLldpDiscovery()) {
            if (m_lldpTopologyUpdater.isRegistered()) {
                m_lldpTopologyUpdater.unschedule();
                m_lldpTopologyUpdater.unregister();
                LldpOnmsTopologyUpdater updater = LldpOnmsTopologyUpdater.clone(m_lldpTopologyUpdater);
                scheduleAndRegisterOnmsTopologyUpdater(updater);
                m_lldpTopologyUpdater = updater;
            } else {
                scheduleAndRegisterOnmsTopologyUpdater(m_lldpTopologyUpdater);
            }
        } else {
            unscheduleAndUnregisterOnmsTopologyUpdater(m_lldpTopologyUpdater);
        }
        
        if (m_linkdConfig.useIsisDiscovery()) {
            if (m_isisTopologyUpdater.isRegistered()) {
                m_isisTopologyUpdater.unschedule();
                m_isisTopologyUpdater.unregister();
                IsisOnmsTopologyUpdater updater = IsisOnmsTopologyUpdater.clone(m_isisTopologyUpdater);
                scheduleAndRegisterOnmsTopologyUpdater(updater);
                m_isisTopologyUpdater = updater;
            } else {
                scheduleAndRegisterOnmsTopologyUpdater(m_isisTopologyUpdater);
            }
        } else {
            unscheduleAndUnregisterOnmsTopologyUpdater(m_isisTopologyUpdater);
        }

        if (m_linkdConfig.useCdpDiscovery()) {
            if (m_cdpTopologyUpdater.isRegistered()) {
                m_cdpTopologyUpdater.unschedule();
                m_cdpTopologyUpdater.unregister();
                CdpOnmsTopologyUpdater updater = CdpOnmsTopologyUpdater.clone(m_cdpTopologyUpdater);
                scheduleAndRegisterOnmsTopologyUpdater(updater);
                m_cdpTopologyUpdater = updater;
            } else {
                scheduleAndRegisterOnmsTopologyUpdater(m_cdpTopologyUpdater);
            }
        } else {
            unscheduleAndUnregisterOnmsTopologyUpdater(m_cdpTopologyUpdater);
        }
        
        if (m_linkdConfig.useBridgeDiscovery()) {
            if (m_bridgeTopologyUpdater.isRegistered()) {
                m_bridgeTopologyUpdater.unschedule();
                m_bridgeTopologyUpdater.unregister();
                BridgeOnmsTopologyUpdater updater = BridgeOnmsTopologyUpdater.clone(m_bridgeTopologyUpdater);
                scheduleAndRegisterOnmsTopologyUpdater(updater);
                m_bridgeTopologyUpdater = updater;
            } else {
                scheduleAndRegisterOnmsTopologyUpdater(m_bridgeTopologyUpdater);
            }
        } else {
            unscheduleAndUnregisterOnmsTopologyUpdater(m_bridgeTopologyUpdater);
        }
        
        if (m_linkdConfig.useBridgeDiscovery()) {
            m_discoveryBridgeDomains.unschedule();
            m_discoveryBridgeDomains = DiscoveryBridgeDomains.clone(m_discoveryBridgeDomains);
            scheduleDiscoveryBridgeDomain();
        } else {
            unscheduleDiscoveryBridgeDomain();
        }

        synchronized (m_nodes) {
            final Set<Node> nodes = new HashSet<Node>();
            for (List<NodeCollector> list: m_nodes.values()) {
                list.stream().forEach(coll -> {
                    coll.unschedule(); 
                    nodes.add(coll.getNode());
                });
            }
            m_nodes.clear();
            nodes.stream().
                forEach(node -> m_nodes.put(node.getNodeId(), scheduleCollectionForNode(node)));
        }
    }
    
    public void reloadConfig() {
        LOG.info("reloadConfig: reload enlinkd configuration file");
        try {
            m_linkdConfig.reload();
        } catch (IOException e) {
            LOG.error("reloadConfig: cannot reload config: {}", e.getMessage());
            return;
        }
        reload();
    }

    @Override
    public void reloadTopology() {
        LOG.info("reloadTopology: reload enlinkd topology updaters");
        LOG.debug("reloadTopology: Loading Bridge Topology.....");
        m_bridgeTopologyService.load();
        LOG.debug("reloadTopology: Bridge Topology Loaded");
        for (ProtocolSupported protocol :ProtocolSupported.values()) {
            forceTopologyUpdaterRun(protocol);
            runTopologyUpdater(protocol);
        }
    }
}
