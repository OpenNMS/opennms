/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.OnmsNode;

public class TopologyDaoHibernate implements TopologyDao {
    protected SessionFactory sessionFactory;

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public OnmsNode getDefaultFocusPoint() {
        // first try getting the node with the most links
        final String query1 = "select dli.nodeParentId from DataLinkInterface as dli group by dli.nodeParentId order by count(*) desc";

        // if there is no node with a link, try getting the node which has the most ifspeed
        final String query2 = "select node.id from OnmsSnmpInterface as snmp join snmp.node as node group by node order by sum(snmp.ifSpeed) desc";

        Session session = sessionFactory.getCurrentSession();
        // is there already a node?
        Integer nodeParentId = (Integer)session.createQuery(query1).setMaxResults(1).uniqueResult();
        OnmsNode focusNode = getNode(nodeParentId, session);

        // no node found, try next query
        if (focusNode == null) {
            Integer nodeId = (Integer)session.createQuery(query2).setMaxResults(1).uniqueResult();
            return getNode(nodeId, session);
        }
        return focusNode;
    }

    private static OnmsNode getNode(Integer nodeId, Session session) {
        if (nodeId != null) {
            Query q = session.createQuery("from OnmsNode as n where n.id = :nodeId");
            q.setInteger("nodeId",  nodeId);
            return (OnmsNode)q.uniqueResult();
        }
        return null;
    }
}
