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
package org.opennms.netmgt.alarmd.northbounder.syslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import com.google.common.collect.Lists;

/**
 * NMS-20382: confirms the SpEL sandbox preserves the rule constructs that shipped example configs and
 * real deployments use — property access, nested property, map indexing, the matches operator, boolean
 * logic, safe navigation, and instance-method calls WITH arguments. Only the code-execution vectors
 * (type refs / constructors / reflection) are removed; ordinary filter rules keep working.
 */
public class SyslogFilterBehaviorPreservedTest {

    private NorthboundAlarm alarm() {
        final OnmsEvent event = new OnmsEvent();
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "owner", "jdoe", "string")));
        final OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setLastEvent(event);
        onmsAlarm.setUei("uei.opennms.org/nodes/nodeDown");
        return new NorthboundAlarm(onmsAlarm);
    }

    private boolean evaluate(final String rule) {
        return new SyslogFilter("test", rule, "localhost").passFilter(alarm());
    }

    /** Property access, the matches operator, map indexing, and boolean logic — the shipped patterns. */
    @Test
    public void realWorldFilterRulesStillEvaluate() {
        assertTrue(evaluate("uei matches '^.*nodeDown$'"));
        assertTrue(evaluate("parameters['owner'] == 'jdoe'"));
        assertTrue(evaluate("parameters['owner'] == 'jdoe' and uei matches '^.*nodeDown$'"));
        assertFalse(evaluate("parameters['owner'] == 'someone-else'"));
        assertFalse(evaluate("uei matches '^.*interfaceDown$'"));
    }

    // --- instance method call WITH arguments + safe navigation, evaluated under the exact sandbox context ---

    public static class Root {
        public Holder getParm(final String key) {
            return "k".equals(key) ? new Holder("v") : null;
        }
    }

    public static class Holder {
        private final String value;
        Holder(final String value) { this.value = value; }
        public String getValue() { return value; }
    }

    @Test
    public void instanceMethodWithArgumentsAndSafeNavigationWork() {
        final SimpleEvaluationContext ctx = SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Root root = new Root();

        final Expression present = parser.parseExpression("getParm('k')?.value == 'v'");
        assertEquals(Boolean.TRUE, present.getValue(ctx, root, Boolean.class));

        // safe-navigation on a missing parm must not throw
        final Expression missing = parser.parseExpression("getParm('nope')?.value == 'v'");
        assertEquals(Boolean.FALSE, missing.getValue(ctx, root, Boolean.class));
    }
}
