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

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

public class ScenarioDriverIT {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
    }

    @Test
    public void canDriverSimpleScenario() {
        final AlarmListener listener = AlarmListener.getInstance();
        final Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                .withNodeDownEvent(1, 1)
                .withNodeUpEvent(2, 1)
                .awaitUntil(() -> await().atMost(30, SECONDS).until(listener::getNumUniqueObserveredAlarmIds, equalTo(2)))
                .build();
        final ScenarioResults results = scenario.play();
        // Simple assertion to validate that we do have some results - this test isn't concerned with what they are though
        assertThat(results, notNullValue());
        assertThat(listener.getNumUniqueObserveredAlarmIds(), equalTo(2));
    }

}
