//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 05: Added ORDER BY to SQL statement.
//
// Orignal code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//

package org.opennms.web.element;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.EventConstants;

/**
 * The source for all network element business objects
 * (nodes, interfaces, services).  Encapsulates all lookup 
 * functionality for the network element business
 * objects in one place.  
 *
 * To use this factory to lookup network elements, you
 * must first initialize the Vault with the database 
 * connection manager * and JDBC URL it will use.  
 * Call the init method to initialize the factory.  
 * After that, you can call any lookup methods.
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NetworkElementFactory extends Object
{

    /**
     * A mapping of service names (strings) to service
     * identifiers (integers).
     */
    protected static Map serviceName2IdMap;


    /**
     * A mapping of service identifiers (integers) to
     * service names (strings).
     */
    protected static Map serviceId2NameMap;


    /** 
     * Private, empty constructor so that this class cannot be instantiated.
     * All of its methods should static and accessed through the class name.
     */
    private NetworkElementFactory() {}


    /**
     * Translate a node id into a human-readable node label.
     * Note these values are not cached.
     * 
     * @return A human-readable node name or null if the node id
     * given does not specify a real node.
     */
    public static String getNodeLabel( int nodeId ) throws SQLException {
        String label = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT NODELABEL FROM NODE WHERE NODEID = ?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
    
            if( rs.next() ) {
                label = rs.getString( "NODELABEL" );
            }
    
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return( label );
    }


    public static Node getNode( int nodeId ) throws SQLException {
        Node node = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID = ?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
    
            Node[] nodes = rs2Nodes( rs );
    
            //what do I do if this actually returns more than one node?
            if( nodes.length > 0 ) {
                node = nodes[0];
            }
    
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return node;
    }


    /**
     * Returns all non-deleted nodes.
     */
    public static Node[] getAllNodes() throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM NODE WHERE NODETYPE != 'D' ORDER BY NODELABEL" );
    
            nodes = rs2Nodes( rs );
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return nodes;
    }


    /**
     * Returns all non-deleted nodes that have the given nodeLabel substring
     * somewhere in their nodeLabel.
     */
    public static Node[] getNodesLike( String nodeLabel ) throws SQLException {
        if( nodeLabel == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer( "%" );
            buffer.append( nodeLabel );
            buffer.append( "%" );

            PreparedStatement stmt = conn.prepareStatement( "SELECT * FROM NODE WHERE LOWER(NODELABEL) LIKE ? AND NODETYPE != 'D' ORDER BY NODELABEL" );
            stmt.setString( 1, buffer.toString() );
            ResultSet rs = stmt.executeQuery();
    
            nodes = rs2Nodes( rs );
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return nodes;
    }


    /**
     * Returns all non-deleted nodes with an IP address like the rule given.
     */
    public static Node[] getNodesWithIpLike( String iplike ) throws SQLException {
        if( iplike == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement( "SELECT DISTINCT * FROM NODE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODETYPE != 'D' ORDER BY NODELABEL" );
            stmt.setString( 1, iplike );
            ResultSet rs = stmt.executeQuery();
    
            nodes = rs2Nodes( rs );
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return nodes;
    }


    /**
     * Returns all non-deleted nodes that have the given service.
     */
    public static Node[] getNodesWithService( int serviceId ) throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement( "SELECT * FROM NODE WHERE NODEID IN (SELECT NODEID FROM IFSERVICES WHERE SERVICEID=?) AND NODETYPE != 'D' ORDER BY NODELABEL" );
            stmt.setInt( 1, serviceId );
            ResultSet rs = stmt.executeQuery();
    
            nodes = rs2Nodes( rs );
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return nodes;
    }


    /**
     * Resolve an IP address to a DNS hostname via the database.
     * If no hostname can be found, the given IP address is returned.
     */
    public static String getHostname( String ipAddress ) throws SQLException {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        String hostname = ipAddress;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement( "SELECT DISTINCT IPADDR, IPHOSTNAME FROM IPINTERFACE WHERE IPADDR=? AND IPHOSTNAME IS NOT NULL" );
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();

            if( rs.next() ) {
                hostname = (String)rs.getString( "IPHOSTNAME" );
            }

            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return hostname;
    }


    public static Interface getInterface( int nodeId, String ipAddress) throws SQLException {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Interface intf = null;
        Connection conn = Vault.getDbConnection();        

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IPADDR=?");
            stmt.setInt( 1, nodeId );
            stmt.setString( 2, ipAddress );
            ResultSet rs = stmt.executeQuery();
    
            Interface[] intfs = rs2Interfaces( rs );

            rs.close();
            stmt.close();    

            augmentInterfacesWithSnmpData( intfs, conn ); 
    
            //what do I do if this actually returns more than one node?
            if( intfs.length > 0 ) {
                intf = intfs[0];
            }
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return intf;
    }
    
    public static Interface getInterface( int nodeId, String ipAddress, int ifindex) throws SQLException {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Interface intf = null;
        Connection conn = Vault.getDbConnection();        

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IPADDR=? AND IFINDEX=?");
            stmt.setInt( 1, nodeId );
            stmt.setString( 2, ipAddress );
        stmt.setInt( 3, ifindex );
        
            ResultSet rs = stmt.executeQuery();
    
            Interface[] intfs = rs2Interfaces( rs );

            rs.close();
            stmt.close();    

            augmentInterfacesWithSnmpData( intfs, conn ); 
    
            //what do I do if this actually returns more than one node?
            if( intfs.length > 0 ) {
                intf = intfs[0];
            }
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return intf;
    }


    public static Interface[] getInterfacesWithIpAddress( String ipAddress ) throws SQLException {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();        

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE IPADDR=?");
            stmt.setString( 1, ipAddress );
            ResultSet rs = stmt.executeQuery();
    
            intfs = rs2Interfaces( rs );

            rs.close();
            stmt.close();    

            augmentInterfacesWithSnmpData( intfs, conn ); 
    
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return intfs;
    }


    public static Interface[] getAllInterfacesOnNode( int nodeId ) throws SQLException {
        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ?");
            stmt.setInt( 1, nodeId );
            ResultSet rs = stmt.executeQuery();
    
            intfs = rs2Interfaces( rs );
          
            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData( intfs, conn ); 
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return intfs;
    }

    public static Interface[] getActiveInterfacesOnNode( int nodeId ) throws SQLException {
        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND ISMANAGED != 'D'");
            stmt.setInt( 1, nodeId );
            ResultSet rs = stmt.executeQuery();
    
            intfs = rs2Interfaces( rs );
          
            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData( intfs, conn ); 
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return intfs;
    }


    public static Interface[] getAllInterfaces() throws SQLException {
        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM IPINTERFACE ORDER BY IPHOSTNAME, NODEID, inet(IPADDR)");
    
            intfs = rs2Interfaces( rs );

            rs.close();
            stmt.close();    

            augmentInterfacesWithSnmpData( intfs, conn );
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return intfs;
    }


    /**
     * Return the service specified by the node identifier, IP address, and 
     * service identifier.
     *
     * <p>Note that if there are both an active service and historically deleted 
     * services with this (nodeid, ipAddress, serviceId) key, then the active
     * service will be returned.  If there are only deleted services, then the
     * first deleted service will be returned.</p>
     */
    public static Service getService( int nodeId, String ipAddress, int serviceId ) throws SQLException {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Service service = null;
        Connection conn = Vault.getDbConnection();

        try {
            //big hack here, I'm relying on the fact that the ifservices.status 
            //field uses 'A' as active, and thus should always turn up before any
            //historically deleted services
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.IPADDR=? AND IFSERVICES.SERVICEID=? ORDER BY IFSERVICES.STATUS");
            stmt.setInt( 1, nodeId );
            stmt.setString( 2, ipAddress );
            stmt.setInt( 3, serviceId );
            ResultSet rs = stmt.executeQuery();
    
            Service[] services = rs2Services( rs );

            //only take the first service, which should be the active service,
            //cause we're sorting by status in the SQL statement above; if there
            //are no active services, then the first deleted service will be returned,
            //which is what we want
            if( services.length > 0 ) {
                service = services[0];
            }
          
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return service;
    }


    public static Service[] getAllServices() throws SQLException {
        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID = SERVICE.SERVICEID ORDER BY SERVICE.SERVICEID, inet(IFSERVICES.IPADDR)" );
    
            services = rs2Services( rs );
          
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return services;
    }


    public static Service[] getServicesOnInterface( int nodeId, String ipAddress ) throws SQLException {
        return getServicesOnInterface(nodeId, ipAddress, false);
    }
        
        
    public static Service[] getServicesOnInterface( int nodeId, String ipAddress, boolean includeDeletions ) throws SQLException {        
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.IPADDR=?");
            
            if( !includeDeletions ) {
                buffer.append(" AND IFSERVICES.STATUS <> 'D'");
            }

            PreparedStatement stmt = conn.prepareStatement(buffer.toString());
            stmt.setInt( 1, nodeId );
            stmt.setString( 2, ipAddress );
            ResultSet rs = stmt.executeQuery();

            services = rs2Services( rs );
          
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return services;
    }

    
    /**
     * Get the list of all services on a given node.
     */
    public static Service[] getServicesOnNode( int nodeId ) throws SQLException {
        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=?");
            stmt.setInt( 1, nodeId );
            ResultSet rs = stmt.executeQuery();

            services = rs2Services( rs );
          
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return services;
    }


    /**
     * Get the list of all instances of a specific service on a given node. 
     */
    public static Service[] getServicesOnNode( int nodeId, int serviceId ) throws SQLException {
        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.SERVICEID=?");
            stmt.setInt( 1, nodeId );
            stmt.setInt( 2, serviceId );
            ResultSet rs = stmt.executeQuery();

            services = rs2Services( rs );
          
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return services;
    }
    
    
    /**
     * This method returns the data from the result set as an array of Node objects.
     */
    protected static Node[] rs2Nodes(ResultSet rs) throws SQLException {
        if( rs == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Node[] nodes = null;
        Vector vector = new Vector();

        while( rs.next() ) {
            Node node = new Node();
    
            Object element = new Integer(rs.getInt("nodeId"));
            node.m_nodeId = ((Integer)element).intValue();
        
            element = rs.getString("dpName");
            node.m_dpname = (String)element;
        
            element = rs.getTimestamp("nodeCreateTime");
            if (element != null)
                node.m_nodeCreateTime = EventConstants.formatToString(new Date(((Timestamp)element).getTime()));
        
            element = new Integer(rs.getInt("nodeParentID"));
            if( element != null ) {
                node.m_nodeParent = ((Integer)element).intValue();
            }
        
            element = rs.getString("nodeType");
            if( element != null ) {            
                node.m_nodeType = ((String)element).charAt(0);
            }
        
            element = rs.getString("nodeSysOID");
            node.m_nodeSysId = (String)element;
        
            element = rs.getString("nodeSysName");
            node.m_nodeSysName = (String)element;
        
            element = rs.getObject("nodeSysDescription");
            node.m_nodeSysDescr = (String)element;
        
            element = rs.getString("nodeSysLocation");
            node.m_nodeSysLocn = (String)element;
        
            element = rs.getString("nodeSysContact");
            node.m_nodeSysContact = (String)element;

            element = rs.getString("nodelabel");
            node.m_label = (String)element;

            element = rs.getString("operatingsystem");
            node.m_operatingSystem = (String)element;

            vector.addElement( node );
        }

        nodes = new Node[vector.size()];

        for( int i=0; i < nodes.length; i++ ) {
            nodes[i] = (Node)vector.elementAt(i);
        }

        return( nodes );
    }



    /**
     * This method returns the data from the result set as an vector of ipinterface objects.
     */
    protected static Interface[] rs2Interfaces(ResultSet rs) throws SQLException {
        Interface[] intfs = null;
        Vector vector = new Vector();

        while(rs.next()) {
            Interface intf = new Interface();

            Object element = new Integer(rs.getInt("nodeid"));
            intf.m_nodeId = ((Integer)element).intValue();

            element = new Integer(rs.getInt("ifIndex"));
            intf.m_ifIndex = ((Integer)element).intValue();
        
            element = new Integer(rs.getInt("ipStatus"));
            intf.m_ipStatus = ((Integer)element).intValue();

            element = rs.getString("ipHostname");
            intf.m_ipHostName = (String)element;

            element = rs.getString("ipAddr");
            intf.m_ipAddr = (String)element;

            element = rs.getString("isManaged");
            if( element != null ) {
                intf.m_isManaged = ((String)element).charAt(0);
            }

            element = rs.getTimestamp("ipLastCapsdPoll");
            if (element != null)
                intf.m_ipLastCapsdPoll = EventConstants.formatToString(new Date(((Timestamp)element).getTime()));

            vector.addElement( intf ) ;
        }

        intfs = new Interface[vector.size()];

        for( int i = 0;i < intfs.length; i++ ) {
            intfs[i] = (Interface)vector.elementAt(i);
        }

        return intfs;
    }


    protected static void augmentInterfacesWithSnmpData( Interface[] intfs, Connection conn ) throws SQLException {
        if( intfs == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
    
    for( int i=0; i < intfs.length; i++ ) {
            if (intfs[i].getIfIndex() != 0) {
                PreparedStatement pstmt = conn.prepareStatement( "SELECT * FROM SNMPINTERFACE WHERE NODEID=? AND SNMPIFINDEX=?" );
            pstmt.setInt( 1, intfs[i].getNodeId() );
                pstmt.setInt( 2, intfs[i].getIfIndex() );
        
            ResultSet rs = pstmt.executeQuery();

            if( rs.next() ) {
                Object element = new Integer(rs.getInt("snmpifindex"));
                intfs[i].m_snmpIfIndex = ((Integer)element).intValue();
    
                element = rs.getString("snmpIpAdEntNetMask");
                intfs[i].m_snmpIpAdEntNetMask = (String)element;
    
                element = rs.getString("snmpPhysAddr");
                intfs[i].m_snmpPhysAddr = (String)element;
    
                element = rs.getString("snmpIfDescr");
                intfs[i].m_snmpIfDescr = (String)element;
    
                element = rs.getString("snmpIfName");
                intfs[i].m_snmpIfName = (String)element;
    
                element = new Integer(rs.getInt("snmpIfType"));
                intfs[i].m_snmpIfType = ((Integer)element).intValue();
    
                element = new Integer(rs.getInt("snmpIfOperStatus"));
                intfs[i].m_snmpIfOperStatus = ((Integer)element).intValue();
    
                element = new Integer(rs.getInt("snmpIfSpeed"));
                intfs[i].m_snmpIfSpeed = ((Integer)element).intValue();
    
                element = new Integer(rs.getInt("snmpIfAdminStatus"));
                intfs[i].m_snmpIfAdminStatus = ((Integer)element).intValue();                
            }

            rs.close();
        pstmt.close();
            }                
        }
    }




    protected static Service[] rs2Services( ResultSet rs ) throws SQLException {
        Service[] services = null;
        Vector vector = new Vector();

        while(rs.next()) {
            Service service = new Service();
        
            Object element = new Integer(rs.getInt("nodeid"));
            service.m_nodeId = ((Integer)element).intValue();
        
            element = new Integer(rs.getInt("ifindex"));
            service.m_ifIndex = ((Integer)element).intValue();
        
            element = rs.getString("ipaddr");
            service.m_ipAddr = (String)element;
        
            element = rs.getTimestamp("lastgood");
            if (element != null)
                service.m_lastGood = EventConstants.formatToString(new Date(((Timestamp)element).getTime()));
        
            element = new Integer(rs.getInt("serviceid"));
            service.m_serviceId = ((Integer)element).intValue();
        
            element = rs.getString("servicename");
            service.m_serviceName = (String)element;
        
            element = rs.getTimestamp("lastfail");
            if (element != null)
                service.m_lastFail = EventConstants.formatToString(new Date(((Timestamp)element).getTime()));
            
            element = rs.getString("notify");
            service.m_notify = (String)element;
            
            element = rs.getString("status");
            if( element != null ) {
                service.m_status = ((String)element).charAt(0);
            }
    
            vector.addElement( service );
        }

        services = new Service[vector.size()];

        for( int i=0; i < services.length; i++ ) {
            services[i] = (Service)vector.elementAt(i);
        }

        return services;
    }


    public static String getServiceNameFromId( int serviceId ) throws SQLException {
        if( serviceId2NameMap == null ) {
            createServiceIdNameMaps();
        }

        String serviceName = (String)serviceId2NameMap.get( new Integer(serviceId) );

        return( serviceName );
    }
 

    public static int getServiceIdFromName( String serviceName ) throws SQLException {
        if( serviceName == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        int serviceId = -1;

        if( serviceName2IdMap == null ) {
            createServiceIdNameMaps();
        }

        Integer value = (Integer)serviceName2IdMap.get( serviceName );

        if( value != null ) {
            serviceId = value.intValue();
        }

        return( serviceId );
    }


    public static Map getServiceIdToNameMap() throws SQLException {
        if( serviceId2NameMap == null ) {
            createServiceIdNameMaps();
        }

        return( new HashMap( serviceId2NameMap ));
    }


    public static Map getServiceNameToIdMap() throws SQLException {
        if( serviceName2IdMap == null ) {
            createServiceIdNameMaps();
        }

        return( new HashMap( serviceName2IdMap ));
    }


    protected static void createServiceIdNameMaps() throws SQLException {
        HashMap idMap = new HashMap();
        HashMap nameMap = new HashMap();
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT SERVICEID, SERVICENAME FROM SERVICE" );

            while( rs.next() ) {
                int id = rs.getInt( "SERVICEID" );
                String name = rs.getString( "SERVICENAME" );

                idMap.put( new Integer(id), name );
                nameMap.put( name, new Integer(id) );
            }
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        serviceId2NameMap = idMap;
        serviceName2IdMap = nameMap;
    }

}
