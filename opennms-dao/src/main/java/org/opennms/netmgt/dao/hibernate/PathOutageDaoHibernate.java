/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.model.OnmsPathOutage;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>PathOutageDaoHibernate class</p>
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 *
 */
public class PathOutageDaoHibernate extends AbstractDaoHibernate<OnmsPathOutage, Integer> implements PathOutageDao{

    /**
     * <p>PathOutageDaoHibernate constructor</p>
     */
    public PathOutageDaoHibernate() {
        super(OnmsPathOutage.class);
    }

    @Override
    public List<Integer> getNodesForPathOutage(final OnmsPathOutage pathOutage) {
        return getNodesForPathOutage(pathOutage.getCriticalPathIp(), pathOutage.getCriticalPathServiceName());
    }

    @Override
    public List<Integer> getNodesForPathOutage(final InetAddress ipAddress, final String serviceName) {

        // SELECT count(DISTINCT pathoutage.nodeid) FROM pathoutage, ipinterface WHERE pathoutage.criticalpathip=? AND pathoutage.criticalpathservicename=? AND pathoutage.nodeid=ipinterface.nodeid AND ipinterface.ismanaged!='D'"" +

        return getHibernateTemplate().execute(new HibernateCallback<List<Integer>>() {
            @Override
            public List<Integer> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("select distinct pathOutage.node.id from OnmsPathOutage as pathOutage left join pathOutage.node as node left join node.ipInterfaces as ipInterfaces left join ipInterfaces.monitoredServices as monitoredServices where pathOutage.criticalPathIp = :ipAddress and pathOutage.criticalPathServiceName = :serviceName and ipInterfaces.isManaged <> 'D' and monitoredServices.status = 'A'");
                query.setParameter("ipAddress", InetAddressUtils.str(ipAddress));
                query.setParameter("serviceName", serviceName);
                List<Integer> result = (List<Integer>)query.list();
                if (result == null) {
                    return Collections.emptyList();
                } else {
                    return result;
                }
            }
        });
    }

    /*
    final org.opennms.core.criteria.Criteria pathOutageCrit = new org.opennms.core.criteria.Criteria(OnmsPathOutage.class)
    .setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.FULL_JOIN)//,
            //new Alias("node.ipInterfaces","ipInterfaces", JoinType.FULL_JOIN)
    }))
    .addRestriction(new EqRestriction("criticalPathIp", InetAddressUtils.addr(criticalPathIp)))
    .addRestriction(new EqRestriction("criticalPathServiceName", criticalPathServiceName))
    .addRestriction(new EqRestriction("node.id", Integer.valueOf(result[1])))
    //.addRestriction(new NeRestriction("ipInterfaces.isManaged", "D"))
    ;

    result[2] = Integer.toString(pathOutageDao.countMatching(pathOutageCrit));
    /*
	    return getHibernateTemplate().execute(new HibernateCallback<OnmsNode>() {
	        @Override
	        public OnmsFilterFavorite doInHibernate(Session session) throws HibernateException, SQLException {
	            Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.filterName = :filterName");
	            query.setParameter("filterName", filterName);
	            query.setParameter("userName", userName);
	            Object result = query.uniqueResult();
	            if (result == null) return null;
	            return (OnmsFilterFavorite)result;
	        }
	    });
	}
     */
}
