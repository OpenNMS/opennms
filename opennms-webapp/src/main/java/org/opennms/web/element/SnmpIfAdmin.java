/*
 * Creato il 9-set-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

import java.net.*;
import java.sql.SQLException;

//import java.io.IOException;
/**
 * @author micmas
 * 
 * Per modificare il modello associato al commento di questo tipo generato,
 * aprire Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e
 * commenti
 */
public class SnmpIfAdmin {

    SnmpAgentConfig m_agent = null;

    int nodeid = -1;

    public static final int NULL = 0;

    public static final int UP = 1;

    public static final int DOWN = 2;

    public static final int TESTING = 3;

    private static final String m_value[] = { "null", "up", "down", "testing" };

    //	.iso.org.dod.internet.mgmt.mib-2.interfaces.ifTable.ifEntry.ifAdminStatus
    private static final String snmpObjectId = ".1.3.6.1.2.1.2.2.1.7";

    /**
     * Construct a SnmpIfAdmin object from a SnmpPeer object
     * 
     * @param snmpPeer
     * @param conn
     * @throws SocketException
     * @throws Exception
     */
    public SnmpIfAdmin(int nodeid, SnmpAgentConfig agent) throws SocketException {
        m_agent = agent;
        this.nodeid = nodeid;
    }

    /**
     * Construct a SnmpIfAdmin object from inetaddress object
     * 
     * @param inetAddress
     * @param conn
     * @param community
     * @throws SocketException
     * @throws Exception
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
     * 
     * @return The status of interface
     * @throws SnmpBadConversionException
     *             Throw if returned code is not an integer
     */
    public boolean setIfAdminUp(int ifindex) throws SQLException {
        return setIfAdmin(ifindex, UP);
    }

    public boolean setIfAdminDown(int ifindex) throws SQLException {
        return setIfAdmin(ifindex, DOWN);
    }

    public boolean isIfAdminStatusUp() {
        return (getIfAdminStatus(UP) == UP);
    }

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
     * 
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
     * @param ifindex
     *            interface index to get
     * 
     * @return The status of interface in human format
     */
    public static String getReadableAdminStatus(int value) {
        return m_value[value];
    }

    private void setIfAdminStatusInDB(int ifindex, int value)
            throws SQLException {
        java.sql.Connection conn = null;
        try {
            conn = org.opennms.core.resource.Vault.getDbConnection();
            java.sql.PreparedStatement stmt = conn
                    .prepareStatement("update snmpinterface set snmpifadminstatus = ? where nodeid = ? and snmpifindex=?;");
            stmt.setInt(1, value);
            stmt.setInt(2, nodeid);
            stmt.setInt(3, ifindex);
            stmt.execute();
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
            }
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
     * 
     * @return The status of interface after operation
     * @throws SnmpBadConversionException
     *             Throw if returned code is not an integer
     */
    public boolean setIfAdmin(int ifindex, int value) throws SQLException {

        if (value != UP && value != DOWN)
            throw new IllegalArgumentException("Value not valid");

        SnmpObjId oid = SnmpObjId.get(snmpObjectId + "." +ifindex);
        SnmpValue val = SnmpUtils.getValueFactory().getInt32(value);
        boolean status = SnmpUtils.set(m_agent, oid,val);

        if (status)
            setIfAdminStatusInDB(ifindex, value);

        return (status);
    }

    public static boolean isValidState(int status) {
        return (status == UP && status == DOWN);
    }

}
