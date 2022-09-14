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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;
import org.opennms.netmgt.scheduler.Executable;
import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.enlinkd.common.SchedulableNodeCollectorGroup;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
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
 * EnhancedLinkd class.
 * </p>
 *
 * @author antonio
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
     * scheduler thread for Collection Groups
     */
    private LegacyScheduler m_scheduler;

    /**
     * executor for Collection Groups
     */
    private LegacyPriorityExecutor m_executor;

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

    private final Map<ProtocolSupported, SchedulableNodeCollectorGroup> m_schedulables = new HashMap();

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

        try {
            LOG.info("init: Creating EnhancedLinkd scheduler");
            m_scheduler = new LegacyScheduler("EnhancedLinkd", m_linkdConfig.getSchedulerThreads());
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create EnhancedLinkd scheduler", e);
            throw e;
        }

        try {
            LOG.info("init: Creating EnhancedLinkd executor");
            m_executor = new LegacyPriorityExecutor("EnhancedLinkd", m_linkdConfig.getExecutorThreads(),m_linkdConfig.getExecutorQueueSize());
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create EnhancedLinkd scheduler", e);
            throw e;
        }

        SchedulableNodeCollectorGroup cdpSchedulableGroup = new SchedulableNodeCollectorGroup(
                m_linkdConfig.getCdpRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getCdpPriority(), "cdpEnlinkdSchedulableCollectionGroup");
        SchedulableNodeCollectorGroup lldpSchedulableGroup = new SchedulableNodeCollectorGroup(
                m_linkdConfig.getLldpRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getLldpPriority(), "lldpEnlinkdSchedulableCollectionGroup");
        SchedulableNodeCollectorGroup bridgeSchedulableGroup = new SchedulableNodeCollectorGroup(
                m_linkdConfig.getBridgeRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getBridgePriority(), "bridgeEnlinkdSchedulableCollectionGroup");
        SchedulableNodeCollectorGroup ospfSchedulableGroup = new SchedulableNodeCollectorGroup(
                m_linkdConfig.getOspfRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getOspfPriority(), "ospfEnlinkdSchedulableCollectionGroup");
        SchedulableNodeCollectorGroup isisSchedulableGroup = new SchedulableNodeCollectorGroup(
                m_linkdConfig.getIsisRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getIsisPriority(), "isisEnlinkdSchedulableCollectionGroup");
        LOG.debug("init: Loading nodes.....");
        for (final Node node : m_queryMgr.findAllSnmpNode()) {
            scheduleCollectionForNode(node);
        }
        cdpSchedulableGroup.setScheduler(m_scheduler);
        cdpSchedulableGroup.schedule();
        m_schedulables.put(ProtocolSupported.CDP, cdpSchedulableGroup);

        lldpSchedulableGroup.setScheduler(m_scheduler);
        lldpSchedulableGroup.schedule();
        m_schedulables.put(ProtocolSupported.LLDP, lldpSchedulableGroup);

        bridgeSchedulableGroup.setScheduler(m_scheduler);
        bridgeSchedulableGroup.schedule();
        m_schedulables.put(ProtocolSupported.BRIDGE, bridgeSchedulableGroup);

        ospfSchedulableGroup.setScheduler(m_scheduler);
        ospfSchedulableGroup.schedule();
        m_schedulables.put(ProtocolSupported.OSPF, ospfSchedulableGroup);

        isisSchedulableGroup.setScheduler(m_scheduler);
        isisSchedulableGroup.schedule();
        m_schedulables.put(ProtocolSupported.ISIS, isisSchedulableGroup);

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
        onmsTopologyUpdater.setPollInterval(m_linkdConfig.getTopologyUpdaterInterval());
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
     */
    private void scheduleCollectionForNode(final Node node) {

        if (m_linkdConfig.useLldpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Lldp: {}",
                    node);
            m_schedulables.get(ProtocolSupported.LLDP).add(new NodeDiscoveryLldp(m_lldpTopologyService,
                    m_locationAwareSnmpClient,
                    node));
        }
        
        if (m_linkdConfig.useCdpDiscovery()) {
            LOG.debug("getSnmpCollections: adding Cdp: {}",
                    node);
            m_schedulables.get(ProtocolSupported.CDP).add(new NodeDiscoveryCdp(m_cdpTopologyService,
                                            m_locationAwareSnmpClient, 
                                            node));
        }
        
        if (m_linkdConfig.useBridgeDiscovery()) {
                LOG.debug("getSnmpCollections: adding IpNetToMedia: {}",
                    node);
            m_schedulables.get(ProtocolSupported.BRIDGE).add(new NodeDiscoveryIpNetToMedia(m_ipNetToMediaTopologyService,
                                                        m_locationAwareSnmpClient, 
                                                        node));
                
                LOG.debug("getSnmpCollections: adding Bridge: {}",
                    node);
            m_schedulables.get(ProtocolSupported.BRIDGE).add(new NodeDiscoveryBridge(m_bridgeTopologyService,
                                                  m_linkdConfig.getMaxBft(),
                                                  m_locationAwareSnmpClient, 
                                                  node,
                                                  m_linkdConfig.disableBridgeVlanDiscovery()));
        }

        if (m_linkdConfig.useOspfDiscovery()) {
            LOG.debug("getSnmpCollections: adding Ospf: {}",
                    node);
            m_schedulables.get(ProtocolSupported.OSPF).add(new NodeDiscoveryOspf(m_ospfTopologyService,
                                                m_locationAwareSnmpClient, 
                                                node));
        }

        if (m_linkdConfig.useIsisDiscovery()) {
            LOG.debug("getSnmpCollections: adding Is-Is: {}",
                    node);
            m_schedulables.get(ProtocolSupported.ISIS).add(new NodeDiscoveryIsis(m_isisTopologyService,
                                                m_locationAwareSnmpClient, 
                                                node));
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
        m_executor.start();
        m_scheduler.start();

    }

    /**
     * <p>
     * onStop
     * </p>
     */
    protected synchronized void onStop() {
              // Stop the scheduler
        m_executor.stop();
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
        m_executor.pause();
    }

    /**
     * <p>
     * onResume
     * </p>
     */
    protected synchronized void onResume() {
        m_executor.resume();
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
        unscheduleNodeCollection(nodeid);
        scheduleCollectionForNode(node);
        return true;
    }

    public List<NodeCollector> getNodeCollectors(final int nodeId) {
        final List<NodeCollector> nodeCollectors = new ArrayList<>();
        EnumSet.allOf(ProtocolSupported.class).stream().filter(p -> m_schedulables.containsKey(p)).forEach(p -> nodeCollectors.addAll(m_schedulables.get(p).get(nodeId)));
        return nodeCollectors;
    }

    public boolean hasNodeCollectors(final int nodeId) {
        for(ProtocolSupported p : ProtocolSupported.values()) {
            if (m_schedulables.containsKey(p)) {
                if (m_schedulables.get(p).hasCollectionFor(nodeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean runSingleSnmpCollection(final int nodeId) {
        List<NodeCollector> nodeCollectors = getNodeCollectors(nodeId);
        if (!hasNodeCollectors(nodeId)) {
            return false;
        }
        for (final NodeCollector snmpColl : getNodeCollectors(nodeId)) {
            snmpColl.collect();
        }
        return true;
    }

    public void runDiscoveryBridgeDomains() {
            m_discoveryBridgeDomains.runSchedulable();
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
                    m_cdpTopologyUpdater.runSchedulable();
                }
                break;
      
            case LLDP:
                if (m_linkdConfig.useLldpDiscovery()) {
                    m_lldpTopologyUpdater.runSchedulable();
                }
                break;
            
            case ISIS:
                if (m_linkdConfig.useIsisDiscovery()) {
                    m_isisTopologyUpdater.runSchedulable();
                }
                break;
            
            case OSPF:
                if (m_linkdConfig.useOspfDiscovery()) {
                    m_ospfTopologyUpdater.runSchedulable();
                }
                break;
            
            case BRIDGE:
                if (m_linkdConfig.useBridgeDiscovery()) {
                    m_bridgeTopologyUpdater.runSchedulable();
                }
                break;

            case NODES:
                m_nodesTopologyUpdater.runSchedulable();
                break;

            case USERDEFINED:
                m_userDefinedLinkTopologyUpdater.runSchedulable();
                break;

            default:
                break;
            
        }
    }

    void wakeUpNodeCollection(int nodeid) {

        if (!hasNodeCollectors(nodeid)) {
            LOG.warn("wakeUpNodeCollection: node not found during scheduling with ID {}",
                           nodeid);
            scheduleNodeCollection(nodeid);
            return;
        } 
        getNodeCollectors(nodeid).forEach(Executable::wakeUp);
    }

    public void addNode() {
        m_queryMgr.updatesAvailable();
    }

    void deleteNode(int nodeid) {
        LOG.info("deleteNode: deleting LinkableNode for node {}",
                        nodeid);
        unscheduleNodeCollection(nodeid);

        m_bridgeTopologyService.delete(nodeid);
        m_cdpTopologyService.delete(nodeid);
        m_isisTopologyService.delete(nodeid);
        m_lldpTopologyService.delete(nodeid);
        m_ospfTopologyService.delete(nodeid);
        m_ipNetToMediaTopologyService.delete(nodeid);
        
        m_queryMgr.updatesAvailable();

    }

    void unscheduleNodeCollection(int nodeid) {
        for (NodeCollector nodeCollector: getNodeCollectors(nodeid)) {
            for (SchedulableNodeCollectorGroup collectorGroup: m_schedulables.values())
                collectorGroup.remove(nodeCollector);
        }
    }
    
    void rescheduleNodeCollection(int nodeid) {
        LOG.info("rescheduleNodeCollection: rescheduling collection for node {}",
                nodeid);        
        unscheduleNodeCollection(nodeid);
        
        scheduleNodeCollection(nodeid);            	
    }
    
    void suspendNodeCollection(int nodeid) {
        LOG.info("suspendNodeCollection: suspend collection for node {}",
                        nodeid);   
        getNodeCollectors(nodeid).forEach(Executable::suspend);
    }

    public NodeTopologyService getQueryManager() {
        return m_queryMgr;
    }

    public void setQueryManager(NodeTopologyService queryMgr) {
        m_queryMgr = queryMgr;
    }

    public LegacyScheduler getScheduler() {
        return m_scheduler;
    }

    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    public EnhancedLinkdConfig getLinkdConfig() {
        return m_linkdConfig;
    }

    public void setLinkdConfig(final EnhancedLinkdConfig config) {
        m_linkdConfig = config;
    }
    public String getSource() {
        return "enlinkd";
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
        switch (m_scheduler.getStatus()) {
            case LegacyScheduler.STARTING:
                LOG.info("reload: scheduler STARTING: calling pause");
                m_scheduler.pause();
                break;
            case LegacyScheduler.RUNNING:
                LOG.info("reload: scheduler RUNNING: pause");
                m_scheduler.pause();
                break;
            case LegacyScheduler.PAUSE_PENDING:
                LOG.info("reload: scheduler PAUSE_PENDING.");
                break;
            case LegacyScheduler.RESUME_PENDING:
                LOG.info("reload: scheduler RESUME_PENDING: pause");
                m_scheduler.pause();
                break;
            case LegacyScheduler.STOP_PENDING:
                LOG.info("reload: scheduler STOP_PENDING.");
                throw new UnsupportedOperationException("scheduler stop pending");
            case LegacyScheduler.STOPPED:
                LOG.info("reload: scheduler STOPPED.");
                throw new UnsupportedOperationException("scheduler stopped");
        }

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

        EnumSet.allOf(ProtocolSupported.class).stream().filter(p -> m_schedulables.containsKey(p)).forEach(p -> {
             SchedulableNodeCollectorGroup schedulableNodeCollectorGroup = m_schedulables.get(p);
             schedulableNodeCollectorGroup.unschedule();
             schedulableNodeCollectorGroup.clear();
            schedulableNodeCollectorGroup.setInitialSleepTime(m_linkdConfig.getInitialSleepTime());
             switch (p) {
                 case CDP:
                     schedulableNodeCollectorGroup.setPollInterval(m_linkdConfig.getCdpRescanInterval());
                     schedulableNodeCollectorGroup.setPriority(m_linkdConfig.getCdpPriority());
                     break;
                 case LLDP:
                     schedulableNodeCollectorGroup.setPollInterval(m_linkdConfig.getLldpRescanInterval());
                     schedulableNodeCollectorGroup.setPriority(m_linkdConfig.getLldpPriority());
                     break;
                 case BRIDGE:
                     schedulableNodeCollectorGroup.setPollInterval(m_linkdConfig.getBridgeRescanInterval());
                     schedulableNodeCollectorGroup.setPriority(m_linkdConfig.getBridgePriority());
                     break;
                 case OSPF:
                     schedulableNodeCollectorGroup.setPollInterval(m_linkdConfig.getOspfRescanInterval());
                     schedulableNodeCollectorGroup.setPriority(m_linkdConfig.getOspfPriority());
                     break;
                 case ISIS:
                     schedulableNodeCollectorGroup.setPollInterval(m_linkdConfig.getIsisRescanInterval());
                     schedulableNodeCollectorGroup.setPriority(m_linkdConfig.getIsisPriority());
                     break;
                 default:
                     break;
             }
        });

        for (final Node node : m_queryMgr.findAllSnmpNode()) {
            scheduleCollectionForNode(node);
        }

        switch (m_scheduler.getStatus()) {
            case LegacyScheduler.STARTING:
                LOG.info("reload: scheduler STARTING,");
                break;
            case LegacyScheduler.RUNNING:
                LOG.info("reload: scheduler RUNNING.");
                break;
            case LegacyScheduler.PAUSE_PENDING:
                LOG.info("reload: scheduler PAUSE_PENDING. resuming");
                m_scheduler.resume();
                break;
            case LegacyScheduler.RESUME_PENDING:
                LOG.info("reload: scheduler RESUME_PENDING: pause");
                break;
            case LegacyScheduler.STOP_PENDING:
                LOG.info("reload: scheduler STOP_PENDING.");
                throw new UnsupportedOperationException("scheduler stop pending");
            case LegacyScheduler.STOPPED:
                LOG.info("reload: scheduler STOPPED.");
                throw new UnsupportedOperationException("scheduler stopped");
            default:
                LOG.info("reload: scheduler status: {}", m_scheduler.getStatus());

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