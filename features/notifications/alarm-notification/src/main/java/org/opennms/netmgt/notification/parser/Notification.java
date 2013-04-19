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
@XmlType(propOrder = { "m_script", "m_ueis" })
@XmlRootElement(name = "notification")
public class Notification {

	@XmlElement(name = "script", required = true)
	protected Script m_script;

	@XmlElement(name = "ueis", required = true)
	protected Ueis m_ueis;

	@XmlAttribute(name = "enable", required = true)
	protected boolean m_enable;

	@XmlAttribute(name = "name", required = true)
	protected String m_name;

	/**
	 * Gets the value of the script property.
	 * 
	 * @return return object is {@link Script }
	 */
	public Script getScript() {
		return m_script;
	}

	/**
	 * Sets the value of the script property.
	 * 
	 * @param value
	 *            allowed object is {@link Script }
	 */
	public void setScript(Script value) {
		this.m_script = value;
	}

	/**
	 * Gets the value of the ueis property.
	 * 
	 * @return return object is {@link Ueis }
	 */
	public Ueis getUeis() {
		return m_ueis;
	}

	/**
	 * Sets the value of the ueis property.
	 * 
	 * @param value
	 *            allowed object is {@link Ueis }
	 */
	public void setUeis(Ueis value) {
		this.m_ueis = value;
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

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setName(String value) {
		this.m_name = value;
	}

	@Override
	public boolean equals(Object obj) {

		Notification otherNotification = (Notification) obj;
		if (obj instanceof Notification)
			if (this.getName().equals(otherNotification.getName()))
				return true;
		return false;
	}

	@Override
	public String toString() {
		String notifications = "Script Details: " + this.getScript().toString()
				+ " Notification Name: " + this.getName() + "List of UEIs: "
				+ this.getUeis().toString();
		return notifications;
	}
}
