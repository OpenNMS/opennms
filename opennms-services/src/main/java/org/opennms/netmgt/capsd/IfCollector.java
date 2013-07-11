/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect all the relevant information from the
 * target designated during construction. The target is initially polled using
 * all the configured plugins, then tested for SMB and SNMP. If either of those
 * plugins were detected then an additional collection of the SMB/SNMP
 * information is performed. If any node has multiple interfaces in it then
 * addition probes of those interfaces are performed. The SNMP/SMB collections
 * are performed only once though.
 *
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public final class IfCollector implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(IfCollector.class);
    
    private PluginManager m_pluginManager;
    
    /**
     * The primary target internet address
     */
    private final InetAddress m_target;

    /**
     * The SMB collector. If the interface is determine to have SMB connectivity
     * then the collector is run.
     */
    private IfSmbCollector m_smbCollector;

    /**
     * If the interface is determined to have SNMP capabilities then the
     * collector is run.
     */
    private IfSnmpCollector m_snmpCollector;

    /**
     * The list of supported protocols on this interface.
     */
    private List<SupportedProtocol> m_protocols;

    /**
     * The list of sub-targets found via SNMP. Indexed by InetAddress
     */
    private Map<InetAddress, List<SupportedProtocol>> m_subTargets;

    /**
     * List of SnmpInt32 objects representing each of the unnamed/non-IP
     * interfaces found via SNMP
     */
    private List<Integer> m_nonIpInterfaces;

    /**
     * Boolean flag which indicates if SNMP collection is to be done.
     */
    private boolean m_doSnmpCollection;

    private Set<InetAddress> m_previouslyProbed;

    /**
     * This class is used to encapsulate the supported protocol information
     * discovered for an interface. The is the combination of the protocol name
     * and the in/out qualifiers for the plugin.
     * 
     * @author <a href="mailto:weave@oculan.com">Weave </a>
     * 
     */
    static final class SupportedProtocol {
        /**
         * The protocol name
         */
        private final String m_name;

        /**
         * The map of qualifiers from the plugin that discovered this protocol.
         */
        private final Map<String, Object> m_qualifiers;

        /**
         * Creates a new supported protocol based upon the protocol string and
         * the qualifier map.
         * 
         * @param protoName
         *            The name of the protocol.
         * @param qualifiers
         *            The protocol qualifiers.
         */
        SupportedProtocol(String protoName, Map<String, Object> qualifiers) {
            m_name = protoName;
            m_qualifiers = qualifiers;
        }

        /**
         * Returns the name of the discovered protocol.
         */
        String getProtocolName() {
            return m_name;
        }

        /**
         * Returns the map of qualifiers from the plugin that discovered this
         * protocol.
         */
        Map<String, Object> getQualifiers() {
            return m_qualifiers;
        }
    }

    /**
     * This method is used to <em>probe</em> the target addresses using the
     * configured list of protocol specifications from the Configuration
     * Manager. The list of supported protocols are added to the supports list.
     * Any failures in the plugins are logged and discarded.
     * 
     * @param target
     *            The target to probe
     * @param supports
     *            The supported protocols (SupportedProtocol)
     * 
     */
    private void probe(InetAddress target, List<SupportedProtocol> supports) {
        String logAddr = InetAddressUtils.str(target);

        CapsdProtocolInfo[] plugins = m_pluginManager.getProtocolSpecification(target);
        
        // First run the plugins to find out all the capabilities
        // for the interface
        //
        for (int i = 0; i < plugins.length; i++) {
            LOG.debug("{} testing plugin {}", logAddr, plugins[i].getProtocol());
            if (plugins[i].isAutoEnabled()) {
                LOG.debug("{} protocol {} is auto enabled", logAddr, plugins[i].getProtocol());
                supports.add(new SupportedProtocol(plugins[i].getProtocol(), null));
                continue;
            }

            try {
                Plugin p = plugins[i].getPlugin();
                Map<String, Object> q = plugins[i].getParameters();
                boolean r = p.isProtocolSupported(target, q);

                LOG.debug("{} protocol {} supported? {}", logAddr, plugins[i].getProtocol(), (r ? "true" : "false"));

                if (r) {
                    supports.add(new SupportedProtocol(plugins[i].getProtocol(), q));
                }
            } catch (UndeclaredThrowableException utE) {
                Throwable t = utE.getUndeclaredThrowable();
                if (t instanceof NoRouteToHostException) {
                    if (CapsdConfigFactory.getInstance().getAbortProtocolScansFlag()) {
                        LOG.info("IfCollector: No route to host {}, aborting protocol scans.", logAddr);
                        break; // Break out of plugin loop
                    } else {
                        LOG.info("IfCollector: No route to host {}, continuing protocol scans.", logAddr);
                    }
                } else {
                    LOG.warn("IfCollector: Caught undeclared throwable exception when testing for protocol {} on host {}",
                             plugins[i].getProtocol(), logAddr, utE);
                }
            } catch (Throwable t) {
                LOG.warn("IfCollector: Caught an exception when testing for protocol {} on host {}", plugins[i].getProtocol(), logAddr, t);
            }
            LOG.debug("{} plugin {} completed!", logAddr, plugins[i].getProtocol());
        }
    }

    /**
     * Constructs a new collector instance. The collector's target is passed as
     * an argument to the constructor. Very little initialization is preformed
     * in the constructor. The main work of the class is preformed in the
     * {@link #run run}method. This provides a well known interface that can be
     * collected in a thread pool or directly invoked.
     * 
     * @param addr
     *            The target of the poll.
     * @param doSnmpCollection
     *            Flag which indicates if SNMP collection should be done.
     * 
     */
    IfCollector(PluginManager pluginManager, InetAddress addr, boolean doSnmpCollection) {
        this(pluginManager, addr, doSnmpCollection, new HashSet<InetAddress>());
    }

    IfCollector(PluginManager pluginManager, InetAddress addr, boolean doSnmpCollection, Set<InetAddress> previouslyProbed) {
        m_pluginManager = pluginManager;
        m_target = addr;
        m_doSnmpCollection = doSnmpCollection;
        m_smbCollector = null;
        m_snmpCollector = null;
        m_protocols = new ArrayList<SupportedProtocol>(8);
        m_subTargets = null;
        m_nonIpInterfaces = null;
        m_previouslyProbed = previouslyProbed;
    }

    /**
     * Returns the target of this collection
     */
    InetAddress getTarget() {
        return m_target;
    }

    /**
     * Returns the supported protocols for this interface.
     */
    List<SupportedProtocol> getSupportedProtocols() {
        return m_protocols;
    }

    /**
     * Returns true if this target had additional interfaces found by SNMP
     */
    boolean hasAdditionalTargets() {
        return m_subTargets != null && !m_subTargets.isEmpty();
    }

    /**
     * Returns the map of additional interface targets. The keys are instances
     * of {@link java.net.InetAddress addresses}and the mapped values are
     * {@link java.util.List lists}of supported protocols.
     * 
     */
    Map<InetAddress, List<SupportedProtocol>> getAdditionalTargets() {
        return m_subTargets;
    }

    /**
     * Returns true if this target has non-IP interfaces found by SNMP
     */
    boolean hasNonIpInterfaces() {
        return m_nonIpInterfaces != null && !m_nonIpInterfaces.isEmpty();
    }

    /**
     * Returns the list of non-IP interfaces..
     * 
     */
    List<Integer> getNonIpInterfaces() {
        return m_nonIpInterfaces;
    }

    /**
     * Returns true if the node supported SMB and the collection succeeded
     */
    boolean hasSmbCollection() {
        return (m_smbCollector != null);
    }

    /**
     * Returns the collected SMB information for the node.
     */
    IfSmbCollector getSmbCollector() {
        return m_smbCollector;
    }

    /**
     * Returns true if the target supported SNMP and the collection succeeded.
     */
    boolean hasSnmpCollection() {
        return (m_snmpCollector != null);
    }

    /**
     * Returns the Snmp Collection of information
     */
    IfSnmpCollector getSnmpCollector() {
        return m_snmpCollector;
    }
    
    void deleteSnmpCollector() {
        m_snmpCollector = null;
    }

    /**
     * The main collection routine of the class. This method is used to poll the
     * address, and any additional interfaces discovered via SNMP.
     */
    @Override
    public void run() {
        LOG.debug("IfCollector.run: run method invoked to collect information for address {}", InetAddressUtils.str(m_target));

        // Now go throught the successful plugin checks
        // and see if either SMB, MSExchange, or SNMP is
        // supported on the target node
        //
        boolean isSnmp = false;
        boolean isSnmpV2 = false;
        boolean isSmb = false;
        boolean hasExchange = false;

        probe(m_target, m_protocols);
        m_previouslyProbed.add(m_target);

        // First run the plugins to find out all the capabilities
        // for the interface
        //
        Iterator<SupportedProtocol> iter = m_protocols.iterator();
        while (iter.hasNext()) {
            SupportedProtocol proto = iter.next();
            if (proto.getProtocolName().equalsIgnoreCase("snmp")) {
                isSnmp = true;
            } else if (proto.getProtocolName().equalsIgnoreCase("smb")) {
                isSmb = true;
            } else if (proto.getProtocolName().equalsIgnoreCase("msexchange")) {
                isSmb = true;
                hasExchange = true;
            }
        }

        // collect the SMB information
        //
        if (isSmb) {
            LOG.debug("IfCollector.run: starting SMB collection");

            try {
                m_smbCollector = new IfSmbCollector(m_target, hasExchange);
                m_smbCollector.run();
            } catch (Throwable t) {
                m_smbCollector = null;
                LOG.warn("IfCollector.run: Caught an exception when collecting SMB information from target {}", InetAddressUtils.str(m_target), t);
            }

            LOG.debug("IfCollector.run: SMB collection completed");
        }

        // collect the snmp information if necessary
        //
        if ((isSnmp || isSnmpV2) && m_doSnmpCollection) {
            LOG.debug("IfCollector.run: starting SNMP collection");

            try {
                m_snmpCollector = new IfSnmpCollector(m_target);
                m_snmpCollector.run();

                if (m_snmpCollector.hasIpAddrTable() && m_snmpCollector.hasIfTable()) {
                    m_subTargets = new TreeMap<InetAddress, List<SupportedProtocol>>(new InetAddressComparator());
                    m_nonIpInterfaces = new ArrayList<Integer>();

                    // Iterate over ifTable entries
                    //
                    for (IfTableEntry ifEntry : m_snmpCollector.getIfTable()) {

                        // Get the ifIndex
                        //
                        Integer ifIndex = ifEntry.getIfIndex();
                        if (ifIndex == null)
                            continue;

                        // Get list of all IP addresses for the current ifIndex
                        //
                        int index = ifIndex.intValue();
                        List<InetAddress> ipAddrs = m_snmpCollector.getIpAddrTable().getIpAddresses(index);
                        if (ipAddrs == null || ipAddrs.size() == 0) {
                            // Non IP interface
                            InetAddress nonIpAddr = null;
                            nonIpAddr = InetAddressUtils.addr("0.0.0.0");

                            if (ipAddrs == null) {
                                ipAddrs = new ArrayList<InetAddress>();
                            }
                            ipAddrs.add(nonIpAddr);
                        }

                        // Iterate over this interface's IP address list
                        //
                        Iterator<InetAddress> s = ipAddrs.iterator();
                        while (s.hasNext()) {
                            InetAddress subtarget = s.next();

                            // if the target failed to convert or if it
                            // is equal to the current target then skip it
                            //
                            if (subtarget == null || subtarget.equals(m_target) || m_previouslyProbed.contains(subtarget))
                                continue;

                            // now find the ifType
                            //
                            Integer ifType = ifEntry.getIfType();

                            // lookup of if type failed, next!
                            //
                            if (ifType == null)
                                continue;

                            // now check for loopback
                            if (subtarget.isLoopbackAddress()) {
                                // Skip if loopback
                                LOG.debug("ifCollector.run: Loopback interface: {}, skipping...", InetAddressUtils.str(subtarget));
                                continue;
                            }

                            // now check for non-IP interface
                            //
                            if (InetAddressUtils.str(subtarget).equals("0.0.0.0")) {
                                // its a non-IP interface...add its ifIndex to
                                // the non-IP interface list
                                //
                                m_nonIpInterfaces.add(ifIndex);
                                continue;
                            }

                            // ok it appears to be ok, so probe it!
                            //
                            List<SupportedProtocol> probelist = new ArrayList<SupportedProtocol>();
                            LOG.debug("----------------------------------------------------------------------------------------");
                            LOG.debug("ifCollector.run: probing subtarget {}", InetAddressUtils.str(subtarget));
                            probe(subtarget, probelist);
                            m_previouslyProbed.add(subtarget);

                            LOG.debug("ifCollector.run: adding subtarget {} # supported protocols: {}", InetAddressUtils.str(subtarget), probelist.size());
                            LOG.debug("----------------------------------------------------------------------------------------");
                            m_subTargets.put(subtarget, probelist);
                        } // end while(more ip addresses)
                    } // end while(more interfaces)
                } // end if(ipAddrTable and ifTable entries collected)

                else if (m_snmpCollector.hasIpAddrTable()) {
                    m_subTargets = new TreeMap<InetAddress, List<SupportedProtocol>>(new InetAddressComparator());

                    List<InetAddress> ipAddrs = m_snmpCollector.getIpAddrTable().getIpAddresses();
                    // Iterate over this interface's IP address list
                    //
                    Iterator<InetAddress> s = ipAddrs.iterator();
                    while (s.hasNext()) {
                        InetAddress subtarget = s.next();

                        // if the target failed to convert or if it
                        // is equal to the current target then skip it
                        //
                        if (subtarget == null || subtarget.equals(m_target)) {
                            continue;
                        }

                        // now check for loopback
                        if (subtarget.isLoopbackAddress()) {
                            // Skip if loopback
                            LOG.debug("ifCollector.run: Loopback interface: {}, skipping...", InetAddressUtils.str(subtarget));
                            continue;
                        }


                        // ok it appears to be ok, so probe it!
                        //
                        List<SupportedProtocol> probelist = new ArrayList<SupportedProtocol>();
                        LOG.debug("----------------------------------------------------------------------------------------");
                        LOG.debug("ifCollector.run: probing subtarget {}", InetAddressUtils.str(subtarget));
                        probe(subtarget, probelist);
                        m_previouslyProbed.add(subtarget);
                        
                        LOG.debug("ifCollector.run: adding subtarget {} # supported protocols: {}", InetAddressUtils.str(subtarget), probelist.size());
                        LOG.debug("----------------------------------------------------------------------------------------");
                        m_subTargets.put(subtarget, probelist);
                    } // end while(more ip addresses)
                } // end if(ipAddrTable entries collected)
            } // end try()
            catch (Throwable t) {
                m_snmpCollector = null;
                LOG.warn("IfCollector.run: Caught an exception when collecting SNMP information from target {}", InetAddressUtils.str(m_target), t);
            }

            LOG.debug("IfCollector.run: SNMP collection completed");
        } // end if(SNMP supported)

        LOG.debug("IfCollector.run: run method exiting after collecting information from address {}", InetAddressUtils.str(m_target));
    }
}
