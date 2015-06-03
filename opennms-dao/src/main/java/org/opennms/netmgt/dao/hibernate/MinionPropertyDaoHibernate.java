/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.MinionPropertyDao;
import org.opennms.netmgt.model.OnmsMonitoringSystemProperty;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>LinkStateDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MinionPropertyDaoHibernate extends AbstractDaoHibernate<OnmsMonitoringSystemProperty, Integer> implements MinionPropertyDao {
    public MinionPropertyDaoHibernate() {
        super(OnmsMonitoringSystemProperty.class);
    }

    @Override
    public Collection<OnmsMonitoringSystemProperty> findAll(final Integer offset, final Integer limit) {
        return getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsMonitoringSystemProperty>>() {

            @Override
            @SuppressWarnings("unchecked")
            public Collection<OnmsMonitoringSystemProperty> doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsMonitoringSystemProperty.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    @Override
    public Collection<OnmsMonitoringSystemProperty> findByMonitoringSystemId(final String systemId) {
        return find("FROM OnmsMonitoringSystemProperty AS mp WHERE mp.system.id = ?", systemId);
    }

    @Override
    public OnmsMonitoringSystemProperty findByKey(final String minionId, final String key) {
        return findUnique("FROM OnmsMonitoringSystemProperty AS mp WHERE mp.system.id = ? AND mp.key = ?", minionId, key);
    }
}
