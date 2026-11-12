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
package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.xml.event.Event;

public class ThresholdEntityTest {

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    private ThresholdEntity entityFor(String expression) throws Exception {
        Expression exp = new Expression();
        exp.setType(ThresholdType.HIGH);
        exp.setDsType("node");
        exp.setValue("10.0");
        exp.setRearm("9.0");
        exp.setTrigger("1");
        exp.setExpression(expression);
        ThresholdEntity entity = new ThresholdEntity(mock(EntityScopeProvider.class));
        entity.addThreshold(new ExpressionConfigWrapper(exp), MockSession.getSession());
        return entity;
    }

    @Test
    public void divideByZeroIsSkippedQuietly() throws Exception {
        ThresholdEntity entity = entityFor("errors / packets * 100");
        Map<String, Double> values = new HashMap<>();
        values.put("errors", 5.0);
        values.put("packets", 0.0);

        List<Event> events = entity.evaluateAndCreateEvents(values, new Date());

        assertTrue(events.isEmpty());
        MockLogAppender.assertNoWarningsOrGreater();

        // The evaluator still works once the denominator is non-zero
        values.put("packets", 10.0);
        events = entity.evaluateAndCreateEvents(values, new Date());
        assertEquals(1, events.size());
    }

    @Test
    public void otherEvaluationErrorsAreWarned() throws Exception {
        ThresholdEntity entity = entityFor("math.noSuchFunction(errors)");
        Map<String, Double> values = new HashMap<>();
        values.put("errors", 5.0);

        List<Event> events = entity.evaluateAndCreateEvents(values, new Date());

        assertTrue(events.isEmpty());
        MockLogAppender.assertLogAtLevel(Level.WARN);
    }
}
