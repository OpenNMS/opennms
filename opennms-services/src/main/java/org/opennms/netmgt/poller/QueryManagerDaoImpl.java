/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;



import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

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
    private EventDao m_eventDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private TransactionOperations m_transcationOps;
    


    /** {@inheritDoc} */
    @Override
    public String getNodeLabel(int nodeId) {
        final OnmsNode onmsNode = m_nodeDao.get(nodeId);
        if (onmsNode == null) {
            return null;
        }
        return onmsNode.getLabel();
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeLocation(int nodeId) {
        final OnmsNode onmsNode = m_nodeDao.get(nodeId);
        if (onmsNode == null) {
            return null;
        }
        return onmsNode.getLocation().getLocationName();
    }

    /** {@inheritDoc} */
    @Override
    public Integer openOutagePendingLostEventId(int nodeId, String ipAddr, String svcName, Date lostTime) {
        LOG.info("opening outage for {}:{}:{} @ {}", nodeId, ipAddr, svcName, lostTime);
        final OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, InetAddressUtils.addr(ipAddr), svcName);
        final OnmsOutage outage = new OnmsOutage(lostTime, service);
        m_outageDao.saveOrUpdate(outage);
        return outage.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void updateOpenOutageWithEventId(int outageId, long lostEventId) {
        LOG.info("updating open outage {} with event id {}", outageId, lostEventId);

        final OnmsEvent event = m_eventDao.get(lostEventId);
        final OnmsOutage outage = m_outageDao.get(outageId);
        if (outage == null) {
            LOG.warn("Failed to update outage {} with event id {}. The outage no longer exists.",
                    outageId, lostEventId);
            return;
        }

        // Update the outage
        outage.setServiceLostEvent(event);
        m_outageDao.saveOrUpdate(outage);
    }

    /** {@inheritDoc} */
    @Override
    public Integer resolveOutagePendingRegainEventId(int nodeId, String ipAddr, String svcName, Date regainedTime) {
        LOG.info("resolving outage for {}:{}:{} @ {}", nodeId, ipAddr, svcName, regainedTime);
        final OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, InetAddressUtils.addr(ipAddr), svcName);
        if (service == null) {
            LOG.warn("Failed to resolve the pending outage for {}:{}:{} @ {}. The service could not be found.",
                    nodeId, ipAddr, svcName, regainedTime);
            return null;
        }

        final OnmsOutage outage = m_outageDao.currentOutageForService(service);
        if (outage == null) {
            return null;
        }

        // Update the outage
        outage.setIfRegainedService(new Timestamp(regainedTime.getTime()));
        m_outageDao.saveOrUpdate(outage);
        return outage.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void updateResolvedOutageWithEventId(int outageId, long regainedEventId) {
        LOG.info("updating resolved outage {} with event id {}", outageId, regainedEventId);

        final OnmsEvent event = m_eventDao.get(regainedEventId);
        final OnmsOutage outage = m_outageDao.get(outageId);
        if (outage == null) {
            LOG.warn("Failed to update outage {} with event id {}. The outage no longer exists.",
                    outageId, regainedEventId);
            return;
        }

        // Update the outage
        outage.setServiceRegainedEvent(event);
        m_outageDao.saveOrUpdate(outage);
    }

    @Override
    public List<String[]> getNodeServices(int nodeId){
        final LinkedList<String[]> servicemap = new LinkedList<>();

        Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("ipInterface", "ipInterface", JoinType.LEFT_JOIN),
            new Alias("ipInterface.node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new NeRestriction("status", "F")); // Ignore forced-unmanaged
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
        criteria.addRestriction(new NullRestriction("perspective"));
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
        criteria.addRestriction(new NullRestriction("perspective"));
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
    public void closeOutagesForNode(Date closeDate, long eventId, int nodeId) {
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.addRestriction(new NullRestriction("perspective"));
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
    public void closeOutagesForInterface(Date closeDate, long eventId, int nodeId, String ipAddr) {
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.addRestriction(new NullRestriction("perspective"));
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
    public void closeOutagesForService(Date closeDate, long eventId, int nodeId, String ipAddr, String serviceName) {
        Criteria criteria = new Criteria(OnmsOutage.class);
        criteria.addRestriction(new NullRestriction("perspective"));
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
        try {
            OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, InetAddress.getByName(ipAddr), serviceName);
            service.setStatus(status);
            m_monitoredServiceDao.saveOrUpdate(service);
        } catch (UnknownHostException e) {
            LOG.error("Failed to set the status for service named {} on node id {} and interface {} to {}.",
                    serviceName, nodeId,  ipAddr, status, e);
        }
    }

    @Override
    public void updateLastGoodOrFail(PollableService pollableService, PollStatus status) {
        final var nodeId = pollableService.getNodeId();
        final var ipAddr = pollableService.getAddress();
        final var serviceName = pollableService.getSvcName();
        try {
            var svc = m_transcationOps.execute((TransactionCallback<Object>) transactionStatus -> {
                final OnmsMonitoredService service = m_monitoredServiceDao.get(nodeId, ipAddr, serviceName);
                if (service == null) {
                    return null;
                }
                if (status.isAvailable()) {
                    service.setLastGood(status.getTimestamp());
                } else if (status.isUnavailable() || status.isUnresponsive()) {
                    service.setLastFail(status.getTimestamp());
                }  // else ignore, not explicitly good or bad
                m_monitoredServiceDao.saveOrUpdate(service);
                return service;
            });
            if (svc != null) {
                LOG.debug("Successfully updated last good/fail timestamp for service named {} on node id {} and interface {}.",
                        serviceName, nodeId, ipAddr);
            } else {
                LOG.debug("Service named {} on node id {} and interface {} has status {}. The service has been deleted since the poll was triggered. "
                + "Last good/fail timestamp will not be updated.", serviceName, nodeId, ipAddr, status);
            }
        } catch (Exception e) {
            LOG.error("Failed to set the last good/fail timestamp for service named {} on node id {} and interface {} for {}.",
                    serviceName, nodeId,  ipAddr, status, e);
        }
    }


}
