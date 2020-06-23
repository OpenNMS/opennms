/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.syslog;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;

import com.google.common.collect.Lists;

/**
 * The Class SyslogFilterTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SyslogFilterTest {

    /**
     * Test filter parsing.
     *
     * @throws Exception the exception
     */
    @Test
    public void testFilterParsing() throws Exception {
        OnmsEvent event = new OnmsEvent();
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "user", "agalue", "string"),
                new OnmsEventParameter(event, "passwd", "0nmsRules", "string")));

        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setLastEvent(event);
        onmsAlarm.setUei("uei.opennms.org/junit/test");
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        SyslogFilter filter = new SyslogFilter("test", "uei matches '^uei\\.opennms\\.org.*' and parameters['user'] == 'agalue'", "localhost");
        Assert.assertTrue(filter.passFilter(alarm));
    }

}
