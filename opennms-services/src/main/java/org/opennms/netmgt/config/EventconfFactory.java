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
// 2008 Jan 05: Add a few new constructors and make them all public,
//              eliminate static fields except for s_instance. - dj@opennms.org
// 2008 Jan 05: Simplify init()/reload()/getInstance(). - dj@opennms.org
// 2008 Jan 05: Organize imports, format code, refactor some, and line up some
//              functionality with EventConfigurationManager. - dj@opennms.org
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.util.StringUtils;

/**
 */
public class EventconfFactory {
    private static final String PROGRAMMATIC_STORE_RELATIVE_PATH = "events" + File.separator + "programmatic.events.xml";

    /**
     * The static singleton instance of the EventconfFactory.
     * Is null if the init() method has not been called.
     */
    private static EventconfFactory s_instance;

    /**
     * The root configuration file 
     */
    private File m_rootConfigFile;
    
    /**
     * The programmatic store configuration file 
     */
    private File m_programmaticStoreFile;
    
    /**
     * List of configured events
     */
    private Map<File, Events> m_eventFiles;

    private static class EventLabelComparator implements Comparator<Event> {
        public int compare(Event e1, Event e2) {
            return e1.getEventLabel().compareToIgnoreCase(e2.getEventLabel());
        }
    }

    /**
     * 
     */
    public EventconfFactory() throws IOException {
        this(getDefaultRootConfigFile());
    }
    
    /**
     * 
     */
    public EventconfFactory(File rootConfigFile) {
        this(rootConfigFile, getDefaultProgrammaticStoreConfigFile(rootConfigFile));
    }
    
    /**
     * 
     */
    public EventconfFactory(File rootConfigFile, File programmaticStoreFile) {
        m_rootConfigFile = rootConfigFile;
        m_programmaticStoreFile = programmaticStoreFile;
    }

    /**
     * 
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (isInitialized()) {
            return;
        }

        EventconfFactory newInstance = new EventconfFactory();
        newInstance.reload();

        setInstance(newInstance);
    }

    private static File getDefaultRootConfigFile() throws IOException {
        return ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
    }

    private static File getDefaultProgrammaticStoreConfigFile(File rootConfigFile) {
        return new File(rootConfigFile.getParent() + File.separator + PROGRAMMATIC_STORE_RELATIVE_PATH);
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
        setInstance(null);
        EventconfFactory.init();
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * EventconfFactory
     * 
     * @return the single eventconf factory instance
     */
    public static synchronized EventconfFactory getInstance() {
        if (!isInitialized()) {
            throw new IllegalStateException("init() not called.");
        }

        return s_instance;
    }

    private static void setInstance(EventconfFactory instance) {
        s_instance = instance;
    }

    private static boolean isInitialized() {
        return s_instance != null;
    }

    /**
     * 
     */
    public synchronized void reload() throws IOException, MarshalException, ValidationException {
        Events events = CastorUtils.unmarshal(Events.class, new FileReader(m_rootConfigFile));
       
        m_eventFiles = new HashMap<File, Events>();
        m_eventFiles.put(m_rootConfigFile, events);

        /*
         * Create an array, and add any nested eventfiles defs found, to the end of the array.  
         * Using the "size" field (rather than an enumeration) means we don't need any funky nesting logic
         */
        List<String> eventFiles = new ArrayList<String>(events.getEventFileCollection());
        
        for (String eventFilePath : eventFiles) {
            File eventFile = new File(eventFilePath);
            if (!eventFile.isAbsolute()) {
                /*
                 * This event file is specified with a relative path.  Get the absolute path relative to the root config file, and use 
                 * that for all later file references
                 */
                File tempFile = new File(m_rootConfigFile.getParent() + File.separator + eventFile.getPath());
                eventFile = tempFile.getCanonicalFile();
            }
            
            FileReader fileIn = new FileReader(eventFile);
            if (fileIn == null) {
                throw new IOException("Eventconf: Failed to load/locate events file: " + eventFile);
            }

            Events filelevel = CastorUtils.unmarshal(Events.class, fileIn);
            m_eventFiles.put(eventFile, filelevel);
            
            if (filelevel.getGlobal() != null) {
                throw new ValidationException("The event file " + eventFile + " included from the top-level event configuration file cannot have a 'global' element");
            }
            if (filelevel.getEventFileCollection().size() > 0) {
                throw new ValidationException("The event file " + eventFile + " included from the top-level event configuration file cannot include other configuration files: " + StringUtils.collectionToCommaDelimitedString(filelevel.getEventFileCollection()));
            }
        }
    }

