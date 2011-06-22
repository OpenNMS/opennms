/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.sql.SQLException;

public class DataLinkInterfaceDaoHibernate extends AbstractDaoHibernate<DataLinkInterface, Integer> implements DataLinkInterfaceDao {
    /**
     * <p>Constructor for DataLinkInterfaceDaoHibernate.</p>
     */
    public DataLinkInterfaceDaoHibernate() {
        super(DataLinkInterface.class);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection<DataLinkInterface> findAll(final Integer offset, final Integer limit) {
        return getHibernateTemplate().execute(new HibernateCallback<Collection<DataLinkInterface>>() {

            public Collection<DataLinkInterface> doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(DataLinkInterface.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    /** {@inheritDoc} */
    public DataLinkInterface findById(Integer id) {
        return findUnique("from DataLinkInterface as dli where dli.id = ?", id);
    }

    /** {@inheritDoc} */
    public Collection<DataLinkInterface> findByNodeId(Integer nodeId) {
        return find("from DataLinkInterface as dli where dli.nodeId = ?", nodeId);
    }

    /** {@inheritDoc} */
    public Collection<DataLinkInterface> findByNodeParentId(Integer nodeParentId) {
        return find("from DataLinkInterface as dli where dli.nodeParentId = ?", nodeParentId);
    }
}
