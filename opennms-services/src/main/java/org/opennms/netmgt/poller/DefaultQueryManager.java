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

package org.opennms.netmgt.poller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.utils.Updater;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DefaultQueryManager class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public class DefaultQueryManager implements QueryManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultQueryManager.class);

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

    final static String SQL_FETCH_INTERFACES_AND_SERVICES_ON_NODE ="SELECT ipaddr,servicename FROM ifservices,service WHERE nodeid= ? AND ifservices.serviceid=service.serviceid";
    
    
    private DataSource m_dataSource;

    /** {@inheritDoc} */
    @Override
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    @Override
    public DataSource getDataSource() {
        return m_dataSource;
    }

    private Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }


    /** {@inheritDoc} */
    @Override
    public boolean activeServiceExists(String whichEvent, int nodeId, String ipAddr, String serviceName) {
        java.sql.Connection dbConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            dbConn = getConnection();
            d.watch(dbConn);

            stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_COUNT_IFSERVICE_STATUS);
            d.watch(stmt);

            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setString(3, serviceName);

            rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                return rs.getInt(1) > 0;
            }

            LOG.debug("{} {}/{}/{} active", whichEvent, nodeId, ipAddr, serviceName);
        } catch (SQLException sqlE) {
            LOG.error("SQLException during check to see if nodeid/ip/service is active", sqlE);
        } finally {
            d.cleanUp();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> getActiveServiceIdsForInterface(String ipaddr) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        java.sql.Connection dbConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            dbConn = getConnection();
            d.watch(dbConn);
            List<Integer> serviceIds = new ArrayList<Integer>();
            stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_FETCH_IFSERVICES_TO_POLL);
            d.watch(stmt);
            stmt.setString(1, ipaddr);
            rs = stmt.executeQuery();
            d.watch(rs);
            LOG.debug("restartPollingInterfaceHandler: retrieve active service to poll on interface: {}", ipaddr);

            while (rs.next()) {
                serviceIds.add(rs.getInt(1));
            }
            return serviceIds;
        } finally {
            d.cleanUp();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getNodeIDForInterface(String ipaddr) throws SQLException {
        int nodeid = -1;
        java.sql.Connection dbConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            // Get database connection from the factory
            dbConn = getConnection();
            d.watch(dbConn);

            // Issue query and extract nodeLabel from result set
            stmt = dbConn.createStatement();
            d.watch(stmt);
            String sql = "SELECT node.nodeid FROM node, ipinterface WHERE ipinterface.ipaddr='" + ipaddr + "' AND ipinterface.nodeid=node.nodeid";
            rs = stmt.executeQuery(sql);
            d.watch(rs);
            if (rs.next()) {
                nodeid = rs.getInt(1);
                LOG.debug("getNodeLabel: ipaddr={} nodeid={}", ipaddr, nodeid);
            }
        } finally {
            d.cleanUp();
        }

        return nodeid;
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeLabel(int nodeId) throws SQLException {
        String nodeLabel = null;
        java.sql.Connection dbConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            // Get database connection from the factory
            dbConn = getConnection();
            d.watch(dbConn);

            // Issue query and extract nodeLabel from result set
            stmt = dbConn.createStatement();
            d.watch(stmt);
            rs = stmt.executeQuery("SELECT nodelabel FROM node WHERE nodeid=" + String.valueOf(nodeId));
            d.watch(rs);
            if (rs.next()) {
                nodeLabel = (String) rs.getString("nodelabel");
                LOG.debug("getNodeLabel: nodeid={} nodelabel={}", nodeId, nodeLabel);
            }
        } finally {
            d.cleanUp();
        }

        return nodeLabel;
    }

    /** {@inheritDoc} */
    @Override
    public int getServiceCountForInterface(String ipaddr) throws SQLException {
        java.sql.Connection dbConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        int count = -1;
        try {
            dbConn = getConnection();
            d.watch(dbConn);
            // Count active services to poll
            stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_COUNT_IFSERVICES_TO_POLL);
            d.watch(stmt);

            stmt.setString(1, ipaddr);

            rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                count = rs.getInt(1);
                LOG.debug("restartPollingInterfaceHandler: count active ifservices to poll for interface: {}", ipaddr);
            }
        } finally {
            d.cleanUp();
        }
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public List<IfKey> getInterfacesWithService(String svcName) throws SQLException {
        List<IfKey> ifkeys = new ArrayList<IfKey>();
        final DBUtils d = new DBUtils(getClass());

        try {
            
        java.sql.Connection dbConn = getConnection();
        d.watch(dbConn);

        LOG.debug("scheduleExistingInterfaces: dbConn = {}, svcName = {}", dbConn, svcName);

        PreparedStatement stmt = dbConn.prepareStatement(DefaultQueryManager.SQL_RETRIEVE_INTERFACES);
        d.watch(stmt);
        stmt.setString(1, svcName); // Service name
        ResultSet rs = stmt.executeQuery();
        d.watch(rs);

        // Iterate over result set and schedule each
        // interface/service
        // pair which passes the criteria
        //
        while (rs.next()) {
            IfKey key = new IfKey(rs.getInt(1), rs.getString(2));
            ifkeys.add(key);
        }

        } finally {
            d.cleanUp();
        }
        
        return ifkeys;
    }

    /** {@inheritDoc} */
    @Override
    public Date getServiceLostDate(int nodeId, String ipAddr, String svcName, int serviceId) {
        LOG.debug("getting last known status for address: {} service: {}", ipAddr, svcName);

        Date svcLostDate = null;
        // Convert service name to service identifier
        //
        if (serviceId < 0) {
            LOG.warn("Failed to retrieve service identifier for interface {} and service '{}'", ipAddr, svcName);
            return svcLostDate;
        }

        PreparedStatement outagesQuery = null;
        ResultSet outagesResult = null;
        Timestamp regainedDate = null;
        Timestamp lostDate = null;

        Connection dbConn = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            dbConn = getConnection();
            d.watch(dbConn);
            // get the outage information for this service on this ip address
            outagesQuery = dbConn.prepareStatement(DefaultQueryManager.SQL_RETRIEVE_SERVICE_STATUS);
            d.watch(outagesQuery);

            // add the values for the main query
            outagesQuery.setInt(1, nodeId);
            outagesQuery.setString(2, ipAddr);
            outagesQuery.setInt(3, serviceId);

            // add the values for the subquery
            outagesQuery.setInt(4, nodeId);
            outagesQuery.setString(5, ipAddr);
            outagesQuery.setInt(6, serviceId);

            outagesResult = outagesQuery.executeQuery();
            d.watch(outagesResult);

            // if there was a result then the service has been down before,
            if (outagesResult.next()) {
                regainedDate = outagesResult.getTimestamp(1);
                lostDate = outagesResult.getTimestamp(2);
                LOG.debug("getServiceLastKnownStatus: lostDate: {}", lostDate);
            }
            // the service has never been down, need to use current date for
            // both
            else {
                Date currentDate = new Date(System.currentTimeMillis());
                regainedDate = new Timestamp(currentDate.getTime());
                lostDate = new Timestamp(currentDate.getTime());
            }
        } catch (SQLException sqlE) {
            LOG.error("SQL exception while retrieving last known service status for {}/{}", ipAddr, svcName);
        } finally {
            d.cleanUp();
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
                LOG.debug("getServiceLastKnownStatus: svcLostDate: {}", svcLostDate);
            }
        }

        return svcLostDate;
    }
    
    
    /**
     * <p>convertEventTimeToTimeStamp</p>
     *
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.sql.Timestamp} object.
     */
    public Timestamp convertEventTimeToTimeStamp(String time) {
        try {
            Date date = EventConstants.parseToDate(time);
            Timestamp eventTime = new Timestamp(date.getTime());
            return eventTime;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format "+time, e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void openOutage(String outageIdSQL, int nodeId, String ipAddr, String svcName, int dbId, String time) {
        
        int attempt = 0;
        boolean notUpdated = true;
        int serviceId = getServiceID(svcName);
        
        while (attempt < 2 && notUpdated) {
            try {
                LOG.info("openOutage: opening outage for {}:{}:{} with cause {}:{}", nodeId, ipAddr, svcName, dbId, time);
                
                SingleResultQuerier srq = new SingleResultQuerier(getDataSource(), outageIdSQL);
                srq.execute();
                Object outageId = srq.getResult();
                
                if (outageId == null) {
                    throw (new Exception("Null outageId returned from Querier with SQL: "+outageIdSQL));
                }
                
                String sql = "insert into outages (outageId, svcLostEventId, nodeId, ipAddr, serviceId, ifLostService) values ("+outageId+", ?, ?, ?, ?, ?)";
                
                Object values[] = {
                        Integer.valueOf(dbId),
                        Integer.valueOf(nodeId),
                        ipAddr,
                        Integer.valueOf(serviceId),
                        convertEventTimeToTimeStamp(time),
                };

                Updater updater = new Updater(getDataSource(), sql);
                updater.execute(values);
                notUpdated = false;
            } catch (Throwable e) {
                if (attempt > 1) {
                    LOG.error("openOutage: Second and final attempt failed opening outage for {}:{}:{}", nodeId, ipAddr, svcName, e);
                } else {
                    LOG.info("openOutage: First attempt failed opening outage for {}:{}:{}", nodeId, ipAddr, svcName, e);
                }
            }
            attempt++;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resolveOutage(int nodeId, String ipAddr, String svcName, int dbId, String time) {
        int attempt = 0;
        boolean notUpdated = true;
        
        while (attempt < 2 && notUpdated) {
            try {
                LOG.info("resolving outage for {}:{}:{} with resolution {}:{}", nodeId, ipAddr, svcName, dbId, time);
                int serviceId = getServiceID(svcName);
                
                String sql = "update outages set svcRegainedEventId=?, ifRegainedService=? where nodeId = ? and ipAddr = ? and serviceId = ? and ifRegainedService is null";
                
                Object values[] = {
                        Integer.valueOf(dbId),
                        convertEventTimeToTimeStamp(time),
                        Integer.valueOf(nodeId),
                        ipAddr,
                        Integer.valueOf(serviceId),
                };

                Updater updater = new Updater(getDataSource(), sql);
                updater.execute(values);
                notUpdated = false;
            } catch (Throwable e) {
                if (attempt > 1) {
                    LOG.error("resolveOutage: Second and final attempt failed resolving outage for {}:{}:{}", nodeId, ipAddr, svcName, e);
                } else {
                    LOG.info("resolveOutage: first attempt failed resolving outage for {}:{}:{}", nodeId, ipAddr, svcName, e);
                }
            }
            attempt++;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        try {
            LOG.info("reparenting outages for {}:{} to new node {}", oldNodeId, ipAddr, newNodeId);
            String sql = "update outages set nodeId = ? where nodeId = ? and ipaddr = ?";
            
            Object[] values = {
                    Integer.valueOf(newNodeId),
                    Integer.valueOf(oldNodeId),
                    ipAddr,
                };

            Updater updater = new Updater(getDataSource(), sql);
            updater.execute(values);
        } catch (Throwable e) {
            LOG.error(" Error reparenting outage for {}:{} to {}", oldNodeId, ipAddr, newNodeId, e);
        }
        
    }

    /**
     * <p>getServiceID</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getServiceID(String serviceName) {
        if (serviceName == null) return -1;

        SingleResultQuerier querier = new SingleResultQuerier(getDataSource(), "select serviceId from service where serviceName = ?");
        querier.execute(serviceName);
        final Integer result = (Integer)querier.getResult();
        return result == null ? -1 : result.intValue();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getCriticalPath(int nodeId) {
        final String[] cpath = new String[2];
        Querier querier = new Querier(getDataSource(), "SELECT criticalpathip, criticalpathservicename FROM pathoutage where nodeid=?") {
    
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                cpath[0] = rs.getString(1);
                cpath[1] = rs.getString(2);
            }
    
        };
        querier.execute(Integer.valueOf(nodeId));
    
        if (cpath[0] == null || cpath[0].equals("")) {
            cpath[0] = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathIp();
            cpath[1] = "ICMP";
        }
        if (cpath[1] == null || cpath[1].equals("")) {
            cpath[1] = "ICMP";
        }
        return cpath;
    }

    @Override
    public List<String[]> getNodeServices(int nodeId){
        final LinkedList<String[]> servicemap = new LinkedList<String[]>();
        Querier querier = new Querier(getDataSource(),SQL_FETCH_INTERFACES_AND_SERVICES_ON_NODE) {
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
               
                String row[] = new String[2];
                row[0] = rs.getString(1);
                row[1] = rs.getString(2);
                
                servicemap.add(row);
                
            }
            
        };
        
        querier.execute(Integer.valueOf(nodeId));
        
        return servicemap;
        
    }
    
    
}
