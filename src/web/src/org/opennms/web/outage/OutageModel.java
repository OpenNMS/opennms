//
// Copyright (C) 2001 Oculan Corp.
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
//	Brian Weaver   <weave@opennms.org>
//	http://www.opennms.org/
//
//

package org.opennms.web.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import org.opennms.core.resource.Vault;


/**
 * As the nonvisual logic for the Services Down (Outage) servlet
 * and JSPs, this class queries the database for current outages 
 * and provides utility methods for manipulating that list of outages.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A> 
 */
public class OutageModel extends Object
{
    /**
     * Create a new <code>OutageModel</code>. 
     */
    public OutageModel() {}


    /**
     * Query the database to retrieve the current outages. 
     * 
     * @returns An array of {@link Outage Outage} objects,
     * or if there are none, an empty array.
     * @throws SQLException If there is a problem getting a database connection
     * or making a query.
     */             
    public Outage[] getCurrentOutages() throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select outages.nodeId, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId, outages.iflostservice, outages.svclosteventid, notifications.notifyId, notifications.answeredBy from outages left outer join notifications on (outages.svclosteventid=notifications.eventid) where ifregainedservice is null and node.nodeid=outages.nodeid and ipinterface.ipaddr=outages.ipaddr and outages.serviceid=service.serviceid order by nodelabel, ipaddr, serviceName");
    
