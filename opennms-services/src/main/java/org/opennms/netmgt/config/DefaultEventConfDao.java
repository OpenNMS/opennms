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
// 2008 Jan 06: Pull non-static code into DefaultEventConfDao. - dj@opennms.org
// 2008 Jan 06: Duplicate all EventConfigurationManager functionality in
//              EventconfFactory. - dj@opennms.org
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.eventd.datablock.EventConfData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;

/**
 */
public class DefaultEventConfDao implements EventConfDao {
    private static final String PROGRAMMATIC_STORE_RELATIVE_PATH = "events" + File.separator + "programmatic.events.xml";

    /**
     * The root configuration file 
     */
    private File m_rootConfigFile;
    
    /**
     * The programmatic store configuration file 
     */
    private File m_programmaticStoreFile;
    
    /**
     * Map of configured event files and their events
     */
    private Map<File, Events> m_eventFiles;
    
    /**
     * The mapping of all the event configuration objects for searching
     */
    private EventConfData m_eventConfData;
    
    /**
     * The list of secure tags.
     */
    private Set<String> m_secureTags;

    private static class EventLabelComparator implements Comparator<Event> {
        public int compare(Event e1, Event e2) {
            return e1.getEventLabel().compareToIgnoreCase(e2.getEventLabel());
        }
    }

    /**
     * 
     */
    public DefaultEventConfDao() {
        this(getDefaultRootConfigFile());
    }
    
    /**
     * 
     */
    public DefaultEventConfDao(File rootConfigFile) {
        this(rootConfigFile, getDefaultProgrammaticStoreConfigFile(rootConfigFile));
    }
    
    /**
     * 
     */
    public DefaultEventConfDao(File rootConfigFile, File programmaticStoreFile) {
        m_rootConfigFile = rootConfigFile;
        m_programmaticStoreFile = programmaticStoreFile;
    }

