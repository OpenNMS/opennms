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
// 2008 Feb 15: Convert to use dependency injection, Resources and use
//              AbstractCastorConfigDao. - dj@opennms.org
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.dao.castor.AbstractCastorConfigDao;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>DefaultEventConfDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultEventConfDao extends AbstractCastorConfigDao<Events, EventConfiguration> implements EventConfDao, InitializingBean {
    private static final String DEFAULT_PROGRAMMATIC_STORE_RELATIVE_URL = "events/programmatic.events.xml";

    private final EventResourceLoader m_resourceLoader = new EventResourceLoader();
    
    /**
     * Relative URL for the programatic store configuration, relative to the
     * root configuration resource (which must be resolvable to a URL).
     */
    private String m_programmaticStoreRelativeUrl = DEFAULT_PROGRAMMATIC_STORE_RELATIVE_URL;

    /**
     * The programmatic store configuration resource.
     */
    private Resource m_programmaticStoreConfigResource;

    private static class EventLabelComparator implements Comparator<Event> {
        public int compare(Event e1, Event e2) {
            return e1.getEventLabel().compareToIgnoreCase(e2.getEventLabel());
        }
    }

    /**
     * <p>Constructor for DefaultEventConfDao.</p>
     */
    public DefaultEventConfDao() {
        super(Events.class, "event configuration");
    }

    /** {@inheritDoc} */
    @Override
    protected String createLoadedLogMessage(EventConfiguration translatedConfig, long diffTime) {
        return "Loaded " + getDescription() + " with " + translatedConfig.getEventCount() + " events from " + translatedConfig.getEventFiles().size() + " files in " + diffTime + "ms";
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#reload()
     */
    /**
     * <p>reload</p>
     *
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public void reload() throws DataAccessException {
        getContainer().reload();
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public void afterPropertiesSet() throws DataAccessException {
        /**
         * It sucks to duplicate this first test from AbstractCastorConfigDao,
         * but we need to do so to ensure we don't get an NPE while initializing
         * programmaticStoreConfigResource (if needed).
         */
        Assert.state(getConfigResource() != null, "property configResource must be set and be non-null");
        
        super.afterPropertiesSet();
    }
    
    /**
     * <p>translateConfig</p>
     *
     * @param events a {@link org.opennms.netmgt.xml.eventconf.Events} object.
     * @return a {@link org.opennms.netmgt.config.EventConfiguration} object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public EventConfiguration translateConfig(Events events) throws DataAccessException {
        EventConfiguration eventConfiguration = new EventConfiguration();

        processEvents(events, getConfigResource(), eventConfiguration, "root", false);

        if (events.getGlobal() != null && events.getGlobal().getSecurity() != null) {
            eventConfiguration.getSecureTags().addAll(events.getGlobal().getSecurity().getDoNotOverrideCollection());
        }

        for (String eventFilePath : events.getEventFileCollection()) {
            Resource childResource = m_resourceLoader.getResource(eventFilePath);
            
            loadAndProcessEvents(childResource, eventConfiguration, "included", true);
        }
        
        return eventConfiguration;
    }

    private Events loadAndProcessEvents(Resource rootResource, EventConfiguration eventConfiguration, String resourceDescription, boolean denyIncludes) {
        if (log().isDebugEnabled()) {
            log().debug("DefaultEventConfDao: Loading " + resourceDescription + " event configuration from " + rootResource);
        }
        
        InputStream in;
        try {
            in = rootResource.getInputStream();
        } catch (IOException e) {
            throw new DataAccessResourceFailureException("Could not get an input stream for resource '" + rootResource + "'; nested exception: " + e, e);
        }
        
        Events events;
        try {
            events =  CastorUtils.unmarshalWithTranslatedExceptions(Events.class, new InputStreamReader(in));
        } finally {
            IOUtils.closeQuietly(in);
        }

        processEvents(events, rootResource, eventConfiguration, resourceDescription, denyIncludes);
        
        return events;
    }

    private void processEvents(Events events, Resource resource, EventConfiguration eventConfiguration, String resourceDescription, boolean denyIncludes) {
        if (denyIncludes) {
            if (events.getGlobal() != null) {
                throw new ObjectRetrievalFailureException(Resource.class, resource, "The event resource " + resource + " included from the root event configuration file cannot have a 'global' element", null);
            }
            if (events.getEventFileCollection().size() > 0) {
                throw new ObjectRetrievalFailureException(Resource.class, resource, "The event resource " + resource + " included from the root event configuration file cannot include other configuration files: " + StringUtils.collectionToCommaDelimitedString(events.getEventFileCollection()), null);
            }
        }

        eventConfiguration.getEventFiles().put(resource, events);
        for (Event event : events.getEventCollection()) {
            eventConfiguration.getEventConfData().put(event);
        }
        
        log().info("DefaultEventConfDao: Loaded " + events.getEventCollection().size() + " events from " + resourceDescription + " event configuration resource: " + resource);
        
        eventConfiguration.incrementEventCount(events.getEventCount());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEvents(java.lang.String)
     */
    /** {@inheritDoc} */
    public List<Event> getEvents(String uei) {
        List<Event> events = new ArrayList<Event>();

        for (Events fileEvents : getEventConfiguration().getEventFiles().values()) {
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
    /**
     * <p>getEventUEIs</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getEventUEIs() {
        List<String> eventUEIs = new ArrayList<String>();
        for (Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            for (Event event : fileEvents.getEventCollection()) {
                eventUEIs.add(event.getUei());
            }
        }
        return eventUEIs;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventLabels()
     */
    /**
     * <p>getEventLabels</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String> getEventLabels() {
        Map<String, String> eventLabels = new TreeMap<String, String>();
        for (Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            for (Event event : fileEvents.getEventCollection()) {
                eventLabels.put(event.getUei(), event.getEventLabel());
            }
        }

        return eventLabels;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventLabel(java.lang.String)
     */
    /** {@inheritDoc} */
    public String getEventLabel(String uei) {
        for (Events fileEvents : getEventConfiguration().getEventFiles().values()) {
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
    /**
     * <p>saveCurrent</p>
     */
    public synchronized void saveCurrent() {
        for (Entry<Resource, Events> entry : getEventConfiguration().getEventFiles().entrySet()) {
            Resource resource = entry.getKey();
            Events fileEvents = entry.getValue();
            
            StringWriter stringWriter = new StringWriter();
            try {
                CastorUtils.marshalWithTranslatedExceptions(fileEvents, stringWriter);
            } catch (DataAccessException e) {
                throw new DataAccessResourceFailureException("Could not marshal configuration file for " + resource + ": " + e, e);
            }
            
            if (stringWriter.toString() != null) {
                File file;
                try {
                    file = resource.getFile();
                } catch (IOException e) {
                    throw new DataAccessResourceFailureException("Event resource '" + resource + "' is not a file resource and cannot be saved.  Nested exception: " + e, e);
                }
                
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
        
        File programmaticStoreFile;
        try {
            programmaticStoreFile = getProgrammaticStoreConfigResource().getFile();
        } catch (IOException e) {
            log().info("Programmatic store resource '" + getProgrammaticStoreConfigResource() + "'; not attempting to delete an unused programmatic store file if it exists (since we can't test for it).");
            programmaticStoreFile = null;
        }
        
        if (programmaticStoreFile != null) {
            // Delete the programmatic store if it exists on disk, but isn't in the main store.  This is for cleanliness
            if (programmaticStoreFile.exists() && (!getEventConfiguration().getEventFiles().containsKey(getProgrammaticStoreConfigResource()))) {
                log().info("Deleting programmatic store configuration file because it is no longer referenced in the root config file " + getConfigResource());
                programmaticStoreFile.delete(); 
            }
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
    /**
     * <p>getEventsByLabel</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Event> getEventsByLabel() {
        List<Event> list = new ArrayList<Event>();
        for (Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            list.addAll(fileEvents.getEventCollection());
        }
        Collections.sort(list, new EventLabelComparator());
        return list;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#addEvent(org.opennms.netmgt.xml.eventconf.Event)
     */
    /** {@inheritDoc} */
    public void addEvent(Event event) {
        Events events = getRootEvents();
        events.addEvent(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#addEventToProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event)
     */
    /** {@inheritDoc} */
    public void addEventToProgrammaticStore(Event event) {
        // Check for, and possibly add the programmatic store to the in-memory structure
        if (!getEventConfiguration().getEventFiles().containsKey(getProgrammaticStoreConfigResource())) {
            // Programmatic store did not already exist.  Add an empty Events object for that file
            getEventConfiguration().getEventFiles().put(getProgrammaticStoreConfigResource(), new Events());
        }
        
        // Check for, and possibly add, the programmatic store event-file entry to the in-memory structure of the root config file
        Events root = getRootEvents();
        if (!root.getEventFileCollection().contains(getProgrammaticStoreRelativeUrl())) {
            root.addEventFile(getProgrammaticStoreRelativeUrl());
        }
        
        // Finally, do what we came here to do
        getProgrammaticStoreEvents().addEvent(event);
    }

    private Events getRootEvents() {
        return getEventConfiguration().getEventFiles().get(getConfigResource());
    }

    private Events getProgrammaticStoreEvents() {
        return getEventConfiguration().getEventFiles().get(getProgrammaticStoreConfigResource());
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#removeEventFromProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event)
     */   
    /** {@inheritDoc} */
    public boolean removeEventFromProgrammaticStore(Event event) {
        if (!getEventConfiguration().getEventFiles().containsKey(getProgrammaticStoreConfigResource())) {
            return false; // Oops, doesn't exist
        }
        
        Events events = getProgrammaticStoreEvents();
        boolean result = events.removeEvent(event);
        if (events.getEventCount() == 0) {
            getEventConfiguration().getEventFiles().remove(getProgrammaticStoreConfigResource());
            getRootEvents().removeEventFile(getProgrammaticStoreRelativeUrl());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#isSecureTag(java.lang.String)
     */
    /** {@inheritDoc} */
    public boolean isSecureTag(String tag) {
        return getEventConfiguration().getSecureTags().contains(tag);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#findByUei(java.lang.String)
     */
    /** {@inheritDoc} */
    public Event findByUei(String uei) {
        return getEventConfiguration().getEventConfData().getEventByUEI(uei);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#findByEvent(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    public Event findByEvent(org.opennms.netmgt.xml.event.Event matchingEvent) {
        return getEventConfiguration().getEventConfData().getEvent(matchingEvent);
    }

    private Resource getProgrammaticStoreConfigResource() {
        if (m_programmaticStoreConfigResource == null) {
            try {
                m_programmaticStoreConfigResource = getConfigResource().createRelative(getProgrammaticStoreRelativeUrl());
            } catch (IOException e) {
                log().warn("Could not get a relative resource for the programmatic store configuration file using relative URL '" + getProgrammaticStoreRelativeUrl() + "': " + e, e);
                throw new DataAccessResourceFailureException("Could not get a relative resource for the programmatic store configuration file using relative URL '" + getProgrammaticStoreRelativeUrl() + "': " + e, e);
            }
        }
        
        return m_programmaticStoreConfigResource;
    }

    /**
     * <p>getProgrammaticStoreRelativeUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProgrammaticStoreRelativeUrl() {
        return m_programmaticStoreRelativeUrl;
    }

    /**
     * <p>setProgrammaticStoreRelativeUrl</p>
     *
     * @param programmaticStoreRelativeUrl a {@link java.lang.String} object.
     */
    public void setProgrammaticStoreRelativeUrl(String programmaticStoreRelativeUrl) {
        m_programmaticStoreRelativeUrl = programmaticStoreRelativeUrl;
    }

    private EventConfiguration getEventConfiguration() {
        return getContainer().getObject();
    }

    private class EventResourceLoader extends DefaultResourceLoader {
        @Override
        public Resource getResource(String location) {
            if (location.contains(":")) {
                return super.getResource(location);
            } else {
                File file = new File(location);
                if (file.isAbsolute()) {
                    return new FileSystemResource(file);
                } else {
                    try {
                        return getConfigResource().createRelative(location);
                    } catch (IOException e) {
                        throw new ObjectRetrievalFailureException(Resource.class, location, "Resource location has a relative path, however the configResource does not reference a file, so the relative path cannot be resolved.  The location is: " + location, null);
                    }
                }
            }
        }
    }
}

