//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 29: Added include files for eventconf.xml
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Global;
import org.opennms.netmgt.xml.eventconf.Mask;

/**
 */
public class EventconfFactory {
    /**
     * The static singleton instance of the EventconfFactory
     */
    private static EventconfFactory instance;

    /**
     * List of configured events
     */
    private static List m_events;

    /**
     * List of global properties
     */
    private static Global m_global;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    private static class EventLabelComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return ((Event) o1).getEventLabel().compareToIgnoreCase(((Event) o2).getEventLabel());
        }
    }

    /**
     * 
     */
    private EventconfFactory() {
    }

    /**
     * 
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (!initialized) {
            reload();
            initialized = true;
        }
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * EventconfFactory
     * 
     * @return the single eventconf factory instance
     */
    static synchronized public EventconfFactory getInstance() {
        if (!initialized)
            return null;

        if (instance == null) {
            instance = new EventconfFactory();
        }

        return instance;
    }

    /**
     * 
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
        InputStream configIn = new FileInputStream(cfgFile);
        Events events = ((Events) Unmarshaller.unmarshal(Events.class, new InputStreamReader(configIn)));
        Collection eventList = events.getEventCollection();
        m_events = new ArrayList();

        Iterator i = eventList.iterator();
        while (i.hasNext()) {
            m_events.add((Event) i.next());
        }

        Enumeration e = events.enumerateEventFile();
        while (e.hasMoreElements()) {
            String eventfile = (String) e.nextElement();
            InputStream fileIn = new FileInputStream(eventfile);
            if (fileIn == null) {
                throw new IOException("Eventconf: Failed to load/locate events file: " + eventfile);
            }

            Reader filerdr = new InputStreamReader(fileIn);
            Events filelevel = null;
            filelevel = (Events) Unmarshaller.unmarshal(Events.class, filerdr);
            Enumeration efile = filelevel.enumerateEvent();
            while (efile.hasMoreElements()) {
                Event event = (Event) efile.nextElement();
                m_events.add(event);
            }
        }

        m_global = events.getGlobal();

        initialized = true;
    }

    /**
     */
    /*
     * private Map makeMap(List eventsList) { HashMap newMap = new HashMap();
     * 
     * for (int i = 0; i < eventsList.size(); i++) { Event curEvent =
     * (Event)eventsList.get(i); newMap.put(curEvent.getUei(), new Integer(i)); }
     * 
     * return newMap; }
     */

    /**
     * 
     */
    public List getEvents(String uei) {
        List events = new ArrayList();

        for (int i = 0; i < m_events.size(); i++) {
            Event curEvent = (Event) m_events.get(i);

            if (curEvent.getUei().equals(uei)) {
                events.add(curEvent);
            }
        }

        if (events.size() > 0)
            return events;
        else
            return null;
    }

    /**
     * @deprecated This function is not implemented completely. It will not
     *             perform as expected. When it is implemented, remove this
     *             deprecation tag.
     */
    public Event getEvent(String uei, Mask mask) {
        List ueiMatches = getEvents(uei);

        Event curEvent = null;
        for (int i = 0; i < ueiMatches.size(); i++) {
            // Compare the masks for a match
        }

        return curEvent;
    }

    /**
     */
    public List getEventUEIs() {
        List eventUEIs = new ArrayList();

        for (int i = 0; i < m_events.size(); i++) {
            Event curEvent = (Event) m_events.get(i);
            eventUEIs.add(curEvent.getUei());
        }

        return eventUEIs;
    }

    /**
     */
    public Map getEventLabels() {
        TreeMap eventLabels = new TreeMap();

        for (int i = 0; i < m_events.size(); i++) {
            Event curEvent = (Event) m_events.get(i);
            eventLabels.put(curEvent.getUei(), curEvent.getEventLabel());
        }

        return eventLabels;
    }

    /**
     */
    public String getEventLabel(String uei) {
        System.out.println("looking for " + uei);
        for (int i = 0; i < m_events.size(); i++) {
            Event curEvent = (Event) m_events.get(i);
            if (curEvent.getUei().equals(uei)) {
                System.out.println("returning " + curEvent.getEventLabel());
                return curEvent.getEventLabel();
            }
        }

        return "No label found for " + uei;
    }

    /**
     */
    public synchronized void saveEvents(Collection eventsList) throws MarshalException, IOException, ValidationException {
        Iterator itr = eventsList.iterator();

        // Remove all of the events from the current map
        while (itr.hasNext()) {
            Event current = (Event) itr.next();
            removeEvent(current.getUei(), current.getMask());
        }

        // Add all of the events into the list
        m_events.addAll(eventsList);

        saveCurrent();
    }

    /**
     */
    public synchronized void removeEvents(String uei) throws MarshalException, ValidationException, IOException {
        List events = getEvents(uei);

        // return null if the event asked for doesn't exist
        if (events == null)
            return;

        // Remove the corresponding UEIs
        if (m_events.removeAll(events) == true)
            ;
        {
            // And if this causes the list to change,
            // rewrite the config file
            saveCurrent();
        }
    }

    /**
     */
    public synchronized void removeEvent(String uei, Mask mask) throws MarshalException, IOException, ValidationException {
        List events = getEvents(uei);

        // return null if the event asked for doesn't exist
        if (events == null)
            return;

        Iterator itr = events.iterator();

        while (itr.hasNext()) {
            Event current = (Event) itr.next();

            // If the event mask matches
            if (current.getMask() == mask) {
                // Remove the corresponding event
                if (m_events.remove(current) == true)
                    ;
                {
                    // And if this causes the list to change,
                    // rewrite the config file
                    saveCurrent();
                }
                return;
            }
        }

        return;
    }

    /**
     * This method saves an event configuration. NOTE: If an existing event
     * config has the same uei as the new one it will be overridden (updated) by
     * the new event config.
     * 
     * @param Event
     */
    /*
     * public synchronized void saveEvent(Event event) throws XMLWriteException {
     * Integer index = (Integer)m_eventsMap.get(event.getUei());
     * 
     * //if the index is null then it is a new event if (index == null) {
     * m_events.add((Event)event.clone()); m_eventsMap.put(event.getUei(), new
     * Integer(m_events.size()-1)); } //replace the existing event else {
     * m_events.set(index.intValue(), (Event)event.clone()); }
     * 
     * writeXML(m_events); }
     */

    /**
     * 
     */
    public synchronized void saveCurrent() throws MarshalException, IOException, ValidationException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);

        Events newEvents = new Events();
        newEvents.setEventCollection(new ArrayList(m_events));
        newEvents.setGlobal(m_global);

        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(newEvents, stringWriter);
        if (stringWriter.toString() != null) {
            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(stringWriter.toString());
            fileWriter.flush();
            fileWriter.close();
        }

        reload();
    }

    public List getEventsByLabel() {
        ArrayList list = new ArrayList(m_events);
        Collections.sort(list, new EventLabelComparator());
        return list;
    }

    /**
     */
    /*
     * public synchronized void renameEvent(String newName, Event event) throws
     * XMLWriteException { Integer index =
     * (Integer)m_eventsMap.get(event.getUei());
     * 
     * m_eventsMap.remove(event.getUei()); m_eventsMap.put(newName, index);
     * 
     * Event originalEvent = (Event)m_events.get(index.intValue());
     * originalEvent.setUei(newName);
     * 
     * writeXML(m_events); }
     */

    /**
     */
    /*
     * private void writeXML(Collection events) throws XMLWriteException { List
     * globalAndEvents = new ArrayList();
     * globalAndEvents.add(m_globalInformation); globalAndEvents.addAll(events);
     * 
     * m_eventConfWriter.save((Collection)globalAndEvents); m_lastModified =
     * m_eventConfFile.lastModified(); }
     */
}
