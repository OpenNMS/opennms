package org.opennms.features.poller.remote.gwt.client.utils;


/**
 * <p>HashCodeBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class HashCodeBuilder {
	private int m_constant = 0;
	private int m_total = 0;

	/**
	 * <p>Constructor for HashCodeBuilder.</p>
	 */
	public HashCodeBuilder() {
		m_total = 15;
		m_constant = 41;
	}

	/**
	 * <p>Constructor for HashCodeBuilder.</p>
	 *
	 * @param initialNumber a int.
	 * @param multiplier a int.
	 */
	public HashCodeBuilder(final int initialNumber, final int multiplier) {
		m_total = initialNumber;
		m_constant = multiplier;
	}

	/**
	 * <p>append</p>
	 *
	 * @param o a {@link java.lang.Object} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder} object.
	 */
	public HashCodeBuilder append(Object o) {
		if (o == null) {
			m_total = m_total * m_constant;
		} else {
			m_total = m_total * m_constant + o.hashCode();
		}
		return this;
	}

	/**
	 * <p>toHashcode</p>
	 *
	 * @return a int.
	 */
	public int toHashcode() {
		return m_total;
	}
}
