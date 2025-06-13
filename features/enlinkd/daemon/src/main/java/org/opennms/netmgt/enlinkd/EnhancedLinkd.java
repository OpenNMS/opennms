/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;
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
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedulable;
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
     * threads scheduler
     */
    private LegacyScheduler m_scheduler;

    /**
     * threads priority executor
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
    private OspfAreaOnmsTopologyUpdater m_ospfAreaTopologyUpdater;
    @Autowired
    private DiscoveryBridgeDomains m_discoveryBridgeDomains;
    @Autowired
    private UserDefinedLinkTopologyUpdater m_userDefinedLinkTopologyUpdater;
    @Autowired
    private NetworkRouterTopologyUpdater m_networkRouterTopologyUpdater;

    private final List<SchedulableNodeCollectorGroup> m_groups = new ArrayList<>();
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
        try {
            LOG.info("init: Creating EnhancedLinkd scheduler");
            m_scheduler = new LegacyScheduler("EnhancedLinkd", m_linkdConfig.getThreads());
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create EnhancedLinkd scheduler", e);
            throw e;
        }

        try {
            LOG.info("init: Creating EnhancedLinkd executor");
            m_executor = new LegacyPriorityExecutor("EnhancedLinkd", m_linkdConfig.getExecutorThreads(), m_linkdConfig.getExecutorQueueSize());
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create EnhancedLinkd executor", e);
            throw e;
        }
        LOG.debug("init: Loading Bridge Topology.....");
        m_bridgeTopologyService.load();
        LOG.debug("init: Bridge Topology loaded.");

        schedule(true);
    }

    private void schedule(boolean init) {
        if (init) {
            scheduleAndRegisterOnmsTopologyUpdater(m_nodesTopologyUpdater);
            scheduleAndRegisterOnmsTopologyUpdater(m_networkRouterTopologyUpdater);
            scheduleAndRegisterOnmsTopologyUpdater(m_userDefinedLinkTopologyUpdater);
        }

        if (m_linkdConfig.useCdpDiscovery()) {
            NodeCollectionGroupCdp nodeCollectionGroupCdp = new NodeCollectionGroupCdp(m_linkdConfig.getCdpRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getCdpPriority(), m_queryMgr, m_locationAwareSnmpClient, m_cdpTopologyService);
            nodeCollectionGroupCdp.setScheduler(m_scheduler);
            nodeCollectionGroupCdp.schedule();
            m_groups.add(nodeCollectionGroupCdp);
            scheduleAndRegisterOnmsTopologyUpdater(m_cdpTopologyUpdater);
        } else {
            m_cdpTopologyService.deletePersistedData();
        }

        if (m_linkdConfig.useLldpDiscovery()) {
            NodeCollectionGroupLldp nodeCollectionGroupLldp = new NodeCollectionGroupLldp(m_linkdConfig.getLldpRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getLldpPriority(), m_queryMgr, m_locationAwareSnmpClient, m_lldpTopologyService);
            nodeCollectionGroupLldp.setScheduler(m_scheduler);
            nodeCollectionGroupLldp.schedule();
            m_groups.add(nodeCollectionGroupLldp);
            scheduleAndRegisterOnmsTopologyUpdater(m_lldpTopologyUpdater);
       } else {
            m_lldpTopologyService.deletePersistedData();
        }

        if (m_linkdConfig.useIsisDiscovery()) {
            NodeCollectionGroupIsis nodeCollectionGroupIsis = new NodeCollectionGroupIsis(m_linkdConfig.getIsisRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getIsisPriority(), m_queryMgr, m_locationAwareSnmpClient, m_isisTopologyService);
            nodeCollectionGroupIsis.setScheduler(m_scheduler);
            nodeCollectionGroupIsis.schedule();
            m_groups.add(nodeCollectionGroupIsis);
            scheduleAndRegisterOnmsTopologyUpdater(m_isisTopologyUpdater);
        } else {
            m_isisTopologyService.deletePersistedData();
        }
        
        if (m_linkdConfig.useOspfDiscovery()) {
            NodeCollectionGroupOspf nodeCollectionGroupOspf = new NodeCollectionGroupOspf(m_linkdConfig.getOspfRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getOspfPriority(), m_queryMgr, m_locationAwareSnmpClient, m_ospfTopologyService);
            nodeCollectionGroupOspf.setScheduler(m_scheduler);
            nodeCollectionGroupOspf.schedule();
            m_groups.add(nodeCollectionGroupOspf);
            scheduleAndRegisterOnmsTopologyUpdater(m_ospfTopologyUpdater);
            scheduleAndRegisterOnmsTopologyUpdater(m_ospfAreaTopologyUpdater);
        } else {
            m_ospfTopologyService.deletePersistedData();
        }

        if (m_linkdConfig.useBridgeDiscovery()) {
            NodeCollectionGroupIpNetToMedia nodeCollectionGroupIpNetToMedia = new NodeCollectionGroupIpNetToMedia(m_linkdConfig.getBridgeRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getBridgePriority(), m_queryMgr, m_locationAwareSnmpClient, m_ipNetToMediaTopologyService);
            nodeCollectionGroupIpNetToMedia.setScheduler(m_scheduler);
            nodeCollectionGroupIpNetToMedia.schedule();
            m_groups.add(nodeCollectionGroupIpNetToMedia);
            NodeCollectionGroupBridge nodeCollectionGroupBridge = new NodeCollectionGroupBridge(m_linkdConfig.getBridgeRescanInterval(), m_linkdConfig.getInitialSleepTime(), m_executor, m_linkdConfig.getBridgePriority(), m_queryMgr, m_locationAwareSnmpClient, m_bridgeTopologyService, m_linkdConfig.getMaxBft(), m_linkdConfig.disableBridgeVlanDiscovery());
            nodeCollectionGroupBridge.setScheduler(m_scheduler);
            nodeCollectionGroupBridge.schedule();
            m_groups.add(nodeCollectionGroupBridge);
            scheduleDiscoveryBridgeDomain();
            scheduleAndRegisterOnmsTopologyUpdater(m_bridgeTopologyUpdater);
        } else {
            m_bridgeTopologyService.deletePersistedData();
        }
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
     * <p>
     * onStart
     * </p>
     */
    protected synchronized void onStart() {

        // start the scheduler
        //
        m_scheduler.start();
        
        m_executor.start();

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
        m_executor.stop();
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
        m_scheduler.resume();
        m_executor.resume();
    }

    public boolean execSingleSnmpCollection(final int nodeId) {
        final Node node = m_queryMgr.getSnmpNode(nodeId);
        if (node == null) {
            return false;
        }
        for (SchedulableNodeCollectorGroup group: m_groups) {
            m_executor.addPriorityReadyRunnable(group.getNodeCollector(node, 0));
        }
        return true;
    }

    public boolean runSingleSnmpCollection(final String nodeId, String proto) {
        final Node node = m_queryMgr.getSnmpNode(nodeId);
        if (node == null) {
            return false;
        }
        boolean runned = false;
        for (SchedulableNodeCollectorGroup group: m_groups) {
            if (group.getProtocolSupported().name().equalsIgnoreCase(proto)) {
                group.getNodeCollector(node, 0).collect();
                runned = true;
            }
        }
        return runned;
    }

    public boolean runSingleSnmpCollection(final int nodeId) {
        final Node node = m_queryMgr.getSnmpNode(nodeId);
        if (node == null) {
            return false;
        }
        for (SchedulableNodeCollectorGroup group: m_groups) {
            group.getNodeCollector(node, 0).collect();
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

        case OSPFAREA:
            if (m_linkdConfig.useOspfDiscovery()) {
                m_ospfAreaTopologyUpdater.forceRun();
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

        case NETWORKROUTER:
            m_networkRouterTopologyUpdater.forceRun();
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

            case OSPFAREA:
                if (m_linkdConfig.useOspfDiscovery()) {
                    m_ospfAreaTopologyUpdater.runSchedulable();
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

            case NETWORKROUTER:
                m_networkRouterTopologyUpdater.runSchedulable();
                break;

            default:
                break;
        }
    }

    public void addNode() {
        m_queryMgr.updatesAvailable();
    }

    void deleteNode(int nodeid) {
        LOG.info("deleteNode: deleting LinkableNode for node {}",
                        nodeid);
        m_bridgeTopologyService.delete(nodeid);
        m_cdpTopologyService.delete(nodeid);
        m_isisTopologyService.delete(nodeid);
        m_lldpTopologyService.delete(nodeid);
        m_ospfTopologyService.delete(nodeid);
        m_ipNetToMediaTopologyService.delete(nodeid);
        
        m_queryMgr.updatesAvailable();

    }

    void suspendNodeCollection(final int nodeid) {
        LOG.info("suspendNodeCollection: suspend collection LinkableNode for node {}",
                        nodeid);
        m_groups.forEach(g -> g.suspend(nodeid));
    }

    void wakeUpNodeCollection(int nodeid) {
        LOG.info("wakeUpNodeCollection: wakeUp collection LinkableNode for node {}",
                nodeid);
        m_groups.forEach(g -> g.wakeUp(nodeid));
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
    public NetworkRouterTopologyUpdater getNetworkRouterTopologyUpdater() {
        return m_networkRouterTopologyUpdater;
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
    public OspfAreaOnmsTopologyUpdater getOspfAreaTopologyUpdater() {
        return m_ospfAreaTopologyUpdater;
    }

    @Override
    public void reload() {
        LOG.info("reload: reload enlinkd daemon service");

        m_groups.forEach(Schedulable::unschedule);
        m_groups.clear();

        if (m_ospfTopologyUpdater.isRegistered()) {
            m_ospfTopologyUpdater.unschedule();
            m_ospfTopologyUpdater.unregister();
            m_ospfTopologyUpdater = OspfOnmsTopologyUpdater.clone(m_ospfTopologyUpdater);
        }

        if (m_ospfAreaTopologyUpdater.isRegistered()) {
                m_ospfAreaTopologyUpdater.unschedule();
                m_ospfAreaTopologyUpdater.unregister();
                m_ospfAreaTopologyUpdater = OspfAreaOnmsTopologyUpdater.clone(m_ospfAreaTopologyUpdater);
        }

        if (m_lldpTopologyUpdater.isRegistered()) {
            m_lldpTopologyUpdater.unschedule();
            m_lldpTopologyUpdater.unregister();
            m_lldpTopologyUpdater = LldpOnmsTopologyUpdater.clone(m_lldpTopologyUpdater);
        }

        if (m_isisTopologyUpdater.isRegistered()) {
            m_isisTopologyUpdater.unschedule();
            m_isisTopologyUpdater.unregister();
            m_isisTopologyUpdater = IsisOnmsTopologyUpdater.clone(m_isisTopologyUpdater);
        }

        if (m_cdpTopologyUpdater.isRegistered()) {
            m_cdpTopologyUpdater.unschedule();
            m_cdpTopologyUpdater.unregister();
            m_cdpTopologyUpdater = CdpOnmsTopologyUpdater.clone(m_cdpTopologyUpdater);
        }
        
        if (m_bridgeTopologyUpdater.isRegistered()) {
            m_bridgeTopologyUpdater.unschedule();
            m_bridgeTopologyUpdater.unregister();
            m_bridgeTopologyUpdater = BridgeOnmsTopologyUpdater.clone(m_bridgeTopologyUpdater);
            m_discoveryBridgeDomains.unschedule();
            m_discoveryBridgeDomains = DiscoveryBridgeDomains.clone(m_discoveryBridgeDomains);
        }

        schedule(false);
    }

    @Override
    public boolean reloadConfig() {
        LOG.info("reloadConfig: reload enlinkd configuration file and daemon service");
        try {
            m_linkdConfig.reload();
        } catch (IOException e) {
            LOG.error("reloadConfig: cannot reload config: {}", e.getMessage());
            return false;
        }
        reload();
        return true;
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
