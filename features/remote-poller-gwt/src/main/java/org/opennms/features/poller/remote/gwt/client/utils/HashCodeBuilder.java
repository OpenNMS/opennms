package org.opennms.features.poller.remote.gwt.client.utils;


public class HashCodeBuilder {
	private int m_constant = 0;
	private int m_total = 0;

	public HashCodeBuilder() {
		m_total = 15;
		m_constant = 41;
	}

	public HashCodeBuilder(final int initialNumber, final int multiplier) {
		m_total = initialNumber;
		m_constant = multiplier;
	}

	public HashCodeBuilder append(Object o) {
		if (o == null) {
			m_total = m_total * m_constant;
		} else {
			m_total = m_total * m_constant + o.hashCode();
		}
		return this;
	}

	public int toHashcode() {
		return m_total;
	}
}
