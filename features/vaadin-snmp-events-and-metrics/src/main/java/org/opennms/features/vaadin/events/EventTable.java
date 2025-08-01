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
package org.opennms.features.vaadin.events;

import java.util.List;

import org.opennms.features.vaadin.api.OnmsBeanContainer;

import com.vaadin.v7.ui.Table;

/**
 * The Class Event Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventTable extends Table {

    /** The Table Container for Events. */
    private final OnmsBeanContainer<org.opennms.netmgt.xml.eventconf.Event> container =
            new OnmsBeanContainer<org.opennms.netmgt.xml.eventconf.Event>(org.opennms.netmgt.xml.eventconf.Event.class);

    /**
     * Instantiates a new event table.
     *
     * @param events the OpenNMS events
     */
    public EventTable(final List<org.opennms.netmgt.xml.eventconf.Event> events) {
        container.addAll(events);
        setContainerDataSource(container);
        setImmediate(true);
        setSelectable(true);
        addStyleName("light");
        setVisibleColumns(new Object[] { "eventLabel", "uei" });
        setColumnHeaders(new String[] { "Event Label", "Event UEI" });
        setWidth("100%");
        setHeight("250px");
    }

    /**
     * Gets the event.
     *
     * @param eventId the event ID (the Item ID associated with the container)
     * @return the event
     */
    public org.opennms.netmgt.xml.eventconf.Event getEvent(Object eventId) {
        return container.getOnmsBean(eventId);
    }

    /**
     * Adds the event.
     *
     * @param event the new event
     * @return the eventId
     */
    public Object addEvent(org.opennms.netmgt.xml.eventconf.Event event) {
        Object eventId = container.addOnmsBean(event);
        select(eventId);
        return eventId;
    }

    /**
     * Gets the events.
     *
     * @return the events
     */
    public List<org.opennms.netmgt.xml.eventconf.Event> getOnmsEvents() {
        return container.getOnmsBeans();
    }
}
