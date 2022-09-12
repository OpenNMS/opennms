/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.core.criteria;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.opennms.core.criteria.restrictions.Restriction;

public class Criteria {

	/**
	 * This enum provides all of the locking modes that are available in the
	 * ORM implementation.
	 */
	public enum LockType {
		NONE,
		READ,
		UPGRADE_NOWAIT,
		WRITE,
		OPTIMISTIC,
		OPTIMISTIC_FORCE_INCREMENT,
		PESSIMISTIC_READ,
		PESSIMISTIC_WRITE,
		PESSIMISTIC_FORCE_INCREMENT
	}

    public static interface CriteriaVisitor {
        default void visitClassAndRootAlias(final Class<?> clazz, final String rootAlias) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitOrder(final Order order) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitOrdersFinished() {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitAlias(final Alias alias) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitAliasesFinished() {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitFetch(final Fetch fetch) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitFetchesFinished() {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitLockType(final LockType lock) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitRestriction(final Restriction restriction) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitRestrictionsFinished() {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitDistinct(final boolean distinct) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitLimit(final Integer limit) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }

        default void visitOffset(final Integer offset) {
            // empty visitor methods allow for implementers to only write code for the bits that are relevant
        }
    }

    public void visit(final CriteriaVisitor visitor) {
        visitor.visitClassAndRootAlias(getCriteriaClass(), getRootAlias());

        for (final Order order : getOrders()) {
            visitor.visitOrder(order);
        }
        visitor.visitOrdersFinished();

        for (final Alias alias : getAliases()) {
            visitor.visitAlias(alias);
        }
        visitor.visitAliasesFinished();

        for (final Fetch fetch : getFetchTypes()) {
            visitor.visitFetch(fetch);
        }
        visitor.visitFetchesFinished();

        for (final Restriction restriction : getRestrictions()) {
            visitor.visitRestriction(restriction);
        }
        visitor.visitRestrictionsFinished();

        visitor.visitDistinct(isDistinct());
        visitor.visitLimit(getLimit());
        visitor.visitOffset(getOffset());
    }

    private static final Pattern SPLIT_ON = Pattern.compile("\\.");

    private Class<?> m_class;

    private final String m_rootAlias;

    private List<Order> m_orders = new ArrayList<>();

    private List<Alias> m_aliases = new ArrayList<>();

    private Set<Fetch> m_fetchTypes = new LinkedHashSet<>();

    private Set<Restriction> m_restrictions = new LinkedHashSet<>();

    private boolean m_distinct = false;

    private Integer m_limit = null;

    private Integer m_offset = null;

    private LockType m_lockType = null;

    private boolean m_isMultipleAnd = false;

    public Criteria(final Class<?> clazz) {
        this(clazz, null);
    }

    public Criteria(final Class<?> clazz, String rootAlias) {
        m_class = clazz;
        m_rootAlias = rootAlias;
    }

    public Criteria(final Criteria c) {
        m_class = c.m_class;
        m_rootAlias = c.m_rootAlias;
        this.setAliases(c.getAliases());
        this.setDistinct(c.isDistinct());
        this.setFetchTypes(c.getFetchTypes());
        this.setLimit(c.getLimit());
        this.setOffset(c.getOffset());
        this.setOrders(c.getOrders());
        this.setRestrictions(c.getRestrictions());
    }

    public void setClass(final Class<?> clazz) {
        this.m_class = clazz;
    }

    public final Class<?> getCriteriaClass() {
        return m_class;
    }

    public final String getRootAlias() {
        return m_rootAlias;
    }

    public final Collection<Order> getOrders() {
        return Collections.unmodifiableList(m_orders);
    }

    public final Criteria setOrders(final Collection<? extends Order> orderCollection) {
        if (m_orders == orderCollection) return this;
        m_orders.clear();
        if (orderCollection != null) {
            m_orders.addAll(orderCollection);
        }
        return this;
    }

    public final Collection<Fetch> getFetchTypes() {
        return Collections.unmodifiableList(new ArrayList<Fetch>(m_fetchTypes));
    }

    public final Criteria setFetchTypes(final Collection<? extends Fetch> fetchTypes) {
        if (m_fetchTypes == fetchTypes) return this;
        m_fetchTypes.clear();
        m_fetchTypes.addAll(fetchTypes);
        return this;
    }

    public final Collection<Alias> getAliases() {
        return Collections.unmodifiableList(m_aliases);
    }

    public final Criteria setAliases(final Collection<? extends Alias> aliases) {
        if (m_aliases == aliases) return this;
        m_aliases.clear();
        m_aliases.addAll(aliases);
        return this;
    }

    public final Collection<Restriction> getRestrictions() {
        return Collections.unmodifiableList(new ArrayList<Restriction>(m_restrictions));
    }

    public final Criteria setRestrictions(Collection<? extends Restriction> restrictions) {
        if (m_restrictions == restrictions) return this;
        m_restrictions.clear();
        m_restrictions.addAll(restrictions);
        return this;
    }

    public final Criteria addRestriction(final Restriction restriction) {
        m_restrictions.add(restriction);
        return this;
    }

    public final boolean isDistinct() {
        return m_distinct;
    }

    public final Criteria setDistinct(final boolean distinct) {
        m_distinct = distinct;
        return this;
    }

    public final boolean isMultipleAnd() {
        return m_isMultipleAnd;
    }

    public final Criteria setMultipleAnd(final boolean multipleAnd) {
        m_isMultipleAnd = multipleAnd;
        return this;
    }

    public final Integer getLimit() {
        return m_limit;
    }

    public final Criteria setLimit(final Integer limit) {
        m_limit = limit;
        return this;
    }

    public final LockType getLockType() {
        return m_lockType ;
    }

    public final Criteria setLockType(final LockType lock) {
    	m_lockType = lock;
        return this;
    }

    public final Integer getOffset() {
        return m_offset;
    }

    public final Criteria setOffset(final Integer offset) {
        m_offset = offset;
        return this;
    }

    public final Class<?> getType(final String path) throws IntrospectionException {
        return getType(this.getCriteriaClass(), path);
    }

    private final Class<?> getType(final Class<?> clazz, final String path) throws IntrospectionException {
        final String[] split = SPLIT_ON.split(path);
        final List<String> pathSections = Arrays.asList(split);
        return getType(clazz, pathSections, new ArrayList<>(getAliases()));
    }

    /**
     * Given a class, a list of spring-resource-style path sections, and an
     * array of aliases to process, return the type of class associated with
     * the resource.
     * 
     * @param clazz
     *            The class to process for properties.
     * @param pathSections
     *            The path sections, eg: node.ipInterfaces
     * @param aliases
     *            A list of aliases that have not yet been processed yet. We
     *            use this to detect whether an alias has already been
     *            resolved so it doesn't loop. See {@class
     *            ConcreteObjectTest#testAliases()} for an example of why this
     *            is necessary.
     * @return The class type that matches.
     * @throws IntrospectionException
     */
    private final Class<?> getType(final Class<?> clazz, final List<String> pathSections, final List<Alias> aliases) throws IntrospectionException {
        if (pathSections.isEmpty()) {
            return clazz;
        }

        final String pathElement = pathSections.get(0);
        final List<String> remaining = pathSections.subList(1, pathSections.size());

        final Iterator<Alias> aliasIterator = aliases.iterator();
        while (aliasIterator.hasNext()) {
            final Alias alias = aliasIterator.next();
            if (alias.getAlias().equals(alias.getAssociationPath())) {
                // in some cases, we will alias eg "node" -> "node", skip if
                // they're identical
                continue;
            }

            if (alias.getAlias().equals(pathElement)) {
                aliasIterator.remove();

                final String associationPath = alias.getAssociationPath();
                // we have a match, retry with the "real" path
                final List<String> paths = new ArrayList<>();
                paths.addAll(Arrays.asList(SPLIT_ON.split(associationPath)));
                paths.addAll(remaining);
                return getType(clazz, paths, aliases);
            }
        }

        final BeanInfo bi = Introspector.getBeanInfo(clazz);
        for (final PropertyDescriptor pd : bi.getPropertyDescriptors()) {
            if (pathElement.equals(pd.getName())) {
                final Class<?> propertyType = pd.getPropertyType();
                if (Collection.class.isAssignableFrom(propertyType)) {
                    final Type[] t = getGenericReturnType(pd);
                    if (t != null && t.length == 1) {
                        return getType((Class<?>) t[0], remaining, aliases);
                    }
                }
                return getType(propertyType, remaining, aliases);
            }
        }

        return null;
    }

    private final Type[] getGenericReturnType(final PropertyDescriptor pd) {
        final Method m = pd.getReadMethod();
        if (m != null) {
            final Type returnType = m.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                final ParameterizedType pt = (ParameterizedType) returnType;
                return pt.getActualTypeArguments();
            }
        }
        return new Type[0];
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        final List<String> entries = new ArrayList<>();
        sb.append("Criteria [");
        if (m_class != null) entries.add("class=" + m_class.toString());
        if (m_orders != null && !m_orders.isEmpty()) entries.add("orders=" + m_orders.toString());
        if (m_aliases != null && !m_aliases.isEmpty()) entries.add("aliases=" + m_aliases.toString());
        if (m_fetchTypes != null && !m_fetchTypes.isEmpty()) entries.add("fetchTypes=" + m_fetchTypes.toString());
        if (m_restrictions != null && !m_restrictions.isEmpty()) entries.add("restrictions=" + m_restrictions.toString());
        entries.add("distinct=" + m_distinct);
        if (m_limit != null) entries.add("limit=" + m_limit);
        if (m_offset != null) entries.add("offset=" + m_offset);
        for (final ListIterator<String> it = entries.listIterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
