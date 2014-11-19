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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.topology.AtInterface;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>
 * Linkd class.
 * </p>
 * 
 * @author ranger
 * @version $Id: $
 */
public class Linkd extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Linkd.class);

    /**
     * The log4j category used to log messages.
     */
    protected static final String LOG_PREFIX = "linkd";

    /**
     *  scheduler thread
     */
    private Scheduler m_scheduler;

    /**
     * data handler
     */
    private QueryManager m_queryMgr;

    /**
     * Linkd Configuration
     */
    private LinkdConfig m_linkdConfig;

    /**
     * List Linkable Nodes.
     */
    private List<LinkableNode> m_nodes;

    private Map<String, Map<String, List<AtInterface>>> m_macToAtinterface = new HashMap<String, Map<String, List<AtInterface>>>();

    /**
     * the list of {@link java.net.InetAddress} for which new suspect event is
     * sent
     */
    private Set<InetAddress> m_newSuspectEventsIpAddr = null;

    /**
     * Event handler
     */
    private volatile EventForwarder m_eventForwarder;

    /**
     * <p>getNextHopNet</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getNetwork(InetAddress ipaddress, InetAddress netmask) {
        final byte[] ipAddress = ipaddress.getAddress();
        final byte[] netMask = netmask.getAddress();
        final byte[] netWork = new byte[4];

        for (int i=0;i< 4; i++) {
            netWork[i] = Integer.valueOf(ipAddress[i] & netMask[i]).byteValue();

        }
        return InetAddressUtils.getInetAddress(netWork);
    }


    /**
     * <p>
     * Constructor for Linkd.
     * </p>
     */
    public Linkd() {
        super(LOG_PREFIX);
        m_nodes = new ArrayList<LinkableNode>();
        Assert.notNull(m_nodes);
        m_newSuspectEventsIpAddr = Collections.synchronizedSet(new TreeSet<InetAddress>(new InetAddressComparator()));
        m_newSuspectEventsIpAddr.add(InetAddressUtils.ONE_TWENTY_SEVEN);
        m_newSuspectEventsIpAddr.add(InetAddressUtils.ZEROS);
        Assert.notNull(m_newSuspectEventsIpAddr);
    }

    /**
     * <p>
     * onInit
     * </p>
     */
    @Override
    protected void onInit() {
        BeanUtils.assertAutowiring(this);

        Assert.state(m_eventForwarder != null,
                "must set the eventForwarder property");

        Assert.state(m_queryMgr != null,
                "must set the queryManager property");

        Assert.state(m_linkdConfig != null,
                "must set the linkdConfig property");

        Assert.state(m_scheduler != null,
                "must set the scheduler property");

        m_queryMgr.updateDeletedNodes();

        schedule(m_queryMgr.getSnmpNodeList());

        LOG.info("init: LINKD CONFIGURATION INITIALIZED");
    }

    private void schedule(List<LinkableSnmpNode> nodes) {
        for (final LinkableSnmpNode node : nodes) {
            schedule(node);
        }
    }

    /**
     * This method schedules a {@link SnmpCollection} for node for each
     * package. Also schedule discovery link on package when not still
     * activated.
     * 
     * @param node
     */
    private void schedule(final LinkableSnmpNode node) {

        for (final SnmpCollection snmpcoll : getSnmpCollections(node.getNodeId(),
                                                                node.getSnmpPrimaryIpAddr(),
                                                                node.getSysoid())) {
            if (isDiscoveryLinkScheduled(snmpcoll.getPackageName())) {
                LOG.debug("schedule: package active: {}", snmpcoll.getPackageName());
            } else {
                // schedule discovery link
                final DiscoveryLink discovery = getDiscoveryLink(snmpcoll.getPackageName());
                if (discovery.getScheduler() == null) {
                    discovery.setScheduler(m_scheduler);
                }
                LOG.debug("schedule:. Scheduling {}", discovery.getInfo());
                discovery.schedule();
            }
            
            if (snmpcoll.getScheduler() == null) {
                snmpcoll.setScheduler(m_scheduler);
            }
            
            m_nodes.add(new LinkableNode(node, snmpcoll.getPackageName()));
            LOG.debug("schedule: NodeId: {}. Scheduling {}", node.getNodeId(), snmpcoll.getInfo());
            snmpcoll.schedule();
        }
    }

    private boolean isDiscoveryLinkScheduled(String packageName) {
         synchronized (m_nodes) {
            for (LinkableNode node: m_nodes) {
                if (packageName.equals(node.getPackageName()))
                    return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public DiscoveryLink getDiscoveryLink(final String pkgName) {
        final Package pkg = m_linkdConfig.getPackage(pkgName);

        if (pkg == null)
            return null;

        final DiscoveryLink discoveryLink = new DiscoveryLink();
        discoveryLink.setLinkd(this);
        discoveryLink.setPackageName(pkg.getName());
        discoveryLink.setInitialSleepTime(m_linkdConfig.getInitialSleepTime());

        discoveryLink.setInterval(pkg.hasSnmp_poll_interval() ? pkg.getSnmp_poll_interval()
            : m_linkdConfig.getSnmpPollInterval());
        discoveryLink.setDiscoveryInterval(pkg.hasDiscovery_link_interval() ? pkg.getDiscovery_link_interval()
            : m_linkdConfig.getDiscoveryLinkInterval());
        discoveryLink.setDiscoveryUsingBridge(pkg.hasUseBridgeDiscovery() ? pkg.getUseBridgeDiscovery()
            : m_linkdConfig.useBridgeDiscovery());
        discoveryLink.setDiscoveryUsingCdp(pkg.hasUseCdpDiscovery() ? pkg.getUseCdpDiscovery()
            : m_linkdConfig.useCdpDiscovery());
        discoveryLink.setDiscoveryUsingRoutes(pkg.hasUseIpRouteDiscovery() ? pkg.getUseIpRouteDiscovery()
            : m_linkdConfig.useIpRouteDiscovery());
        discoveryLink.setDiscoveryUsingLldp(pkg.hasUseLldpDiscovery() ? pkg.getUseLldpDiscovery()
            : m_linkdConfig.useLldpDiscovery());
        discoveryLink.setDiscoveryUsingOspf(pkg.hasUseOspfDiscovery() ? pkg.getUseOspfDiscovery()
            : m_linkdConfig.useOspfDiscovery());
        discoveryLink.setDiscoveryUsingIsIs(pkg.hasUseIsisDiscovery() ? pkg.getUseIsisDiscovery()
            : m_linkdConfig.useIsIsDiscovery());
        discoveryLink.setDiscoveryUsingWifi(pkg.hasUseWifiDiscovery() ? pkg.getUseWifiDiscovery()
            : m_linkdConfig.useWifiDiscovery());
        return discoveryLink;
    }

    /**
     * {@inheritDoc}
     * 
     * @param nodeid
     */
    protected SnmpCollection getSnmpCollection(final int nodeid,
            final InetAddress ipaddr, final String sysoid,
            final String pkgName) {
        final Package pkg = m_linkdConfig.getPackage(pkgName);
        if (pkg != null) {
            final SnmpCollection collection = createCollection(nodeid, ipaddr);
            populateSnmpCollection(collection, pkg, sysoid);
            return collection;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @param nodeid
     */
    private List<SnmpCollection> getSnmpCollections(int nodeid,
                                                   final InetAddress ipaddr, final String sysoid) {
        List<SnmpCollection> snmpcolls = new ArrayList<SnmpCollection>();

        for (final String pkgName : m_linkdConfig.getAllPackageMatches(ipaddr)) {
            snmpcolls.add(getSnmpCollection(nodeid, ipaddr, sysoid, pkgName));
        }

        return snmpcolls;
    }

    private SnmpCollection createCollection(int nodeid,
            final InetAddress ipaddr) {
        SnmpCollection coll = null;
        try {
            coll = new SnmpCollection(
                                      this,
                                      nodeid,
                                      getSnmpAgentConfig(ipaddr));
        } catch (final Throwable t) {
            LOG.error("getSnmpCollection: Failed to load snmpcollection parameter from SNMP configuration file", t);
        }

        return coll;
    }

    public SnmpAgentConfig getSnmpAgentConfig(InetAddress ipaddr) {
        return SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
    }

    public boolean saveRouteTable(String pkgName) {
        Package pkg = m_linkdConfig.getPackage(pkgName);
        return pkg.hasSaveRouteTable() ? pkg.getSaveRouteTable()
            : m_linkdConfig.saveRouteTable();
    }

    public boolean saveStpNodeTable(String pkgName) {
        Package pkg = m_linkdConfig.getPackage(pkgName);
        return pkg.hasSaveStpNodeTable() ? pkg.getSaveStpNodeTable()
            : m_linkdConfig.saveStpNodeTable();
    }

    public boolean saveStpInterfaceTable(String pkgName) {
        Package pkg = m_linkdConfig.getPackage(pkgName);
        return pkg.hasSaveStpInterfaceTable() ? pkg.getSaveStpInterfaceTable()
            : m_linkdConfig.saveStpInterfaceTable();
    }

    public boolean forceIpRoutediscoveryOnEthernet(String pkgName) {
        Package pkg = m_linkdConfig.getPackage(pkgName);
        return pkg.hasForceIpRouteDiscoveryOnEthernet() ? pkg.getForceIpRouteDiscoveryOnEthernet()
            : m_linkdConfig.forceIpRouteDiscoveryOnEthernet();
    }
    
    private void populateSnmpCollection(final SnmpCollection coll,
            final Package pkg, final String sysoid) {
        coll.setPackageName(pkg.getName());

        String ipRouteClassName =  m_linkdConfig.getDefaultIpRouteClassName();
        if (m_linkdConfig.hasIpRouteClassName(sysoid)) {
            ipRouteClassName = m_linkdConfig.getIpRouteClassName(sysoid);
            LOG.debug("populateSnmpCollection: found class to get ipRoute: {}", ipRouteClassName);
        } else {
            LOG.debug("populateSnmpCollection: Using default class to get ipRoute: {}", ipRouteClassName);
        }

        final long initialSleepTime = m_linkdConfig.getInitialSleepTime();
        final long snmpPollInterval =(pkg.hasSnmp_poll_interval() ? pkg.getSnmp_poll_interval()
            : m_linkdConfig.getSnmpPollInterval()); 
        final boolean useCdpDiscovery = (pkg.hasUseCdpDiscovery() ? pkg.getUseCdpDiscovery()
            : m_linkdConfig.useCdpDiscovery());
        final boolean useIpRouteDiscovery = (pkg.hasUseIpRouteDiscovery() ? pkg.getUseIpRouteDiscovery()
            : m_linkdConfig.useIpRouteDiscovery());
        final boolean useLldpDiscovery = (pkg.hasUseLldpDiscovery() ? pkg.getUseLldpDiscovery()
            : m_linkdConfig.useLldpDiscovery());
        final boolean useOspfDiscovery = (pkg.hasUseOspfDiscovery() ? pkg.getUseOspfDiscovery()
            : m_linkdConfig.useOspfDiscovery());
        final boolean useIsIsDiscovery = (pkg.hasUseIsisDiscovery() ? pkg.getUseIsisDiscovery()
                                                                    : m_linkdConfig.useIsIsDiscovery());
        final boolean useBridgeDiscovery = (pkg.hasUseBridgeDiscovery() ? pkg.getUseBridgeDiscovery()
            : m_linkdConfig.useBridgeDiscovery());
        final boolean useWifiDiscovery = (pkg.hasUseWifiDiscovery() ? pkg.getUseWifiDiscovery()
                                                                      : m_linkdConfig.useWifiDiscovery());
        coll.setIpRouteClass(ipRouteClassName);
        coll.setInitialSleepTime(initialSleepTime);
        coll.setPollInterval(snmpPollInterval);
        coll.collectCdp(useCdpDiscovery);
        coll.collectIpRoute(useIpRouteDiscovery || saveRouteTable(pkg.getName()));
        coll.collectLldp(useLldpDiscovery);
        coll.collectOspf(useOspfDiscovery);
        coll.collectIsIs(useIsIsDiscovery);
        coll.collectBridge(useBridgeDiscovery);
        coll.collectStp(useBridgeDiscovery || saveStpNodeTable(pkg.getName()) || saveStpInterfaceTable(pkg.getName()));
        coll.collectIpNetToMedia(useWifiDiscovery || useBridgeDiscovery);
        coll.collectWifi(useWifiDiscovery && sysoid.startsWith(".1.3.6.1.4.1.14988."));

        if ( (pkg.hasEnableVlanDiscovery()  && pkg.getEnableVlanDiscovery()) 
                || 
                (!pkg.hasEnableVlanDiscovery() && m_linkdConfig.isVlanDiscoveryEnabled())
                && m_linkdConfig.hasClassName(sysoid)) {
            coll.setVlanClass(m_linkdConfig.getVlanClassName(sysoid));
            LOG.debug("populateSnmpCollection: found class to get Vlans: {}", coll.getVlanClass());
        } else {
            LOG.debug("populateSnmpCollection: no class found to get Vlans or VlanDiscoveryDisabled for Package: {}", pkg.getName());
        }


    }

    /**
     * <p>
     * onStart
     * </p>
     */
    @Override
    protected synchronized void onStart() {

        // start the scheduler
        //
        LOG.debug("start: Starting linkd scheduler");
        m_scheduler.start();
    }

    /**
     * <p>
     * onStop
     * </p>
     */
    @Override
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
    @Override
    protected synchronized void onPause() {
        m_scheduler.pause();
    }

    /**
     * <p>
     * onResume
     * </p>
     */
    @Override
    protected synchronized void onResume() {
        m_scheduler.resume();
    }


    /**
     * <p>
     * getLinkableNodesOnPackage
     * </p>
     * 
     * @param pkg
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<LinkableNode> getLinkableNodesOnPackage(String pkg) {
        List<LinkableNode> nodes = new ArrayList<LinkableNode>();
        synchronized (m_nodes) {
            for (LinkableNode node : m_nodes){
                if (node.getPackageName().equals(pkg))
                    nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * <p>
     * isInterfaceInPackage
     * </p>
     * 
     * @param ipaddr
     *            a {@link java.lang.String} object.
     * @param pkg
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isInterfaceInPackage(InetAddress ipaddr, String pkg) {
        return m_linkdConfig.isInterfaceInPackage(ipaddr,
                                                  m_linkdConfig.getPackage(pkg));
    }

    public boolean scheduleNodeCollection(int nodeid) {

        if (isScheduled(nodeid)) {
            LOG.debug("scheduleNodeCollection: Found Scheduled Linkable node {}. Skipping ", nodeid);
            return false;
        }
        // database changed need reload packageiplist
        m_linkdConfig.update();

        // First of all get Linkable Node
        LOG.debug("scheduleNodeCollection: Loading node {} from database", nodeid);
        LinkableSnmpNode node = m_queryMgr.getSnmpNode(nodeid);
        if (node == null) {
            LOG.debug("scheduleNodeCollection: Failed to get linkable node from database with nodeid {}. Exiting", nodeid);
            return false;
        }
        schedule(node);
        return true;
    }

    public boolean runSingleSnmpCollection(final int nodeId) {
        final LinkableSnmpNode node = m_queryMgr.getSnmpNode(nodeId);

        for (final SnmpCollection snmpColl : getSnmpCollections(nodeId,
                                                                node.getSnmpPrimaryIpAddr(),
                                                                node.getSysoid())) {
            snmpColl.setScheduler(m_scheduler);
            snmpColl.run();
        }

        return true;
    }

    public boolean runSingleLinkDiscovery(final String packageName) {
        final DiscoveryLink link = getDiscoveryLink(packageName);
        link.setScheduler(m_scheduler);
        link.run();

        return true;
    }

    void wakeUpNodeCollection(int nodeid) {


        if (!isScheduled(nodeid)) {
            LOG.warn("wakeUpNodeCollection: node not found during scheduling with ID {}", nodeid);
            scheduleNodeCollection(nodeid);
        } else {
            // get collections
            // get readyRunnuble
            // wakeup RR
            LinkableSnmpNode node = m_queryMgr.getSnmpNode(nodeid);
            Collection<SnmpCollection> collections = getSnmpCollections(nodeid,
                                                                        node.getSnmpPrimaryIpAddr(),
                                                                        node.getSysoid());
            LOG.debug("wakeUpNodeCollection: fetched SnmpCollections from scratch, iterating over {} objects to wake them up", collections.size());
            for (SnmpCollection collection : collections) {
                ReadyRunnable rr = getReadyRunnable(collection);
                if (rr == null) {
                    LOG.warn("wakeUpNodeCollection: found null ReadyRunnable for nodeid {}", nodeid);
                } else {
                    rr.wakeUp();
                }
            }
        }

    }

    void deleteNode(int nodeid) {
        LOG.debug("deleteNode: deleting LinkableNode for node {}", nodeid);

        m_queryMgr.update(nodeid, StatusType.DELETED);

        LinkableSnmpNode node = removeNode(nodeid);

        if (node == null) {
            LOG.warn("deleteNode: node not found: {}", nodeid);
        } else {
            Collection<SnmpCollection> collections = getSnmpCollections(nodeid,
                                                                        node.getSnmpPrimaryIpAddr(),
                                                                        node.getSysoid());
            LOG.debug("deleteNode: fetched SnmpCollections from scratch, iterating over {} objects to wake them up", collections.size());
            for (SnmpCollection collection : collections) {
                ReadyRunnable rr = getReadyRunnable(collection);

                if (rr == null) {
                    LOG.warn("deleteNode: found null ReadyRunnable");
                    return;
                } else {
                    rr.unschedule();
                }

            }

        }

        // database changed need reload packageiplist
        m_linkdConfig.update();

    }

    /**
     * Update database when an interface is deleted
     * 
     * @param nodeid
     *            the nodeid for the node
     * @param ipAddr
     *            the ip address of the interface
     * @param ifIndex
     *            the ifIndex of the interface
     */
    void deleteInterface(int nodeid, String ipAddr, int ifIndex) {

        LOG.debug("deleteInterface: marking table entries as deleted for node {} with IP address {} and ifIndex {}", nodeid, ipAddr, (ifIndex > -1 ? "" + ifIndex : "N/A"));

        m_queryMgr.updateForInterface(nodeid, ipAddr, ifIndex,
                                      StatusType.DELETED);

        // database changed need reload packageiplist
        m_linkdConfig.update();

    }

    void suspendNodeCollection(int nodeid) {
        LOG.debug("suspendNodeCollection: suspend collection LinkableNode for node {}", nodeid);

        m_queryMgr.update(nodeid, StatusType.INACTIVE);

        if (!isScheduled(nodeid)) {
            LOG.warn("suspendNodeCollection: found null ReadyRunnable");
        } else {
            // get collections
            // get readyRunnuble
            // suspend RR
            LinkableSnmpNode node = m_queryMgr.getSnmpNode(nodeid);
            Collection<SnmpCollection> collections = getSnmpCollections(nodeid,
                                                                        node.getSnmpPrimaryIpAddr(),
                                                                        node.getSysoid());
            LOG.debug("suspendNodeCollection: fetched SnmpCollections from scratch, iterating over {} objects to suspend them down", collections.size());
            for (SnmpCollection collection : collections) {
                ReadyRunnable rr = getReadyRunnable(collection);
                if (rr == null) {
                    LOG.warn("suspendNodeCollection: suspend: node not found: {}", nodeid);
                    return;
                } else {
                    rr.suspend();
                }
            }
        }

    }

    private ReadyRunnable getReadyRunnable(ReadyRunnable runnable) {
        LOG.debug("getReadyRunnable: get ReadyRunnable from scheduler: {}", runnable.getInfo());

        return m_scheduler.getReadyRunnable(runnable);

    }

    /**
     * Method that updates info in List nodes and also save info into
     * database. This method is called by SnmpCollection after all stuff is
     * done
     * 
     * @param snmpcoll
     */
    @Transactional
    public void updateNodeSnmpCollection(final SnmpCollection snmpcoll) {
        LOG.debug("Updating SNMP collection for {}", str(snmpcoll.getTarget()));
        LinkableNode node = removeNode(snmpcoll.getPackageName(),snmpcoll.getTarget());
        if (node == null) {
            LOG.error("No linkable node found for SNMP collection: {}!", snmpcoll.getInfo());
        }

        LinkableSnmpNode snmpNode = m_queryMgr.getSnmpNode(node.getNodeId());
        if (snmpNode == null) {
            LOG.info("No node found in database for SNMP collection: {} unscheduling!", snmpcoll.getInfo());
            return;
        }
        node = new LinkableNode(snmpNode,snmpcoll.getPackageName());
        node = m_queryMgr.storeSnmpCollection(node, snmpcoll);
        if (node != null) {
            synchronized (m_nodes) {
                m_nodes.add(node);
            }
        }
        snmpcoll.schedule();
    }

    /**
     * Method that uses info in hash snmpprimaryip2nodes and also save info
     * into database. This method is called by DiscoveryLink after all stuff
     * is done
     * 
     * @param discover
     */
    void updateDiscoveryLinkCollection(final DiscoveryLink discover) {

        m_queryMgr.storeDiscoveryLink(discover);
        discover.schedule();
    }

    /**
     * Send a newSuspect event for the interface construct event with 'linkd'
     * as source
     * 
     * @param ipInterface
     *            The interface for which the newSuspect event is to be
     *            generated
     * @param ipowner
     *            The host that hold this ipInterface information
     * @pkgName The package Name of the ready runnable involved
     */
    void sendNewSuspectEvent(InetAddress ipaddress, InetAddress ipowner,
            String pkgName) {

        if (ipaddress == null) {
            LOG.info("sendNewSuspectEvent: nothing to send,  IP addressis null");
            return;    		
        }

        if (m_newSuspectEventsIpAddr.contains(ipaddress)) {
            LOG.info("sendNewSuspectEvent: nothing to send, suspect event previously sent for IP address: {}", str(ipaddress));
            return;
        } else if (!isInterfaceInPackage(ipaddress, pkgName)) {
            LOG.info("sendNewSuspectEvent: nothing to send for IP address: {}, not in package: {}", str(ipaddress), pkgName);
            return;
        }

        org.opennms.netmgt.config.linkd.Package pkg = m_linkdConfig.getPackage(pkgName);

        boolean autodiscovery = false;
        if (pkg.hasAutoDiscovery())
            autodiscovery = pkg.getAutoDiscovery();
        else
            autodiscovery = m_linkdConfig.isAutoDiscoveryEnabled();

        if (autodiscovery) {

            EventBuilder bldr = new EventBuilder(
                                                 EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI,
                    "linkd");

            bldr.setHost(str(ipowner));
            bldr.setInterface(ipaddress);

            m_eventForwarder.sendNow(bldr.getEvent());

            m_newSuspectEventsIpAddr.add(ipaddress);

        }
    }

    boolean isScheduled(int nodeid) {
        synchronized (m_nodes) {
            for (LinkableNode node : m_nodes) {
                if (node.getNodeId() == nodeid)
                    return true;
            }
            return false;
        }
    }

    private LinkableSnmpNode removeNode(int nodeid) {
        LinkableSnmpNode snmpnode=null;
        synchronized (m_nodes) {
            Iterator<LinkableNode> ite = m_nodes.iterator();
            while (ite.hasNext()) {
                LinkableNode curNode = ite.next();
                if (curNode.getNodeId() == nodeid) {
                    snmpnode = curNode.getLinkableSnmpNode();
                    ite.remove();
                }
            }
            return snmpnode;
        }
    }

    protected LinkableNode removeNode(String packageName, InetAddress ipaddr) {
        synchronized (m_nodes) {
            Iterator<LinkableNode> ite = m_nodes.iterator();
            while (ite.hasNext()) {
                LinkableNode curNode = ite.next();
                if (curNode.getSnmpPrimaryIpAddr().equals(ipaddr) && curNode.getPackageName().equals(packageName)) {
                    ite.remove();
                    return curNode;
                }
            }
        }
        return null;
    }

    public QueryManager getQueryManager() {
        return m_queryMgr;
    }

    /**
     * <p>
     * setQueryManager
     * </p>
     * 
     * @param queryMgr
     *            a {@link org.opennms.netmgt.linkd.QueryManager} object.
     */
    public void setQueryManager(QueryManager queryMgr) {
        m_queryMgr = queryMgr;
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
     * getLinkdConfig
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.config.LinkdConfig} object.
     */
    public LinkdConfig getLinkdConfig() {
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
    public void setLinkdConfig(final LinkdConfig config) {
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

    // Here all the information related to the
    // mapping between ipaddress and mac address are stored
    public void addAtInterface(final String packageName, final AtInterface atinterface) {
        LOG.debug("addAtInterface: adding at interface {}/{}", atinterface.getIpAddress().getHostAddress(),atinterface.getMacAddress());
        final String macAddress = atinterface.getMacAddress();
        final InetAddress ipAddress = atinterface.getIpAddress();

        if (!isInterfaceInPackage(ipAddress, packageName)) {
            LOG.debug("addAtInterface: ip {} not in package {}. Skipping", atinterface.getIpAddress().getHostAddress(),packageName);
            return;
        }

        synchronized (m_macToAtinterface) {
            if (!m_macToAtinterface.containsKey(packageName)) {
                LOG.debug("addAtInterface: creating map for package {}.",packageName);
                m_macToAtinterface.put(packageName, new HashMap<String, List<AtInterface>>());
            }

            final List<AtInterface> atis;
            if (m_macToAtinterface.get(packageName).containsKey(macAddress)) {
                atis = m_macToAtinterface.get(packageName).get(macAddress);
            } else {
                atis = new ArrayList<AtInterface>();
            }

            if (atis.contains(atinterface)) {
                LOG.debug("addAtInterface: Interface/package {}/{} found not adding.", atinterface.getIpAddress().getHostAddress(),packageName);
            } else {
                LOG.debug("addAtInterface: add ip/mac/ifindex {}/{}/{} on package {}.", atinterface.getIpAddress().getHostAddress(), atinterface.getMacAddress(), atinterface.getIfIndex(), packageName);
                atis.add(atinterface);
                m_macToAtinterface.get(packageName).put(macAddress, atis);
            }
        }
    }

    public Set<String> getMacAddressesOnPackage(final String packageName) {
        synchronized(m_macToAtinterface) {
            final Map<String, List<AtInterface>> interfaceMaps = m_macToAtinterface.get(packageName);
            if (interfaceMaps != null) return Collections.unmodifiableSet(interfaceMaps.keySet());
        }
        return Collections.emptySet();
    }

    public List<AtInterface> getAtInterfaces(final String packageName, final String macAddress) {
        synchronized (m_macToAtinterface) {
            final Map<String, List<AtInterface>> interfaceMaps = m_macToAtinterface.get(packageName);
            if (interfaceMaps != null) {
                final List<AtInterface> interfaces = interfaceMaps.get(macAddress);
                if (interfaces != null) return Collections.unmodifiableList(interfaces);
            }
        }
        return Collections.emptyList();
    }

    public void clearPackageSavedData(final String packageName) {
        synchronized (m_macToAtinterface) {
            final Map<String, List<AtInterface>> interfaces = m_macToAtinterface.get(packageName);
            if (interfaces != null) interfaces.clear();
        }
    }

    public String getSource() {
        return "linkd";
    }


    public Set<String> getActivePackages() {
        Set<String> packages = new HashSet<String>();
        synchronized (m_nodes) {
            for (LinkableNode node: m_nodes)
                packages.add(node.getPackageName());
        }
        return packages;
    }
}
