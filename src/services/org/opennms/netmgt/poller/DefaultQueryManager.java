//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Modifications:
//
//2004 Nov 14:Created
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DbConnectionFactory;
import org.opennms.netmgt.utils.Updater;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DefaultQueryManager implements QueryManager {

    final static String SQL_RETRIEVE_INTERFACES = "SELECT nodeid,ipaddr FROM ifServices, service WHERE ifServices.serviceid = service.serviceid AND service.servicename = ? AND ifServices.status='A'";

    final static String SQL_RETRIEVE_SERVICE_IDS = "SELECT serviceid,servicename  FROM service";

    final static String SQL_RETRIEVE_SERVICE_STATUS = "SELECT ifregainedservice,iflostservice FROM outages WHERE nodeid = ? AND ipaddr = ? AND serviceid = ? AND iflostservice = (SELECT max(iflostservice) FROM outages WHERE nodeid = ? AND ipaddr = ? AND serviceid = ?)";

    /**
     * SQL statement used to query the 'ifServices' for a nodeid/ipaddr/service
     * combination on the receipt of a 'nodeGainedService' to make sure there is
     * atleast one row where the service status for the tuple is 'A'.
     */
    final static String SQL_COUNT_IFSERVICE_STATUS = "select count(*) FROM ifServices, service WHERE nodeid=? AND ipaddr=? AND status='A' AND ifServices.serviceid=service.serviceid AND service.servicename=?";

    /**
     * SQL statement used to count the active ifservices on the specified ip
     * address.
     */
    final static String SQL_COUNT_IFSERVICES_TO_POLL = "SELECT COUNT(*) FROM ifservices WHERE status = 'A' AND ipaddr = ?";

    /**
     * SQL statement used to retrieve an active ifservice for the scheduler to
     * poll.
     */
    final static String SQL_FETCH_IFSERVICES_TO_POLL = "SELECT if.serviceid FROM ifservices if, service s WHERE if.serviceid = s.serviceid AND if.status = 'A' AND if.ipaddr = ?";

    private DbConnectionFactory m_dbConnectionFactory;

    /**
     * @param whichEvent
     * @param nodeId
     * @param ipAddr
     * @param serviceName
     * @return
     */
    public boolean activeServiceExists(String whichEvent, int nodeId, String ipAddr, String serviceName) {
        Category log = ThreadCategory.getInstance(getClass());
        java.sql.Connection dbConn = null;
        PreparedStatement stmt = null;
        try {
            dbConn = getConnection();

            stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_COUNT_IFSERVICE_STATUS);

            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setString(3, serviceName);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getInt(1) > 0;
            }

            if (log.isDebugEnabled())
                log.debug(whichEvent + nodeId + "/" + ipAddr + "/" + serviceName + " active");
        } catch (SQLException sqlE) {
            log.error("SQLException during check to see if nodeid/ip/service is active", sqlE);
        } finally {
            // close the statement
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException sqlE) {
                }
            ;

            // close the connection
            if (dbConn != null)
                try {
                    dbConn.close();
                } catch (SQLException sqlE) {
                }
            ;
        }
        return false;
    }

    private Connection getConnection() throws SQLException {
        return m_dbConnectionFactory.getConnection();
    }

    /**
     * @param nameToId
     * @param idToName
     * @return
     */
    public void buildServiceNameToIdMaps(Map nameToId, Map idToName) {
        Category log = ThreadCategory.getInstance(getClass());
        java.sql.Connection ctest = null;
        ResultSet rs = null;
        try {
            ctest = getConnection();

            PreparedStatement loadStmt = ctest.prepareStatement(DefaultQueryManager.SQL_RETRIEVE_SERVICE_IDS);

            // go ahead and load the service table
            //
            rs = loadStmt.executeQuery();
            while (rs.next()) {
                Integer id = new Integer(rs.getInt(1));
                String name = rs.getString(2);

                nameToId.put(name, id);
                idToName.put(id, name);
            }
        } catch (SQLException sqlE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("start: Error accessing database.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("start: an error occured closing the result set", e);
                }
            }
            if (ctest != null) {
                try {
                    ctest.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("start: an error occured closing the SQL connection", e);
                }
            }
        }
    }

    /**
     * @param ipaddr
     * @return
     * @throws SQLException
     */
    public List getActiveServiceIdsForInterface(String ipaddr) throws SQLException {
        java.sql.Connection dbConn = getConnection();
        try {
            List serviceIds = new ArrayList();
            Category log = ThreadCategory.getInstance(getClass());
            PreparedStatement stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_FETCH_IFSERVICES_TO_POLL);
            stmt.setString(1, ipaddr);
            ResultSet rs = stmt.executeQuery();
            if (log.isDebugEnabled())
                log.debug("restartPollingInterfaceHandler: retrieve active service to poll on interface: " + ipaddr);

            while (rs.next()) {
                serviceIds.add(rs.getObject(1));
            }
            return serviceIds;
        } finally {
            dbConn.close();
        }
    }

    /**
     * @param ipaddr
     * @return
     * @throws SQLException
     */
    public int getNodeIDForInterface(String ipaddr) throws SQLException {
        Category log = ThreadCategory.getInstance(getClass());

        int nodeid = -1;
        java.sql.Connection dbConn = null;
        Statement stmt = null;
        try {
            // Get datbase connection from the factory
            dbConn = getConnection();

            // Issue query and extract nodeLabel from result set
            stmt = dbConn.createStatement();
            String sql = "SELECT node.nodeid FROM node, ipinterface WHERE ipinterface.ipaddr='" + ipaddr + "' AND ipinterface.nodeid=node.nodeid";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                nodeid = rs.getInt(1);
                if (log.isDebugEnabled())
                    log.debug("getNodeLabel: ipaddr=" + ipaddr + " nodeid=" + nodeid);
            }
        } finally {
            // Close the statement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("getNodeLabel: an exception occured closing the SQL statement", e);
                }
            }

            // Close the database connection
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (Throwable t) {
                    if (log.isDebugEnabled())
                        log.debug("getNodeLabel: an exception occured closing the SQL connection", t);
                }
            }
        }

        return nodeid;
    }

    /**
     * @param nodeId
     * @return
     * @throws SQLException
     */
    public String getNodeLabel(int nodeId) throws SQLException {
        Category log = ThreadCategory.getInstance(getClass());

        String nodeLabel = null;
        java.sql.Connection dbConn = null;
        Statement stmt = null;
        try {
            // Get datbase connection from the factory
            dbConn = getConnection();

            // Issue query and extract nodeLabel from result set
            stmt = dbConn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nodelabel FROM node WHERE nodeid=" + String.valueOf(nodeId));
            if (rs.next()) {
                nodeLabel = (String) rs.getString("nodelabel");
                if (log.isDebugEnabled())
                    log.debug("getNodeLabel: nodeid=" + nodeId + " nodelabel=" + nodeLabel);
            }
        } finally {
            // Close the statement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("getNodeLabel: an exception occured closing the SQL statement", e);
                }
            }

            // Close the database connection
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (Throwable t) {
                    if (log.isDebugEnabled())
                        log.debug("getNodeLabel: an exception occured closing the SQL connection", t);
                }
            }
        }

        return nodeLabel;
    }

    /**
     * @param ipaddr
     * @return
     * @throws SQLException
     */
    public int getServiceCountForInterface(String ipaddr) throws SQLException {
        Category log = ThreadCategory.getInstance(getClass());
        java.sql.Connection dbConn = getConnection();
        try {
            int count = -1;
            // Count active services to poll
            PreparedStatement stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_COUNT_IFSERVICES_TO_POLL);

            stmt.setString(1, ipaddr);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
                if (log.isDebugEnabled())
                    log.debug("restartPollingInterfaceHandler: count active ifservices to poll for interface: " + ipaddr);
            }
            stmt.close();
            return count;
        } finally {
            dbConn.close();
        }
    }

    /**
     * @param svcName
     * @return
     * @throws SQLException
     */
    public List getInterfacesWithService(String svcName) throws SQLException {
        List ifkeys;
        Category log = ThreadCategory.getInstance(getClass());
        java.sql.Connection dbConn = getConnection();

        if (log.isDebugEnabled())
            log.debug("scheduleExistingInterfaces: dbConn = " + dbConn + ", svcName = " + svcName);

        PreparedStatement stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_RETRIEVE_INTERFACES);
        stmt.setString(1, svcName); // Service name
        ResultSet rs = stmt.executeQuery();

        // Iterate over result set and schedule each
        // interface/service
        // pair which passes the criteria
        //
        ifkeys = new ArrayList();
        while (rs.next()) {
            IfKey key = new IfKey(rs.getInt(1), rs.getString(2));
            ifkeys.add(key);
        }
        rs.close();
        return ifkeys;
    }

    /**
     * @param poller
     * @param nodeId
     * @param ipAddr
     * @param svcName
     * @return
     */
    public Date getServiceLostDate(int nodeId, String ipAddr, String svcName, int serviceId) {
        Category log = ThreadCategory.getInstance(Poller.class);
        log.debug("getting last known status for address: " + ipAddr + " service: " + svcName);

        Date svcLostDate = null;
        // Convert service name to service identifier
        //
        if (serviceId < 0) {
            log.warn("Failed to retrieve service identifier for interface " + ipAddr + " and service '" + svcName + "'");
            return svcLostDate;
        }

        ResultSet outagesResult = null;
        Timestamp regainedDate = null;
        Timestamp lostDate = null;

        Connection dbConn = null;
        try {
            dbConn = getConnection();
            // get the outage information for this service on this ip address
            PreparedStatement outagesQuery = dbConn.prepareStatement(DefaultQueryManager.SQL_RETRIEVE_SERVICE_STATUS);

            // add the values for the main query
            outagesQuery.setInt(1, nodeId);
            outagesQuery.setString(2, ipAddr);
            outagesQuery.setInt(3, serviceId);

            // add the values for the subquery
            outagesQuery.setInt(4, nodeId);
            outagesQuery.setString(5, ipAddr);
            outagesQuery.setInt(6, serviceId);

            outagesResult = outagesQuery.executeQuery();

            // if there was a result then the service has been down before,
            if (outagesResult.next()) {
                regainedDate = outagesResult.getTimestamp(1);
                lostDate = outagesResult.getTimestamp(2);
                log.debug("getServiceLastKnownStatus: lostDate: " + lostDate);
            }
            // the service has never been down, need to use current date for
            // both
            else {
                Date currentDate = new Date(System.currentTimeMillis());
                regainedDate = new Timestamp(currentDate.getTime());
                lostDate = lostDate = new Timestamp(currentDate.getTime());
            }
        } catch (SQLException sqlE) {
            log.error("SQL exception while retrieving last known service status for " + ipAddr + "/" + svcName);
        } finally {
            if (outagesResult != null) {
                try {
                    outagesResult.close();
                    if (dbConn != null)
                        dbConn.close();
                } catch (SQLException slqE) {
                    // Do nothing
                }
            }
        }

        // Now use retrieved outage times to determine current status
        // of the service. If there was an error and we were unable
        // to retrieve the outage times the default of AVAILABLE will
        // be returned.
        //
        if (lostDate != null) {
            // If the service was never regained then simply
            // assign the svc lost date.
            if (regainedDate == null) {
                svcLostDate = new Date(lostDate.getTime());
                log.debug("getServiceLastKnownStatus: svcLostDate: " + svcLostDate);
            }
        }

        return svcLostDate;
    }
    
    

    public void setDbConnectionFactory(DbConnectionFactory dbConnectionFactory) {
        m_dbConnectionFactory = dbConnectionFactory;
    }
    
    public Timestamp convertEventTimeToTimeStamp(String time) {
        try {
            Date date = EventConstants.parseToDate(time);
            Timestamp eventTime = new Timestamp(date.getTime());
            return eventTime;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format "+time, e);
        }
    }


    
    public void openOutage(String outageIdSQL, int nodeId, String ipAddr, int serviceId, int dbId, String time) {
        try {
            ThreadCategory.getInstance(getClass()).debug("opening outage for "+nodeId+":"+ipAddr+":"+serviceId);

            String sql = "insert into outages (outageId, svcLostEventId, nodeId, ipAddr, serviceId, ifLostService) values (" +
            "("+outageIdSQL+"), " +
            "?, ?, ?, ?, ?)";
            
            Object values[] = {
                    new Integer(dbId),
                    new Integer(nodeId),
                    ipAddr,
                    new Integer(serviceId),
                    convertEventTimeToTimeStamp(time),
            };
            Updater updater = new Updater(m_dbConnectionFactory, sql);
            updater.execute(values);
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).fatal(" Error opening outage for "+nodeId+":"+ipAddr+":"+serviceId, e);
        }
    }
    
    public void resolveOutage(int nodeId, String ipAddr, int serviceId, int dbId, String time) {
        try {
            ThreadCategory.getInstance(getClass()).debug("resolving outage for "+nodeId+":"+ipAddr+":"+serviceId);

            String sql = "update outages set svcRegainedEventId=?, ifRegainedService=? where nodeId = ? and ipAddr = ? and serviceId = ?";
            
            Object values[] = {
                    new Integer(dbId),
                    convertEventTimeToTimeStamp(time),
                    new Integer(nodeId),
                    ipAddr,
                    new Integer(serviceId),
            };
            Updater updater = new Updater(m_dbConnectionFactory, sql);
            updater.execute(values);
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).fatal(" Error resolving outage for "+nodeId+":"+ipAddr+":"+serviceId, e);
        }
    }
    
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        try {
            String sql = "update outages set nodeId = ? where nodeId = ? and ipaddr = ?";
            
            Object[] values = {
                    new Integer(newNodeId),
                    new Integer(oldNodeId),
                    ipAddr,
                };
            Updater updater = new Updater(m_dbConnectionFactory, sql);
            updater.execute(values);
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).fatal(" Error reparenting outage for "+oldNodeId+":"+ipAddr+" to "+newNodeId, e);
        }
        
    }
}
