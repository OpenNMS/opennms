/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2006-2011 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.capsd.snmp.SnmpStore;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.snmp.CdpCacheTable;
import org.opennms.netmgt.linkd.snmp.CiscoVlanTable;
import org.opennms.netmgt.linkd.snmp.IntelVlanTable;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTable;
import org.opennms.netmgt.linkd.snmp.LldpLocTable;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.linkd.snmp.VlanCollectorEntry;
import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class SnmpCollection implements ReadyRunnable {

    /**
     * The VLAN string to define VLAN name when collection is made for all
     * VLAN
     */
    public final static String TRUNK_VLAN_NAME = "AllVlans";

    /**
     * The VLAN string to define VLAN index when collection is made for all
     * VLAN
     */
    public final static int TRUNK_VLAN_INDEX = 0;

    /**
     * The VLAN string to define default VLAN name
     */
    public final static String DEFAULT_VLAN_NAME = "default";

    /**
     * The VLAN string to define default VLAN index
     */
    public final static int DEFAULT_VLAN_INDEX = 1;

    /**
     * The SnmpPeer object used to communicate via SNMP with the remote host.
     */
    private final SnmpAgentConfig m_agentConfig;

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
    private boolean m_collectVlanTable = false;

    /**
     * A boolean used to decide if you can collect Route Table
     */
    private boolean m_collectIpRouteTable = false;

    /**
     * A boolean used to decide if you can collect STP Base Info
     */
    private boolean m_collectStpNode = false;

    /**
     * A boolean used to decide if save StpNode Table
     */
    private boolean m_saveStpNodeTable = false;

    /**
     * A boolean used to decide if save IpRouteTable
     */
    private boolean m_saveIpRouteTable = false;

    /**
     * A boolean used to decide if you save StpInterfaceTable
     */
    private boolean m_saveStpInterfaceTable = false;

    /**
     * A boolean used to decide if you can collect STP Table
     */
    private boolean m_collectStpTable = false;

    /**
     * A boolean used to decide if you can collect Bridge Forwarding Table
     */
    private boolean m_collectBridgeForwardingTable = false;

    /**
     * A boolean used to decide if you can collect CDP Table
     */
    private boolean m_collectCdpTable = false;

    /**
     * A boolean used to decide if you can collect LLDP Table
     */
    private boolean m_collectLldpTable = false;

    public LldpLocalGroup m_lldpLocalGroup;
    public LldpLocTable m_lldpLocTable;
    public LldpRemTable m_lldpRemTable;
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
    public CdpCacheTable m_CdpCache;

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

    boolean hasLldpLocalGroup() {
        return (m_lldpLocalGroup != null && !m_lldpLocalGroup.failed());
    }

    LldpLocalGroup getLldpLocalGroup() {
        return m_lldpLocalGroup;
    }

    boolean hasLldpRemTable() {
        return (m_lldpRemTable != null && !m_lldpRemTable.failed());
    }

    LldpRemTable getLldpRemTable() {
        return m_lldpRemTable;
    }

    boolean hasLldpLocTable() {
        return (m_lldpLocTable != null && !m_lldpLocTable.failed());
    }

    LldpLocTable getLldpLocTable() {
        return m_lldpLocTable;
    }

    /**
     * Returns true if the IP net to media table was collected.
     */
    boolean hasIpNetToMediaTable() {
        return (m_ipNetToMedia != null && !m_ipNetToMedia.failed());
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
        return (m_ipRoute != null && !m_ipRoute.failed());
    }

    /**
     * Returns the collected IP route table.
     */
    SnmpTable<SnmpStore> getIpRouteTable() {
        return m_ipRoute;
    }

    /**
     * Returns true if the CDP Cache table was collected.
     */
    boolean hasCdpCacheTable() {
        return (m_CdpCache != null && !m_CdpCache.failed());
    }

    /**
     * Returns the collected IP route table.
     */
    CdpCacheTable getCdpCacheTable() {
        return m_CdpCache;
    }

    /**
     * Returns true if the VLAN table was collected.
     */
    boolean hasVlanTable() {
        return (m_vlanTable != null && !m_vlanTable.failed());
    }

    /**
     * Returns the collected VLAN table.
     */
    SnmpTable<SnmpStore> getVlanTable() {
        return m_vlanTable;
    }

    /**
     * Returns the VLAN name from vlanindex.
     * 
     * @param m_vlan
     *            a int.
     * @return a {@link java.lang.String} object.
     */
    public String getVlanName(int m_vlan) {
        if (this.hasVlanTable()) {
            for (final SnmpStore ent : this.getVlanTable()) {
                int vlanIndex = ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
                if (vlanIndex == m_vlan) {
                    return ent.getDisplayString(VlanCollectorEntry.VLAN_NAME);
                }
            }
        }
        return null;
    }

    /**
     * Returns the VLAN vlanindex from name.
     * 
     * @param m_vlanname
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public int getVlanIndex(String m_vlanname) {
        if (this.hasVlanTable()) {
            for (final SnmpStore ent : this.getVlanTable()) {
                String vlanName = ent.getDisplayString(VlanCollectorEntry.VLAN_NAME);
                if (vlanName.equals(m_vlanname)) {
                    return ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
                }
            }
        }
        return -1;
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
    @SuppressWarnings("unchecked")
    public void run() {
        if (suspendCollection) {
            LogUtils.debugf(this, "run: address: %s Suspended!",
                            str(m_address));
        } else {
            runCollection();
        }
    }

    private void runCollection() {

        EventBuilder builder = new EventBuilder(
                                                "uei.opennms.org/internal/linkd/nodeLinkDiscoveryStarted",
                                                "Linkd");
        builder.setNodeid(m_nodeid);
        builder.setInterface(m_address);
        m_linkd.getEventForwarder().sendNow(builder.getEvent());

        final String hostAddress = str(m_address);

        m_ipNetToMedia = new IpNetToMediaTable(m_address);

        m_CdpCache = new CdpCacheTable(m_address);

        m_lldpLocalGroup = new LldpLocalGroup(m_address);

        m_lldpRemTable = new LldpRemTable(m_address);

        m_lldpLocTable = new LldpLocTable(m_address);

        LogUtils.debugf(this, "run: collecting : %s", m_agentConfig);

        SnmpWalker walker = null;

        boolean collectIpRouteTable = m_collectIpRouteTable;
        if (collectIpRouteTable) {
            Class<SnmpTable<SnmpStore>> ipRouteGetter = null;
            try {
                ipRouteGetter = (Class<SnmpTable<SnmpStore>>) Class.forName(m_ipRouteClass);
            } catch (ClassNotFoundException e) {
                LogUtils.errorf(this, e, "run: " + m_ipRouteClass
                        + " class not found ");
                collectIpRouteTable = false;
            }

            Class<?>[] classes = { InetAddress.class };
            Constructor<SnmpTable<SnmpStore>> constr = null;
            try {
                constr = ipRouteGetter.getConstructor(classes);
            } catch (Throwable e) {
                LogUtils.errorf(this, e, "run: " + m_ipRouteClass
                        + " unable to get constructor.");
                collectIpRouteTable = false;
            }
            Object[] argum = { m_address };
            try {
                m_ipRoute = (SnmpTable<SnmpStore>) constr.newInstance(argum);
            } catch (Throwable e) {
                LogUtils.errorf(this, e, "run: " + m_ipRouteClass
                        + " unable to invoke class.");
                collectIpRouteTable = false;
            }
        }

        boolean collectVlanTable = m_collectVlanTable;
        if (collectVlanTable) {
            Class<SnmpTable<SnmpStore>> vlanGetter = null;
            try {
                vlanGetter = (Class<SnmpTable<SnmpStore>>) Class.forName(m_vlanClass);
            } catch (ClassNotFoundException e) {
                LogUtils.warnf(this, e, "run: %s class not found",
                               m_vlanClass);
                collectVlanTable = false;
            }
            Class<?>[] classes = { InetAddress.class };
            Constructor<SnmpTable<SnmpStore>> constr = null;
            try {
                constr = vlanGetter.getConstructor(classes);
            } catch (NoSuchMethodException e) {
                LogUtils.warnf(this, e, "run: %s class has no such method",
                               m_vlanClass);
                collectVlanTable = false;
            } catch (SecurityException s) {
                LogUtils.warnf(this, s, "run: %s class security violation",
                               m_vlanClass);
                collectVlanTable = false;
            }
            Object[] argum = { m_address };
            try {
                m_vlanTable = (SnmpTable<SnmpStore>) constr.newInstance(argum);
            } catch (Throwable e) {
                LogUtils.warnf(this, e,
                               "run: unable to instantiate class %s",
                               m_vlanClass);
                collectVlanTable = false;
            }
        }

        String name = null;
        CollectionTracker[] tracker = new CollectionTracker[0];

        if (m_collectLldpTable && collectVlanTable && collectIpRouteTable
                && m_collectCdpTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/ipRouteTable/cdpCacheTable/vlanTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia,
                    m_ipRoute, m_CdpCache, m_vlanTable };
        } else if (m_collectLldpTable && m_collectCdpTable
                && collectIpRouteTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/ipRouteTable/cdpCacheTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia,
                    m_ipRoute, m_CdpCache };
        } else if (m_collectLldpTable && collectVlanTable
                && collectIpRouteTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/ipRouteTable/vlanTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia,
                    m_ipRoute, m_vlanTable };
        } else if (m_collectLldpTable && collectVlanTable
                && m_collectCdpTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/vlanTable/cdpCacheTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia,
                    m_vlanTable, m_CdpCache };
        } else if (collectVlanTable && collectIpRouteTable
                && m_collectCdpTable) {
            name = "ipRouteTable/cdpCacheTable/vlanTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_ipRoute,
                    m_CdpCache, m_vlanTable };
        } else if (m_collectLldpTable && collectIpRouteTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/ipRouteTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia, m_ipRoute };
        } else if (m_collectLldpTable && collectVlanTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/vlanTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia,
                    m_vlanTable };
        } else if (m_collectLldpTable && m_collectCdpTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable/cdpCacheTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia,
                    m_CdpCache };
        } else if (m_collectCdpTable && collectIpRouteTable) {
            name = "ipNetToMediaTable/ipRouteTable/cdpCacheTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_ipRoute,
                    m_CdpCache };
        } else if (collectVlanTable && collectIpRouteTable) {
            name = "ipNetToMediaTable/ipRouteTable/vlanTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_ipRoute,
                    m_vlanTable };
        } else if (collectVlanTable && m_collectCdpTable) {
            name = "ipNetToMediaTable/vlanTable/cdpCacheTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_vlanTable,
                    m_CdpCache };
        } else if (m_collectLldpTable) {
            name = "lldpLocalGroup/lldpLocTable/lldpRemTable/ipNetToMediaTable";
            tracker = new CollectionTracker[] { m_lldpLocalGroup,
                    m_lldpLocTable, m_lldpRemTable, m_ipNetToMedia };
        } else if (collectIpRouteTable) {
            name = "ipNetToMediaTable/ipRouteTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_ipRoute };
        } else if (collectVlanTable) {
            name = "ipNetToMediaTable/vlanTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_vlanTable };
        } else if (m_collectCdpTable) {
            name = "ipNetToMediaTable/cdpCacheTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia, m_CdpCache };
        } else {
            name = "ipNetToMediaTable";
            tracker = new CollectionTracker[] { m_ipNetToMedia };
        }

        // if (name == null) {
        // LogUtils.infof(this,
        // "run: Unable to determine data to collect from %s",
        // str(m_agentConfig.getEffectiveAddress()));
        // return;
        // } else {
        LogUtils.infof(this, "run: Collecting %s from %s", name,
                       str(m_agentConfig.getEffectiveAddress()));
        // }
        walker = SnmpUtils.createWalker(m_agentConfig, name, tracker);

        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            LogUtils.errorf(this, e, "run: collection interrupted, exiting");
            return;
        }

        // Log any failures
        //
        if (m_collectLldpTable && !this.hasLldpLocalGroup())
            LogUtils.infof(this,
                           "run: failed to collect lldpLocalGroup for %s",
                           hostAddress);
        if (m_collectLldpTable && !this.hasLldpLocTable())
            LogUtils.infof(this,
                           "run: failed to collect lldpLocTable for %s",
                           hostAddress);
        if (m_collectLldpTable && !this.hasLldpRemTable())
            LogUtils.infof(this,
                           "run: failed to collect lldpRemTable for %s",
                           hostAddress);
        if (!this.hasIpNetToMediaTable())
            LogUtils.infof(this,
                           "run: failed to collect ipNetToMediaTable for %s",
                           hostAddress);
        if (collectIpRouteTable && !this.hasRouteTable())
            LogUtils.infof(this,
                           "run: failed to collect ipRouteTable for %s",
                           hostAddress);
        if (m_collectCdpTable && !this.hasCdpCacheTable())
            LogUtils.infof(this,
                           "run: failed to collect dpCacheTable for %s",
                           hostAddress);
        if (collectVlanTable && !this.hasVlanTable())
            LogUtils.infof(this, "run: failed to collect VLAN for %s",
                           hostAddress);
        // Schedule SNMP VLAN collection only on VLAN.
        // If it has not VLAN collection no data download is done.

        // OnmsVlan vlan = null;

        if (this.hasVlanTable()) {
            if (!m_vlanClass.equals(CiscoVlanTable.class.getName())
                    && !m_vlanClass.equals(IntelVlanTable.class.getName())) {
                runAndSaveSnmpVlanCollection(new OnmsVlan(
                                                          TRUNK_VLAN_INDEX,
                                                          TRUNK_VLAN_NAME,
                                                          VlanCollectorEntry.VLAN_STATUS_OPERATIONAL));
            } else {
                LogUtils.debugf(this,
                                "run: start collection for %d VLAN entries",
                                getVlanTable().size());
                for (final SnmpStore ent : m_vlanTable) {
                    int vlanindex = ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
                    if (vlanindex == -1) {
                        LogUtils.debugf(this,
                                        "run: found null value for VLAN.");
                        continue;
                    }
                    String vlanname = ent.getDisplayString(VlanCollectorEntry.VLAN_NAME);
                    if (vlanname == null)
                        vlanname = DEFAULT_VLAN_NAME;
                    Integer status = ent.getInt32(VlanCollectorEntry.VLAN_STATUS);

                    if (status == null
                            || status != VlanCollectorEntry.VLAN_STATUS_OPERATIONAL) {
                        LogUtils.infof(this,
                                       "run: skipping VLAN %s: NOT ACTIVE or null",
                                       vlanindex);
                        continue;
                    }

                    String community = m_agentConfig.getReadCommunity();
                    LogUtils.debugf(this,
                                    "run: peer community: %s with VLAN %s",
                                    community, vlanindex);

                    Integer type = ent.getInt32(VlanCollectorEntry.VLAN_TYPE);
                    if (type == null
                            || type != VlanCollectorEntry.VLAN_TYPE_ETHERNET) {
                        LogUtils.infof(this,
                                       "run: skipping VLAN %s NOT ETHERNET TYPE",
                                       vlanindex);
                        continue;
                    }
                    if (vlanindex != 1)
                        m_agentConfig.setReadCommunity(community + "@"
                                + vlanindex);

                    runAndSaveSnmpVlanCollection(new OnmsVlan(vlanindex,
                                                              vlanname,
                                                              status));
                    m_agentConfig.setReadCommunity(community);
                }
            }
        } else {
            runAndSaveSnmpVlanCollection(new OnmsVlan(
                                                      DEFAULT_VLAN_INDEX,
                                                      DEFAULT_VLAN_NAME,
                                                      VlanCollectorEntry.VLAN_STATUS_OPERATIONAL));
        }
        // update info in linkd used correctly by {@link DiscoveryLink}
        LogUtils.debugf(this, "run: saving collection into database for %s",
                        str(m_agentConfig.getEffectiveAddress()));

        m_linkd.updateNodeSnmpCollection(this);
        // clean memory
        // first make everything clean
        m_ipNetToMedia = null;
        m_ipRoute = null;
        m_CdpCache = null;
        m_vlanTable = null;
        m_lldpLocalGroup = null;
        m_lldpLocTable = null;
        m_lldpRemTable = null;
        m_snmpVlanCollection.clear();

        builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoveryCompleted",
                                   "Linkd");
        builder.setNodeid(m_nodeid);
        builder.setInterface(m_address);
        m_linkd.getEventForwarder().sendNow(builder.getEvent());

        // reschedule itself
        reschedule();
        runned = true;
    }

    private void runAndSaveSnmpVlanCollection(OnmsVlan vlan) {
        SnmpVlanCollection snmpvlancollection = new SnmpVlanCollection(
                                                                       m_agentConfig,
                                                                       m_collectStpNode,
                                                                       m_collectStpTable,
                                                                       m_collectBridgeForwardingTable);
        snmpvlancollection.run();

        if (snmpvlancollection.failed()) {
            LogUtils.debugf(this,
                            "runAndSaveSnmpVlanCollection: no bridge info found for %s",
                            m_agentConfig);
        } else {
            LogUtils.debugf(this,
                            "runAndSaveSnmpVlanCollection: adding bridge info to snmpcollection, VLAN = %s, SnmpVlanCollection = %s",
                            vlan, snmpvlancollection);
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
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(initial_sleep_time, this);
    }

    /**
	 * 
	 */
    private void reschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(poll_interval, this);
    }

    /**
     * <p>
     * isReady
     * </p>
     * 
     * @return a boolean.
     */
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
    public boolean isSuspended() {
        return suspendCollection;
    }

    /**
     * <p>
     * suspend
     * </p>
     */
    public void suspend() {
        this.suspendCollection = true;
    }

    /**
     * <p>
     * wakeUp
     * </p>
     */
    public void wakeUp() {
        this.suspendCollection = false;
    }

    /**
     * <p>
     * unschedule
     * </p>
     */
    public void unschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "rescedule: Cannot schedule a service whose scheduler is set to null");
        if (runned) {
            m_scheduler.unschedule(this, poll_interval);
        } else {
            m_scheduler.unschedule(this, poll_interval + initial_sleep_time);
        }
    }

    public String getIpRouteClass() {
        return m_ipRouteClass;
    }

    public void setIpRouteClass(String className) {
        if (className == null || className.equals(""))
            return;
        m_ipRouteClass = className;
        m_collectIpRouteTable = true;
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
        m_collectVlanTable = true;
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
        return m_collectVlanTable;
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
    public boolean equals(ReadyRunnable run) {
        if (run instanceof SnmpCollection
                && this.getPackageName().equals(run.getPackageName())) {
            SnmpCollection c = (SnmpCollection) run;
            if (c.getTarget().equals(m_address) && c.getPort() == getPort()
                    && c.getReadCommunity().equals(getReadCommunity()))
                return true;
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
    public String getInfo() {
        return "ReadyRunnable SnmpCollection" + " ip=" + str(getTarget())
                + " port=" + getPort() + " community=" + getReadCommunity()
                + " package=" + getPackageName()
                + " collectBridgeForwardingTable="
                + getCollectBridgeForwardingTable() + " collectStpNode="
                + getCollectStpNode() + " collectStpTable="
                + getCollectStpTable() + " collectCdpTable="
                + getCollectCdpTable() + " collectIpRouteTable="
                + getCollectIpRouteTable() + " saveIpRouteTable="
                + getSaveIpRouteTable() + " saveStpInterfaceTable="
                + getSaveStpInterfaceTable() + " saveStpNodeTable="
                + getSaveStpNodeTable();

    }

    public boolean getCollectLldpTable() {
        return m_collectLldpTable;
    }

    public void collectLldpTable(boolean collectLldpTable) {
        m_collectLldpTable = collectLldpTable;
    }

    /**
     * <p>
     * getCollectBridgeForwardingTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectBridgeForwardingTable() {
        return m_collectBridgeForwardingTable;
    }

    /**
     * <p>
     * collectBridgeForwardingTable
     * </p>
     * 
     * @param bridgeForwardingTable
     *            a boolean.
     */
    public void collectBridgeForwardingTable(boolean bridgeForwardingTable) {
        m_collectBridgeForwardingTable = bridgeForwardingTable;
    }

    /**
     * <p>
     * getCollectCdpTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectCdpTable() {
        return m_collectCdpTable;
    }

    /**
     * <p>
     * collectCdpTable
     * </p>
     * 
     * @param cdpTable
     *            a boolean.
     */
    public void collectCdpTable(boolean cdpTable) {
        m_collectCdpTable = cdpTable;
    }

    /**
     * <p>
     * getCollectIpRouteTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectIpRouteTable() {
        return m_collectIpRouteTable;
    }

    /**
     * <p>
     * collectIpRouteTable
     * </p>
     * 
     * @param ipRouteTable
     *            a boolean.
     */
    public void collectIpRouteTable(boolean ipRouteTable) {
        m_collectIpRouteTable = ipRouteTable;
    }

    /**
     * <p>
     * getCollectStpNode
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectStpNode() {
        return m_collectStpNode;
    }

    /**
     * <p>
     * collectStpNode
     * </p>
     * 
     * @param stpNode
     *            a boolean.
     */
    public void collectStpNode(boolean stpNode) {
        m_collectStpNode = stpNode;
    }

    /**
     * <p>
     * getCollectStpTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getCollectStpTable() {
        return m_collectStpTable;
    }

    /**
     * <p>
     * collectStpTable
     * </p>
     * 
     * @param stpTable
     *            a boolean.
     */
    public void collectStpTable(boolean stpTable) {
        m_collectStpTable = stpTable;
    }

    /**
     * <p>
     * Getter for the field <code>packageName</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return packageName;
    }

    /** {@inheritDoc} */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * <p>
     * getSaveStpNodeTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getSaveStpNodeTable() {
        return m_saveStpNodeTable;
    }

    /**
     * <p>
     * saveStpNodeTable
     * </p>
     * 
     * @param stpNodeTable
     *            a boolean.
     */
    public void saveStpNodeTable(boolean stpNodeTable) {
        m_saveStpNodeTable = stpNodeTable;
    }

    /**
     * <p>
     * getSaveIpRouteTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getSaveIpRouteTable() {
        return m_saveIpRouteTable;
    }

    /**
     * <p>
     * SaveIpRouteTable
     * </p>
     * 
     * @param ipRouteTable
     *            a boolean.
     */
    public void SaveIpRouteTable(boolean ipRouteTable) {
        m_saveIpRouteTable = ipRouteTable;
    }

    /**
     * <p>
     * getSaveStpInterfaceTable
     * </p>
     * 
     * @return a boolean.
     */
    public boolean getSaveStpInterfaceTable() {
        return m_saveStpInterfaceTable;
    }

    /**
     * <p>
     * saveStpInterfaceTable
     * </p>
     * 
     * @param stpInterfaceTable
     *            a boolean.
     */
    public void saveStpInterfaceTable(boolean stpInterfaceTable) {
        m_saveStpInterfaceTable = stpInterfaceTable;
    }

}
