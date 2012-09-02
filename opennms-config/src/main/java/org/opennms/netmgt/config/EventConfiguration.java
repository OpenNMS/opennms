/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.eventd.datablock.EventConfData;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.Resource;

class EventConfiguration {
    /**
     * Map of configured event files and their events
     */
    private Map<Resource, Events> m_eventFiles = new HashMap<Resource, Events>();
    
    /**
     * The mapping of all the event configuration objects for searching
     */
    private EventConfData m_eventConfData = new EventConfData();
    
    /**
     * The list of secure tags.
     */
    private Set<String> m_secureTags = new HashSet<String>();
    
    /**
     * Total count of events in these files.
     */
    private int m_eventCount = 0;

    /**
     * <p>getEventConfData</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.datablock.EventConfData} object.
     */
    public EventConfData getEventConfData() {
        return m_eventConfData;
    }

    /**
     * <p>setEventConfData</p>
     *
     * @param eventConfData a {@link org.opennms.netmgt.eventd.datablock.EventConfData} object.
     */
    public void setEventConfData(EventConfData eventConfData) {
        m_eventConfData = eventConfData;
    }

    /**
     * <p>getEventFiles</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Resource, Events> getEventFiles() {
        return m_eventFiles;
    }

    /**
     * <p>setEventFiles</p>
     *
     * @param eventFiles a {@link java.util.Map} object.
     */
    public void setEventFiles(Map<Resource, Events> eventFiles) {
        m_eventFiles = eventFiles;
    }

    /**
     * <p>getSecureTags</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getSecureTags() {
        return m_secureTags;
    }

    /**
     * <p>setSecureTags</p>
     *
     * @param secureTags a {@link java.util.Set} object.
     */
    public void setSecureTags(Set<String> secureTags) {
        m_secureTags = secureTags;
    }

    /**
     * <p>getEventCount</p>
     *
     * @return a int.
     */
    public int getEventCount() {
        return m_eventCount;
    }

    /**
     * <p>incrementEventCount</p>
     *
     * @param incrementCount a int.
     */
    public void incrementEventCount(int incrementCount) {
        m_eventCount += incrementCount;
    }
}
