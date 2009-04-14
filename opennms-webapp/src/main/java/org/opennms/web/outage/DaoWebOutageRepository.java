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

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageCriteria.OutageCriteriaVisitor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author brozow
 *
 */
public class DaoWebOutageRepository implements WebOutageRepository {
    
    @Autowired
    private OutageDao m_outageDao;
    
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
    


    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#countMatchingOutageSummaries(org.opennms.web.outage.filter.OutageCriteria)
     */
    public int countMatchingOutageSummaries(OutageCriteria criteria) {
        throw new UnsupportedOperationException("DaoWebOutageRepository.countMatchingOutageSummaries is not yet implemented");

    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#countMatchingOutages(org.opennms.web.outage.filter.OutageCriteria)
     */
    public int countMatchingOutages(OutageCriteria criteria) {
        throw new UnsupportedOperationException("DaoWebOutageRepository.countMatchingOutages is not yet implemented");

    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#getMatchingOutageSummaries(org.opennms.web.outage.filter.OutageCriteria)
     */
    public OutageSummary[] getMatchingOutageSummaries(OutageCriteria criteria) {
        throw new UnsupportedOperationException("DaoWebOutageRepository.getMatchingOutageSummaries is not yet implemented");

    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#getMatchingOutages(org.opennms.web.outage.filter.OutageCriteria)
     */
    public Outage[] getMatchingOutages(OutageCriteria criteria) {
        throw new UnsupportedOperationException("DaoWebOutageRepository.getMatchingOutages is not yet implemented");

    }

    /* (non-Javadoc)
     * @see org.opennms.web.outage.WebOutageRepository#getOutage(int)
     */
    public Outage getOutage(int OutageId) {
        throw new UnsupportedOperationException("DaoWebOutageRepository.getOutage is not yet implemented");

    }

}
