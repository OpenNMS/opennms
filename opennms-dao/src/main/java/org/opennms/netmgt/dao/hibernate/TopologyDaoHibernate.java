/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class TopologyDaoHibernate extends HibernateDaoSupport implements TopologyDao {
    @Override
    public OnmsNode getDefaultFocusPoint() {

        // getting the node which has the most ifspeed
        final String query2 = "select node.id from OnmsSnmpInterface as snmp join snmp.node as node group by node order by sum(snmp.ifSpeed) desc";

        // is there already a node?
        OnmsNode focusNode = getHibernateTemplate().execute(new HibernateCallback<OnmsNode>() {
            public OnmsNode doInHibernate(Session session) throws HibernateException, SQLException {
                Integer nodeId = (Integer)session.createQuery(query2).setMaxResults(1).uniqueResult();
                return getNode(nodeId, session);
            }
        });

        return focusNode;
    }

    private OnmsNode getNode(Integer nodeId, Session session) {
        if (nodeId != null) {
            Query q = session.createQuery("from OnmsNode as n where n.id = :nodeId");
            q.setInteger("nodeId",  nodeId);
            return (OnmsNode)q.uniqueResult();
        }
        return null;
    }
}
