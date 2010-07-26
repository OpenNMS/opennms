/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.web.outage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageCriteria.OutageCriteriaVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>DaoWebOutageRepository class.</p>
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class DaoWebOutageRepository implements WebOutageRepository {
    
    @Autowired
    private OutageDao m_outageDao;
    
    @Autowired
    private NodeDao m_nodeDao;
    
    /*
     * NOTE: Criteria building for Outages must included the following aliases"
     * 
     * monitoredService as monitoredService
     * monitoredService.ipInterface as ipInterface
     * monitoredService.ipInterface.node as node
     * monitoredService.serviceType as serviceType
     * 
     */
    
    private OnmsCriteria getOnmsCriteria(final OutageCriteria outageCriteria) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsOutage.class);
        criteria.createAlias("monitoredService", "monitoredService");
        criteria.createAlias("monitoredService.ipInterface", "ipInterface");
        criteria.createAlias("monitoredService.ipInterface.node", "node");
        criteria.createAlias("monitoredService.serviceType", "serviceType");
        
        outageCriteria.visit(new OutageCriteriaVisitor<RuntimeException>(){

            public void visitOutageType(OutageType ackType) throws RuntimeException {
                if (ackType == OutageType.CURRENT) {
                    criteria.add(Restrictions.isNull("ifRegainedService"));
                } else if (ackType == OutageType.RESOLVED) {
                    criteria.add(Restrictions.isNotNull("ifRegainedService"));
                }
            }

            public void visitFilter(Filter filter) throws RuntimeException {
                criteria.add(filter.getCriterion());
            }

            public void visitGroupBy() throws RuntimeException {
                
            }
            
            public void visitLimit(int limit, int offset) throws RuntimeException {
                criteria.setMaxResults(limit);
                criteria.setFirstResult(offset);
            }

            public void visitSortStyle(SortStyle sortStyle) throws RuntimeException {
                switch (sortStyle) {
                case NODE:
                    criteria.addOrder(Order.desc("node.label"));
                    break;
                case INTERFACE:
                    criteria.addOrder(Order.desc("ipInterface.ipAddress"));
                    break;
                case SERVICE:
                    criteria.addOrder(Order.desc("serviceType.name"));
                    break;
                case IFLOSTSERVICE:
                    criteria.addOrder(Order.desc("ifLostService"));
                    break;
                case IFREGAINEDSERVICE:
                    criteria.addOrder(Order.desc("ifRegainedService"));
                    break;
                case ID:
                    criteria.addOrder(Order.desc("id"));
                    break;
                case REVERSE_NODE:
                    criteria.addOrder(Order.asc("node.label"));
                    break;
                case REVERSE_INTERFACE:
                    criteria.addOrder(Order.asc("ipInterface.ipAddress"));
                    break;
                case REVERSE_SERVICE:
                    criteria.addOrder(Order.asc("serviceType.name"));
                    break;
                case REVERSE_IFLOSTSERVICE:
                    criteria.addOrder(Order.asc("ifLostService"));
                    break;
                case REVERSE_IFREGAINEDSERVICE:
                    criteria.addOrder(Order.asc("ifRegainedService"));
                    break;
                case REVERSE_ID:
                    criteria.addOrder(Order.asc("id"));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SortStyle: " + sortStyle);
                }
            }

        });
        
        return criteria;
    }
    
    private Outage mapOnmsOutageToOutage(OnmsOutage onmsOutage) {
        if(onmsOutage != null){
            Outage outage = new Outage();    
            outage.outageId = onmsOutage.getId();
            outage.ipAddress = onmsOutage.getIpAddress();
            outage.hostname = onmsOutage.getIpAddress();
            outage.lostServiceEventId = onmsOutage.getServiceLostEvent() != null ? onmsOutage.getServiceLostEvent().getId() : 0;
            //outage.lostServiceNotificationAcknowledgedBy = 
            outage.lostServiceTime = onmsOutage.getIfLostService();
            outage.nodeId = onmsOutage.getNodeId();
            outage.nodeLabel = m_nodeDao.get(onmsOutage.getNodeId()).getLabel();
            outage.regainedServiceEventId = onmsOutage.getServiceRegainedEvent() != null ? onmsOutage.getServiceRegainedEvent().getId() : 0;
            outage.regainedServiceTime = onmsOutage.getIfRegainedService();
            outage.serviceId = onmsOutage.getServiceId();
            outage.serviceName = onmsOutage.getMonitoredService() != null ? onmsOutage.getMonitoredService().getServiceName() : "";
            outage.suppressedBy = onmsOutage.getSuppressedBy();
            outage.suppressTime = onmsOutage.getSuppressTime();
            
            return outage;
        }else{
            return null;
        }
    }
    
    private OutageSummary mapOnmsOutageToOutageSummary(final OnmsOutage onmsOutage) {
        return new OutageSummary(
            onmsOutage.getNodeId(),
            onmsOutage.getMonitoredService().getIpInterface().getNode().getLabel(),
            onmsOutage.getIfLostService(),
            onmsOutage.getIfRegainedService(),
            new Date()
        );
    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#countMatchingOutageSummaries(org.opennms.web.outage.filter.OutageCriteria)
     */
    /** {@inheritDoc} */
    @Transactional
    public int countMatchingOutageSummaries(final OutageCriteria criteria) {
        return getMatchingOutageSummaries(criteria).length;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#countMatchingOutages(org.opennms.web.outage.filter.OutageCriteria)
     */
    /** {@inheritDoc} */
    @Transactional
    public int countMatchingOutages(OutageCriteria criteria) {
        return m_outageDao.countMatching(getOnmsCriteria(criteria));
    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#getMatchingOutageSummaries(org.opennms.web.outage.filter.OutageCriteria)
     */
    /** {@inheritDoc} */
    @Transactional
    public OutageSummary[] getMatchingOutageSummaries(final OutageCriteria criteria) {
        
        
        List<OnmsOutage> onmsOutages = m_outageDao.findMatching(getOnmsCriteria(criteria));
        
        return getOutageSummary(onmsOutages).toArray(new OutageSummary[0]);
    }

    private List<OutageSummary> getOutageSummary(List<OnmsOutage> onmsOutages) {
        List<OutageSummary> outages = new ArrayList<OutageSummary>();
        
        if(onmsOutages.size() > 0){
            Iterator<OnmsOutage> outageIt = onmsOutages.iterator();
            while(outageIt.hasNext()){
                OnmsOutage outage = outageIt.next();
                if(outage.getIfRegainedService() == null){
                    outages.add(mapOnmsOutageToOutageSummary(outage));
                }
            }
            
            return elimenateDuplicates(outages);
        }else {
            return outages;
        }
    }

    private List<OutageSummary> elimenateDuplicates(final List<OutageSummary> outagesSummaries) {
        final Map<Integer,OutageSummary> uniqueSummaries = new HashMap<Integer,OutageSummary>();

        for (final OutageSummary outageSum : outagesSummaries) {
            if (!uniqueSummaries.containsKey(outageSum.getNodeId())) {
                uniqueSummaries.put(outageSum.getNodeId(), outageSum);
            }
        }

        List<OutageSummary> uniqueList = new ArrayList<OutageSummary>(uniqueSummaries.values());
        Collections.sort(uniqueList);
        return uniqueList;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#getMatchingOutages(org.opennms.web.outage.filter.OutageCriteria)
     */
    /** {@inheritDoc} */
    @Transactional
    public Outage[] getMatchingOutages(final OutageCriteria criteria) {
        final List<Outage> outages = new ArrayList<Outage>();
        final List<OnmsOutage> onmsOutages = m_outageDao.findMatching(getOnmsCriteria(criteria));
        
        for (final OnmsOutage outage : onmsOutages) {
            outages.add(mapOnmsOutageToOutage(outage));
        }
        
        return outages.toArray(new Outage[0]);

    }


    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#getOutage(int)
     */
    /** {@inheritDoc} */
    @Transactional
    public Outage getOutage(final int OutageId) {
        return mapOnmsOutageToOutage(m_outageDao.get(OutageId));
    }

    @Transactional
    public int countCurrentOutages() {
        return m_outageDao.countOutagesByNode();
    }

    @Transactional
    public OutageSummary[] getCurrentOutages(final int rows) {
        return m_outageDao.getNodeOutageSummaries(rows).toArray(new OutageSummary[0]);
    }

}
