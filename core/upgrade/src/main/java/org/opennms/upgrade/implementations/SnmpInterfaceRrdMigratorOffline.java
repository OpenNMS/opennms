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
package org.opennms.upgrade.implementations;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.upgrade.api.Ignore;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class RRD/JRB Migrator for SNMP Interfaces Data (Offline Version)
 * 
 * <p>1.12 always add the MAC Address to the snmpinterface table if exist, which
 * is different from the 1.10 behavior. For this reason, some interfaces are going
 * to appear twice, and the data must be merged.</p>
 * 
 * <ul>
 * <li>NMS-6056</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@Ignore
public class SnmpInterfaceRrdMigratorOffline extends SnmpInterfaceRrdMigratorOnline {

    /**
     * Instantiates a new SNMP interface RRD migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public SnmpInterfaceRrdMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 3;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Merge SNMP Interface directories (Offline Version): NMS-6056";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.implementations.SnmpInterfaceRrdMigratorOnline#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
        try {
            SnmpPeerFactory.init();
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't initialize SNMP Peer Factory because " + e.getMessage());
        }
        super.preExecute();
    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected List<SnmpInterfaceUpgrade> getInterfacesToMerge() throws OnmsUpgradeException {
        updatePhysicalInterfaces();
        return super.getInterfacesToMerge();
    }

    /**
     * Update physical interfaces.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void updatePhysicalInterfaces() throws OnmsUpgradeException {
        Connection conn = getDbConnection();
        final DBUtils db = new DBUtils(getClass());
        db.watch(conn);
        try {
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            db.watch(st);
            String query = "select n.nodeid, n.nodelabel, i.ipaddr from node n, ipinterface i, ifservices s where n.nodeid = i.nodeid and n.nodeid = s.nodeid and i.issnmpprimary='P' and s.serviceid in (select serviceid from service where servicename='SNMP')";
            ResultSet rs = st.executeQuery(query);
            db.watch(rs);
            while (rs.next()) {
                int nodeId = rs.getInt("nodeid");
                String nodeLabel = rs.getString("nodelabel");
                String ipAddress = rs.getString("ipaddr");
                try {
                    log("Retrieving IF-MIB::ifPhysAddress for node %s using IP %s\n", nodeLabel, ipAddress);
                    InetAddress address = InetAddressUtils.addr(ipAddress);
                    final SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
                    Map<SnmpInstId, SnmpValue> values = SnmpUtils.getOidValues(agentConfig, "ifPhysAddress", SnmpObjId.get(".1.3.6.1.2.1.2.2.1.6"));
                    if (values == null || values.isEmpty()) {
                        log("Warning: there is no ifPhysAddress data for %s using IP %s\n", nodeLabel, ipAddress);
                    } else {
                        for (Entry<SnmpInstId,SnmpValue> entry : values.entrySet()) {
                            final String mac = getPhysAddr(entry.getValue());
                            if (mac != null) {
                                log("Updating the snmpPhysAddress to '%s' for ifIndex %s on node %s (id %s) using IP %s\n", mac, entry.getKey().toInt(), nodeLabel, nodeId, ipAddress);
                                PreparedStatement upt = conn.prepareStatement("update snmpinterface set snmpphysaddr=? where nodeid=? and snmpifindex=?");
                                db.watch(upt);
                                upt.setString(1, mac);
                                upt.setInt(2, nodeId);
                                upt.setInt(3, entry.getKey().toInt());
                                upt.executeUpdate();
                            }
                        }
                    }
                } catch (Exception e) {
                    log("Warning: can't update the ifPhysAddress entries on node " + nodeLabel + " (IP " + ipAddress + ") because " + e.getMessage());
                }
            }
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                log("Error: can't rollback changes because " + ex.getMessage());
            }
        } finally {
            db.cleanUp();
        }
    }

    /**
     * Gets the physical address.
     *
     * @param value the value
     * @return the physical address
     */
    public String getPhysAddr(SnmpValue value) {
        String hexString = value.toHexString();
        if (hexString != null) {
            if (hexString.length() == 12) {
                return hexString;
            } else {
                try {
                    String displayString = value.toDisplayString();
                    return displayString == null || displayString.trim().isEmpty() ? null : InetAddressUtils.normalizeMacAddress(displayString);
                } catch (IllegalArgumentException e) {
                    return value.toDisplayString();
                }
            }
        }
        return value.toDisplayString();
    }

}
