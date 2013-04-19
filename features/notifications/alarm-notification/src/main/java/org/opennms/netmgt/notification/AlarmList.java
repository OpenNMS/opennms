/**
 * /**************************************************************************
 * ***** This file is part of OpenNMS(R). Copyright (C) 2009-2011 The OpenNMS
 * Group, Inc. OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/ This class represents the *
 * list of alarms that will be forwarded to the script.
 */

package org.opennms.netmgt.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_nbiAlarm" })
@XmlRootElement(name = "alarms")
public class AlarmList {

	@XmlElement(required = true)
	protected List<NBIAlarm> m_nbiAlarm;

	/**
	 * Gets the value of the alarm property.
	 * 
	 * @return possible object is {@link Alarm }
	 */
	public List<NBIAlarm> getAlarm() {
		if (m_nbiAlarm == null) {
			m_nbiAlarm = new ArrayList<NBIAlarm>();
		}
		return this.m_nbiAlarm;
	}

	@Override
	public String toString() {
		List<NBIAlarm> nbiAlarmList = this.m_nbiAlarm;
		String nbiAlarms = null;
		Iterator<NBIAlarm> nbiIterator = nbiAlarmList.iterator();
		while (nbiIterator.hasNext()) {
			NBIAlarm nbiAlarm = nbiIterator.next();
			if (nbiAlarms == null)
				nbiAlarms = nbiAlarm.toString() + "";
			else
				nbiAlarms = "[" + nbiAlarms + "]," + nbiAlarm.toString();
		}
		return nbiAlarms;
	}
}
