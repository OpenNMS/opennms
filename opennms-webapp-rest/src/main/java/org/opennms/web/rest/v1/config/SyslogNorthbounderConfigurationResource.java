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

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
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

import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfigDao;
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

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_syslogNorthbounderConfigDao, "syslogNorthbounderConfigDao must be set!");
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
    public Response getStatus(@QueryParam("enabled") Boolean enabled) throws WebApplicationException {
        writeLock();
        try {
            m_syslogNorthbounderConfigDao.getConfig().setEnabled(enabled);
            m_syslogNorthbounderConfigDao.save();
            return Response.ok().build();
        } catch (Throwable t) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets the syslog destination.
     *
     * @param destinationName the destination name
     * @return the syslog destination
     */
    @GET
    @Path("destination/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getSyslogDestination(@PathParam("destinationName") String destinationName) {
        SyslogDestination destination = m_syslogNorthbounderConfigDao.getConfig().getSyslogDestination(destinationName);
        if (destination == null) {
            return Response.status(404).build();
        }
        return Response.ok(destination).build();
    }

    /**
     * Sets the syslog destination.
     *
     * @param destination the destination
     * @return the response
     */
    @POST
    @Path("destination")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setSyslogDestination(SyslogDestination destination) {
        writeLock();
        try {
            m_syslogNorthbounderConfigDao.getConfig().addSyslogDestination(destination);
            m_syslogNorthbounderConfigDao.save();
            return Response.ok().build();
        } catch (Throwable t) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update Syslog Destination.
     *
     * @param uriInfo the URI info
     * @param destinationName the destination name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("destination/{destinationName}")
    public Response updateSyslogDestination(@Context final UriInfo uriInfo, @PathParam("destinationName") String destinationName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            SyslogDestination destination = m_syslogNorthbounderConfigDao.getConfig().getSyslogDestination(destinationName);
            if (destination == null) {
                return Response.status(404).build();
            }
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(destination);
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            m_syslogNorthbounderConfigDao.save();
            return Response.seeOther(getRedirectUri(uriInfo)).build();
        } catch (Throwable t) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
        } finally {
            writeUnlock();
        }
    }

}
