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
 *******************************************************************************/
package org.opennms.netmgt.notification.parser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_retryIntervalInseconds",
		"m_numberOfRetries" })
@XmlRootElement(name = "errorhandling")
public class Errorhandling {

	@XmlElement(name = "retry_interval_inseconds")
	protected Integer m_retryIntervalInseconds;

	@XmlElement(name = "number_of_retries")
	protected Integer m_numberOfRetries;

	@XmlAttribute(name = "enable", required = true)
	protected boolean m_enable;

	/**
	 * Gets the value of the retryIntervalInseconds property.
	 * 
	 * @return return object is {@link Integer }
	 */
	public Integer getRetryIntervalInseconds() {
		return m_retryIntervalInseconds;
	}

	/**
	 * Sets the value of the retryIntervalInseconds property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 */
	public void setRetryIntervalInseconds(Integer value) {
		this.m_retryIntervalInseconds = value;
	}

	/**
	 * Gets the value of the numberOfRetries property.
	 * 
	 * @return return object is {@link Integer }
	 */
	public Integer getNumberOfRetries() {
		return m_numberOfRetries;
	}

	/**
	 * Sets the value of the numberOfRetries property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 */
	public void setNumberOfRetries(Integer value) {
		this.m_numberOfRetries = value;
	}

	/**
	 * Gets the value of the enable property.
	 */
	public boolean isEnable() {
		return m_enable;
	}

	/**
	 * Sets the value of the enable property.
	 */
	public void setEnable(boolean value) {
		this.m_enable = value;
	}

	@Override
	public boolean equals(Object obj) {
		Errorhandling errorhandlingOther = (Errorhandling) obj;
		if (obj instanceof Filter)
			if ((this.getNumberOfRetries().equals(errorhandlingOther
					.getNumberOfRetries()))
					&& (this.getRetryIntervalInseconds()
							.equals(errorhandlingOther
									.getRetryIntervalInseconds())))
				return true;
		return false;
	}

	@Override
	public String toString() {

		String errorHandling = "Number of Retries "
				+ this.getNumberOfRetries().toString()
				+ " Retry Interval In seconds"
				+ this.getRetryIntervalInseconds().toString();
		return errorHandling;
	}
}
