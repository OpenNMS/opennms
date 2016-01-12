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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapNorthbounderConfigDao;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapSink;
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
 * The Class SnmpTrapNorthbounderConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class SnmpTrapNorthbounderConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The SNMP Trap Northbounder configuration DAO. */
    @Resource(name="snmpTrapNorthbounderConfigDao")
    private SnmpTrapNorthbounderConfigDao m_snmpTrapNorthbounderConfigDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class SnmpTrapSinkList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="trap-sinks")
    public static class SnmpTrapSinkList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new SNMP trap sink list.
         */
        public SnmpTrapSinkList() {}

        /**
         * Instantiates a new SNMP trap sink list.
         *
         * @param trapSinks the trap sinks
         */
        public SnmpTrapSinkList(List<SnmpTrapSink> trapSinks) {
            trapSinks.forEach(d -> add(d.getName()));
        }

        /**
         * Gets the destinations.
         *
         * @return the destinations
         */
        @XmlElement(name="trap-sink")
        public List<String> getDestinations() {
            return getObjects();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_snmpTrapNorthbounderConfigDao, "snmpTrapNorthbounderConfigDao must be set!");
        Assert.notNull(m_eventProxy, "eventProxy must be set!");
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
        return Response.ok(m_snmpTrapNorthbounderConfigDao.getConfig().isEnabled()).build();
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
            m_snmpTrapNorthbounderConfigDao.getConfig().setEnabled(enabled);
            return saveConfiguration();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets all the SNMP trap sinks.
     *
     * @return the SNMP trap sinks
     */
    @GET
    @Path("trapsinks")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getSnmpTrapSinks() {
        SnmpTrapSinkList trapSinks = new SnmpTrapSinkList(m_snmpTrapNorthbounderConfigDao.getConfig().getSnmpTrapSinks());
        return Response.ok(trapSinks).build();
    }

    /**
     * Gets the SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @return the SNMP trap sink
     */
    @GET
    @Path("trapsinks/{trapsinkName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getSnmpTrapSink(@PathParam("trapsinkName") final String trapSinkName) {
        SnmpTrapSink trapSink = m_snmpTrapNorthbounderConfigDao.getConfig().getSnmpTrapSink(trapSinkName);
        if (trapSink == null) {
            return Response.status(404).build();
        }
        return Response.ok(trapSink).build();
    }

    /**
     * Sets a SNMP trap sink.
     * <p>If there is a trap sunk with the same name, the existing one will be overridden.</p>
     *
     * @param snmpTrapSink the SNMP trap sink
     * @return the response
     */
    @POST
    @Path("trapsinks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setSnmpTrapSink(@Context final UriInfo uriInfo, final SnmpTrapSink snmpTrapSink) {
        writeLock();
        try {
            m_snmpTrapNorthbounderConfigDao.getConfig().addSnmpTrapSink(snmpTrapSink);
            saveConfiguration();
            return Response.seeOther(getRedirectUri(uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update a specific SNMP trap sink.
     *
     * @param uriInfo the URI info
     * @param trapSinkName the trap sink name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trapsinks/{trapsinkName}")
    public Response updateSnmpTrapSink(@Context final UriInfo uriInfo, @PathParam("trapsinkName") final String trapSinkName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            boolean changed = false;
            SnmpTrapSink trapSink = m_snmpTrapNorthbounderConfigDao.getConfig().getSnmpTrapSink(trapSinkName);
            if (trapSink == null) {
                return Response.status(404).build();
            }
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(trapSink);
            for (final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            if (changed) {
                saveConfiguration();
                return Response.seeOther(getRedirectUri(uriInfo)).build();
            }
            return Response.notModified().build();
        } catch (Throwable t) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes a specific SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @return the response
     */
    @DELETE
    @Path("trapsinks/{trapsinkName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response removeSnmpTrapSink(@PathParam("trapsinkName") final String trapSinkName) {
        if (m_snmpTrapNorthbounderConfigDao.getConfig().removeSnmpTrapSink(trapSinkName)) {
            return saveConfiguration();
        }
        return Response.status(404).build();
    }

    /**
     * Saves the configuration.
     *
     * @return the response
     */
    private Response saveConfiguration() {
        try {
            m_snmpTrapNorthbounderConfigDao.save();
            EventBuilder eb = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "ReST");
            eb.addParam(EventConstants.PARM_DAEMON_NAME, "SnmpTrapNBI");
            m_eventProxy.send(eb.getEvent());
            return Response.ok().build();
        } catch (Throwable t) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
        }
    }

}
