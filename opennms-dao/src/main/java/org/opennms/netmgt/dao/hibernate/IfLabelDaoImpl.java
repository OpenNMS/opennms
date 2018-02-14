/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.netmgt.dao.api.IfLabel;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.util.AbstractIfLabel;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A convenience class for methods to encode/decode ifLabel descriptions for
 * storing SNMP data in an RRD file.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 */
public class IfLabelDaoImpl extends AbstractIfLabel implements IfLabel {

    private static final Logger LOG = LoggerFactory.getLogger(IfLabelDaoImpl.class);

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    public static IfLabel getInstance() {
        return BeanUtils.getBean("daoContext", "ifLabel", IfLabel.class);
    }

    /**
     * Return a map of useful SNMP information for the interface specified by
     * the nodeId and ifLabel. Essentially a "decoding" algorithm for the
     * ifLabel.
     *
     * @param nodeId
     *            Node id
     * @param ifLabel
     *            Interface label of format: <description>- <macAddr>
     * @return Map of SNMP info keyed by 'snmpInterface' table column names for
     *         the interface specified by nodeId and ifLabel args.
     * @throws SQLException
     *             if error occurs accessing the database.
     */
    @Override
    public Map<String, String> getInterfaceInfoFromIfLabel(int nodeId, String ifLabel) {
        if (ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final Map<String, String> info = new HashMap<String, String>();
        String desc = ifLabel;
        String mac = null;

        // first I have to strip off the MAC address from the end, if there is
        // one
        int dashIndex = ifLabel.lastIndexOf('-');

        if (dashIndex >= 0) {
            desc = ifLabel.substring(0, dashIndex);
            mac = ifLabel.substring(dashIndex + 1, ifLabel.length());
        }
        
       final String desc2 = desc;
       final String mac2 = mac;
 
        LOG.debug("getInterfaceInfoFromIfLabel: desc={} mac={}", desc, mac);

        String queryDesc = desc.replace('_', '%');

        OnmsSnmpInterface iface = m_snmpInterfaceDao.findByNodeIdAndDescription(nodeId, queryDesc);

        if (iface != null) {
            // If the description portion of ifLabel matches an entry
            // in the snmpinterface table...

            /*
             * When Cisco Express Forwarding (CEF) or some ATM encapsulations
             * (AAL5) are used on Cisco routers, an additional entry might be 
             * in the ifTable for these sub-interfaces, but there is no
             * performance data available for collection.  This check excludes
             * ifTable entries where ifDescr contains "-cef".  See bug #803.
             */
            if (iface.getIfDescr() != null) {
                if (Pattern.matches(".*-cef.*", iface.getIfDescr())) {
                    return Collections.unmodifiableMap(info);
                }
            }

            if ((AlphaNumeric.parseAndReplace(iface.getIfName(), '_').equals(desc2)) || (AlphaNumeric.parseAndReplace(iface.getIfDescr(), '_').equals(desc2))) {

                // If the MAC address portion of the ifLabel matches
                // an entry in the snmpinterface table...
                if (mac2 == null || mac2.equals(iface.getPhysAddr())) {
                    // Get extra information about the interface
                    info.put("id", String.valueOf(iface.getId()));
                    info.put("nodeid", String.valueOf(iface.getNodeId()));
                    info.put("snmpphysaddr", String.valueOf(iface.getPhysAddr()));
                    info.put("snmpifindex", String.valueOf(iface.getIfIndex()));
                    info.put("snmpifdescr", String.valueOf(iface.getIfDescr()));
                    info.put("snmpiftype", String.valueOf(iface.getIfType()));
                    info.put("snmpifname", String.valueOf(iface.getIfName()));
                    info.put("snmpifspeed", String.valueOf(iface.getIfSpeed()));
                    info.put("snmpifadminstatus", String.valueOf(iface.getIfAdminStatus()));
                    info.put("snmpifoperstatus", String.valueOf(iface.getIfOperStatus()));
                    info.put("snmpifalias", String.valueOf(iface.getIfAlias()));
                    info.put("snmpcollect", String.valueOf(iface.getCollect()));
                    info.put("snmplastcapsdpoll", String.valueOf(iface.getLastCapsdPoll()));
                    info.put("snmppoll", String.valueOf(iface.getPoll()));
                    info.put("snmplastsnmppoll", String.valueOf(iface.getLastSnmpPoll()));
                }
            }
        }

        // The map will remain empty if the information was not located in the DB
        return Collections.unmodifiableMap(info);
    }

    /**
     * <p>getIfLabel</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getIfLabel(final int nodeId, final InetAddress ipAddr) {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces", "ipInterfaces", JoinType.LEFT_JOIN);
        builder.eq("node.id", nodeId);
        builder.ne("ipInterfaces.isManaged", "D");
        builder.eq("ipInterfaces.ipAddress", ipAddr);

        List<OnmsSnmpInterface> ifaces = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (ifaces.size() > 0) {
            if (ifaces.size() > 1) {
                LOG.warn("Found multiple OnmsSnmpInterface objects for: {}, {}, using the first entry", nodeId, str(ipAddr));
            }
            OnmsSnmpInterface iface = ifaces.iterator().next();

            String name = iface.getIfName();
            String descr = iface.getIfDescr();
            String physAddr = iface.getPhysAddr();

            if (name != null || descr != null) {
                return getIfLabel(name, descr, physAddr);
            } else {
                LOG.warn("Interface (nodeId/ipAddr={}/{}) has no ifName and no ifDescr...setting to label to '{}'.", nodeId, str(ipAddr), IfLabel.NO_IFLABEL);
                return IfLabel.NO_IFLABEL;
            }
        } else {
            LOG.warn("No OnmsSnmpInterface found for: {}, {}; setting to label to '{}'", nodeId, str(ipAddr), IfLabel.NO_IFLABEL);
            return IfLabel.NO_IFLABEL;
        }
    }

    /**
     * <p>getIfLabelfromIfIndex</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getIfLabelfromIfIndex(final int nodeId, final InetAddress ipAddr, final int ifIndex) {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (ifIndex == -1) {
            return getIfLabel(nodeId, ipAddr);
        }

        CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.alias("ipInterfaces", "ipInterfaces", JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces.node", "node", JoinType.LEFT_JOIN);
        builder.eq("ifIndex", ifIndex);
        builder.eq("node.id", nodeId);
        builder.ne("ipInterfaces.isManaged", "D");
        builder.eq("ipInterfaces.ipAddress", ipAddr);

        List<OnmsSnmpInterface> ifaces = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (ifaces.size() > 0) {
            if (ifaces.size() > 1) {
                LOG.warn("Found multiple OnmsSnmpInterface objects for: {}, {}, {}, using the first entry", nodeId, str(ipAddr), ifIndex);
            }
            OnmsSnmpInterface iface = ifaces.iterator().next();

            String name = iface.getIfName();
            String descr = iface.getIfDescr();
            String physAddr = iface.getPhysAddr();

            if (name != null || descr != null) {
                return getIfLabel(name, descr, physAddr);
            } else {
                LOG.warn("Interface (nodeId/ipAddr={}/{}) has no ifName and no ifDescr...setting to label to '{}'.", nodeId, str(ipAddr), IfLabel.NO_IFLABEL);
                return IfLabel.NO_IFLABEL;
            }
        } else {
            LOG.warn("No OnmsSnmpInterface found for: {}, {}, {}, using node ID and IP address only", nodeId, str(ipAddr), ifIndex);
            return getIfLabel(nodeId, ipAddr);
        }
    }
 
    /**
     * Return the ifLabel as a string for the given node and ifIndex. Intended for
     * use with non-ip interfaces.
     *
     * @return String
     * @param nodeId a int.
     * @param ifIndex a int.
     */
    @Override
    public String getIfLabelfromSnmpIfIndex(final int nodeId, final int ifIndex) {

        OnmsSnmpInterface iface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex);

        String name = iface.getIfName();
        String descr = iface.getIfDescr();
        String physAddr = iface.getPhysAddr();

        if (name != null || descr != null) {
            return getIfLabel(name, descr, physAddr);
        } else {
            LOG.warn("Interface (nodeId/ifIndex={}/{}) has no ifName and no ifDescr...setting to label to '{}'.", nodeId, ifIndex, IfLabel.NO_IFLABEL);
            return IfLabel.NO_IFLABEL;
        }
    }
}
