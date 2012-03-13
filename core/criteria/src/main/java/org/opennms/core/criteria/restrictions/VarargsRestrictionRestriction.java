package org.opennms.core.criteria.restrictions;

import java.util.ArrayList;
import java.util.List;

public abstract class VarargsRestrictionRestriction extends BaseRestriction {

	private List<Restriction> m_restrictions = new ArrayList<Restriction>();

	public VarargsRestrictionRestriction(final RestrictionType type, final Restriction... restrictions) {
		super(type);
		for (final Restriction r : restrictions) {
			m_restrictions.add(r);
		}
	}

	public List<Restriction> getRestrictions() {
		return m_restrictions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + m_restrictions.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof VarargsRestrictionRestriction)) return false;
		final VarargsRestrictionRestriction other = (VarargsRestrictionRestriction) obj;
		if (!m_restrictions.equals(other.m_restrictions)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "VarargsRestrictionRestriction [type=" + getType() + ", restrictions=" + m_restrictions + "]";
	}

}
