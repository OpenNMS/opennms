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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    
    MonitoredServiceDao m_monitoredServiceDao;
    
    OutageDao m_outageDao;
    
    IpInterfaceDao m_ipInterfaceDao;

    NodeDao m_nodeDao;

    ServiceTypeDao m_serviceTypeDao;

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
        
        List<OnmsMonitoredService> monitoredServiceList = m_monitoredServiceDao.getByNodeIdIpAddrServiceName(nodeId, ipAddr, serviceName);
        if ( monitoredServiceList != null ) {
            for(OnmsMonitoredService monitoredService : monitoredServiceList) {
                if(monitoredService.getStatus() != "A") {
                    monitoredServiceList.remove(monitoredService);
                }
            }
            LOG.debug("{} {}/{}/{} active", whichEvent, nodeId, ipAddr, serviceName);
            return monitoredServiceList.size() > 0;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> getActiveServiceIdsForInterface(String ipaddr) throws SQLException {
        List<OnmsMonitoredService> monitoredServiceList = m_monitoredServiceDao.getByIpaddr(ipaddr);
        
        List<Integer> serviceIds = new ArrayList<Integer>();
        for(OnmsMonitoredService monitoredService : monitoredServiceList) {
            if(monitoredService.getStatus() == "A")
                serviceIds.add(monitoredService.getServiceId());
        }
        return serviceIds;
    }

    /** {@inheritDoc} */
    @Override
    public int getNodeIDForInterface(String ipaddr) throws SQLException {
        int nodeid = -1;
        nodeid = m_ipInterfaceDao.getNodeIdByIpAddr(ipaddr);
        LOG.debug("getNodeLabel: ipaddr={} nodeid={}", ipaddr, nodeid);

        return nodeid;
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeLabel(int nodeId) throws SQLException {
        String nodeLabel = null;
        nodeLabel = m_nodeDao.getLabelForId(nodeId);
        LOG.debug("getNodeLabel: nodeid={} nodelabel={}", nodeId, nodeLabel);

        return nodeLabel;
    }

    /** {@inheritDoc} */
    @Override
    public int getServiceCountForInterface(String ipaddr) throws SQLException {
        int count = -1;
        List<OnmsMonitoredService> monitoredServiceList = m_monitoredServiceDao.findByIpaddr(ipaddr);
        
        for (OnmsMonitoredService monitoredService : monitoredServiceList) {
            if(monitoredService.getStatus() != "A") {
                monitoredServiceList.remove(monitoredService);
            }
        }

        count = monitoredServiceList.size();
        LOG.debug("restartPollingInterfaceHandler: count active ifservices to poll for interface: {}", ipaddr);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public List<IfKey> getInterfacesWithService(String svcName) throws SQLException {
        List<IfKey> ifkeys = new ArrayList<IfKey>();

        List<OnmsMonitoredService> monitoredServiceList = m_monitoredServiceDao.getByServiceName(svcName);
        
        for(OnmsMonitoredService monitoredService : monitoredServiceList) {
            if (monitoredService.getStatus() != "A") {
                monitoredServiceList.remove(monitoredService);
            }
        }

        // Iterate over result set and schedule each
        // interface/service
        // pair which passes the criteria
        //
        for(OnmsMonitoredService monitoredService : monitoredServiceList) {
            if (monitoredService.getStatus() != "A") {
                monitoredServiceList.remove(monitoredService);
            }
            else {
                IfKey key = new IfKey(monitoredService.getNodeId(), monitoredService.getIpAddress().getHostName());
                ifkeys.add(key);
            }
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

        Timestamp regainedDate = null;
        Timestamp lostDate = null;

        Connection dbConn = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            dbConn = getConnection();
            d.watch(dbConn);
            List<OnmsOutage> outageList = m_outageDao.findbyNodeIdIpAddrServiceId(nodeId, ipAddr, serviceId);

            // get the outage information for this service on this ip address
            if ( outageList != null ) {
                
                Date ifLostService = outageList.get(0).getIfLostService();
                
                int flag = 0;
                for (OnmsOutage outage : outageList) {
                    if (outage.getIfLostService().compareTo(ifLostService) > 0 ) {
                        ifLostService = outage.getIfLostService();
                        flag = outageList.indexOf(outage);
                    }
                }

                regainedDate = new Timestamp(outageList.get(flag).getIfRegainedService().getTime());
                lostDate     = new Timestamp(outageList.get(flag).getIfLostService().getTime());
                // if there was a result then the service has been down before,
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
                OnmsOutage outage = new OnmsOutage();
                outage.setId((Integer) outageId);
                outage.getServiceLostEvent().setId(dbId);
                outage.getMonitoredService().getIpInterface().getNode().setId(nodeId);
                outage.getMonitoredService().getIpInterface().setIpAddress(InetAddressUtils.addr(ipAddr));
                outage.getMonitoredService().setId(serviceId);
                outage.setIfLostService(convertEventTimeToTimeStamp(time));
                
                m_outageDao.saveOrUpdate(outage);
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
                
                List<OnmsOutage> outageList = m_outageDao.findbyNodeIdIpAddrServiceId(nodeId, ipAddr, serviceId);
                for (OnmsOutage outage : outageList) {
                    if (outage.getIfRegainedService() == null ) {
                        outage.getServiceRegainedEvent().setId(dbId);
                        outage.setIfRegainedService(convertEventTimeToTimeStamp(time));
                        m_outageDao.update(outage);
                    }
                }
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
        List<OnmsOutage> outageList = m_outageDao.findbyNodeIdAndIpAddr(oldNodeId, ipAddr);
        
        for (OnmsOutage outage : outageList) {
            outage.getMonitoredService().getIpInterface().getNode().setId(newNodeId);
            m_outageDao.update(outage);
        }
        
        LOG.info("reparenting outages for {}:{} to new node {}", oldNodeId, ipAddr, newNodeId);
    }

    /**
     * <p>getServiceID</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getServiceID(String serviceName) {
        if (serviceName == null) return -1;
        final OnmsServiceType result = m_serviceTypeDao.findByName(serviceName);
        return result == null ? -1 : result.getId().intValue();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getCriticalPath(int nodeId) {
        final String[] cpath = new String[2];
        
        OnmsNode pathOutage = m_nodeDao.getPathOutageByNodeId(nodeId);
        cpath[0] = pathOutage.getPathElement().getIpAddress();
        cpath[1] = pathOutage.getPathElement().getServiceName();
        
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
        
        List<OnmsMonitoredService> monitoredServiceList = m_monitoredServiceDao.getByNodeId(nodeId);
        for (OnmsMonitoredService monitoredService : monitoredServiceList) {
            String row[] = new String[2];
            row[0] = monitoredService.getIpAddress().getHostName();
            row[1] = monitoredService.getServiceName();
            
            servicemap.add(row);
        }
        
        return servicemap;
        
    }
    
    @Autowired
    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    @Autowired
    public void setOutageDao(OutageDao outageDao) {
        m_outageDao = outageDao;
    }
    
    @Autowired
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    @Autowired
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Autowired
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

}
