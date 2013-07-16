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

package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;

/**
 * <p>DefaultOutageService class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultOutageService implements OutageService {

    private OutageDao m_dao;

    /**
     * <p>Constructor for DefaultOutageService.</p>
     */
    public DefaultOutageService() {

    }

    /**
     * <p>Constructor for DefaultOutageService.</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.api.OutageDao} object.
     */
    public DefaultOutageService(OutageDao dao) {
        m_dao = dao;
    }

    /**
     * <p>getDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.OutageDao} object.
     */
    public OutageDao getDao() {
        return m_dao;
    }

    /**
     * <p>setDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.api.OutageDao} object.
     */
    public void setDao(OutageDao dao) {
        this.m_dao = dao;
    }

    /**
     * <p>getCurrentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getCurrentOutageCount() {
        return m_dao.currentOutageCount();
    }
    
    /** {@inheritDoc} */
    @Override
    public Integer getOutageCount(OnmsCriteria criteria) {
        criteria.createAlias("monitoredService", "monitoredService");
        criteria.createAlias("monitoredService.ipInterface", "ipInterface");
        criteria.createAlias("monitoredService.serviceType", "serviceType");
        criteria.createAlias("ipInterface.node", "node");

        criteria.setProjection(Projections.rowCount());
        
        return m_dao.countMatching(criteria);
    }

    /**
     * <p>getCurrentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> getCurrentOutages() {
        return m_dao.currentOutages();
    }

    /**
     * <p>getCurrentOutagesOrdered</p>
     *
     * @param orderBy a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsOutage> getCurrentOutagesOrdered(String orderBy) {
        throw new UnsupportedOperationException("not implemented.. Invalid ");
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getOutagesForInterface(int nodeId,
                                                         String ipInterface) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>getOutagesForInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> getOutagesForInterface(int nodeId,
                                                         String ipAddr, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getOutagesForNode(int nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>getOutagesForNode</p>
     *
     * @param nodeId a int.
     * @param time a java$util$Date object.
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getOutagesForService(int nodeId,
                                                       String ipInterface, int serviceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>getOutagesForService</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param time a java$util$Date object.
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> getOutagesForService(int nodeId,
                                                       String ipAddr, int serviceId, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>getSuppressedOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getSuppressedOutageCount() {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /**
     * <p>getSuppressedOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> getSuppressedOutages() {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /**
     * <p>getOpenAndResolved</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsOutage> getOpenAndResolved() {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /** {@inheritDoc} */
    @Override
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

    /**
     * <p>getSuppressedOutagesByRange</p>
     *
     * @param Offset a {@link java.lang.Integer} object.
     * @param Limit a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer Offset,
                                                              Integer Limit) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /**
     * <p>getOpenAndResolved</p>
     *
     * @param Offset a {@link java.lang.Integer} object.
     * @param Limit a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsOutage> getOpenAndResolved(Integer Offset,
                                                     Integer Limit) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");

    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getCurrentOutages(String ordering) {
        throw new UnsupportedOperationException("not implemented.. Invalid ");
    }

    /** {@inheritDoc} */
    @Override
    public OnmsOutage load(Integer outageid) {
        return m_dao.load(outageid);
    }

    /** {@inheritDoc} */
    @Override
    public void update(OnmsOutage outage) {
        this.m_dao.update(outage);
    }

    /**
     * <p>getOutagesByRange</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @param orderProperty a {@link java.lang.String} object.
     * @param direction a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /**
     * <p>getOutagesByRange</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @param orderProperty a {@link java.lang.String} object.
     * @param direction a {@link java.lang.String} object.
     * @param filter a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /**
     * <p>getOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getOutageCount() {
        return m_dao.countAll();
    }

    /** {@inheritDoc} */
    @Override
    public Integer outageCountFiltered(String filter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }

    /** {@inheritDoc} */
    @Override
    public Integer outageResolvedCountFiltered(String searchFilter) {
        throw new UnsupportedOperationException("not implemented since switch to hibernate");
    }
}
