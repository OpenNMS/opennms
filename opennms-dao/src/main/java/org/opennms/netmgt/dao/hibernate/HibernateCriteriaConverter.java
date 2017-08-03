/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.opennms.core.criteria.AbstractCriteriaVisitor;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Criteria.LockType;
import org.opennms.core.criteria.Fetch;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.Order.OrderVisitor;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.BaseRestrictionVisitor;
import org.opennms.core.criteria.restrictions.BetweenRestriction;
import org.opennms.core.criteria.restrictions.EqPropertyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.GeRestriction;
import org.opennms.core.criteria.restrictions.GtRestriction;
import org.opennms.core.criteria.restrictions.IlikeRestriction;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.criteria.restrictions.IplikeRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.core.criteria.restrictions.LikeRestriction;
import org.opennms.core.criteria.restrictions.LtRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.core.criteria.restrictions.NotNullRestriction;
import org.opennms.core.criteria.restrictions.NotRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.RestrictionVisitor;
import org.opennms.core.criteria.restrictions.SqlRestriction;
import org.opennms.netmgt.dao.api.CriteriaConverter;

import com.google.common.base.Strings;

public class HibernateCriteriaConverter implements CriteriaConverter<DetachedCriteria> {
    public org.hibernate.Criteria convert(final Criteria criteria, final Session session) {
        final HibernateCriteriaVisitor visitor = new HibernateCriteriaVisitor();
        criteria.visit(visitor);

        return visitor.getCriteria(session);
    }

    @Override
    public DetachedCriteria convert(final Criteria criteria) {
        final HibernateCriteriaVisitor visitor = new HibernateCriteriaVisitor();
        criteria.visit(visitor);

        return visitor.getCriteria();
    }

    public org.hibernate.Criteria convertForCount(final Criteria criteria, final Session session) {
        final HibernateCriteriaVisitor visitor = new CountHibernateCriteriaVisitor();
        criteria.visit(visitor);

        return visitor.getCriteria(session);
    }

    @Override
    public DetachedCriteria convertForCount(final Criteria criteria) {
        final HibernateCriteriaVisitor visitor = new HibernateCriteriaVisitor() {
            @Override
            public void visitOrder(final Order order) {
                // skip order-by when converting for count
            }
        };
        criteria.visit(visitor);

        return visitor.getCriteria();
    }

    public static class CountHibernateCriteriaVisitor extends HibernateCriteriaVisitor {
        @Override
        public void visitOrder(final Order order) {
            // skip order-by when converting for count
        }
    }

    public static class HibernateCriteriaVisitor extends AbstractCriteriaVisitor {
        private DetachedCriteria m_criteria;

        private Class<?> m_class;

        private Set<org.hibernate.criterion.Order> m_orders = new LinkedHashSet<>();

        private Set<org.hibernate.criterion.Criterion> m_criterions = new LinkedHashSet<>();

        private boolean m_distinct = false;

        private Integer m_limit;

        private Integer m_offset;

        public org.hibernate.Criteria getCriteria(final Session session) {
            final org.hibernate.Criteria hibernateCriteria = getCriteria().getExecutableCriteria(session);
            if (m_limit != null)
                hibernateCriteria.setMaxResults(m_limit);
            if (m_offset != null)
                hibernateCriteria.setFirstResult(m_offset);
            return hibernateCriteria;
        }

        public DetachedCriteria getCriteria() {
            if (m_criteria == null) {
                throw new IllegalStateException("Unable to determine Class<?> of this criteria!");
            }

            for (final Criterion criterion : m_criterions) {
                m_criteria.add(criterion);
            }

            /*
             * By implementing distinct() as a subquery, we lose the ability to sort the
             * results on any of the aliased columns. See bug NMS-7830 for more details.
             * 
             * @see http://issues.opennms.org/browse/NMS-7830
             */
            if (m_distinct) {
                /*
                 * This technique is documented in the comments at:
                 * 
                 * @see http://floledermann.blogspot.com/2007/10/solving-hibernate-criterias-distinct.html?showComment=1262701416137#c529514229952853263
                 */
                m_criteria.setProjection(Projections.distinct(Projections.id()));

                final DetachedCriteria newCriteria = DetachedCriteria.forClass(m_class);
                newCriteria.add(Subqueries.propertyIn("id", m_criteria));

                m_criteria = newCriteria;
            }

            for (final org.hibernate.criterion.Order order : m_orders) {
                m_criteria.addOrder(order);
            }

            return m_criteria;
        }

        /**
         * If {@code rootAlias} is null, then Hibernate will use a default
         * alias of {@code this}.
         */
        @Override
        public void visitClassAndRootAlias(final Class<?> clazz, final String rootAlias) {
            m_class = clazz;
            if (rootAlias == null) {
                m_criteria = DetachedCriteria.forClass(clazz);
            } else {
                m_criteria = DetachedCriteria.forClass(clazz, rootAlias);
            }
        }

