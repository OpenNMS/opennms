/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;


/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class EventBuilderTest {

	/**
	 * Test method for {@link org.opennms.netmgt.model.events.EventBuilder#getEvent()}.
	 */
    @Test
	public final void testGetEvent() {
		EventBuilder builder = new EventBuilder("uei.opennms.org/test", "test");
		builder.setSeverity("Warning");
		assertEquals("Warning", builder.getEvent().getSeverity());
		
		builder.setSeverity("Waning");
		assertEquals("Indeterminate", builder.getEvent().getSeverity());
	}
    
    @Test
    public final void testUsingPassedInDate() throws Exception {
        Date date = new Date(12345);
        
        EventBuilder builder = new EventBuilder("uei.opennms.org/test", "test", date);
        Event ifEvent = builder.getEvent();
        assertEquals(date.toString(), EventConstants.parseToDate(ifEvent.getTime()).toString());
        
        
    }

}
