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
import java.sql.Statement;
import java.util.List;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.service.snmp.IfTable;
import org.opennms.netmgt.provision.service.snmp.IfTableEntry;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
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
        final DBUtils d = new DBUtils(getClass());
        try {
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            d.watch(st);
            String query = "select n.nodeid, n.nodelabel, i.ipaddr from node n, ipinterface i, ifservices s where n.nodeid = i.nodeid and n.nodeid = s.nodeid and i.issnmpprimary='P' and s.serviceid in (select serviceid from service where servicename='SNMP')";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int nodeId = rs.getInt("nodeid");
                String nodeLabel = rs.getString("nodelabel");
                String ipAddress = rs.getString("ipaddr");
                IfTable ifTable = null;
                try {
                    log("Retrieving IF-MIB::ifTable for node %s using IP %s\n", nodeLabel, ipAddress);
                    InetAddress address = InetAddressUtils.addr(ipAddress);
                    final SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
                    ifTable = new IfTable(address);
                    SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable", ifTable);
                    walker.start();
                    walker.waitFor();
                } catch (Exception e) {
                    log("Can't retrieve SNMP data from %s\n", ipAddress);
                    continue;
                }
                if (ifTable != null) {
                    log("Updating the SNMP Interfaces for node %s\n", nodeLabel);
                    String update = "update snmpinterface set snmpphysaddr=? where nodeid=? and snmpifindex=?";
                    for (IfTableEntry entry : ifTable.getEntries()) {
                        if (entry.getPhysAddr() == null) {
                            continue;
                        }
                        PreparedStatement upt = conn.prepareStatement(update);
                        d.watch(upt);
                        upt.setString(1, entry.getPhysAddr());
                        upt.setInt(2, nodeId);
                        upt.setInt(3, entry.getIfIndex());
                        upt.executeUpdate();
                    }
                }
            }
            conn.commit();
            conn.close();
        } catch (Exception e) {
            d.cleanUp();
        }
    }
}
