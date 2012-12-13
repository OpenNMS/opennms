/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>
 * AccessPointDaoHibernate class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class AccessPointDaoHibernate extends AbstractDaoHibernate<OnmsAccessPoint, Integer> implements AccessPointDao {

    /**
     * <p>
     * Constructor for AccessPointDaoHibernate.
     * </p>
     */
    public AccessPointDaoHibernate() {
        super(OnmsAccessPoint.class);
    }

    /** {@inheritDoc} */
    public OnmsAccessPoint findByPhysAddr(final String physaddr) {
        // Case insensitive search
        return findUnique("from OnmsAccessPoint as aps where upper(aps.physAddr) = ?", physaddr.toUpperCase());
    }

    /** {@inheritDoc} */
    public OnmsAccessPointCollection findByPackage(final String pkg) {
        String hql = "from OnmsAccessPoint as aps where aps.pollingPackage = ?";
        OnmsAccessPointCollection aps = new OnmsAccessPointCollection();
        aps.addAll(super.find(hql, pkg));
        return aps;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<String> findDistinctPackagesLike(final String pkg) {
        final HibernateCallback<List<String>> callback = new HibernateCallback<List<String>>() {
            public List<String> doInHibernate(final Session session) throws SQLException {
                return session.createCriteria(OnmsAccessPoint.class).setProjection(Projections.groupProperty("pollingPackage")).add(Restrictions.like("pollingPackage", pkg)).list();
            }
        };
        return getHibernateTemplate().executeFind(callback);
    }
}
