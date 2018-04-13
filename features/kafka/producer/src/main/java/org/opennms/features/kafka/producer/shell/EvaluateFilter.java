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

package org.opennms.features.kafka.producer.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

@Command(scope = "kafka-producer", name = "evaluate-filter", description = "Compiles the given expression and optionally test it against an object.")
@Service
public class EvaluateFilter implements Action {
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    @Reference
    private AlarmDao alarmDao;

    @Reference
    private TransactionOperations transactionOperations;

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
            transactionOperations.execute((TransactionCallback<Void>) status -> {
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