            outages = rs2Outages(rs, false, true);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }
                
        return outages;
    }


    public int getCurrentOutageCount() throws SQLException {
        int count = 0;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select count(iflostservice) from outages where ifregainedservice is null;");
    
            if(rs.next()) {
                count = rs.getInt("count");
            }
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }
                
        return count;
    }
            
    
    public Outage[] getCurrentOutagesForNode(int nodeId) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr and ifregainedservice is null order by iflostservice desc");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs, false);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }

    
    public Outage[] getNonCurrentOutagesForNode(int nodeId ) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr and ifregainedservice is not null order by iflostservice desc");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }


    /**
     * Get all outages for a given node.
     */
    public Outage[] getOutagesForNode( int nodeId ) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr order by iflostservice desc");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }

                
    /**
     * Get all current outages and any resolved outages since the given time for
     * the given node.
     *
     * @param nodeId this is the node to query
     * @param time no resolved outages older than this time will be returned
     * @return All current outages and resolved outages no older than <code>time</code>.
     */
    public Outage[] getOutagesForNode( int nodeId, Date time ) throws SQLException {
        if( time == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
            }
            
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();
        long timeLong = time.getTime();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr and (ifregainedservice >= ? or ifregainedservice is null) order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setTimestamp( 2, new Timestamp(timeLong));
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }
    
    
    public Outage[] getOutagesForInterface( int nodeId, String ipInterface ) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and outages.ipaddr=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipInterface);
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }


    /**
     * Get all current outages and any resolved outages since the given time for
     * the given interface.
     *
     * @param nodeId this is the node to query
     * @param ipAddr this is the interface to query
     * @param time no resolved outages older than this time will be returned
     * @return All current outages and resolved outages no older than <code>time</code>.
     */
    public Outage[] getOutagesForInterface( int nodeId, String ipAddr, Date time ) throws SQLException {
        if( ipAddr == null || time == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();
        long timeLong = time.getTime();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and ipaddr=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr and (ifregainedservice >= ? or ifregainedservice is null) order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setTimestamp(3, new Timestamp(timeLong));
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }
    
    
    public Outage[] getOutagesForService( int nodeId, String ipInterface, int serviceId) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and outages.ipaddr=? and outages.serviceid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipInterface);
            stmt.setInt(3, serviceId);
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();    
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }

                
    /**
     * Get all current outages and any resolved outages since the given time for
     * the given service.
     *
     * @param nodeId this is the node to query
     * @param ipAddr this is the interface to query
     * @param serviceId this is the service to query
     * @param time no resolved outages older than this time will be returned
     * @return All current outages and resolved outages no older than <code>time</code>.
     */
    public Outage[] getOutagesForService( int nodeId, String ipAddr, int serviceId, Date time ) throws SQLException {
        if( ipAddr == null || time == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
            }
            
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();
        long timeLong = time.getTime();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT iflostservice, ifregainedservice, nodeID, node.nodeLabel, ipaddr, ipinterface.iphostname, service.servicename, serviceId from outages where nodeid=? and ipaddr=? and serviceid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr and (ifregainedservice >= ? or ifregainedservice is null) order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setInt(3, serviceId);
            stmt.setTimestamp(4, new Timestamp(timeLong));
            ResultSet rs = stmt.executeQuery();
    
            outages = rs2Outages(rs);
            
            rs.close();
            stmt.close();
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return outages;
    }


    /**
     * Return a list of IP addresses, the number of services down on each
     * IP address, and the longest time a service has been down for each
     * IP address.  The list will be sorted in ascending order from the
     * service down longest to the service down shortest.
     */ 
    public OutageSummary[] getCurrentOutageSummaries() throws SQLException {
        OutageSummary[] summaries = new OutageSummary[0];        
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select nodeid, max(iflostservice) as timeDown, node.nodelabel from outages where ifregainedservice is null and node.nodeid=outages.nodeid group by nodeid, nodelabel order by timeDown desc;");

            ArrayList list = new ArrayList();
            
            while( rs.next() ) {
                int nodeId = rs.getInt( "nodeID" );
                Timestamp timeDownTS = rs.getTimestamp( "timeDown" );
                long timeDown = timeDownTS.getTime();
                Date downDate = new Date(timeDown);
                String nodeLabel = rs.getString( "nodelabel" );
                
                list.add( new OutageSummary( nodeId, nodeLabel, downDate ));
            }
            
            rs.close();
            stmt.close();
    
            summaries = (OutageSummary[])list.toArray( new OutageSummary[list.size()] );
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return summaries;
    } 


    protected static Outage[] rs2Outages( ResultSet rs ) throws SQLException {
        return rs2Outages(rs, true);
    }    
    
    
    protected static Outage[] rs2Outages( ResultSet rs, boolean includesRegainedTime ) throws SQLException {
        return rs2Outages(rs, includesRegainedTime, false);
    }
        
    /* LJK Feb 21, 2002: all these special case result set methods need to be cleaned up */    
    protected static Outage[] rs2Outages( ResultSet rs, boolean includesRegainedTime, boolean includesNotifications ) throws SQLException {        
        Outage[] outages = null;
        Vector vector = new Vector();

        while(rs.next()) {
            Outage outage = new Outage();

            Object element = new Integer(rs.getInt("nodeid"));
            outage.nodeId = ((Integer)element).intValue();
        
            element = rs.getString("ipaddr");
            outage.ipAddress = (String)element;
        
            element = new Integer(rs.getInt("serviceid"));
            outage.serviceId = ((Integer)element).intValue();
        
            element = rs.getString("nodeLabel");
            outage.nodeLabel = (String)element;
                    
            element = rs.getString("iphostname");
            outage.hostname = (String)element;            
            
            element = rs.getString("servicename");
            outage.serviceName = (String)element;

            element = rs.getTimestamp("iflostservice");
            if( element != null) {
                outage.lostServiceTime = new Date(((Timestamp)element).getTime());                
            }
            
            if( includesRegainedTime ) {                
                element = rs.getTimestamp("ifregainedservice");
                if( element != null) {
                    outage.regainedServiceTime = new Date(((Timestamp)element).getTime());                
                }            
            }
                        
            if(includesNotifications) {                
                int intElement = rs.getInt("svclosteventid");
                if(intElement != 0 ) {
                    outage.lostServiceEventId = new Integer(intElement);
                }
                
                intElement = rs.getInt("notifyid");
                if(intElement != 0 ) {
                    outage.lostServiceNotificationId = new Integer(intElement);
                }

                element = rs.getString("answeredby");
                outage.lostServiceNotificationAcknowledgedBy = (String)element;
             
            }
    
            vector.addElement( outage );
        }

        outages = (Outage[])vector.toArray( new Outage[vector.size()] );

        return outages;
    }
    
}


