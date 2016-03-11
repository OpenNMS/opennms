/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatusEntity;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsSeverity;

public class ApplicationDaoHibernate extends AbstractDaoHibernate<OnmsApplication, Integer> implements ApplicationDao {

	/**
	 * <p>Constructor for ApplicationDaoHibernate.</p>
	 */
	public ApplicationDaoHibernate() {
		super(OnmsApplication.class);
	}

	/** {@inheritDoc} */
        @Override
	public OnmsApplication findByName(final String name) {
		return findUnique("from OnmsApplication as app where app.name = ?", name);
	}

	@Override
	public List<ApplicationStatusEntity> getAlarmStatus() {
		final StringBuilder sql = new StringBuilder();
		sql.append("select distinct alarm.node.id, alarm.ipAddr, alarm.serviceType.id, min(alarm.lastEventTime), max(alarm.severity), (count(*) - count(alarm.alarmAckTime)) ");
		sql.append("from OnmsAlarm alarm ");
		sql.append("where alarm.severity > 3 and alarm.node.id != null and alarm.ipAddr != null and alarm.serviceType.id != null and alarm.alarmAckTime is null ");
		sql.append("group by alarm.node.id, alarm.ipAddr, alarm.serviceType.id");

		List<ApplicationStatusEntity> entityList = new ArrayList<>();
		List<Object[][]> objects = (List<Object[][]>) getHibernateTemplate().find(sql.toString());
		for (Object[] eachRow : objects) {
			ApplicationStatusEntity entity = new ApplicationStatusEntity((Integer)eachRow[0], (InetAddress)eachRow[1], (Integer) eachRow[2], (Date) eachRow[3], (OnmsSeverity) eachRow[4], (Long) eachRow[5]);
			entityList.add(entity);
		}
		return entityList;
	}
}
