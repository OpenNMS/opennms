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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.EnterpriseIdPartition;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.EventLabelComparator;
import org.opennms.netmgt.xml.eventconf.EventOrdering;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Events.EventCallback;
import org.opennms.netmgt.xml.eventconf.Events.EventCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;

public class MockEventConfDao implements EventConfDao, InitializingBean {
    private Resource m_resource;
    private Events m_events;

    public void setResource(final Resource resource) {
        m_resource = resource;
    }

    public Resource getResource() {
        return m_resource;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_resource);
        reload();
    }

    @Override
    public void reload() throws DataAccessException {
        InputStream is = null;
        InputStreamReader isr = null;
        try {
            is = m_resource.getInputStream();
            isr = new InputStreamReader(is);
            final Reader reader = isr;
            m_events = JaxbUtils.unmarshal(Events.class, reader);
            m_events.loadEventFiles(m_resource);
            m_events.initialize(new EnterpriseIdPartition(), new EventOrdering());
        } catch (final IOException e) {
            throw new DataRetrievalFailureException("Failed to read from " + m_resource.toString(), e);
        } finally {
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public List<Event> getEvents(final String uei) {
        final List<Event> events = new ArrayList<>();
        m_events.forEachEvent(events, new EventCallback<List<Event>>() {
            @Override
            public List<Event> process(final List<Event> events, final Event event) {
                if (uei.equals(event.getUei())) {
                    events.add(event);
                }
                return events;
            }
        });
        return events;
    }

    @Override
    public List<String> getEventUEIs() {
        final Set<String> ueis = new HashSet<>();
        m_events.forEachEvent(ueis, new EventCallback<Set<String>>() {
            @Override
            public Set<String> process(final Set<String> ueis, final Event event) {
                ueis.add(event.getUei());
                return ueis;
            }
        });
        return new ArrayList<String>(ueis);
    }

    @Override
    public Map<String, String> getEventLabels() {
        final Map<String,String> labels = new HashMap<String,String>();
        m_events.forEachEvent(labels, new EventCallback<Map<String,String>>() {
            @Override
            public Map<String,String> process(final Map<String,String> labels, final Event event) {
                labels.put(event.getUei(), event.getEventLabel());
                return labels;
            }
        });
        return labels;
    }

    @Override
    public String getEventLabel(final String uei) {
        return getEventLabels().get(uei);
    }

    @Override
    public void saveCurrent() {
            m_events.save(m_resource);
    }

    @Override
    public List<Event> getEventsByLabel() {
        SortedSet<Event> events = m_events.forEachEvent(new TreeSet<Event>(new EventLabelComparator()), new EventCallback<SortedSet<Event>>() {
            @Override
            public SortedSet<Event> process(SortedSet<Event> accum, Event event) {
                accum.add(event);
                return accum;
            }
        });
        return new ArrayList<Event>(events);
    }

    @Override
    public void addEvent(final Event event) {
        m_events.addEvent(event);
    }

    @Override
    public void addEventToProgrammaticStore(final Event event) {
        m_events.addEvent(event);
    }

    @Override
    public boolean removeEventFromProgrammaticStore(final Event event) {
        return m_events.removeEvent(event);
    }

    @Override
    public boolean isSecureTag(final String tag) {
        return m_events.isSecureTag(tag);
    }

    @Override
    public Event findByUei(final String uei) {
        return m_events.findFirstMatchingEvent(new EventCriteria() {
            @Override public boolean matches(final Event e) {
                return uei.equals(e.getUei());
            }
        });
    }

    @Override
    public Event findByEvent(final org.opennms.netmgt.xml.event.Event matchingEvent) {
        return m_events.findFirstMatchingEvent(matchingEvent);
    }

    @Override
    public Events getRootEvents() {
        return m_events;
    }

}
