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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_scriptname", "m_errorhandling" })
@XmlRootElement(name = "script")
public class Script {

	@XmlElement(name = "scriptname", required = true)
	protected String m_scriptname;

	@XmlElement(name = "errorhandling", required = true)
	protected Errorhandling m_errorhandling;

	/**
	 * Gets the value of the scriptname property.
	 * 
	 * @return return object is {@link String }
	 */
	public String getScriptname() {
		return m_scriptname;
	}

	/**
	 * Sets the value of the scriptname property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setScriptname(String value) {
		this.m_scriptname = value;
	}

	/**
	 * Gets the value of the errorhandling property.
	 * 
	 * @return return object is {@link Errorhandling }
	 */
	public Errorhandling getErrorhandling() {
		return m_errorhandling;
	}

	/**
	 * Sets the value of the errorhandling property.
	 * 
	 * @param value
	 *            allowed object is {@link Errorhandling }
	 */
	public void setErrorhandling(Errorhandling value) {
		this.m_errorhandling = value;
	}

	@Override
	public boolean equals(Object obj) {
		Script scriptOther = (Script) obj;
		if (obj instanceof Script)
			if ((this.getScriptname().equals(scriptOther.getScriptname()))
			// &&
			// (this.getErrorhandling().equals(scriptOther.getErrorhandling()))
			)
				return true;

		return false;
	}

	@Override
	public String toString() {
		String script = "Script Name " + this.getScriptname()
				+ "Error Handling Details" + this.getErrorhandling().toString();
		return script;
	}
}
