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
 * http://www.opennms.org/ http://www.opennms.com/ OnjectFactory to create
 * list of alarms
 */

package org.opennms.netmgt.notification;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class NBIAlarmObjectFactory {

	private static NBIAlarmObjectFactory s_nbiAlarmObjectFactory;

	private NBIAlarmObjectFactory() {

	}

	public static NBIAlarmObjectFactory getInstance() {
		if (s_nbiAlarmObjectFactory == null) {
			s_nbiAlarmObjectFactory = new NBIAlarmObjectFactory();
		}
		return s_nbiAlarmObjectFactory;
	}

	/**
	 * Create an instance of {@link Alarms }
	 */
	public AlarmList createAlarmList() {
		return new AlarmList();
	}

	/**
	 * Create an instance of {@link Alarm }
	 */
	public NBIAlarm createAlarm() {
		return new NBIAlarm();
	}

	/**
	 * Create an instance of {@link Alarms }
	 */
	public AlarmList createAlarmList(NBIAlarm nbiAlarm, AlarmList alarmList) {
		if (alarmList == null)
			alarmList = new AlarmList();
		alarmList.getAlarm().add(nbiAlarm);
		return alarmList;
	}
}