        @Override
        public void visitOrder(final Order order) {
            final HibernateOrderVisitor visitor = new HibernateOrderVisitor();
            order.visit(visitor);
            // we hold onto these later because they need to be applied after
            // distinct projection
            m_orders.add(visitor.getOrder());
        }

        @Override
        public void visitAlias(final Alias alias) {
            int aliasType = 0;
            switch (alias.getType()) {
            case FULL_JOIN:
                aliasType = org.hibernate.Criteria.FULL_JOIN;
                break;
            case LEFT_JOIN:
                aliasType = org.hibernate.Criteria.LEFT_JOIN;
                break;
            case INNER_JOIN:
                aliasType = org.hibernate.Criteria.INNER_JOIN;
                break;
            default:
                aliasType = org.hibernate.Criteria.INNER_JOIN;
                break;
            }
            if (alias.hasJoinCondition()) { // an additional condition for the join
                final HibernateRestrictionVisitor visitor = new HibernateRestrictionVisitor();
                alias.getJoinCondition().visit(visitor);
                m_criteria.createAlias(alias.getAssociationPath(), alias.getAlias(), aliasType, visitor.getCriterions().get(0));
            } else { // no additional condition for the join
                m_criteria.createAlias(alias.getAssociationPath(), alias.getAlias(), aliasType);
            }
        }

        @Override
        public void visitFetch(final Fetch fetch) {
            switch (fetch.getFetchType()) {
            case DEFAULT:
                m_criteria.setFetchMode(fetch.getAttribute(), FetchMode.DEFAULT);
                break;
            case EAGER:
                m_criteria.setFetchMode(fetch.getAttribute(), FetchMode.JOIN);
                break;
            case LAZY:
                m_criteria.setFetchMode(fetch.getAttribute(), FetchMode.SELECT);
                break;
            default:
                m_criteria.setFetchMode(fetch.getAttribute(), FetchMode.DEFAULT);
                break;
            }
        }

        @Override
        public void visitLockType(final LockType lock) {
            if (lock == null) return;

            switch (lock) {
            case NONE:
                m_criteria.setLockMode(LockMode.NONE);
                break;
            case OPTIMISTIC:
                m_criteria.setLockMode(LockMode.OPTIMISTIC);
                break;
            case OPTIMISTIC_FORCE_INCREMENT:
                m_criteria.setLockMode(LockMode.OPTIMISTIC_FORCE_INCREMENT);
                break;
            case PESSIMISTIC_FORCE_INCREMENT:
                m_criteria.setLockMode(LockMode.PESSIMISTIC_FORCE_INCREMENT);
                break;
            case PESSIMISTIC_READ:
                m_criteria.setLockMode(LockMode.PESSIMISTIC_READ);
                break;
            case PESSIMISTIC_WRITE:
                m_criteria.setLockMode(LockMode.PESSIMISTIC_WRITE);
                break;
            case READ:
                m_criteria.setLockMode(LockMode.READ);
                break;
            case UPGRADE_NOWAIT:
                m_criteria.setLockMode(LockMode.UPGRADE_NOWAIT);
                break;
            case WRITE:
                m_criteria.setLockMode(LockMode.WRITE);
                break;
            }
        }

        @Override
        public void visitRestriction(final Restriction restriction) {
            final HibernateRestrictionVisitor visitor = new HibernateRestrictionVisitor();
            restriction.visit(visitor);
            m_criterions.addAll(visitor.getCriterions());
        }

        @Override
        public void visitDistinct(final boolean distinct) {
            m_distinct = distinct;
        }

        @Override
        public void visitLimit(final Integer limit) {
            m_limit = limit;
        }

        @Override
        public void visitOffset(final Integer offset) {
            m_offset = offset;
        }

    };

    public static final class HibernateOrderVisitor implements OrderVisitor {
        private String m_attribute;

        private boolean m_ascending = true;

        @Override
        public void visitAttribute(final String attribute) {
            m_attribute = attribute;
        }

        @Override
        public void visitAscending(final boolean ascending) {
            m_ascending = ascending;
        }

        public org.hibernate.criterion.Order getOrder() {
            if (m_ascending) {
                return org.hibernate.criterion.Order.asc(m_attribute);
            } else {
                return org.hibernate.criterion.Order.desc(m_attribute);
            }
        }
    }

    public static final class HibernateRestrictionVisitor extends BaseRestrictionVisitor implements RestrictionVisitor {
        private static final StringType STRING_TYPE = new StringType();

        private List<Criterion> m_criterions = new ArrayList<>();

