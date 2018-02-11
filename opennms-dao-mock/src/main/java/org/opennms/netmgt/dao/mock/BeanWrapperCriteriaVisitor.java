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

package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria.CriteriaVisitor;
import org.opennms.core.criteria.Criteria.LockType;
import org.opennms.core.criteria.Fetch;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restriction;

public class BeanWrapperCriteriaVisitor implements CriteriaVisitor {
    // private static final Logger LOG = LoggerFactory.getLogger(BeanWrapperCriteriaVisitor.class);

    private List<Order> m_orders = new ArrayList<>();
    private List<Alias> m_aliases = new ArrayList<>();
    private List<Fetch> m_fetches = new ArrayList<>();
    private boolean m_distinct = false;
    private Integer m_limit = 0;
    private Integer m_offset = 0;
    private List<?> m_entities;
    private List<?> m_matching;

    public BeanWrapperCriteriaVisitor(final Object... obj) {
        m_entities = Arrays.asList(obj);
        m_matching = Arrays.asList(obj);
    }

    public BeanWrapperCriteriaVisitor(final List<?> obj) {
        m_entities = obj;
        m_matching = new ArrayList<Object>(obj);
    }

    public void reset() {
        m_matching = new ArrayList<Object>(m_entities);
    }

    @Override
    public void visitClassAndRootAlias(final Class<?> clazz, final String rootAlias) {
        final List<Object> matching = new ArrayList<>();
        for (final Object o : m_matching) {
            if (o.getClass().isAssignableFrom(clazz)) {
                matching.add(o);
            }
        }
        m_matching = matching;
    }

    @Override
    public void visitOrder(final Order order) {
        m_orders.add(order);
    }

    @Override
    public void visitOrdersFinished() {
        //throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void visitAlias(final Alias alias) {
        m_aliases.add(alias);
    }

    @Override
    public void visitAliasesFinished() {
        //throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void visitLockType(final LockType lock) {
        //throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void visitFetch(final Fetch fetch) {
        m_fetches.add(fetch);
    }

    @Override
    public void visitFetchesFinished() {
        //throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void visitRestriction(final Restriction restriction) {
        final List<Object> matching = new ArrayList<>();
        for (final Object entity : m_matching) {
            final BeanWrapperRestrictionVisitor visitor = new BeanWrapperRestrictionVisitor(entity, m_aliases);
            restriction.visit(visitor);
            if (visitor.matches()) {
                matching.add(entity);
            }
        }
        m_matching = matching;
    }

    @Override
    public void visitRestrictionsFinished() {
        //throw new UnsupportedOperationException("Not yet implemented!");
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

    public Collection<?> getMatches() {
        List<Object> matches = new ArrayList<Object>(m_matching);
        if (m_offset != null && m_offset > 0) {
            matches = matches.subList(m_offset, matches.size());
        }
        if (m_limit != null && m_limit > 0) {
            matches = matches.subList(0, m_limit);
        }
        if (m_distinct) {
            return new LinkedHashSet<Object>(matches);
        } else {
            return new ArrayList<Object>(matches);
        }
    }
}
