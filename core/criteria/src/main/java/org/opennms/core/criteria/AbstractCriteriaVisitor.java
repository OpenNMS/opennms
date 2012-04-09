package org.opennms.core.criteria;

import org.opennms.core.criteria.Criteria.CriteriaVisitor;
import org.opennms.core.criteria.restrictions.Restriction;

public class AbstractCriteriaVisitor implements CriteriaVisitor {

	@Override
	public void visitClass(final Class<?> clazz) {
	}

	@Override
	public void visitOrder(final Order order) {
	}

	@Override
	public void visitOrdersFinished() {
	}

	@Override
	public void visitAlias(final Alias alias) {
	}

	@Override
	public void visitAliasesFinished() {
	}

	@Override
	public void visitFetch(final Fetch fetch) {
	}

	@Override
	public void visitFetchesFinished() {
	}

	@Override
	public void visitRestriction(final Restriction restriction) {
	}

	@Override
	public void visitRestrictionsFinished() {
	}

	@Override
	public void visitDistinct(final boolean distinct) {
	}

	@Override
	public void visitLimit(final Integer limit) {
	}

	@Override
	public void visitOffset(final Integer offset) {
	}

}
