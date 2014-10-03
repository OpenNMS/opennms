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

/*
 * Creato il 9-set-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

import java.net.InetAddress;
import java.net.SocketException;
import java.sql.SQLException;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpBadConversionException;

//import java.io.IOException;
/**
 * <p>SnmpIfAdmin class.</p>
 *
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato,
 * aprire Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e
 * commenti
 * @version $Id: $
 * @since 1.8.1
 */
public class SnmpIfAdmin {

    SnmpAgentConfig m_agent = null;

    int nodeid = -1;

    /** Constant <code>NULL=0</code> */
    public static final int NULL = 0;

    /** Constant <code>UP=1</code> */
    public static final int UP = 1;

    /** Constant <code>DOWN=2</code> */
    public static final int DOWN = 2;

    /** Constant <code>TESTING=3</code> */
    public static final int TESTING = 3;

    private static final String[] m_value = { "null", "up", "down", "testing" };

    //	.iso.org.dod.internet.mgmt.mib-2.interfaces.ifTable.ifEntry.ifAdminStatus
    private static final String snmpObjectId = ".1.3.6.1.2.1.2.2.1.7";

    /**
     * Construct a SnmpIfAdmin object from a SnmpPeer object
     *
     * @throws java.net.SocketException if any.
     * @throws java.net.SocketException if any.
     * @param nodeid a int.
     * @param agent a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpIfAdmin(int nodeid, SnmpAgentConfig agent) throws SocketException {
        m_agent = agent;
        this.nodeid = nodeid;
    }

    /**
     * Construct a SnmpIfAdmin object from inetaddress object
     *
     * @param inetAddress a {@link java.net.InetAddress} object.
     * @param community a {@link java.lang.String} object.
     * @throws java.net.SocketException if any.
     * @throws java.net.SocketException if any.
     * @param nodeid a int.
     * @throws java.lang.Exception if any.
     */
    public SnmpIfAdmin(int nodeid, InetAddress inetAddress, String community)
            throws SocketException, Exception {
        this.nodeid = nodeid;
        m_agent = new SnmpAgentConfig(inetAddress);
        m_agent.setWriteCommunity(community);
    }

    /**
     * <p>
     * Set admin interface status to "up".
     * </p>
     *
     * @param ifindex
     *            interface index to set
     * @return The status of interface
     * @throws SnmpBadConversionException
     *             Throw if returned code is not an integer
     * @throws java.sql.SQLException if any.
     */
    public boolean setIfAdminUp(int ifindex) throws SQLException {
        return setIfAdmin(ifindex, UP);
    }

    /**
     * <p>setIfAdminDown</p>
     *
     * @param ifindex a int.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean setIfAdminDown(int ifindex) throws SQLException {
        return setIfAdmin(ifindex, DOWN);
    }

    /**
     * <p>isIfAdminStatusUp</p>
     *
     * @return a boolean.
     */
    public boolean isIfAdminStatusUp() {
        return (getIfAdminStatus(UP) == UP);
    }

    /**
     * <p>isIfAdminStatusDown</p>
     *
     * @return a boolean.
     */
    public boolean isIfAdminStatusDown() {
        return (getIfAdminStatus(DOWN) == DOWN);
    }

    /**
     * <p>
     * Get desired admin interface status.
     * </p>
     *
     * @param ifindex
     *            interface index to get
     * @return The status of interface
     * @throws SnmpBadConversionException
     *             Throw if returned code is not an integer
     */
    public int getIfAdminStatus(int ifindex) {

        SnmpObjId oid = SnmpObjId.get(snmpObjectId + "." +ifindex);
        SnmpValue status = SnmpUtils.get(m_agent, oid);

        return status.toInt();
    }

    /**
     * <p>
     * Get status in readable human format.
     * </p>
     *
     * @return The status of interface in human format
     * @param value a int.
     */
    public static String getReadableAdminStatus(int value) {
        return m_value[value];
    }

    private void setIfAdminStatusInDB(int ifindex, int value) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            java.sql.Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
            java.sql.PreparedStatement stmt = conn.prepareStatement("update snmpinterface set snmpifadminstatus = ? where nodeid = ? and snmpifindex=?;");
            d.watch(stmt);
            stmt.setInt(1, value);
            stmt.setInt(2, nodeid);
            stmt.setInt(3, ifindex);
            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }

    /**
     * <p>
     * Set admin interface status to value.
     * </p>
     *
     * @param ifindex
     *            interface index to set
     * @param value
     *            desired interface status value
     * @return The status of interface after operation
     * @throws SnmpBadConversionException
     *             Throw if returned code is not an integer
     * @throws java.sql.SQLException if any.
     */
    public boolean setIfAdmin(int ifindex, int value) throws SQLException {

        if (value != UP && value != DOWN)
            throw new IllegalArgumentException("Value not valid");

        SnmpObjId oid = SnmpObjId.get(snmpObjectId + "." +ifindex);
        SnmpValue val = SnmpUtils.getValueFactory().getInt32(value);
        SnmpValue result = SnmpUtils.set(m_agent, oid,val);

        if (result != null && result.isNumeric()) {
            int retvalue = result.toInt();
            setIfAdminStatusInDB(ifindex, retvalue);
            if (retvalue == value) return true;
        }
        return false;
    }

    /**
     * <p>isValidState</p>
     *
     * @param status a int.
     * @return a boolean.
     */
    public static boolean isValidState(int status) {
        return (status == UP && status == DOWN);
    }

}
