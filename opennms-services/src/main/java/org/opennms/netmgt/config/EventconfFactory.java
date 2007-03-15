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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.opennms.netmgt.xml.eventconf.Mask;

/**
 */
public class EventconfFactory {
    /**
     * The static singleton instance of the EventconfFactory
     */
    private static EventconfFactory instance;

    /**
     * The root configuration file 
     */
    private static File m_rootConfigFile;
    
    private static File m_programmaticStoreFile;
    
    /**
     * List of configured events
     */
    private static Map<File, Events> m_eventFiles;

    /**
     * List of global properties
     */
    //private static Global m_global;

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
            m_rootConfigFile=ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
            m_programmaticStoreFile=new File(m_rootConfigFile.getParent() + File.separator + "events/programmatic.events.xml");
            reload();
            initialized = true;
        }
    }

    /**
     * A full reinitialization, from scratch.  Subtly different from a reload (more encompassing).
     * Safe to call in place of init if you so desire
     * @throws IOException 
     * @throws ValidationException 
     * @throws MarshalException 
     *
     */
    public static synchronized void reinit() throws MarshalException, ValidationException, IOException {
        initialized=false;
        EventconfFactory.init();
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
        InputStream configIn = new FileInputStream(m_rootConfigFile);
        Events events = ((Events) Unmarshaller.unmarshal(Events.class, new InputStreamReader(configIn)));
       
        m_eventFiles=new HashMap<File, Events>();
        m_eventFiles.put(m_rootConfigFile, events);

        //Create an array, and add any nested eventfiles defs found, to the end of the array.  
        //Using the "size" field (rather than an enumeration) means we don't need any funky nesting logic
        List<String> eventFiles=new ArrayList(events.getEventFileCollection());
        
        for(int i=0; i<eventFiles.size(); i++) {
            String eventFilePath = (String) eventFiles.get(i);
            File eventFile=new File(eventFilePath);
            if(!eventFile.isAbsolute()) {
                //This event file is specified with a relative path.  Get the absolute path relative to the root config file, and use 
                // that for all later file references
                File tempFile=new File(m_rootConfigFile.getParent() + File.separator + eventFile.getPath());
                eventFile=tempFile.getCanonicalFile();
            }
            InputStream fileIn = new FileInputStream(eventFile);
            if (fileIn == null) {
                throw new IOException("Eventconf: Failed to load/locate events file: " + eventFile);
            }

            Reader filerdr = new InputStreamReader(fileIn);
            Events filelevel = null;
            filelevel = (Events) Unmarshaller.unmarshal(Events.class, filerdr);
            m_eventFiles.put(eventFile, filelevel);
            
            //There are nested event-file definitions - load them as well
            if(filelevel.getEventFileCount()>0) {
                eventFiles.addAll(filelevel.getEventFileCollection());
            }
        }

        //m_global = events.getGlobal();

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
    public List<Event> getEvents(String uei) {
        List<Event> events = new ArrayList<Event>();

        for(Events fileEvents : m_eventFiles.values()) {
            for(int i=0; i<fileEvents.getEventCount(); i++) {
                Event event=fileEvents.getEvent(i);
                if (event.getUei().equals(uei)) {
                    events.add(event);
                }
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
    public List<String> getEventUEIs() {
        List<String> eventUEIs = new ArrayList<String>();
        for(Events fileEvents : m_eventFiles.values()) {
            for(int i=0; i<fileEvents.getEventCount(); i++) {
                Event event=fileEvents.getEvent(i);
                eventUEIs.add(event.getUei());
            }
        }
        return eventUEIs;
    }

    /**
     */
    public Map getEventLabels() {
        TreeMap eventLabels = new TreeMap();
        for(Events fileEvents : m_eventFiles.values()) {
            for(int i=0; i<fileEvents.getEventCount(); i++) {
                Event event=fileEvents.getEvent(i);
                eventLabels.put(event.getUei(), event.getEventLabel());
            }
        }

        return eventLabels;
    }

    /**
     */
    public String getEventLabel(String uei) {
        for(Events fileEvents : m_eventFiles.values()) {
            for(int i=0; i<fileEvents.getEventCount(); i++) {
                Event event=fileEvents.getEvent(i);
                if (event.getUei().equals(uei)) {
                    return event.getEventLabel();
                }   
            }
        }
        return "No label found for " + uei;
    }

    /**
     * 
     */
    public synchronized void saveCurrent() throws MarshalException, IOException, ValidationException {        
        for(File file : m_eventFiles.keySet()) {
            Events fileEvents=m_eventFiles.get(file);
            StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(fileEvents, stringWriter);
            if (stringWriter.toString() != null) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(stringWriter.toString());
                fileWriter.flush();
                fileWriter.close();
            }
        }
        //Delete the programmatic store if it exists on disk, but isn't in the main store.  THis is for cleanliness
        if(m_programmaticStoreFile.exists() && (!m_eventFiles.containsKey(m_programmaticStoreFile))) {
            m_programmaticStoreFile.delete(); 
        }
        reload();
    }

    public List getEventsByLabel() {
        ArrayList list = new ArrayList();
        for(Events fileEvents : m_eventFiles.values()) {
            list.addAll(fileEvents.getEventCollection());
        }
        Collections.sort(list, new EventLabelComparator());
        return list;
    }

    /**
     * Adds the event to the root level event config storage (file).  Does not save (you must save independently with saveCurrent)
     * @param event The fully configured Event object to add.  
     */
    public void addEvent(Event event) {
        Events events=m_eventFiles.get(m_rootConfigFile);
        events.addEvent(event);
    }

    /**
     * Adds the given event to the programmatic event store.  This store currently implemented as a file (referenced from eventconf.xml)
     * The programmatic store is a separate storage area, so that incidental programmatic editing of events (e.g. custom UEIs for thresholds, edited 
     * through the Web-UI) does not clutter up the otherwise carefully maintained event files.  This method does not save (persist) the changes
     * 
     * @param event The fully configured Event object to add.
     */
    public void addEventToProgrammaticStore(Event event) {

        //Check for, and possibly add the programmatic store to the in-memory structure
        if(!m_eventFiles.containsKey(m_programmaticStoreFile)) {
            //programmatic store did not already exist.  Add an empty Events object for that file
            m_eventFiles.put(m_programmaticStoreFile, new Events());
        }
        //Check for, and possibly add, the programmatic store event-file entry to the in-memory structure of the root config file
        Events root=m_eventFiles.get(m_rootConfigFile);
        String programmaticStorePath=m_programmaticStoreFile.getAbsolutePath();
        if(!root.getEventCollection().contains(programmaticStorePath)) {
            root.addEventFile(programmaticStorePath);
        }
        
        Events events=m_eventFiles.get(m_programmaticStoreFile);
        events.addEvent(event);
    }

    /**
     * Removes the given event from the programmatic event store.  This store currently implemented as a file (referenced from eventconf.xml)
     * The programmatic store is a separate storage area, so that incidental programmatic editing of events (e.g. custom UEIs for thresholds, edited 
     * through the Web-UI) does not clutter up the otherwise carefully maintained event files.  This method does not save (persist) the changes
     * 
     * @param event The fully configured Event object to remove.
     * @returns true if the event was removed, false if it wasn't found (either not in the programmatic store, or the store didn't exist)
     */   
    public boolean removeEventFromProgrammaticStore(Event event) {
        if(!m_eventFiles.containsKey(m_programmaticStoreFile)) {
            return false; //Oops, doesn't exist
        }
        Events events=m_eventFiles.get(m_programmaticStoreFile);
        boolean result=events.removeEvent(event);
        if(events.getEventCount()==0) {
            //No more events in the programmatic store.  We must remove that file entry 
            m_eventFiles.remove(m_programmaticStoreFile);
            Events root=m_eventFiles.get(m_rootConfigFile);
            root.removeEventFile(m_programmaticStoreFile.getAbsolutePath());
            //The file will be deleted by saveCurrent, not here
        }
        return result;
    }
}

