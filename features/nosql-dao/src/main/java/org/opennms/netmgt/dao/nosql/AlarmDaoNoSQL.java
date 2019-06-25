/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.nosql;

import java.util.Objects;
import java.util.UUID;

import org.opennms.netmgt.model.OnmsAlarm;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class AlarmDaoNoSQL extends AbstractDaoNoSQL<OnmsAlarm, UUID> {

    private final Session session;
    private PreparedStatement alarmInsertStmt;
    private PreparedStatement alarmSelectStmt;

    public AlarmDaoNoSQL(Session session) {
        super(session);
        this.session = Objects.requireNonNull(session);
        init();
    }

    public AlarmDaoNoSQL() {
        session = getSession();
        init();
    }

    public void init() {
        session.execute("USE opennms");
        alarmInsertStmt = session.prepare("INSERT INTO alarms (id, eventuei, nodeid, ipaddr, serviceid," +
                "reductionkey, type, counter, severity, lasteventid, firsteventtime, lasteventtime," +
                "firstautomationtime, lastautomationtime, description, logmsg, operinstruct, tticketid," +
                "tticketstate, mouseovertext, suppresseduntil, suppresseduser, suppressedtime, alarmackuser," +
                "alarmacktime, managedobjectinstance, managedobjecttype, applicationdn, ossprimarykey," +
                "x733alarmtype, x733probablecause, qosalarmstate, clearkey, ifindex, stickymemo, systemid)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        alarmSelectStmt = session.prepare("SELECT eventuei, nodeid, ipaddr, serviceid," +
                "reductionkey, type, counter, severity, lasteventid, firsteventtime, lasteventtime," +
                "firstautomationtime, lastautomationtime, description, logmsg, operinstruct, tticketid," +
                "tticketstate, mouseovertext, suppresseduntil, suppresseduser, suppressedtime, alarmackuser," +
                "alarmacktime, managedobjectinstance, managedobjecttype, applicationdn, ossprimarykey," +
                "x733alarmtype, x733probablecause, qosalarmstate, clearkey, ifindex, stickymemo, systemid" +
                "  FROM alarms WHERE id = ?");
    }

    @Override
    public UUID save(OnmsAlarm alarm) {
        // Generate a random UUID if none is set
        if (alarm.getUuid() == null) {
            alarm.setUuid(UUID.randomUUID());
        }

        session.execute(alarmInsertStmt.bind(alarm.getUuid(),
                alarm.getUei(),
                alarm.getNodeId(),
                alarm.getIpAddr(),
                alarm.getServiceType() != null ? alarm.getServiceType().getId() : null,
                alarm.getReductionKey(),
                alarm.getAlarmType(),
                alarm.getCounter(),
                alarm.getSeverityId(),
                alarm.getLastEvent() != null ? alarm.getLastEvent().getId() : null,
                alarm.getFirstEventTime(),
                alarm.getLastEventTime(),
                alarm.getFirstAutomationTime(),
                alarm.getLastAutomationTime(),
                alarm.getDescription(),
                alarm.getLogMsg(),
                alarm.getOperInstruct(),
                alarm.getTTicketId(),
                alarm.getTTicketState(),
                alarm.getMouseOverText(),
                alarm.getSuppressedUntil(),
                alarm.getSuppressedUser(),
                alarm.getSuppressedTime(),
                alarm.getAlarmAckUser(),
                alarm.getAlarmAckTime(),
                alarm.getManagedObjectInstance(),
                alarm.getManagedObjectType(),
                alarm.getApplicationDN(),
                alarm.getOssPrimaryKey(),
                alarm.getX733AlarmType(),
                alarm.getX733ProbableCause(),
                alarm.getQosAlarmState(),
                alarm.getClearKey(),
                alarm.getIfIndex(),
                alarm.getStickyMemo() != null ? alarm.getStickyMemo().getId() : null,
                alarm.getDistPoller() != null ? alarm.getDistPoller().getId() : null));
        return alarm.getUuid();
    }

    @Override
    public OnmsAlarm get(UUID id) {
        final ResultSet result = session.execute(alarmSelectStmt.bind(id));
        final Row row = result.one();

        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUuid(id);
        alarm.setUei(row.get("eventuei", String.class));
        final Integer nodeId = row.get("nodeid", Integer.class);
        alarm.setReductionKey(row.get("reductionkey", String.class));
        alarm.setAlarmType(row.get("type", Integer.class));
        return alarm;
    }

}
