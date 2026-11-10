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
package org.opennms.features.amqp.alarmnorthbounder;

import java.util.List;
import java.util.Properties;

import org.apache.camel.BeanInject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

/**
 * Simple test that verifies the Blueprint syntax.
 *
 * @author jwhite
 */
@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
public class AMQPAlarmNorthbounderBlueprintTest extends CamelBlueprintTest {

    @BeanInject
    protected AlarmNorthbounder alarmNorthbounder;

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-alarm-northbounder.xml";
    }

    @Override
    protected String setConfigAdminInitialConfiguration(Properties props) {
        props.put("destination", "mock:destination");

        // Return the PID
        return "org.opennms.features.amqp.alarmnorthbounder";
    }

    @Test
    public void canForwardAlarm() throws Exception {
        getMockEndpoint("mock:destination").expectedMessageCount(1);

        // Forward a single alarm
        OnmsAlarm alarm = new OnmsAlarm();
        NorthboundAlarm northboundAlarm = new NorthboundAlarm(alarm);
        List<NorthboundAlarm> northboundAlarms = Lists.newArrayList(northboundAlarm);
        alarmNorthbounder.forwardAlarms(northboundAlarms);

        assertMockEndpointsSatisfied();
    }
}
