/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>SequenceParameter class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"m_key", "m_value"})
public class SequenceParameter {
	@XmlAttribute(name="key")
	private String m_key;

	@XmlAttribute(name="value")
	private String m_value;
	
	/**
	 * <p>Constructor for SequenceParameter.</p>
	 */
	public SequenceParameter() {
	}
	
	/**
	 * <p>Constructor for SequenceParameter.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public SequenceParameter(String key, String value) {
		m_key = key;
		m_value = value;
	}
	
	/**
	 * <p>getKey</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getKey() {
		return m_key;
	}
	
	/**
	 * <p>getValue</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return m_value;
	}
}
