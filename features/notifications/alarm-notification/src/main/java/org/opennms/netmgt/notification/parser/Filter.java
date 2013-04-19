/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 */

package org.opennms.netmgt.notification.parser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "filter")
public class Filter {

	@XmlAttribute(name = "devicefamily")
	protected String m_devicefamily;

	@XmlAttribute(name = "severity")
	protected String m_severity;

	/**
	 * Gets the value of the devicefamily property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getDevicefamily() {
		return m_devicefamily;
	}

	/**
	 * Sets the value of the devicefamily property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setDevicefamily(String value) {
		this.m_devicefamily = value;
	}

	/**
	 * Gets the value of the severity property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSeverity() {
		return m_severity;
	}

	/**
	 * Sets the value of the severity property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSeverity(String value) {
		this.m_severity = value;
	}

	@Override
	public boolean equals(Object obj) {
		Filter filterOther = (Filter) obj;
		if (obj instanceof Filter)
			if ((this.getDevicefamily().equals(filterOther.getDevicefamily()))
					&& (this.getSeverity().equals(filterOther.getSeverity())))
				return true;
		return false;
	}

	@Override
	public String toString() {
		String filter = "Device Family " + this.getDevicefamily().toString()
				+ " Severity " + this.getSeverity().toString();
		return filter;
	}
}
