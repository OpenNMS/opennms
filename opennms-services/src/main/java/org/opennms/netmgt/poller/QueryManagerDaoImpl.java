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

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.utils.InetAddressUtils;
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

    /** {@inheritDoc} */
    @Override
    public String getNodeLabel(int nodeId) {
        return m_nodeDao.get(nodeId).getLabel();
    }

    /**
     * <p>convertEventTimeToTimeStamp</p>
     *
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.sql.Timestamp} object.
     */
    private static Date convertEventTimeToTimeStamp(String time) {
        try {
            return EventConstants.parseToDate(time);
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

            Criteria criteria = new Criteria(OnmsOutage.class);
            criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN),
                new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN),
            }));
            criteria.addRestriction(new EqRestriction("node.id", oldNodeId));
            criteria.addRestriction(new EqRestriction("ipInterface.ipAddress", addr(ipAddr)));
            List<OnmsOutage> outages = m_outageDao.findMatching(criteria);

            for (OnmsOutage outage : outages) {
                OnmsMonitoredService service = m_monitoredServiceDao.get(newNodeId, addr(ipAddr), outage.getServiceId());
                if (service == null) {
                    LOG.warn(" Cannot find monitored service to reparent outage from {}:{} to {}", oldNodeId, ipAddr, newNodeId);
                } else {
                    outage.setMonitoredService(service);
                    m_outageDao.save(outage);
                }
            }
        } catch (Throwable e) {
            LOG.error(" Error reparenting outage for {}:{} to {}", oldNodeId, ipAddr, newNodeId, e);
        }
        
    }

    @Override
    public List<String[]> getNodeServices(int nodeId){
        final LinkedList<String[]> servicemap = new LinkedList<String[]>();

        Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
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
        Date closeDate = new java.util.Date();
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService", "monitoredService", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new AnyRestriction(
            new EqRestriction("monitoredService.status", "D"),
            new EqRestriction("monitoredService.status", "F"),
            new EqRestriction("monitoredService.status", "U")
        ));
        criteria.addRestriction(new NullRestriction("ifRegainedService"));
        List<OnmsOutage> outages = m_outageDao.findMatching(criteria);
        
        for (OnmsOutage outage : outages) {
            outage.setIfRegainedService(closeDate);
            m_outageDao.update(outage);
        }

        criteria = new Criteria(OnmsOutage.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new AnyRestriction(
            new EqRestriction("ipInterface.isManaged", "F"),
            new EqRestriction("ipInterface.isManaged", "U")
        ));
        criteria.addRestriction(new NullRestriction("ifRegainedService"));
        outages = m_outageDao.findMatching(criteria);
        
        for (OnmsOutage outage : outages) {
            outage.setIfRegainedService(closeDate);
            m_outageDao.update(outage);
        }
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
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new NullRestriction("ifRegainedService"));
        List<OnmsOutage> outages = m_outageDao.findMatching(criteria);
        
        for (OnmsOutage outage : outages) {
            outage.setIfRegainedService(closeDate);
            outage.setServiceRegainedEvent(m_eventDao.get(eventId));
            m_outageDao.update(outage);
        }
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
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new EqRestriction("ipInterface.ipAddress", addr(ipAddr)));
        criteria.addRestriction(new NullRestriction("ifRegainedService"));
        List<OnmsOutage> outages = m_outageDao.findMatching(criteria);
        
        for (OnmsOutage outage : outages) {
            outage.setIfRegainedService(closeDate);
            outage.setServiceRegainedEvent(m_eventDao.get(eventId));
            m_outageDao.update(outage);
        }
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
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("monitoredService.serviceType", "serviceType", JoinType.LEFT_JOIN),
            new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new EqRestriction("ipInterface.ipAddress", addr(ipAddr)));
        criteria.addRestriction(new EqRestriction("serviceType.name", serviceName));
        criteria.addRestriction(new NullRestriction("ifRegainedService"));
        List<OnmsOutage> outages = m_outageDao.findMatching(criteria);
        
        for (OnmsOutage outage : outages) {
            outage.setIfRegainedService(closeDate);
            outage.setServiceRegainedEvent(m_eventDao.get(eventId));
            m_outageDao.update(outage);
            LOG.info("Calling closeOutagesForService: {}",outage);
            
        }
        
        
        
    }

    @Override
    public void updateServiceStatus(int nodeId, String ipAddr, String serviceName, String status) {
        Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("monitoredService.serviceType", "serviceType", JoinType.LEFT_JOIN),
            new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new EqRestriction("ipInterface.ipAddress", addr(ipAddr)));
        criteria.addRestriction(new EqRestriction("serviceType.name", serviceName));
        criteria.addRestriction(new NullRestriction("ifRegainedService"));
        List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(criteria);
        
        for (OnmsMonitoredService service : services) {
            service.setStatus(status);
            m_monitoredServiceDao.save(service);
        }
    }

}
