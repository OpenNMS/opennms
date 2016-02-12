/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.persistence.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.orm.hibernate3.HibernateCallback;

public class BusinessServiceDaoImpl extends AbstractDaoHibernate<BusinessServiceEntity, Long> implements BusinessServiceDao {

    public BusinessServiceDaoImpl() {
        super(BusinessServiceEntity.class);
    }

    @Override
    public Set<BusinessServiceEntity> findParents(BusinessServiceEntity child) {
        final long childId =  Objects.requireNonNull(child).getId();
        Set<BusinessServiceEntity> parents = getHibernateTemplate().execute(new HibernateCallback<Set<BusinessServiceEntity>>() {
            @Override
            public Set<BusinessServiceEntity> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("select edge from BusinessServiceEdgeEntity edge where type(edge) = BusinessServiceChildEdgeEntity and edge.child.id = :childId");
                query.setParameter("childId", childId);
                @SuppressWarnings("unchecked")
                List<BusinessServiceEdgeEntity> list = query.list();
                return list.stream().map(e -> e.getBusinessService()).collect(Collectors.toSet());
            }
        });
        return parents;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BusinessServiceEntity> findMatching(final org.opennms.core.criteria.Criteria criteria) {
        final HibernateCallback<List<BusinessServiceEntity>> callback = session -> {
            final Criteria hibernateCriteria = m_criteriaConverter.convert(criteria, session);
            // Manually override default. Otherwise for each 1 - n relationship (with n > 1), n entities are returned instead of 1
            hibernateCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            return (List<BusinessServiceEntity>)(hibernateCriteria.list());
        };
        return getHibernateTemplate().execute(callback);
    }

}
