/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.util.List;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AlarmDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AlarmDaoHibernate extends AbstractDaoHibernate<OnmsAlarm, Integer> implements AlarmDao {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmDaoHibernate.class);
	
	/**
	 * <p>Constructor for AlarmDaoHibernate.</p>
	 */
	public AlarmDaoHibernate() {
		super(OnmsAlarm.class);
	}

    /** {@inheritDoc} */
        @Override
    public OnmsAlarm findByReductionKey(String reductionKey) {
        String hql = "from OnmsAlarm as alarms where alarms.reductionKey = ?";
        return super.findUnique(hql, reductionKey);
    }

    /** {@inheritDoc} */
    public int deleteAlarmById(Integer alarmId) {
        try{
            String hql = "delete from OnmsAlarm where alarmid = ?";
            Object[] values = {alarmId};
            return bulkDelete(hql, values);
        } catch (final Exception e) {
            LOG.warn("Unable to delete an alarm with Id {}", alarmId, e);
        }
        return 0;
    }

    @Override
    public List<AlarmSummary> getNodeAlarmSummaries(Integer... nodeIds) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT new org.opennms.netmgt.model.alarm.AlarmSummary(node.id, node.label, min(alarm.lastEventTime), max(alarm.severity), count(*)) ");
        sql.append("FROM OnmsAlarm AS alarm ");
        sql.append ("LEFT JOIN alarm.node AS node ");
        sql.append("WHERE node.id IS NOT NULL AND alarm.alarmAckTime IS NULL AND alarm.severity > 3 ");

        // optional
        if (nodeIds != null && nodeIds.length > 0) {
            if (nodeIds.length == 1) {
                sql.append("AND node.id = " + nodeIds[0] + " ");
            } else {
                sql.append("AND node.id in (");
                for (int i=0; i<nodeIds.length; i++) {
                    sql.append(nodeIds[i]);
                    if (i < nodeIds.length -1) sql.append(",");
                }
                sql.append(") ");
            }
        }
        sql.append("GROUP BY node.id, node.label ");
        sql.append("ORDER BY min(alarm.lastEventTime) DESC, node.label ASC");
        return findObjects(AlarmSummary.class,sql.toString());
    }
}
