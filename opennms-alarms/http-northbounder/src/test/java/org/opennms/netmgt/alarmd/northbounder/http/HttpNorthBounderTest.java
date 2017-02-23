/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.northbounder.http.HttpNorthbounderConfig.HttpMethod;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the HTTP North Bound Interface
 * FIXME: This is far from completed.
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/test-context.xml")
public class HttpNorthBounderTest {

    /**
     * Test forward alarms.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    @JUnitHttpServer(port=10342)
    public void testForwardAlarms() throws InterruptedException {
        
        HttpNorthbounder nb = new HttpNorthbounder();
        HttpNorthbounderConfig config = new HttpNorthbounderConfig("localhost");
        config.setMethod(HttpMethod.POST);
        config.setPath("/jms/post");
        config.setPort(Integer.valueOf(10342));
        
        nb.setConfig(config);
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(1);
        alarm.setUei("uei.opennms.org/test/httpNorthBounder");
        
        NorthboundAlarm a = new NorthboundAlarm(alarm);
        nb.onAlarm(a);
    }

}
