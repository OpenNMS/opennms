/*
 * Creato il 9-set-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.core.utils;

import org.opennms.protocols.snmp.*;
import org.opennms.netmgt.utils.SnmpResponseHandler;
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
    SnmpSession m_session = null;

    SnmpPeer m_snmpPeer = null;

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
    public SnmpIfAdmin(int nodeid, SnmpPeer snmpPeer) throws SocketException {
        m_snmpPeer = snmpPeer;
        this.nodeid = nodeid;
        openSession();
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
        m_snmpPeer = new SnmpPeer(inetAddress);
        m_snmpPeer.getParameters().setWriteCommunity(community);
        openSession();
    }

    /**
     * @throws SocketException
     * @throws Exception
     */
    private void openSession() throws SocketException {
        try {
            m_session = new SnmpSession(m_snmpPeer);
        } catch (SocketException e) {
            if (m_session != null) {
                try {
                    m_session.close();
                } catch (Exception ex) {
                    // to log
                }
            }
            throw e;
        }
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
    public boolean setIfAdminUp(int ifindex) throws SnmpBadConversionException,
            SQLException {
        return setIfAdmin(ifindex, UP);
    }

    public boolean setIfAdminDown(int ifindex)
            throws SnmpBadConversionException, SQLException {
        return setIfAdmin(ifindex, DOWN);
    }

    public boolean isIfAdminStatusUp() throws SnmpBadConversionException {
        return (getIfAdminStatus(UP) == UP);
    }

    public boolean isIfAdminStatusDown() throws SnmpBadConversionException {
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
    public int getIfAdminStatus(int ifindex) throws SnmpBadConversionException {
        SnmpResponseHandler handler = new SnmpResponseHandler();
        SnmpPduPacket out = new SnmpPduRequest(SnmpPduPacket.GET,
                new SnmpVarBind[] { new SnmpVarBind(new SnmpObjectId(
                        snmpObjectId + "." + ifindex)) });
        SnmpPduPacket.nextSequence();
        synchronized (handler) {
            m_session.send(out, handler);
            try {
                handler.wait((long) (m_snmpPeer.getRetries() + 1)
                        * (long) m_snmpPeer.getTimeout());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        int status = NULL;

        if (handler.getResult() != null) {
            String ifCountStr = handler.getResult().getValue().toString();
            try {
                status = Integer.parseInt(ifCountStr);
            } catch (NumberFormatException nfE) {
                throw new SnmpBadConversionException(
                        "Retrieval of interface admin status failed for "
                                + m_snmpPeer.getPeer().getHostAddress());
            }
        }

        return status;
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

    /**
     * <p>
     * Close snmp session
     * </p>
     * 
     * @param ifindex
     *            interface index to get
     * 
     * @return The status of interface in human format
     */
    public void close() {
        try {
            m_session.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
    public boolean setIfAdmin(int ifindex, int value) throws SQLException,
            SnmpBadConversionException, IllegalArgumentException {
        int status = NULL;

        if (value != UP && value != DOWN)
            throw new IllegalArgumentException("Value not valid");
        
        boolean returnValue = false;
        SnmpResponseHandler handler = new SnmpResponseHandler();
        SnmpPduPacket out = new SnmpPduRequest(SnmpPduPacket.SET,
                new SnmpVarBind[] { new SnmpVarBind(new SnmpObjectId(
                        snmpObjectId + "." + ifindex), new SnmpInt32(value)) });

        synchronized (handler) {
            m_session.send(out, handler);
            try {
                handler.wait((long) (m_snmpPeer.getRetries() + 1)
                        * (long) m_snmpPeer.getTimeout());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (handler.getResult() != null) {
            String ifCountStr = handler.getResult().getValue().toString();
            try {
                status = Integer.parseInt(ifCountStr);
            } catch (NumberFormatException nfE) {
                throw new SnmpBadConversionException(
                        "Retrieval of interface admin status failed for "
                                + m_snmpPeer.getPeer().getHostAddress());
            }
        }
        returnValue = (status == value);

        if (returnValue)
            setIfAdminStatusInDB(ifindex, value);

        return (returnValue);
    }

    public static boolean isValidState(int status) {
        return (status == UP && status == DOWN);
    }

    public static void main(String[] args) {
        try {
            int nodeid = 152;
            InetAddress[] inet = InetAddress.getAllByName("10.0.0.250");
            SnmpIfAdmin a = new SnmpIfAdmin(nodeid, inet[0], "private");
            System.out.println("Get");
            int value = a.getIfAdminStatus(14);
            System.out.println(value);
            System.out.println(SnmpIfAdmin.getReadableAdminStatus(value));
            System.out.println("Cut");
            if (a.setIfAdminDown(14)) {
                System.out.println("Admin status interface set to down");
                if (a.setIfAdminUp(14))
                    System.out.println("Admin status interface set up");
            } else
                System.out.println("Set status operation interface failed");
            a.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
