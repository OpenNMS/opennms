/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AlarmRestServiceTest extends AbstractSpringJerseyRestTestCase {
	protected void afterServletStart() {
		final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		context.getBean("databasePopulator", DatabasePopulator.class).populateDatabase();
	}

	@Test
	public void testAlarms() throws Exception {
		String xml = sendRequest(GET, "/alarms", parseParamData("orderBy=lastEventTime&order=desc&alarmAckUser=null&limit=1"), 200);
		assertTrue(xml.contains("This is a test alarm"));

		xml = sendRequest(GET, "/alarms/1", parseParamData("orderBy=lastEventTime&order=desc&alarmAckUser=null&limit=1"), 200);
		assertTrue(xml.contains("This is a test alarm"));
		assertTrue(xml.contains("<nodeLabel>node1</nodeLabel>"));
	}

    @Test
    public void testAlarmQueryBySeverityEquals() throws Exception {
        String xml = null;
        
        xml = sendRequest(GET, "/alarms", parseParamData("comparator=eq&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=eq&severity=MAJOR&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));
    }

    @Test
    public void testAlarmQueryBySeverityLessThan() throws Exception {
        String xml = null;

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=le&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=lt&severity=NORMAL&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=lt&severity=WARNING&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));
    }

    @Test
    public void testAlarmQueryBySeverityGreaterThan() throws Exception {
        String xml = null;

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=ge&severity=NORMAL&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=gt&severity=NORMAL&limit=1"), 200);
        assertFalse(xml.contains("This is a test alarm"));

        xml = sendRequest(GET, "/alarms", parseParamData("comparator=gt&severity=CLEARED&limit=1"), 200);
        assertTrue(xml.contains("This is a test alarm"));
    }
}
