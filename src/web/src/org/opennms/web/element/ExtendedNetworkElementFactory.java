/*
 * Creato il 22-giu-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.opennms.web.element.Node;
import org.opennms.web.element.AtInterface;
import org.opennms.web.element.IpRouteInterface;
import org.opennms.web.element.StpNode;
import org.opennms.web.element.StpInterface;
import org.opennms.web.element.DataLinkInterface;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.EventConstants;

import java.util.Vector;

/**
 * @author micmas
 * @author rssntn67
 * 
 * Per modificare il modello associato al commento di questo tipo generato,
 * aprire Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e
 * commenti
 * 
 * 31/10/2004 Aggiunti i metodi per accedere alle nuove tabelle atinterface,
 * iprouteinterface, stpnode, stpinterface datalinkinterface
 *  
 */
public class ExtendedNetworkElementFactory {
    public static Node[] getNodesLikeAndIpLike(String nodeLabel, String iplike,
            int serviceId) throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) AND NODETYPE != 'D' AND LOWER(NODELABEL) LIKE ? AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODE.NODEID=IPINTERFACE.NODEID ORDER BY NODELABEL");
            stmt.setInt(1, serviceId);
            stmt.setString(2, buffer.toString());
            stmt.setString(3, iplike);

            ResultSet rs = stmt.executeQuery();

            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getNodesLike(String nodeLabel, int serviceId)
            throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM NODE WHERE LOWER(NODELABEL) LIKE ? AND NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            stmt.setString(1, buffer.toString());
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();

            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getNodesWithIpLike(String iplike, int serviceId)
            throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT * FROM NODE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            stmt.setString(1, iplike);
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();

            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getAllNodes(int serviceId) throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM NODE WHERE NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            stmt.setInt(1, serviceId);
            ResultSet rs = stmt.executeQuery();
            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static AtInterface[] getAtInterfacesFromPhysaddr(String AtPhysAddr)
            throws SQLException {

        if (AtPhysAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM ATINTERFACE WHERE ATPHYSADDR LIKE '%"
                            + AtPhysAddr + "%' AND STATUS != 'D'");
            ResultSet rs = stmt.executeQuery();
            nodes = rs2AtInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getNodesFromPhysaddr(String AtPhysAddr)
            throws SQLException {

        if (AtPhysAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT(*) FROM IPINTERFACE WHERE NODEID IN "
                            + "(SELECT NODEID FROM ATINTERFACE WHERE ATPHYSADDR LIKE '%"
                            + AtPhysAddr + "%' AND STATUS != 'D'");
            ResultSet rs = stmt.executeQuery();
            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static AtInterface getAtInterface(int nodeID, String ipaddr)
            throws SQLException {

        if (ipaddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        AtInterface node = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM ATINTERFACE WHERE NODEID = ? AND IPADDR = ? AND STATUS != 'D'");
            stmt.setInt(1, nodeID);
            stmt.setString(2, ipaddr);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2AtInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
        if (nodes.length > 0) {
            return nodes[0];
        }
        return node;
    }

    public static IpRouteInterface[] getIpRoute(int nodeID) throws SQLException {

        IpRouteInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM IPROUTEINTERFACE WHERE NODEID = ? AND STATUS != 'D' ORDER BY ROUTEDEST");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2IpRouteInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static IpRouteInterface[] getIpRoute(int nodeID, int ifindex)
            throws SQLException {

        IpRouteInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM IPROUTEINTERFACE WHERE NODEID = ? AND ROUTEIFINDEX = ? AND STATUS != 'D' ORDER BY ROUTEDEST");
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2IpRouteInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static boolean isParentNode(int nodeID) throws SQLException {

        Connection conn = Vault.getDbConnection();
        boolean isPN = false;
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isPN = true;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return isPN;
    }

    public static boolean isBridgeNode(int nodeID) throws SQLException {

        Connection conn = Vault.getDbConnection();
        boolean isPN = false;
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) FROM STPNODE WHERE NODEID = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isPN = true;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return isPN;
    }

    public static boolean isRouteInfoNode(int nodeID) throws SQLException {

        Connection conn = Vault.getDbConnection();
        boolean isRI = false;
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) FROM IPROUTEINTERFACE WHERE NODEID = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isRI = true;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return isRI;
    }

    public static DataLinkInterface[] getDataLinks(int nodeID)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' ORDER BY IFINDEX");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static DataLinkInterface getDataLink(int nodeID, int ifindex)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        DataLinkInterface node = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' AND IFINDEX = ?");
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        if (nodes.length > 0) {
            return nodes[0];
        }
        return node;
    }

    public static DataLinkInterface[] getDataLinks(int nodeID, String IpAddress)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();
        Interface iface = NetworkElementFactory.getInterface(nodeID, IpAddress);
        int ifindex = iface.getIfIndex();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' AND IFINDEX = ?");
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static DataLinkInterface[] getDataLinksFromNodeParent(int nodeID)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D' ORDER BY PARENTIFINDEX");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static DataLinkInterface[] getDataLinksFromNodeParent(int nodeID,
            int ifindex) throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND PARENTIFINDEX = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static DataLinkInterface[] getAllDataLinks() throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE STATUS != 'D' ORDER BY NODEID, IFINDEX");
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static StpInterface[] getStpInterface(int nodeID)
            throws SQLException {

        StpInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {

            String sqlQuery = "SELECT DISTINCT(stpnode.nodeid) AS droot, stpinterfacedb.* FROM "
                    + "((SELECT DISTINCT(stpnode.nodeid) AS dbridge, stpinterface.* FROM "
                    + "stpinterface LEFT JOIN stpnode ON SUBSTR(stpportdesignatedbridge,5,16) = stpnode.basebridgeaddress "
                    + "WHERE stpinterface.status != 'D' AND stpinterface.nodeid = ?) AS stpinterfacedb "
                    + "LEFT JOIN stpnode ON SUBSTR(stpportdesignatedroot, 5, 16) = stpnode.basebridgeaddress) order by stpinterfacedb.stpvlan, stpinterfacedb.ifindex;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2StpInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static StpInterface[] getStpInterface(int nodeID, int ifindex)
            throws SQLException {

        StpInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            String sqlQuery = "SELECT DISTINCT(stpnode.nodeid) AS droot, stpinterfacedb.* FROM "
                + "((SELECT DISTINCT(stpnode.nodeid) AS dbridge, stpinterface.* FROM "
                + "stpinterface LEFT JOIN stpnode ON SUBSTR(stpportdesignatedbridge,5,16) = stpnode.basebridgeaddress "
                + "WHERE stpinterface.status != 'D' AND stpinterface.nodeid = ? AND stpinterface.ifindex = ?) AS stpinterfacedb "
                + "LEFT JOIN stpnode ON SUBSTR(stpportdesignatedroot, 5, 16) = stpnode.basebridgeaddress) order by stpinterfacedb.stpvlan, stpinterfacedb.ifindex;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2StpInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static StpNode[] getStpNode(int nodeID) throws SQLException {

        StpNode[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement(
                    //		"SELECT * FROM STPNODE WHERE NODEID = ? AND STATUS != 'D'
                    // ORDER BY basevlan");
                    "select distinct(e2.nodeid) as stpdesignatedrootnodeid, e1.* from (stpnode e1 left join stpnode e2 on substr(e1.stpdesignatedroot, 5, 16) = e2.basebridgeaddress) where e1.nodeid = ? AND e1.status != 'D' ORDER BY e1.basevlan");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2StpNode(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * This method returns the data from the result set as an array of
     * AtInterface objects.
     */
    protected static AtInterface[] rs2AtInterface(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        Vector vector = new Vector();

        while (rs.next()) {
            AtInterface node = new AtInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            node.m_nodeId = ((Integer) element).intValue();

            element = rs.getString("ipaddr");
            node.m_ipaddr = (String) element;

            element = rs.getString("atphysaddr");
            node.m_physaddr = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null)
                node.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));

            element = new Integer(rs.getInt("sourcenodeID"));
            if (element != null) {
                node.m_sourcenodeid = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("ifindex"));
            if (element != null) {
                node.m_ifindex = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                node.m_status = ((String) element).charAt(0);
            }

            vector.addElement(node);
        }

        nodes = new AtInterface[vector.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = (AtInterface) vector.elementAt(i);
        }

        return (nodes);
    }

    /**
     * This method returns the data from the result set as an array of
     * IpRouteInterface objects.
     */
    protected static IpRouteInterface[] rs2IpRouteInterface(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        IpRouteInterface[] nodes = null;
        Vector vector = new Vector();

        while (rs.next()) {
            IpRouteInterface node = new IpRouteInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            node.m_nodeId = ((Integer) element).intValue();

            element = rs.getString("routedest");
            node.m_routedest = (String) element;

            element = rs.getString("routemask");
            node.m_routemask = (String) element;

            element = rs.getString("routenexthop");
            node.m_routenexthop = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null)
                node.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));

            element = new Integer(rs.getInt("routeifindex"));
            if (element != null) {
                node.m_routeifindex = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric1"));
            if (element != null) {
                node.m_routemetric1 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric2"));
            if (element != null) {
                node.m_routemetric2 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric3"));
            if (element != null) {
                node.m_routemetric4 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric4"));
            if (element != null) {
                node.m_routemetric4 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric5"));
            if (element != null) {
                node.m_routemetric5 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routetype"));
            if (element != null) {
                node.m_routetype = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routeproto"));
            if (element != null) {
                node.m_routeproto = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                node.m_status = ((String) element).charAt(0);
            }

            vector.addElement(node);
        }

        nodes = new IpRouteInterface[vector.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = (IpRouteInterface) vector.elementAt(i);
        }

        return (nodes);
    }

    /**
     * This method returns the data from the result set as an array of
     * StpInterface objects.
     */
    protected static StpInterface[] rs2StpInterface(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StpInterface[] nodes = null;
        Vector vector = new Vector();

        while (rs.next()) {
            StpInterface node = new StpInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            node.m_nodeId = ((Integer) element).intValue();

            element = rs.getTimestamp("lastpolltime");
            if (element != null)
                node.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));

            element = new Integer(rs.getInt("bridgeport"));
            if (element != null) {
                node.m_bridgeport = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("ifindex"));
            if (element != null) {
                node.m_ifindex = ((Integer) element).intValue();
            }

            element = rs.getString("stpportdesignatedroot");
            node.m_stpdesignatedroot = (String) element;

            element = new Integer(rs.getInt("stpportdesignatedcost"));
            if (element != null) {
                node.m_stpportdesignatedcost = ((Integer) element).intValue();
            }

            element = rs.getString("stpportdesignatedbridge");
            node.m_stpdesignatedbridge = (String) element;

            element = rs.getString("stpportdesignatedport");
            node.m_stpdesignatedport = (String) element;

            element = new Integer(rs.getInt("stpportpathcost"));
            if (element != null) {
                node.m_stpportpathcost = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stpportstate"));
            if (element != null) {
                node.m_stpportstate = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stpvlan"));
            if (element != null) {
                node.m_stpvlan = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                node.m_status = ((String) element).charAt(0);
            }

            element = new Integer(rs.getInt("dbridge"));
            if (element != null) {
                node.m_stpbridgenodeid = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("droot"));
            if (element != null) {
                node.m_stprootnodeid = ((Integer) element).intValue();
            }

            vector.addElement(node);
        }

        nodes = new StpInterface[vector.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = (StpInterface) vector.elementAt(i);
        }

        return (nodes);
    }

    /**
     * This method returns the data from the result set as an array of StpNode
     * objects.
     */
    protected static StpNode[] rs2StpNode(ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StpNode[] nodes = null;
        Vector vector = new Vector();

        while (rs.next()) {
            StpNode node = new StpNode();

            Object element = new Integer(rs.getInt("nodeId"));
            node.m_nodeId = ((Integer) element).intValue();

            element = rs.getString("basebridgeaddress");
            node.m_basebridgeaddress = (String) element;

            element = rs.getString("stpdesignatedroot");
            node.m_stpdesignatedroot = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null)
                node.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));

            element = new Integer(rs.getInt("basenumports"));
            if (element != null) {
                node.m_basenumports = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("basetype"));
            if (element != null) {
                node.m_basetype = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("basevlan"));
            if (element != null) {
                node.m_basevlan = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stppriority"));
            if (element != null) {
                node.m_stppriority = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stpprotocolspecification"));
            if (element != null) {
                node.m_stpprotocolspecification = ((Integer) element)
                        .intValue();
            }

            element = new Integer(rs.getInt("stprootcost"));
            if (element != null) {
                node.m_stprootcost = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stprootport"));
            if (element != null) {
                node.m_stprootport = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                node.m_status = ((String) element).charAt(0);
            }

            element = new Integer(rs.getInt("stpdesignatedrootnodeid"));
            if (element != null) {
                node.m_stprootnodeid = ((Integer) element).intValue();
            }

            vector.addElement(node);
        }

        nodes = new StpNode[vector.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = (StpNode) vector.elementAt(i);
        }

        return (nodes);
    }

    /**
     * This method returns the data from the result set as an array of
     * DataLinkInterface objects.
     */
    protected static DataLinkInterface[] rs2DataLink(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        DataLinkInterface[] nodes = null;
        Vector vector = new Vector();

        while (rs.next()) {
            DataLinkInterface node = new DataLinkInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            node.m_nodeId = ((Integer) element).intValue();

            element = new Integer(rs.getInt("ifindex"));
            if (element != null) {
                node.m_ifindex = ((Integer) element).intValue();
            }

            element = rs.getTimestamp("lastpolltime");
            if (element != null)
                node.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));

            element = new Integer(rs.getInt("nodeparentid"));
            if (element != null) {
                node.m_nodeparentid = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("parentifindex"));
            if (element != null) {
                node.m_parentifindex = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                node.m_status = ((String) element).charAt(0);
            }

            node.m_parentipaddress = getIpAddress(node.get_nodeparentid(), node
                    .get_parentifindex());

            if (node.get_ifindex() == 0) {
                node.m_ipaddress = getIpAddress(node.get_nodeId());
            } else {
                node.m_ipaddress = getIpAddress(node.get_nodeId(), node
                        .get_ifindex());
            }

            vector.addElement(node);
        }

        nodes = new DataLinkInterface[vector.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = (DataLinkInterface) vector.elementAt(i);
        }

        return (nodes);
    }

    private static String getIpAddress(int nodeid) throws SQLException {

        String ipaddr = null;
        Connection conn = Vault.getDbConnection();

        try {

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT(IPADDR) FROM IPINTERFACE WHERE NODEID = ?");
            stmt.setInt(1, nodeid);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ipaddr = rs.getString("ipaddr");
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return ipaddr;

    }

    public static String getIpAddress(int nodeid, int ifindex)
            throws SQLException {
        String ipaddr = null;
        Connection conn = Vault.getDbConnection();

        try {

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT(IPADDR) FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX = ? ");
            stmt.setInt(1, nodeid);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ipaddr = rs.getString("ipaddr");
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return ipaddr;

    }

}
