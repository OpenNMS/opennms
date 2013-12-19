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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.utils.Updater;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsOutage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>QueryManagerDaoImpl class.</p>
 *
 * @author brozow
 */
public class QueryManagerDaoImpl implements QueryManager {

    private static final Logger LOG = LoggerFactory.getLogger(QueryManagerDaoImpl.class);

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    private static final String SQL_FETCH_INTERFACES_AND_SERVICES_ON_NODE ="SELECT ipaddr,servicename FROM ifservices,service WHERE nodeid= ? AND ifservices.serviceid=service.serviceid";

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
            Timestamp eventTime = new Timestamp(date.getTime());
            return eventTime;
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
               
                String[] row = new String[2];
                row[0] = rs.getString(1);
                row[1] = rs.getString(2);
                
                servicemap.add(row);
                
            }
            
        };
        
        querier.execute(Integer.valueOf(nodeId));
        
        return servicemap;
        
    }
    
    
}
