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
package org.opennms.netmgt.model.events;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
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
        assertEquals(date, ifEvent.getTime());
    }

}
