/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.snmp.CdpCacheTable;
import org.opennms.netmgt.linkd.snmp.CdpGlobalGroup;
import org.opennms.netmgt.linkd.snmp.CdpInterfaceTable;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTable;
import org.opennms.netmgt.linkd.snmp.IsIsSystemObjectGroup;
import org.opennms.netmgt.linkd.snmp.IsisCircTable;
import org.opennms.netmgt.linkd.snmp.IsisISAdjTable;
import org.opennms.netmgt.linkd.snmp.LldpLocTable;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.linkd.snmp.MtxrWlRtabTable;
import org.opennms.netmgt.linkd.snmp.OspfGeneralGroup;
import org.opennms.netmgt.linkd.snmp.OspfNbrTable;
import org.opennms.netmgt.linkd.snmp.SnmpTable;
import org.opennms.netmgt.linkd.snmp.VlanTable;
import org.opennms.netmgt.linkd.snmp.VlanTableBasic;
import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class SnmpCollection implements ReadyRunnable {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollection.class);

    /**
     * The SnmpPeer object used to communicate via SNMP with the remote host.
     */
    private SnmpAgentConfig m_agentConfig;

    public SnmpAgentConfig getAgentConfig() {
		return m_agentConfig;
	}

	public void setAgentConfig(SnmpAgentConfig agentConfig) {
		m_agentConfig = agentConfig;
	}

	/**
     * The node ID of the system used to collect the SNMP information
     */
    private final int m_nodeid;

    /**
     * The IP address used to collect the SNMP information
     */
    private final InetAddress m_address;

    /**
     * The Class used to collect the VLAN IDs
     */
    private String m_vlanClass = null;

    /**
     * The Class used to collect the ipRoute IDs
     */
    private String m_ipRouteClass = null;

    /**
     * A boolean used to decide if you can collect VLAN Table and Bridge Data
     */
    private boolean m_collectVlan = false;

    /**
     * A boolean used to decide if you can collect Route Table
     */
    private boolean m_collectIpRoute = false;

    /**
     * A boolean used to decide if you can collect STP Base Info
     */
    private boolean m_collectStp = false;

    /**
     * A boolean used to decide if you can collect Bridge Forwarding Table
     */
    private boolean m_collectBridge = false;

    /**
     * A boolean used to decide if you can collect Ip Net To Media Table
     */
    private boolean m_collectIpNetToMedia = false;

    /**
     * A boolean used to decide if you can collect Wireless R Table
     */
    private boolean m_collectWifi = false;

    /**
     * A boolean used to decide if you can collect CDP Table
     */
    private boolean m_collectCdp = false;

    /**
     * A boolean used to decide if you can collect LLDP Table
     */
    private boolean m_collectLldp = false;

    /**
     * A boolean used to decide if you can collect OSPF Table
     */
    private boolean m_collectOspf = false;

    /**
     * A boolean used to decide if you can collect IS-IS Table
     */
    private boolean m_collectIsIs = false;

    public MtxrWlRtabTable m_mtxrWlRtabTable;
    
    public LldpLocalGroup m_lldpLocalGroup;
    public LldpLocTable m_lldpLocTable;
    public LldpRemTable m_lldpRemTable;
    
    public OspfGeneralGroup m_ospfGeneralGroup;
    public OspfNbrTable m_ospfNbrTable;
    
    public IsIsSystemObjectGroup m_isisSystemObjectGroup;
    public IsisISAdjTable m_isisISAdjTable;
    public IsisCircTable m_isisCircTable;
    /**
     * The ipnettomedia table information
     */
    public IpNetToMediaTable m_ipNetToMedia;

    /**
     * The ipRoute table information
     */
    public SnmpTable<SnmpStore> m_ipRoute;

    /**
     * The CdpCache table information
     */
    public CdpGlobalGroup m_cdpGlobalGroup;
    public CdpCacheTable m_cdpCache;
    public CdpInterfaceTable m_cdpInterface;

    /**
     * The VLAN Table information
     */
    public SnmpTable<SnmpStore> m_vlanTable;

    /**
     * The list of VLAN SNMP collection object
     */
    public final Map<OnmsVlan, SnmpVlanCollection> m_snmpVlanCollection = new HashMap<OnmsVlan, SnmpVlanCollection>();

    /**
     * The scheduler object
     */
    private Scheduler m_scheduler;

    /**
     * The interval, default value 30 minutes
     */
    private long poll_interval = 1800000;

    /**
     * The initial sleep time, default value 5 minutes
     */
    private long initial_sleep_time = 600000;

    private boolean suspendCollection = false;

    private boolean runned = false;

    private String packageName;

    private final Linkd m_linkd;

    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param nodeid
     * @param config
     *            The SnmpPeer object to collect from.
     */
    public SnmpCollection(final Linkd linkd, final int nodeid,
            final SnmpAgentConfig config) {
        m_linkd = linkd;
        m_agentConfig = config;
        m_nodeid = nodeid;
        m_address = m_agentConfig.getEffectiveAddress();
    }

    boolean hasMtxrWlRtabTable() {
        return (m_mtxrWlRtabTable != null && !m_mtxrWlRtabTable.failed() && !m_mtxrWlRtabTable.isEmpty());
    }

    MtxrWlRtabTable getMtxrWlRtabTable() {
        return m_mtxrWlRtabTable;
    }

    boolean hasIsIsSysObjectGroup() {
        return (m_isisSystemObjectGroup != null && !m_isisSystemObjectGroup.failed() && m_isisSystemObjectGroup.getIsisSysId() != null);
    }
    
    IsIsSystemObjectGroup getIsIsSystemObjectGroup() {
        return m_isisSystemObjectGroup;
    }

    boolean hasOspfGeneralGroup() {
        return (m_ospfGeneralGroup != null && !m_ospfGeneralGroup.failed() && m_ospfGeneralGroup.getOspfRouterId() != null);        
    }

    boolean hasIsisCircTable() {
        return (m_isisCircTable != null && !m_isisCircTable.failed() && !m_isisCircTable.isEmpty());
    }
    
    IsisCircTable getIsisCircTable() {
        return m_isisCircTable;
    }
    
    boolean hasIsisISAdjTable() {
        return (m_isisISAdjTable != null && !m_isisISAdjTable.failed() && !m_isisISAdjTable.isEmpty());
    }
    
    IsisISAdjTable getIsisISAdjTable() {
        return m_isisISAdjTable;
    }
    
    OspfGeneralGroup getOspfGeneralGroup() {
        return m_ospfGeneralGroup; 
    }
    
    public boolean hasOspfNbrTable() {
        return (m_ospfNbrTable != null && !m_ospfNbrTable.failed() && !m_ospfNbrTable.isEmpty());
    }

    OspfNbrTable getOspfNbrTable() {
        return m_ospfNbrTable;    
    }
    
    boolean hasLldpLocalGroup() {
        return (m_lldpLocalGroup != null && !m_lldpLocalGroup.failed() && m_lldpLocalGroup.getLldpLocChassisid() != null);
    }

    LldpLocalGroup getLldpLocalGroup() {
        return m_lldpLocalGroup;
    }

    boolean hasLldpRemTable() {
        return (m_lldpRemTable != null && !m_lldpRemTable.failed() && !m_lldpRemTable.isEmpty());
    }

    LldpRemTable getLldpRemTable() {
        return m_lldpRemTable;
    }

    boolean hasLldpLocTable() {
        return (m_lldpLocTable != null && !m_lldpLocTable.failed() && !m_lldpLocTable.isEmpty());
    }

    LldpLocTable getLldpLocTable() {
        return m_lldpLocTable;
    }

    /**
     * Returns true if the IP net to media table was collected.
     */
    boolean hasIpNetToMediaTable() {
        return (m_ipNetToMedia != null && !m_ipNetToMedia.failed() && !m_ipNetToMedia.isEmpty());
    }

    /**
     * Returns the collected IP net to media table.
     */
    IpNetToMediaTable getIpNetToMediaTable() {
        return m_ipNetToMedia;
    }

    /**
     * Returns true if the IP route table was collected.
     */
    boolean hasRouteTable() {
        return (m_ipRoute != null && !m_ipRoute.failed() && !m_ipRoute.isEmpty());
    }

    /**
     * Returns the collected IP route table.
     */
    SnmpTable<SnmpStore> getIpRouteTable() {
        return m_ipRoute;
    }

    /**
     * Returns true if the CDP Global Group table was collected.
     */
    boolean hasCdpGlobalGroup() {
        return (m_cdpGlobalGroup != null && !m_cdpGlobalGroup.failed() && m_cdpGlobalGroup.getCdpDeviceId() != null);
    }

    CdpGlobalGroup getCdpGlobalGroup() {
    	return m_cdpGlobalGroup;
    }
    
    /**
     * Returns true if the CDP Cache table was collected.
     */
    boolean hasCdpCacheTable() {
        return (m_cdpCache != null && !m_cdpCache.failed() && !m_cdpCache.isEmpty());
    }
    
    /**
     * Returns the collected IP route table.
     */
    CdpCacheTable getCdpCacheTable() {
        return m_cdpCache;
    }

    boolean hasCdpInterfaceTable() {
        return (m_cdpInterface != null && !m_cdpInterface.failed() && !m_cdpInterface.isEmpty());
        
    }

    CdpInterfaceTable getCdpInterfaceTable() {
        return m_cdpInterface;
    }

    /**
     * Returns true if the VLAN table was collected.
     */
    boolean hasVlanTable() {
        return (m_vlanTable != null && !m_vlanTable.failed() && !m_vlanTable.isEmpty());
    }

    /**
     * Returns the collected VLAN table.
     */
    SnmpTable<SnmpStore> getVlanTable() {
        return m_vlanTable;
    }


    Map<OnmsVlan, SnmpVlanCollection> getSnmpVlanCollections() {
        return m_snmpVlanCollection;
    }

    /**
     * <p>
     * Performs the collection for the targeted IP address. The success or
     * failure of the collection should be tested via the <code>failed</code>
     * method.
     * </p>
     * <p>
     * No synchronization is performed, so if this is used in a separate
     * thread context synchronization must be added.
     * </p>
     */
    private void sendSuspendedEvent() {
        sendEvent(new EventBuilder("uei.opennms.org/internal/linkd/nodeLinkDiscoverySuspended",
                                   "Linkd"));
    }

    private void sendStartedEvent() {
        sendEvent(new EventBuilder("uei.opennms.org/internal/linkd/nodeLinkDiscoveryStarted",
                                                "Linkd"));
    }

    private void sendCompletedEvent() {
        sendEvent(new EventBuilder("uei.opennms.org/internal/linkd/nodeLinkDiscoveryCompleted",
                                           "Linkd"));
    }

    private void sendEvent(EventBuilder builder) {
        builder.setNodeid(m_nodeid);
        builder.setInterface(m_address);
        builder.addParam("runnable", "snmpCollection/"+getPackageName());
        m_linkd.getEventForwarder().sendNow(builder.getEvent());
    }
    
    @Override
    public void run() {
        runned = true;
        if (suspendCollection) {
            sendSuspendedEvent();
            LOG.debug("run: address: {} Suspended!", str(m_address));
        } else {
            sendStartedEvent();
            runCollection();
            sendCompletedEvent();
        }
    }
    
    private static class TrackerBuilder {
    	private final CollectionTracker[] OF_TRACKERS = new CollectionTracker[0];
    	private String m_msg = null;
    	private List<CollectionTracker> m_trackerList = new ArrayList<CollectionTracker>();
    	
    	public void add(String label, CollectionTracker... trackers) {
    		if (m_msg == null) {
    			m_msg = label;
    		} else {
    			m_msg += "/" + label;
    		}
    		
    		m_trackerList.addAll(Arrays.asList(trackers));
    	}
    	
    	public String getMessage() { return m_msg; }
    	public CollectionTracker[] getTrackers() { return m_trackerList.toArray(OF_TRACKERS); }
    	public boolean isEmpty() { return m_trackerList.isEmpty(); }
    }

    private void runCollection() {

        final String hostAddress = str(m_address);

        m_ipNetToMedia = new IpNetToMediaTable(m_address);

        m_cdpGlobalGroup = new CdpGlobalGroup(m_address);

        m_cdpCache = new CdpCacheTable(m_address);

        m_cdpInterface = new CdpInterfaceTable(m_address);

        m_lldpLocalGroup = new LldpLocalGroup(m_address);

        m_lldpRemTable = new LldpRemTable(m_address);

        m_lldpLocTable = new LldpLocTable(m_address);
        
        m_ospfGeneralGroup = new OspfGeneralGroup(m_address);
        
        m_ospfNbrTable = new OspfNbrTable(m_address);
        
        m_isisSystemObjectGroup = new IsIsSystemObjectGroup(m_address);
        
        m_isisCircTable = new IsisCircTable(m_address);
        
        m_isisISAdjTable = new IsisISAdjTable(m_address);
        
        m_mtxrWlRtabTable = new MtxrWlRtabTable(m_address);

        if (m_collectIpRoute) {
        	m_ipRoute = createClass(m_ipRouteClass, m_address);
        }

	if (m_collectVlan) {
		m_vlanTable = createClass(m_vlanClass, m_address);
	}
		
	LOG.debug("run: address {} package {}: collecting on agent: {}", str(m_address), getPackageName(), m_agentConfig);

        LOG.info("run: address {} package {}: collectIpNetToMedia: {}",str(m_address), getPackageName(),m_collectIpNetToMedia);
        LOG.info("run: address {} package {}: collectOspf: {}",str(m_address), getPackageName(),m_collectOspf);
        LOG.info("run: address {} package {}: collectIsIs: {}",str(m_address), getPackageName(),m_collectIsIs);
        LOG.info("run: address {} package {}: collectLldp: {}",str(m_address), getPackageName(),m_collectLldp);
        LOG.info("run: address {} package {}: collectIpRoute: {}",str(m_address), getPackageName(),m_collectIpRoute);
        LOG.info("run: address {} package {}: collectCdp: {}",str(m_address), getPackageName(),m_collectCdp);
	LOG.info("run: address {} package {}: collectVlan: {}",str(m_address), getPackageName(),m_collectVlan);
        LOG.info("run: address {} package {}: collectWifi: {}",str(m_address), getPackageName(),m_collectWifi);

        SnmpWalker walker = null;

        TrackerBuilder bldr = new TrackerBuilder();
        if (m_collectIpNetToMedia) {
        	bldr.add("ipNetToMediaTable", m_ipNetToMedia);
        }
        if (m_collectOspf) {
        	bldr.add("ospfGeneralGroup/ospfNbrTable", m_ospfGeneralGroup, m_ospfNbrTable);
        }
        if (m_collectIsIs) {
            bldr.add("isisSystemObjectGroup/isisCircTable/isisISAdjTable", m_isisSystemObjectGroup, m_isisCircTable,m_isisISAdjTable);
        }
        if (m_collectLldp) {
        	bldr.add("lldpLocalGroup/lldpLocTable/lldpRemTable", m_lldpLocalGroup, m_lldpLocTable, m_lldpRemTable);
        }
        if (m_collectIpRoute && m_ipRoute != null) {
        	bldr.add("ipRouteTable", m_ipRoute);
        }
        if (m_collectCdp) {
        	bldr.add("cdpGlobalGroup/cdpInterface/cdpCacheTable",m_cdpGlobalGroup,m_cdpInterface,m_cdpCache);
        }
        if (m_collectVlan && m_vlanTable != null) {
        	bldr.add("vlanTable", m_vlanTable);
        }
        if (m_collectWifi && m_mtxrWlRtabTable != null) {
            bldr.add("mtxrWlRtabTable", m_mtxrWlRtabTable);
        }
        
        
        LOG.debug("run: package {}: Collecting {} from {}", getPackageName(),bldr.getMessage(), str(m_agentConfig.getEffectiveAddress()));

        if (!bldr.isEmpty()) {
            walker = SnmpUtils.createWalker(m_agentConfig, bldr.getMessage(), bldr.getTrackers());

            walker.start();

            try {
                walker.waitFor();
            } catch (final InterruptedException e) {
                LOG.error("run: collection interrupted, exiting", e);
                return;
            }
        }
        // Log any failures
        //
        if (m_collectOspf && !this.hasOspfGeneralGroup())
            LOG.info("run: failed to collect ospfGeneralGroup for {}", hostAddress);
        if (m_collectOspf && !this.hasOspfNbrTable())
            LOG.info("run: failed to collect ospfNbrTable for {}", hostAddress);
        if (m_collectIsIs && !this.hasIsIsSysObjectGroup())
            LOG.info("run: failed to collect IsIsSysObjectGroup for {}", hostAddress);
        if (m_collectIsIs && !this.hasIsisCircTable())
            LOG.info("run: failed to collect IsisCircTable for {}", hostAddress);
        if (m_collectIsIs && !this.hasIsisISAdjTable())
            LOG.info("run: failed to collect IsisIsAdjTable for {}", hostAddress);
        if (m_collectLldp && !this.hasLldpLocalGroup())
            LOG.info("run: failed to collect lldpLocalGroup for {}", hostAddress);
        if (m_collectLldp && !this.hasLldpLocTable())
            LOG.info("run: failed to collect lldpLocTable for {}", hostAddress);
        if (m_collectLldp && !this.hasLldpRemTable())
            LOG.info("run: failed to collect lldpRemTable for {}", hostAddress);
        if (m_collectIpNetToMedia && !this.hasIpNetToMediaTable())
            LOG.info("run: failed to collect ipNetToMediaTable for {}", hostAddress);
        if (m_collectIpRoute && m_ipRoute != null && !this.hasRouteTable())
            LOG.info("run: failed to collect ipRouteTable for {}", hostAddress);
        if (m_collectCdp && !this.hasCdpGlobalGroup())
            LOG.info("run: failed to collect cdpGlobalGroup for {}", hostAddress);
        if (m_collectCdp && !this.hasCdpInterfaceTable())
            LOG.info("run: failed to collect cdpInterfaceTable for []", hostAddress);
        if (m_collectCdp && !this.hasCdpCacheTable())
            LOG.info("run: failed to collect cdpCacheTable for []", hostAddress);
        if (m_collectVlan && m_vlanTable != null && !this.hasVlanTable())
            LOG.info("run: failed to collect VLAN for {}", hostAddress);
        if (m_collectWifi && m_mtxrWlRtabTable != null && !this.hasMtxrWlRtabTable())
            LOG.info("run: failed to collect Wifi for {}", hostAddress);
        
        

        if (this.hasVlanTable()) {
        	VlanTableBasic basicvlans = (VlanTableBasic) m_vlanTable;
                LOG.debug("run: start snmp collection for {} VLAN entries", basicvlans.size());
        	for (OnmsVlan vlan: basicvlans.getVlansForSnmpCollection()) {
                String community = m_agentConfig.getReadCommunity();
                Integer vlanindex = vlan.getVlanId();
                LOG.debug("run: peer community: {} with VLAN {}", community, vlanindex);
                if (vlanindex != 1)
                    m_agentConfig.setReadCommunity(community + "@"
                            + vlanindex);
                runAndSaveSnmpVlanCollection(vlan);
                m_agentConfig.setReadCommunity(community);
            }
        } else {
            runAndSaveSnmpVlanCollection(new OnmsVlan(VlanTable.DEFAULT_VLAN_INDEX, VlanTable.DEFAULT_VLAN_NAME, VlanTable.DEFAULT_VLAN_STATUS));
        }
        // update info in linkd used correctly by {@link DiscoveryLink}
        LOG.debug("run: saving collection into database for {}", str(m_agentConfig.getEffectiveAddress()));

        m_linkd.updateNodeSnmpCollection(this);
        // clean memory
        // first make everything clean
        m_ipNetToMedia = null;
        m_ipRoute = null;
        m_cdpGlobalGroup = null;
        m_cdpCache = null;
        m_cdpInterface = null;
        m_vlanTable = null;
        m_lldpLocalGroup = null;
        m_lldpLocTable = null;
        m_lldpRemTable = null;
        m_ospfGeneralGroup = null;
        m_ospfNbrTable = null;
        m_isisSystemObjectGroup = null;
        m_isisCircTable = null;
        m_isisISAdjTable = null;
        m_mtxrWlRtabTable = null;

        m_snmpVlanCollection.clear();
    }

	@SuppressWarnings("unchecked")
	private SnmpTable<SnmpStore> createClass(String className, InetAddress address) {
		SnmpTable<SnmpStore> vlanTable = null;
		Class<SnmpTable<SnmpStore>> getter = null;
		try {
		    getter = (Class<SnmpTable<SnmpStore>>) Class.forName(className);
		} catch (ClassNotFoundException e) {
		    LOG.warn("run: {} class not found", className, e);
		}
		Class<?>[] classes = { InetAddress.class };
		Constructor<SnmpTable<SnmpStore>> constr = null;
		try {
		    constr = getter.getConstructor(classes);
		} catch (NoSuchMethodException e) {
		    LOG.warn("run: {} class has no such method", className, e);
		} catch (SecurityException s) {
		    LOG.warn("run: {} class security violation", className, s);
		}
		Object[] argum = { address };
		try {
			vlanTable = (SnmpTable<SnmpStore>) constr.newInstance(argum);
		} catch (Throwable e) {
		    LOG.warn("run: unable to instantiate class {}", className, e);
		}
		return vlanTable;
	}

    private void runAndSaveSnmpVlanCollection(OnmsVlan vlan) {
        LOG.info("runAndSaveSnmpVlanCollection: address {} package {}: collectStp: {}",str(m_address), getPackageName(),m_collectStp);
        LOG.info("runAndSaveSnmpVlanCollection: address {} package {}: collectBridge: {}",str(m_address), getPackageName(),m_collectBridge);
        SnmpVlanCollection snmpvlancollection = new SnmpVlanCollection(
                                                                       m_agentConfig,
                                                                       m_collectStp,
                                                                       m_collectBridge);
        snmpvlancollection.setPackageName(getPackageName());
        snmpvlancollection.run();

        if (snmpvlancollection.failed()) {
            LOG.debug("runAndSaveSnmpVlanCollection: no bridge info found for {}", m_agentConfig);
        } else {
            LOG.debug("runAndSaveSnmpVlanCollection: adding bridge info to snmpcollection, VLAN = {}, SnmpVlanCollection = {}", vlan, snmpvlancollection);
            m_snmpVlanCollection.put(vlan, snmpvlancollection);
        }
    }

    /**
     * <p>
     * getScheduler
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>
     * setScheduler
     * </p>
     * 
     * @param scheduler
     *            a {@link org.opennms.netmgt.linkd.scheduler.Scheduler}
     *            object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>
     * getInitialSleepTime
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getInitialSleepTime() {
        return initial_sleep_time;
    }

    /**
     * <p>
     * setInitialSleepTime
     * </p>
     * 
     * @param initial_sleep_time
     *            The initial_sleep_timeto set.
     */
    public void setInitialSleepTime(long initial_sleep_time) {
        this.initial_sleep_time = initial_sleep_time;
    }

    /**
     * <p>
     * getPollInterval
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getPollInterval() {
        return poll_interval;
    }

    /**
     * <p>
     * setPollInterval
     * </p>
     * 
     * @param interval
     *            a long.
     */
    public void setPollInterval(long interval) {
        this.poll_interval = interval;
    }

    /**
     * <p>
     * schedule
     * </p>
     */
    @Override
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        if (runned)
            m_scheduler.schedule(poll_interval, this);
        else
            m_scheduler.schedule(initial_sleep_time, this);
    }

    /**
     * <p>
     * isReady
     * </p>
     * 
     * @return a boolean.
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * <p>
     * isSuspended
     * </p>
     * 
     * @return Returns the suspendCollection.
     */
    @Override
    public boolean isSuspended() {
        return suspendCollection;
    }

    /**
     * <p>
     * suspend
     * </p>
     */
    @Override
    public void suspend() {
        this.suspendCollection = true;
    }

    /**
     * <p>
     * wakeUp
     * </p>
     */
    @Override
    public void wakeUp() {
    	setAgentConfig(m_linkd.getSnmpAgentConfig(m_address));
        this.suspendCollection = false;
    }

    /**
     * <p>
     * unschedule
     * </p>
     */
    @Override
    public void unschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "rescedule: Cannot schedule a service whose scheduler is set to null");
        if (runned) {
            m_scheduler.unschedule(this, poll_interval);
        } else {
            m_scheduler.unschedule(this, initial_sleep_time);
        }
    }

    public String getIpRouteClass() {
        return m_ipRouteClass;
    }

    public void setIpRouteClass(String className) {
        if (className == null || className.equals(""))
            return;
        m_ipRouteClass = className;
        m_collectIpRoute = true;
    }

    /**
     * <p>
     * getVlanClass
     * </p>
     * 
     * @return Returns the m_vlanClass.
     */
    public String getVlanClass() {
        return m_vlanClass;
    }

    /**
     * <p>
     * setVlanClass
     * </p>
     * 
     * @param className
     *            a {@link java.lang.String} object.
     */
    public void setVlanClass(String className) {
        if (className == null || className.equals(""))
            return;
        m_vlanClass = className;
        m_collectVlan = true;
    }

    /**
     * Returns the target address that the collection occurred for.
     * 
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getTarget() {
        return m_address;
    }

    /**
     * <p>
     * collectVlanTable
     * </p>
     * 
     * @return Returns the m_collectVlanTable.
     */
    public boolean collectVlanTable() {
        return m_collectVlan;
    }

    /**
     * <p>
     * getReadCommunity
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getReadCommunity() {
        return m_agentConfig.getReadCommunity();
    }

    /**
     * <p>
     * getPeer
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getPeer() {
        return m_agentConfig;
    }

    /**
     * <p>
     * getPort
     * </p>
     * 
     * @return a int.
     */
    public int getPort() {
        return m_agentConfig.getPort();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object run) {
        if (run instanceof SnmpCollection) {
            SnmpCollection c = (SnmpCollection) run;
            if (this.getPackageName().equals(c.getPackageName()) && c.getTarget().equals(m_address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * getInfo
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInfo() {
        return "ReadyRunnable SnmpCollection" + " ip=" + str(getTarget())
                + " port=" + getPort() + " community=" + getReadCommunity()
                + " package=" + getPackageName()
                + " initial=" + getInitialSleepTime()
                + " interval=" + getPollInterval()
                + " collectBridge=" + getCollectBridge() 
                + " collectStpNode=" + getCollectStp() 
                + " collectCdp=" + getCollectCdp()
                + " collectIpnetToMedia=" + getCollectIpNetToMedia()
                + " collectIpRoute=" + getCollectIpRoute()
                + " collectLldp=" + getCollectLldp() 
                + " collectOspf=" + getCollectOspf() 
                + " collectIsis=" + getCollectIsIs()
                + " collectWifi=" + getCollectWifi();
    }

    public boolean getCollectLldp() {
        return m_collectLldp;
    }

    public void collectLldp(boolean collectLldpTable) {
        m_collectLldp = collectLldpTable;
    }

    /**
     * <p>
     * getCollectBridgeForwardingTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectBridge() {
        return m_collectBridge;
    }

    /**
     * <p>
     * collectBridgeForwardingTable
     * </p>
     * 
     * @param bridgeForwardingTable
     *            a boolean.
     */
    public void collectIpNetToMedia(boolean collectIpNetToMedia) {
        m_collectIpNetToMedia = collectIpNetToMedia;
    }

    /**
     * <p>
     * getCollectBridgeForwardingTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectIpNetToMedia() {
        return m_collectIpNetToMedia;
    }

    /**
     * <p>
     * collectBridgeForwardingTable
     * </p>
     * 
     * @param bridgeForwardingTable
     *            a boolean.
     */
    public void collectBridge(boolean bridgeForwardingTable) {
        m_collectBridge = bridgeForwardingTable;
    }

    /**
     * <p>
     * getCollectCdpTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectCdp() {
        return m_collectCdp;
    }

    /**
     * <p>
     * collectCdpTable
     * </p>
     * 
     * @param cdpTable
     *            a boolean.
     */
    public void collectCdp(boolean cdpTable) {
        m_collectCdp = cdpTable;
    }

    /**
     * <p>
     * getCollectIpRouteTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectIpRoute() {
        return m_collectIpRoute;
    }

    /**
     * <p>
     * collectIpRouteTable
     * </p>
     * 
     * @param ipRouteTable
     *            a boolean.
     */
    public void collectIpRoute(boolean ipRouteTable) {
        m_collectIpRoute = ipRouteTable;
    }

    /**
     * <p>
     * getCollectStpNode
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectStp() {
        return m_collectStp;
    }

    /**
     * <p>
     * collectStpNode
     * </p>
     * 
     * @param stpNode
     *            a boolean.
     */
    public void collectStp(boolean stpNode) {
        m_collectStp = stpNode;
    }

    /**
     * <p>
     * Getter for the field <code>packageName</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getPackageName() {
        return packageName;
    }

    /** {@inheritDoc} */
    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void collectOspf(boolean collectOspfTable) {        
        m_collectOspf = collectOspfTable;
    }

    public boolean getCollectOspf() {
       return m_collectOspf;
    }
    
    public void collectIsIs(boolean collectIsIs) {        
        m_collectIsIs = collectIsIs;
    }

    public boolean getCollectIsIs() {
       return m_collectIsIs;
    }

    public void collectWifi(boolean collectWifi) {        
        m_collectWifi = collectWifi;
    }

    public boolean getCollectWifi() {
       return m_collectWifi;
    }

}
