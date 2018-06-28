/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.driver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;

public class Scenario {

    private final List<Action> actions;

    public Scenario(ScenarioBuilder builder) {
        this.actions = new ArrayList<>(builder.actions);
        this.actions.sort(Comparator.comparing(Action::getTime));
    }

    public List<Action> getActions() {
        return actions;
    }

    public static ScenarioBuilder builder() {
        return new ScenarioBuilder();
    }

    public static class ScenarioBuilder {
        private final List<Action> actions = new ArrayList<>();

        public ScenarioBuilder withNodeDownEvent(long time, int nodeId) {
            EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, "test");
            builder.setTime(new Date(time));
            builder.setNodeid(nodeId);
            builder.setSeverity(OnmsSeverity.MAJOR.getLabel());

            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId));
            builder.setAlarmData(data);

            builder.setLogDest("logndisplay");
            builder.setLogMessage("testing");
            actions.add(new SendEventAction(builder.getEvent()));
            return this;
        }

        public ScenarioBuilder withNodeUpEvent(long time, int nodeId) {
            EventBuilder builder = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test");
            builder.setTime(new Date(time));
            builder.setNodeid(nodeId);
            builder.setSeverity(OnmsSeverity.NORMAL.getLabel());

            AlarmData data = new AlarmData();
            data.setAlarmType(2);
            data.setReductionKey(String.format("%s:%d", EventConstants.NODE_UP_EVENT_UEI, nodeId));
            data.setClearKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId));
            builder.setAlarmData(data);

            builder.setLogDest("logndisplay");
            builder.setLogMessage("testing");
            actions.add(new SendEventAction(builder.getEvent()));
            return this;
        }

        public ScenarioBuilder withAcknowledgmentForNodeDownAlarm(long time, int nodeId) {
            actions.add(new AcknowledgeAlarmAction("test", new Date(time), String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId)));
            return this;
        }

        public Scenario build() {
            return new Scenario(this);
        }
    }

}
