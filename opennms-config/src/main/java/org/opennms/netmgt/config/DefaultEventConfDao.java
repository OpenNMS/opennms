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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
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
public class DefaultEventConfDao extends AbstractJaxbConfigDao<Events, EventConfiguration> implements EventConfDao, InitializingBean {
    private static final String DEFAULT_PROGRAMMATIC_STORE_RELATIVE_URL = "events/programmatic.events.xml";

    private final EventResourceLoader m_resourceLoader = new EventResourceLoader();
    
    /**
     * Relative URL for the programmatic store configuration, relative to the
     * root configuration resource (which must be resolvable to a URL).
     */
    private String m_programmaticStoreRelativeUrl = DEFAULT_PROGRAMMATIC_STORE_RELATIVE_URL;

    /**
     * The programmatic store configuration resource.
     */
    private Resource m_programmaticStoreConfigResource;

    private static class EventLabelComparator implements Comparator<Event>, Serializable {

        private static final long serialVersionUID = 7976730920523203921L;

        public int compare(final Event e1, final Event e2) {
            return e1.getEventLabel().compareToIgnoreCase(e2.getEventLabel());
        }
    }

    /**
     * <p>Constructor for DefaultEventConfDao.</p>
     */
    public DefaultEventConfDao() {
        super(Events.class, "event");
    }

