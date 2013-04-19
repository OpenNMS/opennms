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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_uei" })
@XmlRootElement(name = "ueis")
public class Ueis {

	@XmlElement(name = "uei", required = true)
	protected List<Uei> m_uei;

	/**
	 * Gets the value of the uei property. Objects of the following type(s) are
	 * allowed in the list {@link Uei }
	 */
	public List<Uei> getUei() {
		if (m_uei == null) {
			m_uei = new ArrayList<Uei>();
		}
		return this.m_uei;
	}

	@Override
	public boolean equals(Object obj) {
		Ueis ueisOther = (Ueis) obj;
		if (obj instanceof Ueis)
			if (this.getUei().equals(ueisOther.getUei()))
				return true;
		return false;
	}

	@Override
	public String toString() {
		String ueis = "UEI List :: " + this.getUei().toString();
		return ueis;
	}
}
