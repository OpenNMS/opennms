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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Updater;
import org.opennms.netmgt.EventConstants;
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

    @Autowired
    DataSource m_dataSource;

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

    @Override
    public List<String[]> getNodeServices(int nodeId){
        final LinkedList<String[]> servicemap = new LinkedList<String[]>();

        Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.addRestriction(new EqRestriction("ipInterface.node.id", nodeId));
        for (OnmsMonitoredService service : m_monitoredServiceDao.findMatching(criteria)) {
            servicemap.add(new String[] { service.getIpAddressAsString(), service.getServiceName() });
        }

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
