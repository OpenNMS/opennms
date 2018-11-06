/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.amqp.alarmnorthbounder;

import java.util.List;
import java.util.Properties;

import org.apache.camel.BeanInject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.test.junit.FlappingTests;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;

import com.google.common.collect.Lists;

/**
 * Simple test that verifies the Blueprint syntax.
 *
 * @author jwhite
 */
@Category(FlappingTests.class)
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
