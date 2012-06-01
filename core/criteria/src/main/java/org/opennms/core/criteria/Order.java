package org.opennms.core.criteria;

public class Order {
	public static interface OrderVisitor {
		public void visitAttribute(final String attribute);
		public void visitAscending(final boolean ascending);
	}
	
	private final String m_attribute;
	private final boolean m_ascending;

	public Order(final String attribute, boolean ascending) {
		m_attribute = attribute;
		m_ascending = ascending;
	}

	public void visit(final OrderVisitor visitor) {
		visitor.visitAttribute(getAttribute());
		visitor.visitAscending(asc());
	}
	
	public String getAttribute() {
		return m_attribute;
	}

	public boolean asc() {
		return m_ascending;
	}
	
	public boolean desc() {
		return !m_ascending;
	}

	public static Order asc(final String attribute) {
		return new Order(attribute, true);
	}

	public static Order desc(final String attribute) {
		return new Order(attribute, false);
	}

	/* we don't include m_ascending since a single order attribute should only be used once */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// result = prime * result + (m_ascending ? 1231 : 1237);
		result = prime * result + ((m_attribute == null) ? 0 : m_attribute.hashCode());
		return result;
	}

	/* we don't include m_ascending since a single order attribute should only be used once */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Order)) return false;
		final Order other = (Order) obj;
		// if (m_ascending != other.m_ascending) return false;
		if (m_attribute == null) {
			if (other.m_attribute != null) return false;
		} else if (!m_attribute.equals(other.m_attribute)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Order [attribute=" + m_attribute + ", ascending=" + m_ascending + "]";
	}

}
