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
package org.opennms.features.kafka.producer.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Command(scope = "opennms", name = "kafka-evaluate-filter", description = "Compiles the given expression and optionally test it against an object.")
@Service
public class EvaluateFilter implements Action {
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    @Reference
    private AlarmDao alarmDao;

    @Reference
    private SessionUtils sessionUtils;

    @Option(name = "-a", aliases = "--alarm-id", description = "Lookup an alarm by id and apply the given expression against it.")
    private Integer alarmId;

    @Option(name = "-e", aliases = "--event-uei", description = "Create a new event with the given UEI and apply the given expression against it.")
    private String eventUei;

    @Argument(description = "An SPEL expression.")
    private String spelExpression;

    @Override
    public Object execute() {
        final Expression expression = SPEL_PARSER.parseExpression(spelExpression);
        System.out.printf("SPEL Expression: %s\n", expression.getExpressionString());
        if (alarmId != null) {
            sessionUtils.withReadOnlyTransaction(() -> {
                if (alarmId != null) {
                    final OnmsAlarm alarm = alarmDao.get(alarmId);
                    if (alarm == null) {
                        System.out.printf("No alarm found with ID: %d\n", alarmId);
                    } else {
                        System.out.printf("Alarm with ID %d has reduction key: %s\n", alarmId, alarm.getReductionKey());
                    }
                    System.out.printf("Result: %s\n", expression.getValue(alarm, Boolean.class));
                }
                return null;
            });
        }
        if (eventUei != null) {
            final Event event = new EventBuilder(eventUei, "kafka-producer:evaluate-filter")
                    .getEvent();
            System.out.printf("Event has UEI: %s\n", event.getUei());
            System.out.printf("Result: %s\n", expression.getValue(event, Boolean.class));
        }
        return null;
    }
}
