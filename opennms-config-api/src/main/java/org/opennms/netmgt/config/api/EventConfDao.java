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
package org.opennms.netmgt.config.api;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.dao.DataAccessException;

/**
 * <p>EventConfDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventConfDao {

    /**
     * <p>reload</p>
     *
     * @throws org.springframework.dao.DataAccessException if any.
     */
    void reload() throws DataAccessException;

    /**
     * <p>getEvents</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<Event> getEvents(String uei);

    /**
     * <p>getEventUEIs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getEventUEIs();

    /**
     * <p>getEventLabels</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, String> getEventLabels();

    /**
     * <p>getEventLabel</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getEventLabel(String uei);

    /**
     * <p>getEventsByLabel</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<Event> getEventsByLabel();

    /**
     * Adds the event to the root level event config storage.
     *
     * @param event The fully configured Event object to add.
     */
    void addEvent(Event event);

    /**
     * <p>isSecureTag</p>
     *
     * @param tag a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean isSecureTag(String tag);

    /**
     * <p>findByUei</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.eventconf.Event} object.
     */
    Event findByUei(String uei);

    /**
     * <p>findByEvent</p>
     *
     * @param matchingEvent a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link org.opennms.netmgt.xml.eventconf.Event} object.
     */
    Event findByEvent(org.opennms.netmgt.xml.event.Event matchingEvent);

    /**
     * <p>getRootEvents</p>
     * 
     * @return a {@link org.opennms.netmgt.xml.eventconf.Events} object.
     */
    Events getRootEvents();

    /**
     * Load event conf from DB, should replace loading of event conf from filesystem
     * @param dbEvents
     */
    void loadEventsFromDB(List<EventConfEvent> dbEvents);

}