    /**
     * 
     */
    public List<Event> getEvents(String uei) {
        List<Event> events = new ArrayList<Event>();

        for (Events fileEvents : m_eventFiles.values()) {
            for (Event event : fileEvents.getEventCollection()) {
                if (event.getUei().equals(uei)) {
                    events.add(event);
                }
            }
        }
        
        if (events.size() > 0) {
            return events;
        } else {
            return null;
        }
    }

    /**
     */
    public List<String> getEventUEIs() {
        List<String> eventUEIs = new ArrayList<String>();
        for (Events fileEvents : m_eventFiles.values()) {
            for (Event event : fileEvents.getEventCollection()) {
                eventUEIs.add(event.getUei());
            }
        }
        return eventUEIs;
    }

    /**
     */
    public Map<String, String> getEventLabels() {
        Map<String, String> eventLabels = new TreeMap<String, String>();
        for (Events fileEvents : m_eventFiles.values()) {
            for (Event event : fileEvents.getEventCollection()) {
                eventLabels.put(event.getUei(), event.getEventLabel());
            }
        }

        return eventLabels;
    }

    /**
     */
    public String getEventLabel(String uei) {
        for (Events fileEvents : m_eventFiles.values()) {
            for (Event event : fileEvents.getEventCollection()) {
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
        for (Entry<File, Events> entry : m_eventFiles.entrySet()) {
            File file = entry.getKey();
            Events fileEvents = entry.getValue();
            
            StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(fileEvents, stringWriter);
            
            if (stringWriter.toString() != null) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(stringWriter.toString());
                fileWriter.flush();
                fileWriter.close();
            }
        }
        
        // Delete the programmatic store if it exists on disk, but isn't in the main store.  This is for cleanliness
        if (m_programmaticStoreFile.exists() && (!m_eventFiles.containsKey(m_programmaticStoreFile))) {
            m_programmaticStoreFile.delete(); 
        }
    }

    public List<Event> getEventsByLabel() {
        List<Event> list = new ArrayList<Event>();
        for (Events fileEvents : m_eventFiles.values()) {
            list.addAll(fileEvents.getEventCollection());
        }
        Collections.sort(list, new EventLabelComparator());
        return list;
    }

    /**
     * Adds the event to the root level event config storage (file).
     * Does not save (you must save independently with saveCurrent)
     * 
     * @param event The fully configured Event object to add.  
     */
    public void addEvent(Event event) {
        Events events = m_eventFiles.get(m_rootConfigFile);
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
        // Check for, and possibly add the programmatic store to the in-memory structure
        if (!m_eventFiles.containsKey(m_programmaticStoreFile)) {
            // Programmatic store did not already exist.  Add an empty Events object for that file
            m_eventFiles.put(m_programmaticStoreFile, new Events());
        }
        
        // Check for, and possibly add, the programmatic store event-file entry to the in-memory structure of the root config file
        Events root = m_eventFiles.get(m_rootConfigFile);
        String programmaticStorePath = m_programmaticStoreFile.getAbsolutePath();
        if (!root.getEventFileCollection().contains(programmaticStorePath)) {
            root.addEventFile(programmaticStorePath);
        }
        
        Events events = m_eventFiles.get(m_programmaticStoreFile);
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
        if (!m_eventFiles.containsKey(m_programmaticStoreFile)) {
            return false; // Oops, doesn't exist
        }
        
        Events events = m_eventFiles.get(m_programmaticStoreFile);
        boolean result = events.removeEvent(event);
        if (events.getEventCount() == 0) {
            // No more events in the programmatic store.  We must remove that file entry.
            m_eventFiles.remove(m_programmaticStoreFile);
            Events root = m_eventFiles.get(m_rootConfigFile);
            root.removeEventFile(m_programmaticStoreFile.getAbsolutePath());
            // The file will be deleted by saveCurrent, not here
        }
        return result;
    }
}

