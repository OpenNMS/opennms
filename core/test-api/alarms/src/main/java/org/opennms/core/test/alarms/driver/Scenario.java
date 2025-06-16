/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.test.alarms.driver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.alarmd.AlarmPersisterImpl;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;

public class Scenario {

    private final List<Action> actions;
    
    private final boolean legacyAlarmBehavior;

    private final Runnable awaitUntilRunnable;

    public final long tickLengthMillis;

    public Scenario(ScenarioBuilder builder) {
        this.actions = new ArrayList<>(builder.actions);
        this.actions.sort(Comparator.comparing(Action::getTime));
        this.legacyAlarmBehavior = builder.legacyAlarmBehavior;
        this.awaitUntilRunnable = builder.awaitUntilRunnable;
        this.tickLengthMillis = builder.tickLengthMillis;
    }

    public List<Action> getActions() {
        return actions;
    }
    
    public boolean getLegacyAlarmBehavior() {
        return legacyAlarmBehavior;
    }

    public long getTickLengthMillis() {
        return tickLengthMillis;
    }

    public static ScenarioBuilder builder() {
        return new ScenarioBuilder();
    }

    public static class ScenarioBuilder {
        private final List<Action> actions = new ArrayList<>();
        
        private boolean legacyAlarmBehavior = false;

        private Runnable awaitUntilRunnable = () -> {};

        private long tickLengthMillis = 1;

        public ScenarioBuilder withTickLength(long duration, TimeUnit unit) {
            if (duration < 1) {
                throw new IllegalArgumentException("Duration must be strictly positive!");
            }
            this.tickLengthMillis = unit.toMillis(duration);
            return this;
        }

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

        // Create an event with lower severity
        public ScenarioBuilder withInterfaceDownEvent(long time, int nodeId) {
            EventBuilder builder = new EventBuilder(EventConstants.INTERFACE_DOWN_EVENT_UEI, "test");
            builder.setTime(new Date(time));
            builder.setNodeid(nodeId);
            builder.setSeverity(OnmsSeverity.MINOR.getLabel());

            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(String.format("%s:%d", EventConstants.INTERFACE_DOWN_EVENT_UEI, nodeId));
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

        public ScenarioBuilder withUnAcknowledgmentForNodeDownAlarm(long time, int nodeId) {
            actions.add(new UnAcknowledgeAlarmAction("test", new Date(time), String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId)));
            return this;
        }

        public ScenarioBuilder withAcknowledgmentForSituation(long time, String situtationId) {
            actions.add(new AcknowledgeAlarmAction("test", new Date(time), String.format("%s:%s", EventConstants.SITUATION_EVENT_UEI, situtationId)));
            return this;
        }

        public ScenarioBuilder withUnAcknowledgmentForSituation(long time, String situtationId) {
            actions.add(new UnAcknowledgeAlarmAction("test", new Date(time), String.format("%s:%s", EventConstants.SITUATION_EVENT_UEI, situtationId)));
            return this;
        }

        public ScenarioBuilder withSituationForNodeDownAlarms(long time, String situtationId, int... nodesIds) {
            EventBuilder builder = new EventBuilder(EventConstants.SITUATION_EVENT_UEI, "test");
            builder.setTime(new Date(time));
            builder.setSeverity(OnmsSeverity.NORMAL.getLabel());
            for (int k = 0; k < nodesIds.length; k++) {
                final String reductionKey = String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodesIds[k]);
                builder.addParam(AlarmPersisterImpl.RELATED_REDUCTION_KEY_PREFIX + k, reductionKey);
            }

            AlarmData data = new AlarmData();
            data.setAlarmType(3);
            data.setReductionKey(String.format("%s:%s", EventConstants.SITUATION_EVENT_UEI, situtationId));
            builder.setAlarmData(data);

            actions.add(new SendEventAction(builder.getEvent()));
            return this;
        }

        // create a situation using reduction keys
        public ScenarioBuilder withSituationForAlarmReductionKeys(long time, String situtationId, String... alarms) {
            EventBuilder builder = new EventBuilder(EventConstants.SITUATION_EVENT_UEI, "test");
            builder.setTime(new Date(time));
            builder.setSeverity(OnmsSeverity.NORMAL.getLabel());
            for (int k = 0; k < alarms.length; k++) {
                builder.addParam(AlarmPersisterImpl.RELATED_REDUCTION_KEY_PREFIX + k, alarms[k]);
            }

            AlarmData data = new AlarmData();
            data.setAlarmType(3);
            data.setReductionKey(String.format("%s:%s", EventConstants.SITUATION_EVENT_UEI, situtationId));
            builder.setAlarmData(data);

            actions.add(new SendEventAction(builder.getEvent()));
            return this;
        }
        
        public ScenarioBuilder withLegacyAlarmBehavior() {
            legacyAlarmBehavior = true;
            return this;
        }

        public ScenarioBuilder awaitUntil(Runnable runnable) {
            this.awaitUntilRunnable = Objects.requireNonNull(runnable);
            return this;
        }

        public Scenario build() {
            return new Scenario(this);
        }

    }

    public ScenarioResults play() {
        final JUnitScenarioDriver driver = new JUnitScenarioDriver();
        return driver.run(this);
    }

    public void awaitUntilComplete() {
        awaitUntilRunnable.run();
    }
}
