/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
