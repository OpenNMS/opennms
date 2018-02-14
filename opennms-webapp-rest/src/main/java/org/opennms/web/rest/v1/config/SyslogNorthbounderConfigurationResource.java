/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v1.config;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfigDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.OnmsRestService;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The Class SyslogNorthbounderConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class SyslogNorthbounderConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The Syslog Northbounder configuration DAO. */
    @Resource(name="syslogNorthbounderConfigDao")
    private SyslogNorthbounderConfigDao m_syslogNorthbounderConfigDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class SyslogDestinationList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="syslog-destinations")
    public static class SyslogDestinationList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new syslog destination list.
         */
        public SyslogDestinationList() {}

        /**
         * Instantiates a new syslog destination list.
         *
         * @param destinations the destinations
         */
        public SyslogDestinationList(List<SyslogDestination> destinations) {
            destinations.forEach(d -> add(d.getName()));
        }

        /**
         * Gets the destinations.
         *
         * @return the destinations
         */
        @XmlElement(name="destination")
        public List<String> getDestinations() {
            return getObjects();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_syslogNorthbounderConfigDao, "syslogNorthbounderConfigDao must be set!");
        Assert.notNull(m_eventProxy, "eventProxy must be set!");
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getConfiguration() {
        return Response.ok(m_syslogNorthbounderConfigDao.getConfig()).build();
    }

    /**
     * Sets the configuration.
     *
     * @param config the full configuration object
     * @return the response
     */
    @POST
    public Response setConfiguration(final SyslogNorthbounderConfig config) {
        writeLock();
        try {
            if (config == null) {
                throw getException(Status.BAD_REQUEST, "Syslog NBI configuration object cannot be null");
            }
            try {
                File configFile = m_syslogNorthbounderConfigDao.getConfigResource().getFile();
                JaxbUtils.marshal(config, new FileWriter(configFile));
                notifyDaemons();
            } catch (Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatus() {
        return Response.ok(m_syslogNorthbounderConfigDao.getConfig().isEnabled()).build();
    }

    /**
     * Gets the status.
     *
     * @param enabled the enabled
     * @return the status
     * @throws WebApplicationException the web application exception
     */
    @PUT
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatus(@QueryParam("enabled") final Boolean enabled) throws WebApplicationException {
        writeLock();
        try {
            m_syslogNorthbounderConfigDao.getConfig().setEnabled(enabled);
            return saveConfiguration();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets all the email destinations.
     *
     * @return the email destinations
     */
    @GET
    @Path("destinations")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getEmailDestinations() {
        SyslogDestinationList destinations = new SyslogDestinationList(m_syslogNorthbounderConfigDao.getConfig().getDestinations());
        return Response.ok(destinations).build();
    }

    /**
     * Gets a syslog destination.
     *
     * @param destinationName the destination name
     * @return the syslog destination
     */
    @GET
    @Path("destinations/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public SyslogDestination getSyslogDestination(@PathParam("destinationName") final String destinationName) {
        SyslogDestination destination = m_syslogNorthbounderConfigDao.getConfig().getSyslogDestination(destinationName);
        if (destination == null) {
            throw getException(Status.NOT_FOUND, "Syslog destination {} was not found.", destinationName);
        }
        return destination;
    }

    /**
     * Sets a syslog destination.
     * <p>If there is a destination with the same name, the existing one will be overridden.</p>
     *
     * @param destination the destination
     * @return the response
     */
    @POST
    @Path("destinations")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setSyslogDestination(final SyslogDestination destination) {
        writeLock();
        try {
            if (destination == null) {
                throw getException(Status.BAD_REQUEST, "Syslog destination object cannot be null");
            }
            m_syslogNorthbounderConfigDao.getConfig().addSyslogDestination(destination);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update a specific Syslog Destination.
     *
     * @param destinationName the destination name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("destinations/{destinationName}")
    public Response updateSyslogDestination(@PathParam("destinationName") final String destinationName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            boolean modified = false;
            SyslogDestination destination = getSyslogDestination(destinationName);
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(destination);
            for (final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes a specific syslog destination.
     *
     * @param destinationName the destination name
     * @return the response
     */
    @DELETE
    @Path("destinations/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response removeSyslogDestination(@PathParam("destinationName") final String destinationName) {
        if (m_syslogNorthbounderConfigDao.getConfig().removeSyslogDestination(destinationName)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Saves the configuration.
     *
     * @return the response
     */
    private Response saveConfiguration() {
        try {
            m_syslogNorthbounderConfigDao.save();
            notifyDaemons();
            return Response.noContent().build();
        } catch (Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
        }
    }

    /**
     * Notify daemons.
     *
     * @throws Exception the exception
     */
    private void notifyDaemons() throws Exception {
        EventBuilder eb = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "ReST");
        eb.addParam(EventConstants.PARM_DAEMON_NAME, "SyslogNBI");
        m_eventProxy.send(eb.getEvent());
    }

}
