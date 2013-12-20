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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.Updater;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
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
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    private static final String SQL_FETCH_INTERFACES_AND_SERVICES_ON_NODE ="SELECT ipaddr,servicename FROM ifservices,service WHERE nodeid= ? AND ifservices.serviceid=service.serviceid";

    /** {@inheritDoc} */
    @Override
    public String getNodeLabel(int nodeId) throws SQLException {
        return m_nodeDao.get(nodeId).getLabel();
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
    
    @Override
    public void openOutage(String outageIdSQL, int nodeId, String ipAddr, String svcName, int serviceLostEventId, String time) {
        openOutage(nodeId, ipAddr, svcName, serviceLostEventId, time);
    }

    private void openOutage(int nodeId, String ipAddr, String svcName, int serviceLostEventId, String time) {
        OnmsEvent event = m_eventDao.get(serviceLostEventId);
        OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, InetAddressUtils.addr(ipAddr), svcName);
        OnmsOutage outage = new OnmsOutage(convertEventTimeToTimeStamp(time), event, service);
        m_outageDao.saveOrUpdate(outage);
    }

    /** {@inheritDoc} */
    @Override
    public void resolveOutage(int nodeId, String ipAddr, String svcName, int regainedEventId, String time) {
        LOG.info("resolving outage for {}:{}:{} with resolution {}:{}", nodeId, ipAddr, svcName, regainedEventId, time);
        int serviceId = m_serviceTypeDao.findByName(svcName).getId();
        
        OnmsEvent event = m_eventDao.get(regainedEventId);
        OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, InetAddressUtils.addr(ipAddr), serviceId);

        // Update the outage
        OnmsOutage outage = m_outageDao.currentOutageForService(service);
        if (outage == null) {
            LOG.warn("Cannot find outage for service: {}", service);
        } else {
            outage.setServiceRegainedEvent(event);
            outage.setIfRegainedService(convertEventTimeToTimeStamp(time));
            m_outageDao.saveOrUpdate(outage);
        }

        String sql = "update outages set svcRegainedEventId=?, ifRegainedService=? where nodeId = ? and ipAddr = ? and serviceId = ? and ifRegainedService is null";
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
