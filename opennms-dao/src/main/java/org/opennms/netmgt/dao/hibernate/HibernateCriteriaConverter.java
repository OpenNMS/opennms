package org.opennms.netmgt.dao.hibernate;

import java.util.List;
import java.util.Map.Entry;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Criteria.FetchType;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.restrictions.OrRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.netmgt.dao.CriteriaConverter;

public class HibernateCriteriaConverter implements CriteriaConverter<DetachedCriteria> {
	private static final Restriction[] EMPTY_RESTRICTION_ARRAY = new Restriction[0];

	public org.hibernate.Criteria convert(final Criteria criteria, final Session session) {
		final org.hibernate.Criteria hibernateCriteria = convert(criteria).getExecutableCriteria(session);
		if (criteria.getOffset() != null) hibernateCriteria.setFirstResult(criteria.getOffset());
		if (criteria.getLimit()  != null) hibernateCriteria.setMaxResults(criteria.getLimit());
		return hibernateCriteria;
	}

	@Override
	public DetachedCriteria convert(final Criteria criteria) {
		// final CriteriaImpl hibernateCriteria = new CriteriaImpl(criteria.getCriteriaClass().getName(), null);
		DetachedCriteria hibernateCriteria = DetachedCriteria.forClass(criteria.getCriteriaClass());

		final List<Alias> joins = criteria.getAliases();
		addJoinsToCriteria(joins, hibernateCriteria);
		
		if (criteria.getMatchType().equals("any")) {
			final Restriction restriction = new OrRestriction(criteria.getRestrictions().toArray(EMPTY_RESTRICTION_ARRAY));
			hibernateCriteria.add(restriction.toCriterion());
		} else {
			for (final Restriction restriction : criteria.getRestrictions()) {
				hibernateCriteria.add(restriction.toCriterion());
			}
		}

		if (criteria.isDistinct()) {
			hibernateCriteria.setProjection(Projections.distinct(Projections.id()));
			
			final DetachedCriteria newCriteria = DetachedCriteria.forClass(criteria.getCriteriaClass());
			newCriteria.add(Subqueries.propertyIn("id", hibernateCriteria));

			// re-add these so they're available from the "outside" objects
			// is this really necessary?  skipping seems to work fine
			// addJoinsToCriteria(joins, newCriteria);

			hibernateCriteria = newCriteria;
		}
		
		for (final Entry<String,FetchType> fetchType : criteria.getFetchTypes().entrySet()) {
			switch(fetchType.getValue()) {
				case DEFAULT:
					hibernateCriteria.setFetchMode(fetchType.getKey(), FetchMode.DEFAULT);
				case EAGER:
					hibernateCriteria.setFetchMode(fetchType.getKey(), FetchMode.JOIN);
				case LAZY:
					hibernateCriteria.setFetchMode(fetchType.getKey(), FetchMode.SELECT);
				default:
					hibernateCriteria.setFetchMode(fetchType.getKey(), FetchMode.DEFAULT);
			}
		}
		
		return hibernateCriteria;
	}

	private void addJoinsToCriteria(final List<Alias> joins, final DetachedCriteria criteria) {
		for (final Alias join : joins) {
			int joinType;
			switch(join.getType()) {
				case FULL_JOIN: joinType = org.hibernate.Criteria.FULL_JOIN;
				case LEFT_JOIN: joinType = org.hibernate.Criteria.LEFT_JOIN;
				case INNER_JOIN: joinType = org.hibernate.Criteria.INNER_JOIN;
				default: joinType = org.hibernate.Criteria.INNER_JOIN;
			}
			criteria.createAlias(join.getAssociationPath(), join.getAlias(), joinType);
		}
	}

}
