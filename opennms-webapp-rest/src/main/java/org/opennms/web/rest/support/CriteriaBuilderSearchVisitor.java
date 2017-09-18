/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.cxf.jaxrs.ext.search.PrimitiveStatement;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchConditionVisitor;
import org.apache.cxf.jaxrs.ext.search.SearchUtils;
import org.apache.cxf.jaxrs.ext.search.visitor.AbstractSearchConditionVisitor;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link SearchConditionVisitor} will convert CXF {@link SearchCondition}
 * instances into a {@link Criteria} that is suitable for querying one of our
 * DAO interfaces.
 * 
 * @param <T> Entity object type (eg. OnmsNode)
 * @param <Q> Query bean type. In some cases, this will be the same as the entity
 *   object type but for complex objects, a specific query bean or the CXF {@link SearchBean}
 *   may be used instead.
 */
public class CriteriaBuilderSearchVisitor<T,Q> extends AbstractSearchConditionVisitor<Q, CriteriaBuilder> {

	private static final Logger LOG = LoggerFactory.getLogger(CriteriaBuilderSearchVisitor.class);

	/**
	 * Use this value to represent null values. This will translate into a 
	 * {@link CriteriaBuilder#isNull(String)} or {@link CriteriaBuilder#isNotNull(String)}
	 * call (depending on the operator).
	 */
	public static final String NULL_VALUE = "\u0000";

	/**
	 * Use this value to represent null {@link Date} values. This will translate into a 
	 * {@link CriteriaBuilder#isNull(String)} or {@link CriteriaBuilder#isNotNull(String)}
	 * call (depending on the operator).
	 */
	public static final Date NULL_DATE_VALUE = new Date(0);

	/**
	 * Class that this {@link CriteriaBuilder} will generate {@link Criteria} for.
	 */
	private final Class<T> m_class;

	/**
	 * {@link CriteriaBuilder} that will be generated.
	 */
	private final CriteriaBuilder m_criteriaBuilder;

	private final Map<String,CriteriaBehavior<?>> m_criteriaBehaviors;

	/**
	 * Constructor that just specifies the target class.
	 */
	public CriteriaBuilderSearchVisitor(CriteriaBuilder criteriaBuilder, Class<T> clazz) {
		this(criteriaBuilder, clazz, null);
	}

	/**
	 * Constructor that specifies the target class and a list of field aliases.
	 */
	public CriteriaBuilderSearchVisitor(CriteriaBuilder criteriaBuilder, Class<T> clazz, Map<String,CriteriaBehavior<?>> behaviors) {
		super(null);
		m_class = clazz;
		m_criteriaBuilder = criteriaBuilder;
		m_criteriaBehaviors = behaviors;
		setWildcardStringMatch(true);
	}

