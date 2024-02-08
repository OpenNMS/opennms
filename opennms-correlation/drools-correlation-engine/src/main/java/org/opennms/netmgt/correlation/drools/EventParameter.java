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
package org.opennms.netmgt.correlation.drools;

import java.io.Serializable;

import org.opennms.netmgt.xml.event.Event;

/**
 * <p>EventParameter class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class EventParameter implements Serializable {
    private static final long serialVersionUID = -1084189255958969146L;

    private String m_name;
    private Object m_value;
    private Event m_event;
    
    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
        return m_event;
    }
    /**
     * <p>setEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void setEvent(final Event event) {
        m_event = event;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(final String name) {
        m_name = name;
    }
    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getValue() {
        return m_value;
    }
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.Object} object.
     */
    public void setValue(final Object value) {
        m_value = value;
    }
}
