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
 * http://www.opennms.org/ http://www.opennms.com/ This allows to construct
 * new instances of the Java representation for the xml alarmNotificationConf.
 */

package org.opennms.netmgt.notification.parser;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class AlarmNotificationConfObjectFactory {

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package:
	 * org.opennms.netmgt.notification.parser
	 */
	public AlarmNotificationConfObjectFactory() {
	}

	/**
	 * Create an instance of {@link Filter }
	 */
	public Filter createFilter() {
		return new Filter();
	}

	/**
	 * Create an instance of {@link Uei }
	 */
	public Uei createUei() {
		return new Uei();
	}

	/**
	 * Create an instance of {@link Script }
	 */
	public Script createScript() {
		return new Script();
	}

	/**
	 * Create an instance of {@link Ueis }
	 */
	public Ueis createUeis() {
		return new Ueis();
	}

	/**
	 * Create an instance of {@link Errorhandling }
	 */
	public Errorhandling createErrorhandling() {
		return new Errorhandling();
	}

	/**
	 * Create an instance of {@link Notification }
	 */
	public Notification createNotification() {
		return new Notification();
	}

	/**
	 * Create an instance of {@link AlarmNorthbounderConfig }
	 */
	public AlarmNorthbounderConfig createAlarmNorthbounderConfig() {
		return new AlarmNorthbounderConfig();
	}

}
