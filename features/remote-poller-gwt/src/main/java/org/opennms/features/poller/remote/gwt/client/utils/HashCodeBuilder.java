/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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
