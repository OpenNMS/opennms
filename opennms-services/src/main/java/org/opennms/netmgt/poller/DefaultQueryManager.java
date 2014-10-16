/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.utils.Updater;
import org.opennms.netmgt.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DefaultQueryManager class.</p>
 *
 * @author brozow
 * 
 * @deprecated Use {@link QueryManagerDaoImpl} instead of this class which relies on JDBC.
 */
public class DefaultQueryManager implements QueryManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultQueryManager.class);

    private static final String SQL_FETCH_INTERFACES_AND_SERVICES_ON_NODE ="SELECT ipaddr,servicename FROM ifservices,service WHERE nodeid= ? AND ifservices.serviceid=service.serviceid";

    private DataSource m_dataSource;

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
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

    /**
     * <p>convertEventTimeToTimeStamp</p>
     *
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.sql.Timestamp} object.
     */
    private static Timestamp convertEventTimeToTimeStamp(String time) {
        try {
            Date date = EventConstants.parseToDate(time);
            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + time, e);
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
                
                SingleResultQuerier srq = new SingleResultQuerier(m_dataSource, outageIdSQL);
                srq.execute();
                Object outageId = srq.getResult();
                
                if (outageId == null) {
                    throw (new Exception("Null outageId returned from Querier with SQL: "+outageIdSQL));
                }
                
                String sql = "insert into outages (outageId, svcLostEventId, nodeId, ipAddr, serviceId, ifLostService) values ("+outageId+", ?, ?, ?, ?, ?)";
                
                Object[] values = {
                        Integer.valueOf(dbId),
                        Integer.valueOf(nodeId),
                        ipAddr,
                        Integer.valueOf(serviceId),
                        convertEventTimeToTimeStamp(time),
                };

                Updater updater = new Updater(m_dataSource, sql);
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
                
                Object[] values = {
                        Integer.valueOf(dbId),
                        convertEventTimeToTimeStamp(time),
                        Integer.valueOf(nodeId),
                        ipAddr,
                        Integer.valueOf(serviceId),
                };

                Updater updater = new Updater(m_dataSource, sql);
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

            Updater updater = new Updater(m_dataSource, sql);
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

        SingleResultQuerier querier = new SingleResultQuerier(m_dataSource, "select serviceId from service where serviceName = ?");
        querier.execute(serviceName);
        final Integer result = (Integer)querier.getResult();
        return result == null ? -1 : result.intValue();
    }

    @Override
    public List<String[]> getNodeServices(int nodeId){
        final LinkedList<String[]> servicemap = new LinkedList<String[]>();
        Querier querier = new Querier(m_dataSource,SQL_FETCH_INTERFACES_AND_SERVICES_ON_NODE) {
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
               
                String[] row = new String[2];
                row[0] = rs.getString(1);
                row[1] = rs.getString(2);
                
                servicemap.add(row);
                
            }
            
        };
        
        querier.execute(Integer.valueOf(nodeId));
        
        return servicemap;
        
    }

    /**
     * 
     */
    @Override
    public void closeOutagesForUnmanagedServices() {
        Timestamp closeTime = new Timestamp((new java.util.Date()).getTime());

        final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_SERVICES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ifservices where ((outages.nodeid = ifservices.nodeid) AND (outages.ipaddr = ifservices.ipaddr) AND (outages.serviceid = ifservices.serviceid) AND ((ifservices.status = 'D') OR (ifservices.status = 'F') OR (ifservices.status = 'U')) AND (outages.ifregainedservice IS NULL)))";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_UNMANAGED_SERVICES);
        svcUpdater.execute(closeTime);
        
        final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ipinterface where ((outages.nodeid = ipinterface.nodeid) AND (outages.ipaddr = ipinterface.ipaddr) AND ((ipinterface.ismanaged = 'F') OR (ipinterface.ismanaged = 'U')) AND (outages.ifregainedservice IS NULL)))";
        Updater ifUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES);
        ifUpdater.execute(closeTime);
        


    }
    
    /**
     * <p>closeOutagesForNode</p>
     *
     * @param closeDate a {@link java.util.Date} object.
     * @param eventId a int.
     * @param nodeId a int.
     */
    @Override
    public void closeOutagesForNode(Date closeDate, int eventId, int nodeId) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_NODE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outages.nodeId = ? AND (outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_NODE);
        svcUpdater.execute(closeTime, Integer.valueOf(eventId), Integer.valueOf(nodeId));
    }
    
    /**
     * <p>closeOutagesForInterface</p>
     *
     * @param closeDate a {@link java.util.Date} object.
     * @param eventId a int.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     */
    @Override
    public void closeOutagesForInterface(Date closeDate, int eventId, int nodeId, String ipAddr) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_IFACE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outages.nodeId = ? AND outages.ipAddr = ? AND (outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_IFACE);
        svcUpdater.execute(closeTime, Integer.valueOf(eventId), Integer.valueOf(nodeId), ipAddr);
    }
    
    /**
     * <p>closeOutagesForService</p>
     *
     * @param closeDate a {@link java.util.Date} object.
     * @param eventId a int.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     */
    @Override
    public void closeOutagesForService(Date closeDate, int eventId, int nodeId, String ipAddr, String serviceName) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        final String DB_CLOSE_OUTAGES_FOR_SERVICE = "UPDATE outages set ifregainedservice = ?, svcRegainedEventId = ? where outageid in (select outages.outageid from outages, service where outages.nodeid = ? AND outages.ipaddr = ? AND outages.serviceid = service.serviceId AND service.servicename = ? AND outages.ifregainedservice IS NULL)";
        Updater svcUpdater = new Updater(m_dataSource, DB_CLOSE_OUTAGES_FOR_SERVICE);
        svcUpdater.execute(closeTime, Integer.valueOf(eventId), Integer.valueOf(nodeId), ipAddr, serviceName);
    }

    @Override
    public void updateServiceStatus(int nodeId, String ipAddr, String serviceName, String status) {
        final String sql = "UPDATE ifservices SET status = ? WHERE id " +
        		" IN (SELECT ifs.id FROM ifservices AS ifs JOIN service AS svc ON ifs.serviceid = svc.serviceid " +
        		" WHERE ifs.nodeId = ? AND ifs.ipAddr = ? AND svc.servicename = ?)"; 

        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status, nodeId, ipAddr, serviceName);
        
    }

}
