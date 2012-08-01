/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={})
public class Events implements Serializable {
	private static final long serialVersionUID = -49037181336311348L;

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Event[] EMPTY_EVENT_ARRAY = new Event[0];

	/**
     * Global settings for this configuration
     */
	@XmlElement(name="global", required=false)
    private Global m_global;

	// @Size(min=1)
	@XmlElement(name="event", required=true)
    private List<Event> m_events = new ArrayList<Event>();

	// @Size(min=0)
	@XmlElement(name="event-file", required=false)
    private List<String> m_eventFiles = new ArrayList<String>();

    public void addEvent(final Event event) throws IndexOutOfBoundsException {
        m_events.add(event);
    }

    public void addEvent(final int index, final Event event) throws IndexOutOfBoundsException {
        m_events.add(index, event);
    }

    public void addEventFile(final String eventFile) throws IndexOutOfBoundsException {
        m_eventFiles.add(eventFile.intern());
    }

    public void addEventFile(final int index, final String eventFile) throws IndexOutOfBoundsException {
        m_eventFiles.add(index, eventFile.intern());
    }

    public Enumeration<Event> enumerateEvent() {
        return Collections.enumeration(m_events);
    }

    public Enumeration<String> enumerateEventFile() {
        return Collections.enumeration(m_eventFiles);
    }

    public Event getEvent(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_events.size()) {
            throw new IndexOutOfBoundsException("getEvent: Index value '" + index + "' not in range [0.." + (m_events.size() - 1) + "]");
        }
        return m_events.get(index);
    }

    public Event[] getEvent() {
        return m_events.toArray(EMPTY_EVENT_ARRAY);
    }

    public List<Event> getEventCollection() {
        return m_events;
    }

    public int getEventCount() {
        return m_events.size();
    }

    public String getEventFile(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_eventFiles.size()) {
            throw new IndexOutOfBoundsException("getEventFile: Index value '" + index + "' not in range [0.." + (m_eventFiles.size() - 1) + "]");
        }
        return m_eventFiles.get(index);
    }

    public String[] getEventFile() {
        return m_eventFiles.toArray(EMPTY_STRING_ARRAY);
    }

    public List<String> getEventFileCollection() {
        return m_eventFiles;
    }

    public int getEventFileCount() {
        return m_eventFiles.size();
    }

    public Global getGlobal() {
        return m_global;
    }

    /**
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    public Iterator<Event> iterateEvent() {
        return m_events.iterator();
    }

    public Iterator<String> iterateEventFile() {
        return m_eventFiles.iterator();
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllEvent() {
        m_events.clear();
    }

    public void removeAllEventFile() {
        m_eventFiles.clear();
    }

    public boolean removeEvent(final Event event) {
        return m_events.remove(event);
    }

    public Event removeEventAt(final int index) {
        return m_events.remove(index);
    }

    public boolean removeEventFile(final String eventFile) {
        return m_eventFiles.remove(eventFile);
    }

    public String removeEventFileAt(final int index) {
        return m_eventFiles.remove(index);
    }

    public void setEvent(final int index, final Event event) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_events.size()) {
            throw new IndexOutOfBoundsException("setEvent: Index value '" + index + "' not in range [0.." + (m_events.size() - 1) + "]");
        }
        m_events.set(index, event);
    }

    public void setEvent(final Event[] events) {
        m_events.clear();
        for (final Event event : events) {
        	m_events.add(event);
        }
    }

    public void setEvent(final List<Event> events) {
        m_events.clear();
        m_events.addAll(events);
    }

    public void setEventCollection(final List<Event> events) {
        m_events = events;
    }

    public void setEventFile(final int index, final String eventFile) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_eventFiles.size()) {
            throw new IndexOutOfBoundsException("setEventFile: Index value '" + index + "' not in range [0.." + (m_eventFiles.size() - 1) + "]");
        }
        m_eventFiles.set(index, eventFile.intern());
    }

    public void setEventFile(final String[] eventFiles) {
        m_eventFiles.clear();
        for (final String eventFile : eventFiles) {
        	m_eventFiles.add(eventFile.intern());
        }
    }

    public void setEventFile(final List<String> eventFiles) {
        m_eventFiles.clear();
        for (final String eventFile : eventFiles) {
        	m_eventFiles.add(eventFile.intern());
        }
    }

    public void setEventFileCollection(final List<String> eventFiles) {
    	setEventFile(eventFiles);
    }

    public void setGlobal(final Global global) {
        m_global = global;
    }

    public static Events unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Events) Unmarshaller.unmarshal(Events.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_eventFiles == null) ? 0 : m_eventFiles.hashCode());
		result = prime * result + ((m_events == null) ? 0 : m_events.hashCode());
		result = prime * result + ((m_global == null) ? 0 : m_global.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Events)) return false;
		final Events other = (Events) obj;
		if (m_eventFiles == null) {
			if (other.m_eventFiles != null) return false;
		} else if (!m_eventFiles.equals(other.m_eventFiles)) {
			return false;
		}
		if (m_events == null) {
			if (other.m_events != null) return false;
		} else if (!m_events.equals(other.m_events)) {
			return false;
		}
		if (m_global == null) {
			if (other.m_global != null) return false;
		} else if (!m_global.equals(other.m_global)) {
			return false;
		}
		return true;
	}

}