        public List<Criterion> getCriterions() {
            return m_criterions;
        }

        @Override
        public void visitNull(final NullRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.isNull(restriction.getAttribute()));
        }

        @Override
        public void visitNotNull(final NotNullRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.isNotNull(restriction.getAttribute()));
        }

        @Override
        public void visitEq(final EqRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.eq(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitEqProperty(final EqPropertyRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.eqProperty(restriction.getAttribute(), restriction.getValue().toString()));
        }

        @Override
        public void visitNe(final NeRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.ne(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitGt(final GtRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.gt(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitGe(final GeRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.ge(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitLt(final LtRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.lt(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitLe(final LeRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.le(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitAllComplete(final AllRestriction restriction) {
            final int restrictionSize = restriction.getRestrictions().size();
            final int criterionSize = m_criterions.size();
            if (criterionSize < restrictionSize) {
                throw new IllegalStateException("AllRestriction with " + restrictionSize + " entries encountered, but we only have " + criterionSize + " criterions!");
            }
            final List<Criterion> criterions = m_criterions.subList(criterionSize - restrictionSize, criterionSize);
            final Junction j = org.hibernate.criterion.Restrictions.conjunction();
            for (final Criterion crit : criterions) {
                j.add(crit);
            }
            criterions.clear();
            m_criterions.add(j);
        }

        @Override
        public void visitAnyComplete(final AnyRestriction restriction) {
            final int restrictionSize = restriction.getRestrictions().size();
            final int criterionSize = m_criterions.size();
            if (criterionSize < restrictionSize) {
                throw new IllegalStateException("AllRestriction with " + restrictionSize + " entries encountered, but we only have " + criterionSize + " criterions!");
            }
            final List<Criterion> criterions = m_criterions.subList(criterionSize - restrictionSize, criterionSize);
            final Junction j = org.hibernate.criterion.Restrictions.disjunction();
            for (final Criterion crit : criterions) {
                j.add(crit);
            }
            criterions.clear();
            m_criterions.add(j);
        }

        @Override
        public void visitLike(final LikeRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.like(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitIlike(final IlikeRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.ilike(restriction.getAttribute(), restriction.getValue()));
        }

        @Override
        public void visitIn(final InRestriction restriction) {
            if (restriction.getValues() == null || restriction.getValues().size() == 0) {
                m_criterions.add(org.hibernate.criterion.Restrictions.sqlRestriction("0"));
            } else {
                m_criterions.add(org.hibernate.criterion.Restrictions.in(restriction.getAttribute(), restriction.getValues()));
            }
        }

        @Override
        public void visitNotComplete(final NotRestriction restriction) {
            if (m_criterions.size() == 0) {
                throw new IllegalStateException("NotRestriction called, but no criterions exist to negate!");
            }
            final Criterion criterion = m_criterions.remove(m_criterions.size() - 1);
            m_criterions.add(org.hibernate.criterion.Restrictions.not(criterion));
        }

        @Override
        public void visitBetween(final BetweenRestriction restriction) {
            m_criterions.add(org.hibernate.criterion.Restrictions.between(restriction.getAttribute(), restriction.getBegin(), restriction.getEnd()));
        }

        @Override
        public void visitSql(final SqlRestriction restriction) {
            if (restriction.getParameters() != null && restriction.getParameters().length > 0) {
                // Map our {@link Type} enum values to {@link org.hibernate.type.Type} equivalents 
                org.hibernate.type.Type[] types = Arrays.stream(restriction.getTypes()).map(t -> { 
                    switch(t) {
                    case FLOAT:
                        return new FloatType();
                    case INTEGER:
                        return new IntegerType();
                    case LONG:
                        return new LongType();
                    case STRING:
                        return new StringType();
                    case TIMESTAMP:
                        return new TimestampType();
                    default: 
                        throw new UnsupportedOperationException("Unsupported type specified in SqlRestriction");
                    }
                }).collect(Collectors.toList()).toArray(new org.hibernate.type.Type[restriction.getTypes().length]);
                m_criterions.add(org.hibernate.criterion.Restrictions.sqlRestriction(restriction.getAttribute(), restriction.getParameters(), types));
            } else {
                m_criterions.add(org.hibernate.criterion.Restrictions.sqlRestriction(restriction.getAttribute()));
            }
        }

        @Override
        public void visitIplike(final IplikeRestriction restriction) {
            String attribute = restriction.getAttribute();
            if (Strings.isNullOrEmpty(attribute)) {
                attribute = "ipAddr";
            }
            final String sql = String.format("ipLike({alias}.%s, ?)", attribute);
            m_criterions.add(org.hibernate.criterion.Restrictions.sqlRestriction(sql, restriction.getValue(), STRING_TYPE));
        }
    }
}
