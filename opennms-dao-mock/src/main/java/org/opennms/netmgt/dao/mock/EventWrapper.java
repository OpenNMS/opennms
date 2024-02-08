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
package org.opennms.netmgt.dao.mock;


import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;

/**
 * Need this class because Event doesn't properly implement hashCode
 *
 * @author ranger
 * @version $Id: $
 */
public class EventWrapper {
    private Event m_event;

    /**
     * <p>Constructor for EventWrapper.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public EventWrapper(final Event event) {
        m_event = event;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (o == null) return false;
        if (!(o instanceof EventWrapper)) return false;
        final EventWrapper w = (EventWrapper) o;
        return EventUtils.eventsMatch(m_event, w.m_event);
    }

    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
        return m_event;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return m_event.getUei().hashCode();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
    	return EventUtils.toString(m_event);
    }
}
