/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.status.api.node.strategy;

import java.util.Date;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsSeverity;

public class TestUtils {
    static OnmsAlarm createAlarm(OnmsNode node, OnmsSeverity severity, OnmsDistPoller distpoller) {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
        alarm.setDistPoller(distpoller);
        alarm.setCounter(1);
        alarm.setSeverity(severity);
        alarm.setNode(node);
        return alarm;
    }

    static OnmsOutage createOutage(OnmsMonitoredService service, OnmsEvent svcLostEvent) {
        OnmsOutage outage = new OnmsOutage();
        outage.setMonitoredService(service);
        outage.setIfLostService(new Date());
        outage.setServiceLostEvent(svcLostEvent);
        return outage;
    }

    static OnmsEvent createEvent(OnmsNode node, OnmsSeverity severity, OnmsDistPoller distPoller) {
        OnmsEvent event = new OnmsEvent();
        event.setEventUei(EventConstants.NODE_DOWN_EVENT_UEI);
        event.setEventTime(new Date());
        event.setEventCreateTime(new Date());
        event.setEventSource(TestUtils.class.getName());
        event.setDistPoller(distPoller);
        event.setEventSeverity(severity.getId());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogMsg("Dummy Log Message");
        event.setNode(node);
        return event;
    }
}
