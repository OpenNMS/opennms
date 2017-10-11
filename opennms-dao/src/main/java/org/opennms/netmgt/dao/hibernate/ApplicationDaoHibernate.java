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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatus;
import org.opennms.netmgt.dao.api.ApplicationStatusEntity;
import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.orm.hibernate3.HibernateCallback;

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
	public List<ApplicationStatus> getApplicationStatus() {
		return getApplicationStatus(findAll());
	}

	@Override
	public List<ApplicationStatus> getApplicationStatus(List<OnmsApplication> applications) {
		// Applications do not have a alarm mapping, so we grab all nodeDown, interfaceDown and serviceLost alarms
		// for all monitored services of each application to calculate the maximum severity (-> status)
		final List<ApplicationStatus> statusList = new ArrayList<>();
		for (OnmsApplication application : applications) {
			final Set<String> reductionKeys = new HashSet<>();
			for (OnmsMonitoredService eachService : application.getMonitoredServices()) {
				reductionKeys.addAll(ReductionKeyHelper.getReductionKeys(eachService));
			}

			if (!reductionKeys.isEmpty()) {
				final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
				builder.in("reductionKey", reductionKeys);

				// findMatching would exepct OnmsApplications, but we need OnmsAlarms, so hack it
				HibernateCallback<List<OnmsAlarm>> callback = buildHibernateCallback(builder.toCriteria());
				List<OnmsAlarm> alarms = getHibernateTemplate().execute(callback);

				// All alarms for the current application have been determined, now get the max severity
				final Optional<OnmsAlarm> maxSeverity = alarms.stream().reduce((leftAlarm, rightAlarm) -> {
					if (leftAlarm.getSeverity().isGreaterThan(rightAlarm.getSeverity())) {
						return leftAlarm;
					}
					return rightAlarm;
				});
				if (maxSeverity.isPresent()) {
					statusList.add(new ApplicationStatus(application, maxSeverity.get().getSeverity()));
				} else {
					// ensure that each application has a status
					statusList.add(new ApplicationStatus(application, OnmsSeverity.NORMAL));
				}
			} else {
				// ensure that each application has a status
				statusList.add(new ApplicationStatus(application, OnmsSeverity.NORMAL));
			}
		}
		return statusList;
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
