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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.criteria.Order;
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
                return list.stream().map(BusinessServiceEdgeEntity::getBusinessService).collect(Collectors.toSet());
            }
        });
        return parents;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BusinessServiceEntity> findMatching(final org.opennms.core.criteria.Criteria criteria) {
        final HibernateCallback<List<BusinessServiceEntity>> callback = session -> {
            // If limit and offset are set, we MUST manually limit the result, as by default
            // hibernate would return multiple rows for each entity if the criteria has alias/join definitions
            // or the entity fetched by the criteria has EAGER loaded relationships and this relationship is 1:n and
            // there is more than 1 elements (e.g. each parent has 3 children). Order and Limit definitions are not
            // working in this case, using the default implementation of findMatching. See: BSM-104, NMS-8079
            if (criteria.getLimit() != null || criteria.getOffset() != null) {
                final Criteria idCriteria = m_criteriaConverter.convert(criteria, session);
                idCriteria.setProjection(Projections.distinct(
                        Projections.projectionList()
                            .add(Projections.property("id"))
                            .add(Projections.property("name"))));
                List<Object[]> idList = idCriteria.list();
                if (!idList.isEmpty()) {
                    // Prepare criteria
                    Criteria entityCriteria = session.createCriteria(criteria.getCriteriaClass());
                    entityCriteria.add(Restrictions.in("id", idList.stream().map(e -> e[0]).collect(Collectors.toList())));
                    entityCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

                    // Enforce ordering
                    for (Order eachOnmsOrder : criteria.getOrders()) {
                        if (eachOnmsOrder.asc()) {
                            entityCriteria.addOrder(org.hibernate.criterion.Order.asc(eachOnmsOrder.getAttribute()));
                        } else {
                            entityCriteria.addOrder(org.hibernate.criterion.Order.desc(eachOnmsOrder.getAttribute()));
                        }
                    }
                    return entityCriteria.list();
                }
                return Collections.emptyList();
            } else { // if no offset, limit is set, we can leverage the DISTINCT_ROOT_ENTITY result transformer behaviour.
                // Manually override default. Otherwise for each 1 - n relationship (with n > 1), n entities are returned instead of 1
                final Criteria hibernateCriteria = m_criteriaConverter.convert(criteria, session);
                hibernateCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                return (List<BusinessServiceEntity>)(hibernateCriteria.list());
            }
        };
        return getHibernateTemplate().execute(callback);
    }

}
