package org.opennms.core.criteria.restrictions;

public class NotRestriction extends BaseRestriction {

	private final Restriction m_restriction;

	public NotRestriction(final Restriction restriction) {
		super(RestrictionType.NOT);
		m_restriction = restriction;
	}
	
	public Restriction getRestriction() {
		return m_restriction;
	}

	public void visit(final RestrictionVisitor visitor) {
		visitor.visitNot(this);
		getRestriction().visit(visitor);
		visitor.visitNotComplete(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_restriction == null) ? 0 : m_restriction.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof NotRestriction)) return false;
		final NotRestriction other = (NotRestriction) obj;
		if (m_restriction == null) {
			if (other.m_restriction != null) return false;
		} else if (!m_restriction.equals(other.m_restriction)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NotRestriction [restriction=" + m_restriction + "]";
	}
}