	@Override
	public void visit(SearchCondition<Q> sc) {
		PrimitiveStatement statement = sc.getStatement();
		if (statement != null) {
			if (statement.getProperty() != null) {
				String name = getRealPropertyName(statement.getProperty());

				// TODO: Figure out how to use validators at some point
				//validatePropertyValue(name, originalValue);

				// Introspect the property type
				ClassValue clsValue = getPrimitiveFieldClass(statement, name, statement.getValue().getClass(), statement.getValueType(), statement.getValue());

				// If the property value is a String
				boolean isWildcard = false;
				if (String.class.equals(clsValue.getCls())) {
					// And if it's a FIQL wildcard
					if (SearchUtils.containsWildcard((String)clsValue.getValue())) {
						// Then mark it as a wildcard and replace the * wildcards with % wildcards
						isWildcard = true;
						clsValue.setValue(SearchUtils.toSqlWildcardString((String)clsValue.getValue(), false));
					}
				}

				final Object value;

				// Check to see if we have any criteria behaviors for this search term
				if (m_criteriaBehaviors != null && m_criteriaBehaviors.containsKey(name)) {
					// TODO: Change CriteriaBehaviors so that they can remap prefixes 
					// so that we don't have to put every joined property into m_criteriaMapping
					CriteriaBehavior<?> behavior = m_criteriaBehaviors.get(name);

					// Convert the query bean property name to the Criteria property name
					// if necessary
					name = behavior.getPropertyName() == null ? name : behavior.getPropertyName();

					// If we're using CriteriaBehaviors, assume that the value is a String
					// and convert it to the value that will be used in the Criteria
					value = NULL_VALUE.equals((String)clsValue.getValue()) ? null : behavior.convert((String)clsValue.getValue());

					// Execute any beforeVisit() actions for this query term such as adding
					// additional JOIN aliases
					behavior.beforeVisit(m_criteriaBuilder, value, sc.getConditionType(), isWildcard);

					// If the behavior indicates that we should skip this search term, then return
					if (behavior.shouldSkipProperty(sc.getConditionType(), isWildcard)) {
						return;
					}
				} else {
					value = clsValue.getValue();
				}

				// TODO: Should we get the condition off of the statement instead??
				// I think they're always identical if the PrimitiveStatement has a
				// statement.
				//switch(statement.getCondition()) {

				switch(sc.getConditionType()) {
				case EQUALS:
					if (isWildcard) {
						m_criteriaBuilder.like(name, value);
					} else {
						if (
							value == null || 
							NULL_VALUE.equals(value) ||
							NULL_DATE_VALUE.equals(value)
						) {
							m_criteriaBuilder.isNull(name);
						} else {
							m_criteriaBuilder.eq(name, value);
						}
					}
					break;
				case NOT_EQUALS:
					if (isWildcard) {
						m_criteriaBuilder.not().like(name, value);
					} else {
						if (
							value == null || 
							NULL_VALUE.equals(value) ||
							NULL_DATE_VALUE.equals(value)
						) {
							m_criteriaBuilder.isNotNull(name);
						} else {
							// Match any rows that do not match the value or are null
							m_criteriaBuilder.or(
								Restrictions.ne(name, value),
								Restrictions.isNull(name)
							);
						}
					}
					break;
				case LESS_THAN:
					// TODO: Check for null?
					m_criteriaBuilder.lt(name, value);
					break;
				case GREATER_THAN:
					// TODO: Check for null?
					m_criteriaBuilder.gt(name, value);
					break;
				case LESS_OR_EQUALS:
					// TODO: Check for null?
					m_criteriaBuilder.le(name, value);
					break;
				case GREATER_OR_EQUALS:
					// TODO: Check for null?
					m_criteriaBuilder.ge(name, value);
					break;
				case OR:
				case AND:
				case CUSTOM:
				default:
					// TODO: What do we do here? Probably nothing, the SQL visitor skips it.
				}
			}
		} else {
			List<Restriction> subRestrictions = new ArrayList<>();
			for (SearchCondition<Q> condition : sc.getSearchConditions()) {
				// Create a new CriteriaBuilder
				CriteriaBuilder builder = null;
				try {
					// Try to use the same class as the outside CriteriaBuilder
					builder = m_criteriaBuilder.getClass().getConstructor(Class.class).newInstance(m_class);
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
					LOG.warn("Could not create " + m_criteriaBuilder.getClass().getSimpleName() + "; falling back to CriteriaBuilder: " + e.getClass().getSimpleName() + ": " + e.getMessage());
					builder = new CriteriaBuilder(m_class);
				}
				// Create a new visitor for the SearchCondition
				CriteriaBuilderSearchVisitor<T,Q> newVisitor = new CriteriaBuilderSearchVisitor<T,Q>(builder, m_class, m_criteriaBehaviors);

				// Visit the children
				condition.accept(newVisitor);

				Criteria newCriteria = newVisitor.getQuery().toCriteria();

				// Add any aliases from the subcriteria
				Collection<Alias> aliases = newCriteria.getAliases();
				if (aliases != null) {
					for (Alias alias : aliases) {
						m_criteriaBuilder.alias(alias);
					}
				}

				// Fetch the rendered restrictions
				Collection<Restriction> restrictions = newCriteria.getRestrictions();
				// If there are restrictions...
				if (restrictions != null && restrictions.size() > 0) {
					final Restriction subRestriction;
					// If there are multiple restrictions...
					if (restrictions.size() > 1) {
						// Wrap them in an AND restriction
						subRestriction = Restrictions.all(restrictions);
					} else {
						subRestriction = restrictions.iterator().next();
					}
					LOG.info(subRestriction.toString());
					subRestrictions.add(subRestriction);
				}
			}

			switch(sc.getConditionType()) {
			case OR:
				LOG.info("OR criteria");
				// .or() with current Criteria
				m_criteriaBuilder.or(subRestrictions.toArray(new Restriction[0]));
				break;
			case AND:
				LOG.info("AND criteria");
				// .and() with current Criteria
				m_criteriaBuilder.and(subRestrictions.toArray(new Restriction[0]));
				break;
			default:
				// TODO: What do we do here?
			}
		}
	}

	@Override
	public CriteriaBuilder getQuery() {
		return m_criteriaBuilder;
	}

}
