//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.sql.SQLException;

/**
 * <p>DataLinkInterfaceDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
        return (Collection<DataLinkInterface>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
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