    private static File getDefaultRootConfigFile() throws DataAccessException {
        try {
            return ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
        } catch (IOException e) {
            throw new ObjectRetrievalFailureException(String.class, ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME), "Could not get configuration file for " + ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME), e);
        }
    }

    private static File getDefaultProgrammaticStoreConfigFile(File rootConfigFile) {
        return new File(rootConfigFile.getParent() + File.separator + PROGRAMMATIC_STORE_RELATIVE_PATH);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#reload()
     */
    public synchronized void reload() throws DataAccessException {
        loadConfiguration(m_rootConfigFile);
    }
    
    /**
     * This method is used to load the passed configuration into the currently
     * managed configuration instance. Any events that previously existed are
     * cleared.
     * 
     * @param file
     *            The file to load.
     * 
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @exception java.lang.IOException
     *                Thrown if the file cannot be opened for reading.
     */
    public void loadConfiguration(File file) throws DataAccessException {
        Reader rdr;
        try {
            rdr = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new DataAccessResourceFailureException("Event file '" + file + "' does not exist.  Nested exception: " + e, e);
        }

        try {
            loadConfiguration(rdr, file);
        } finally {
            IOUtils.closeQuietly(rdr);
        }
    }

    protected void loadConfiguration(Reader rdr, File rootConfigFile) throws DataAccessException {
        Map<File, Events> eventFiles = new HashMap<File, Events>();
        EventConfData eventConfData = new EventConfData();
        Set<String> secureTags = new HashSet<String>();
        
        long startTime = System.currentTimeMillis();

        if (log().isDebugEnabled()) {
            log().debug("DefaultEventConfDao: Loading root event configuration file: " + rootConfigFile);
        }
        Events events = CastorUtils.unmarshalWithTranslatedExceptions(Events.class, rdr);
        IOUtils.closeQuietly(rdr);
        
        int count = 0;
        
        count += processEvents(eventFiles, eventConfData, rootConfigFile, events);
        log().info("DefaultEventConfDao: Loaded " + events.getEventCollection().size() + " events from root event configuration file: " + rootConfigFile);

        secureTags.addAll(events.getGlobal().getSecurity().getDoNotOverrideCollection());

        for (String eventFilePath : events.getEventFileCollection()) {
            File eventFile = new File(eventFilePath);
            
            if (!eventFile.isAbsolute()) {
                if (rootConfigFile == null) {
                    throw new ObjectRetrievalFailureException(File.class, eventFile, "Event configuration file contains an eventFile element with a relative path, however loadConfiguration was called without a rootConfigFile parameter, so the relative path cannot be resolved.  The event-file entry is: " + eventFilePath, null);
                }
                
                eventFile = new File(rootConfigFile.getParentFile(), eventFilePath);
                // XXX Should we do getCanonicalFile()???
            }

            FileReader fileIn;
            
            try {
                fileIn = new FileReader(eventFile);
            } catch (FileNotFoundException e) {
                throw new DataAccessResourceFailureException("Event file '" + eventFile + "' does not exist.  Nested exception: " + e, e);
            }

            if (log().isDebugEnabled()) {
                log().debug("DefaultEventConfDao: Loading included event configuration file: " + eventFile);
            }
            Events filelevel = CastorUtils.unmarshalWithTranslatedExceptions(Events.class, fileIn);
            IOUtils.closeQuietly(fileIn);
            
            if (filelevel.getGlobal() != null) {
                throw new ObjectRetrievalFailureException(File.class, eventFile, "The event file " + eventFile + " included from the top-level event configuration file cannot have a 'global' element", null);
            }
            if (filelevel.getEventFileCollection().size() > 0) {
                throw new ObjectRetrievalFailureException(File.class, eventFile, "The event file " + eventFile + " included from the top-level event configuration file cannot include other configuration files: " + StringUtils.collectionToCommaDelimitedString(filelevel.getEventFileCollection()), null);
            }
            
            count += processEvents(eventFiles, eventConfData, eventFile, filelevel);
            
            log().info("DefaultEventConfDao: Loaded " + filelevel.getEventCollection().size() + " events from included event configuration file: " + eventFile);
        }
        
        long endTime = System.currentTimeMillis();

        log().info("DefaultEventConfDao: Loaded a total of " + count + " events in " + (endTime - startTime) + "ms");

        m_eventFiles = eventFiles;
        m_eventConfData = eventConfData;
        m_secureTags = secureTags;
    }

    private static int processEvents(Map<File, Events> eventFileMap, EventConfData eventConfData, File file, Events events) {
        eventFileMap.put(file, events);
        for (Event event : events.getEventCollection()) {
            eventConfData.put(event);
        }
        
        return events.getEventCount();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEvents(java.lang.String)
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventUEIs()
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventLabels()
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventLabel(java.lang.String)
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#saveCurrent()
     */
    public synchronized void saveCurrent() {
        for (Entry<File, Events> entry : m_eventFiles.entrySet()) {
            File file = entry.getKey();
            Events fileEvents = entry.getValue();
            
            StringWriter stringWriter = new StringWriter();
            CastorUtils.marshalWithTranslatedExceptions(fileEvents, stringWriter);
            
            if (stringWriter.toString() != null) {
                FileWriter fileWriter;
                try {
                    fileWriter = new FileWriter(file);
                } catch (IOException e) {
                    throw new DataAccessResourceFailureException("Event file '" + file + "' could not be opened.  Nested exception: " + e, e);
                }
                
                try {
                    fileWriter.write(stringWriter.toString());
                } catch (IOException e) {
                    throw new DataAccessResourceFailureException("Event file '" + file + "' could not be written to.  Nested exception: " + e, e);
                }
                
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new DataAccessResourceFailureException("Event file '" + file + "' could not be closed.  Nested exception: " + e, e);
                }
            }
        }
        
        // Delete the programmatic store if it exists on disk, but isn't in the main store.  This is for cleanliness
        if (m_programmaticStoreFile.exists() && (!m_eventFiles.containsKey(m_programmaticStoreFile))) {
            m_programmaticStoreFile.delete(); 
        }
        
        /*
         * XXX Should we call reload so that the EventConfData object is updated
         * without the caller having to call reload() themselves? 
         */
        //reload();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventsByLabel()
     */
    public List<Event> getEventsByLabel() {
        List<Event> list = new ArrayList<Event>();
        for (Events fileEvents : m_eventFiles.values()) {
            list.addAll(fileEvents.getEventCollection());
        }
        Collections.sort(list, new EventLabelComparator());
        return list;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#addEvent(org.opennms.netmgt.xml.eventconf.Event)
     */
    public void addEvent(Event event) {
        Events events = m_eventFiles.get(m_rootConfigFile);
        events.addEvent(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#addEventToProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event)
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#removeEventFromProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event)
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#isSecureTag(java.lang.String)
     */
    public boolean isSecureTag(String tag) {
        return m_secureTags.contains(tag);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#findByUei(java.lang.String)
     */
    public Event findByUei(String uei) {
        return m_eventConfData.getEventByUEI(uei);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#findByEvent(org.opennms.netmgt.xml.event.Event)
     */
    public Event findByEvent(org.opennms.netmgt.xml.event.Event matchingEvent) {
        return m_eventConfData.getEvent(matchingEvent);
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}

