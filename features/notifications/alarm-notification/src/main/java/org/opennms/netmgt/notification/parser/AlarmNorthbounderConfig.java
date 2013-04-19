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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.NorthbounderException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "alarm-northbounder-config")
public class AlarmNorthbounderConfig implements Serializable,
		Comparable<AlarmNorthbounderConfig> {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "notification", required = true)
	protected List<Notification> m_notification;

	public List<Notification> getNotification() {
		if (m_notification == null) {
			m_notification = new ArrayList<Notification>();
		}
		return this.m_notification;
	}

	@Override
	public boolean equals(Object obj) {
		boolean eq = false;
		if (obj instanceof AlarmNorthbounderConfig) {
			AlarmNorthbounderConfig other = (AlarmNorthbounderConfig) obj;
			try {
				eq = this.getNotification().equals(other.getNotification());
			} catch (NorthbounderException e) {
				eq = false;
			}
		}
		return eq;
	}

	@Override
	public int compareTo(AlarmNorthbounderConfig anotherObject) {
		this.getNotification().toString()
				.compareTo(anotherObject.getNotification().toString());
		return 0;
	}

	@Override
	public String toString() {
		List<Notification> notifList = this.getNotification();
		return notifList.toString();
	}
}