    /** {@inheritDoc} */
    @Override
    protected String createLoadedLogMessage(final EventConfiguration translatedConfig, final long diffTime) {
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
    
    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws DataAccessException {
        /**
         * It sucks to duplicate this first test from AbstractCastorConfigDao,
         * but we need to do so to ensure we don't get an NPE while initializing
         * programmaticStoreConfigResource (if needed).
         */
        Assert.state(getConfigResource() != null, "property configResource must be set and be non-null");
        
        super.afterPropertiesSet();
    }
    
    /** {@inheritDoc} */
    @Override
    protected EventConfiguration translateConfig(final Events events) throws DataAccessException {
    	final EventConfiguration eventConfiguration = new EventConfiguration();

        processEvents(events, getConfigResource(), eventConfiguration, "root", false);

        if (events.getGlobal() != null && events.getGlobal().getSecurity() != null) {
            eventConfiguration.getSecureTags().addAll(events.getGlobal().getSecurity().getDoNotOverrideCollection());
        }

        for (final String eventFilePath : events.getEventFileCollection()) {
            loadAndProcessEvents(m_resourceLoader.getResource(eventFilePath), eventConfiguration, "included", true);
        }
        
        return eventConfiguration;
    }

    private Events loadAndProcessEvents(final Resource rootResource, final EventConfiguration eventConfiguration, final String resourceDescription, final boolean denyIncludes) {
    	LogUtils.debugf(this, "DefaultEventConfDao: Loading %s event configuration from %s", resourceDescription, rootResource);

    	final Events events = JaxbUtils.unmarshal(Events.class, rootResource);
        processEvents(events, rootResource, eventConfiguration, resourceDescription, denyIncludes);
        return events;
    }

    private void processEvents(final Events events, final Resource resource, final EventConfiguration eventConfiguration, final String resourceDescription, final boolean denyIncludes) {
        if (denyIncludes) {
            if (events.getGlobal() != null) {
                throw new ObjectRetrievalFailureException(Resource.class, resource, "The event resource " + resource + " included from the root event configuration file cannot have a 'global' element", null);
            }
            if (events.getEventFileCollection().size() > 0) {
                throw new ObjectRetrievalFailureException(Resource.class, resource, "The event resource " + resource + " included from the root event configuration file cannot include other configuration files: " + StringUtils.collectionToCommaDelimitedString(events.getEventFileCollection()), null);
            }
        }

        eventConfiguration.getEventFiles().put(resource, events);
        for (final Event event : events.getEventCollection()) {
            eventConfiguration.getEventConfData().put(event);
        }
        
        LogUtils.infof(this, "DefaultEventConfDao: Loaded %d events from %s event configuration resource: %s", events.getEventCollection().size(), resourceDescription, resource);
        
        eventConfiguration.incrementEventCount(events.getEventCount());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEvents(java.lang.String)
     */
    /** {@inheritDoc} */
    public List<Event> getEvents(final String uei) {
    	final List<Event> events = new ArrayList<Event>();

        for (final Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            for (final Event event : fileEvents.getEventCollection()) {
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
        final List<String> eventUEIs = new ArrayList<String>();
        for (final Events fileEvents : getEventConfiguration().getEventFiles().values()) {
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
        final Map<String, String> eventLabels = new TreeMap<String, String>();
        for (final Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            for (final Event event : fileEvents.getEventCollection()) {
                eventLabels.put(event.getUei(), event.getEventLabel());
            }
        }

        return Collections.unmodifiableMap(eventLabels);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#getEventLabel(java.lang.String)
     */
    /** {@inheritDoc} */
    public String getEventLabel(final String uei) {
        for (final Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            for (final Event event : fileEvents.getEventCollection()) {
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
        for (final Entry<Resource, Events> entry : getEventConfiguration().getEventFiles().entrySet()) {
            final Resource resource = entry.getKey();
            final Events fileEvents = entry.getValue();
            
            final StringWriter stringWriter = new StringWriter();
            JaxbUtils.marshal(fileEvents, stringWriter);
            
            if (stringWriter.toString() != null) {
                File file;
                try {
                    file = resource.getFile();
                } catch (final IOException e) {
                    throw new DataAccessResourceFailureException("Event resource '" + resource + "' is not a file resource and cannot be saved.  Nested exception: " + e, e);
                }
                
                final Writer fileWriter;
                try {
                    fileWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                } catch (final IOException e) {
                    throw new DataAccessResourceFailureException("Event file '" + file + "' could not be opened.  Nested exception: " + e, e);
                }
                
                try {
                    fileWriter.write(stringWriter.toString());
                } catch (final IOException e) {
                    throw new DataAccessResourceFailureException("Event file '" + file + "' could not be written to.  Nested exception: " + e, e);
                }
                
                try {
                    fileWriter.close();
                } catch (final IOException e) {
                    throw new DataAccessResourceFailureException("Event file '" + file + "' could not be closed.  Nested exception: " + e, e);
                }
            }
        }
        
        final File programmaticStoreFile;
        try {
            programmaticStoreFile = getProgrammaticStoreConfigResource().getFile();
        
            // Delete the programmatic store if it exists on disk, but isn't in the main store.  This is for cleanliness
            if (programmaticStoreFile.exists() && (!getEventConfiguration().getEventFiles().containsKey(getProgrammaticStoreConfigResource()))) {
                LogUtils.infof(this, "Deleting programmatic store configuration file because it is no longer referenced in the root config file %s", getConfigResource());
                if (!programmaticStoreFile.delete()) {
                    LogUtils.warnf(this, "Attempted to delete %s, but failed.", programmaticStoreFile);
                }
            }

        } catch (final IOException e) {
            LogUtils.infof(this, "Programmatic store resource '%s'; not attempting to delete an unused programmatic store file if it exists (since we can't test for it).", getProgrammaticStoreConfigResource());
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
        final List<Event> list = new ArrayList<Event>();
        for (final Events fileEvents : getEventConfiguration().getEventFiles().values()) {
            list.addAll(fileEvents.getEventCollection());
        }
        Collections.sort(list, new EventLabelComparator());
        return list;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#addEvent(org.opennms.netmgt.xml.eventconf.Event)
     */
    /** {@inheritDoc} */
    public void addEvent(final Event event) {
        getRootEvents().addEvent(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#addEventToProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event)
     */
    /** {@inheritDoc} */
    public void addEventToProgrammaticStore(final Event event) {
        // Check for, and possibly add the programmatic store to the in-memory structure
        if (!getEventConfiguration().getEventFiles().containsKey(getProgrammaticStoreConfigResource())) {
            // Programmatic store did not already exist.  Add an empty Events object for that file
            getEventConfiguration().getEventFiles().put(getProgrammaticStoreConfigResource(), new Events());
        }
        
        // Check for, and possibly add, the programmatic store event-file entry to the in-memory structure of the root config file
        final Events root = getRootEvents();
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
    public boolean removeEventFromProgrammaticStore(final Event event) {
        if (!getEventConfiguration().getEventFiles().containsKey(getProgrammaticStoreConfigResource())) {
            return false; // Oops, doesn't exist
        }
        
        final Events events = getProgrammaticStoreEvents();
        final boolean result = events.removeEvent(event);
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
    public boolean isSecureTag(final String tag) {
        return getEventConfiguration().getSecureTags().contains(tag);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#findByUei(java.lang.String)
     */
    /** {@inheritDoc} */
    public Event findByUei(final String uei) {
        return getEventConfiguration().getEventConfData().getEventByUEI(uei);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.EventConfDao#findByEvent(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    public Event findByEvent(final org.opennms.netmgt.xml.event.Event matchingEvent) {
        return getEventConfiguration().getEventConfData().getEvent(matchingEvent);
    }

    private Resource getProgrammaticStoreConfigResource() {
        if (m_programmaticStoreConfigResource == null) {
            try {
                m_programmaticStoreConfigResource = getConfigResource().createRelative(getProgrammaticStoreRelativeUrl());
            } catch (final IOException e) {
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
    public void setProgrammaticStoreRelativeUrl(final String programmaticStoreRelativeUrl) {
        m_programmaticStoreRelativeUrl = programmaticStoreRelativeUrl;
    }

    private EventConfiguration getEventConfiguration() {
        return getContainer().getObject();
    }

    private class EventResourceLoader extends DefaultResourceLoader {
        @Override
        public Resource getResource(final String location) {
        	final String cleanLocation = StringUtils.cleanPath(location);

        	// Check if this is a spring classpath:foo style resource
        	// but first make sure if we're on windows that it's not
        	// just a C:\foo path.
        	
        	boolean uriResource = false;
        	if (org.opennms.core.utils.StringUtils.isLocalWindowsPath(cleanLocation)) {
        		uriResource = false;
        	} else if (cleanLocation.contains(":")) {
        		// otherwise, something with a : is probably a spring URI resource
        		uriResource = true;
        	}

            if (uriResource) {
                return super.getResource(cleanLocation);
            } else {
            	final File file = new File(cleanLocation);
                if (file.isAbsolute()) {
                    return new FileSystemResource(file);
                } else {
                    try {
                        return getConfigResource().createRelative(cleanLocation);
                    } catch (final IOException e) {
                        throw new ObjectRetrievalFailureException(Resource.class, cleanLocation, "Resource location has a relative path, however the configResource does not reference a file, so the relative path cannot be resolved.  The location is: " + cleanLocation, null);
                    }
                }
            }
        }
    }
}

