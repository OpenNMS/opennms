package org.opennms.core.criteria;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Order implements Comparable<Order> {
	private final String m_attribute;
	private final boolean m_ascending;

	public Order(final String attribute, boolean ascending) {
		m_attribute = attribute;
		m_ascending = ascending;
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(m_attribute)
			.append(m_ascending)
			.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		Order that = (Order) obj;
		return new EqualsBuilder()
			.append(this.asc(), that.asc())
			.append(this.getAttribute(), that.getAttribute())
			.isEquals();
	}
	
	@Override
	public int compareTo(final Order that) {
		return new CompareToBuilder()
			.append(this.getAttribute(), that.getAttribute())
			.toComparison();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("attribute", m_attribute)
			.append("order", (m_ascending? "asc" : "desc"))
			.toString();
	}
}
