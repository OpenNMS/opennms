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
package org.opennms.features.apilayer.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opennms.integration.api.v1.config.events.AlarmData;
import org.opennms.integration.api.v1.config.events.AlarmType;
import org.opennms.integration.api.v1.config.events.CollectionGroup;
import org.opennms.integration.api.v1.config.events.EventConfExtension;
import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.integration.api.v1.config.events.LogMessage;
import org.opennms.integration.api.v1.config.events.LogMsgDestType;
import org.opennms.integration.api.v1.config.events.ManagedObject;
import org.opennms.integration.api.v1.config.events.Mask;
import org.opennms.integration.api.v1.config.events.MaskElement;
import org.opennms.integration.api.v1.config.events.Parameter;
import org.opennms.integration.api.v1.config.events.UpdateField;
import org.opennms.integration.api.v1.config.events.Varbind;
import org.opennms.integration.api.v1.model.Severity;

public class VoluminousEventConfExtension implements EventConfExtension {
    private List<EventDefinition> eventDefs = new ArrayList<>();

    public VoluminousEventConfExtension(String ueiBase, int numEvents) {
        populateEventDefs(ueiBase, numEvents);
    }

    public void populateEventDefs(String ueiBase, int eventCount) {
        for (int i = 0; i < eventCount; i++) {
            final int idx = i;
            final LogMessage logmsg = new LogMessage() {
                @Override
                public String getContent() {
                    return String.format("This is voluminous event %d of %d", idx, eventCount);
                }

                @Override
                public LogMsgDestType getDestination() {
                    return LogMsgDestType.LOGNDISPLAY;
                }
            };
            final List<MaskElement> maskElems = new LinkedList<>();
            maskElems.add(new MaskElement() {
                @Override
                public String getName() {
                    return "id";
                }
                @Override
                public List<String> getValues() {
                    List<String> vals = new ArrayList<>();
                    vals.add(String.format(".1.3.6.1.4.1.5813.%d", idx));
                    return vals;
                }
            });
            final Mask mask = new Mask() {
                @Override
                public List<MaskElement> getMaskElements() {
                    return maskElems;
                }
                @Override
                public List<Varbind> getVarbinds() {
                    return new ArrayList<>();
                }
            };
            final AlarmData alarmData = new AlarmData() {
                @Override
                public String getReductionKey() {
                    return "%uei%:%dpname%:%nodeid%";
                }
                @Override
                public AlarmType getType() {
                    return AlarmType.PROBLEM_WITHOUT_RESOLUTION;
                }
                @Override
                public String getClearKey() {
                    return null;
                }
                @Override
                public boolean isAutoClean() {
                    return false;
                }
                @Override
                public List<UpdateField> getUpdateFields() {
                    return Collections.emptyList();
                }
                @Override
                public ManagedObject getManagedObject() {
                    return null;
                }
            };
            final List<Parameter> params = new ArrayList<>();
            params.add(new Parameter() {
                @Override
                public String getName() {
                    return "eventNumber";
                }
                @Override
                public String getValue() {
                    return String.format("%d", idx);
                }
                @Override
                public boolean shouldExpand() {
                    return false;
                }
            });
            final EventDefinition eventDef = new EventDefinition() {
                @Override
                public int getPriority() {
                    return 1000;
                }
                @Override
                public String getUei() {
                    return String.format("%s%d", ueiBase, idx);
                }
                @Override
                public String getLabel() {
                    return String.format("Test event: VoluminousEvents #%d", idx);
                }
                @Override
                public Severity getSeverity() {
                    return (idx % 2 == 0) ? Severity.WARNING : Severity.CRITICAL;
                }
                @Override
                public String getDescription() {
                    return String.format("Test event #%d for voluminous testing", idx);
                }
                @Override
                public String getOperatorInstructions() {
                    return String.format("Operators should do stuff when they see event number %d. Unless they don't feel like it today.", idx);
                }
                @Override
                public LogMessage getLogMessage() {
                    return logmsg;
                }
                @Override
                public AlarmData getAlarmData() {
                    return alarmData;
                }
                @Override
                public Mask getMask() {
                    return mask;
                }
                @Override
                public List<Parameter> getParameters() {
                    return params;
                }
                @Override
                public List<CollectionGroup> getCollectionGroup() {
                    return new ArrayList<>();
                }
            };
            eventDefs.add(eventDef);
        }
    }
    @Override
    public List<EventDefinition> getEventDefinitions() {
        System.out.println("Returning " + eventDefs.size() + " event definitions");
        return eventDefs;
    }
}
