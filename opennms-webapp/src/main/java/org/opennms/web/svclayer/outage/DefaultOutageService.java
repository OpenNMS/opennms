/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Feb 1: Indent code, implement OnmsCriteria-friendly versions for getting outages and outage counts. - dj@opennms.org
 * 
 * Created: August 16, 2006
 *
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
 *	OpenNMS Licensing       <license@opennms.org>
 *	http://www.opennms.org/
 *	http://www.opennms.com/
 */
package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.FetchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;

/**
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */

public class DefaultOutageService implements OutageService {

    private OutageDao m_dao;

    public DefaultOutageService() {

    }

    public DefaultOutageService(OutageDao dao) {
        m_dao = dao;
    }

    public OutageDao getDao() {
        return m_dao;
    }

    public void setDao(OutageDao dao) {
        this.m_dao = dao;
    }

    public Integer getCurrentOutageCount() {
        return m_dao.currentOutageCount();
    }
    
    public Integer getOutageCount(OnmsCriteria criteria) {
        criteria.createAlias("monitoredService", "monitoredService");
        criteria.createAlias("monitoredService.ipInterface", "ipInterface");
        criteria.createAlias("monitoredService.serviceType", "serviceType");
        criteria.createAlias("ipInterface.node", "node");

        criteria.setProjection(Projections.rowCount());
        
        Collection<OnmsOutage> outages = m_dao.findMatching(criteria);
        
        Iterator i = outages.iterator();
        return (Integer) i.next();
    }

    public Collection<OnmsOutage> getCurrentOutages() {
        return m_dao.currentOutages();
    }

    public Collection<OnmsOutage> getCurrentOutagesOrdered(String orderBy) {
        throw new UnsupportedOperationException("not implemented.. Invalid ");
    }

    public Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getOutagesForInterface(int nodeId,
                                                         String ipInterface) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getOutagesForInterface(int nodeId,
                                                         String ipAddr, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getOutagesForNode(int nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getOutagesForService(int nodeId,
                                                       String ipInterface, int serviceId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<OnmsOutage> getOutagesForService(int nodeId,
                                                       String ipAddr, int serviceId, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getSuppressedOutageCount() {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getSuppressedOutages() {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getOpenAndResolved() {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, OnmsCriteria criteria) {
        criteria.setFirstResult(offset);
        criteria.setMaxResults(limit);

        criteria.createAlias("monitoredService", "monitoredService");
        criteria.setFetchMode("monitoredService", FetchMode.JOIN);

        criteria.createAlias("monitoredService.ipInterface", "ipInterface");
        criteria.setFetchMode("ipInterface", FetchMode.JOIN);

        criteria.createAlias("monitoredService.serviceType", "serviceType");
        criteria.setFetchMode("serviceType", FetchMode.JOIN);

        criteria.createAlias("ipInterface.node", "node");
        criteria.setFetchMode("node", FetchMode.JOIN);

        Order hibernateOrder = getHibernateOrder(orderProperty, direction);
        if (hibernateOrder != null) {
            criteria.addOrder(hibernateOrder);
        }
        criteria.addOrder(Order.asc("node.label"));
        criteria.addOrder(Order.asc("ipInterface.ipAddress"));
        criteria.addOrder(Order.asc("serviceType.name"));

        return m_dao.findMatching(criteria);
    }


    private Order getHibernateOrder(String orderProperty, String direction) {
        if (orderProperty == null) {
            return null;
        }

        String hibernateOrderProperty;
        if ("node".equals(orderProperty)) {
            hibernateOrderProperty = "node.label";
        } else if ("ipaddr".equals(orderProperty)) {
            hibernateOrderProperty = "ipInterface.ipAddress";
        } else if ("service".equals(orderProperty)) {
            hibernateOrderProperty = "serviceType.name";
        } else if ("iflostservice".equals(orderProperty)) {
            hibernateOrderProperty = "ifLostService";
        } else if ("ifregainedservice".equals(orderProperty)) {
            hibernateOrderProperty = "ifRegainedService";
        } else if ("outageid".equals(orderProperty)) {
            hibernateOrderProperty = "id";
        } else {
            throw new IllegalArgumentException("Do not support orderProperty='" + orderProperty + "'");
        }

        if ("asc".equals(direction)) {
            return Order.asc(hibernateOrderProperty);
        } else if ("desc".equals(direction)) {
            return Order.desc(hibernateOrderProperty);
        } else {
            throw new IllegalArgumentException("Do not support direction='" + direction + "'");
        }
    }

    public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer Offset,
                                                              Integer Limit) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getOpenAndResolved(Integer Offset,
                                                     Integer Limit) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");

    }

    public Collection<OnmsOutage> getCurrentOutages(String ordering) {
        throw new UnsupportedOperationException("not implemented.. Invalid ");
    }

    public OnmsOutage load(Integer outageid) {
        return m_dao.load(outageid);
    }

    public void update(OnmsOutage outage) {
        this.m_dao.update(outage);
    }

    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Integer getOutageCount() {
        return m_dao.countAll();
    }

    public Integer outageCountFiltered(String filter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    public Integer outageResolvedCountFiltered(String searchFilter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }
}
