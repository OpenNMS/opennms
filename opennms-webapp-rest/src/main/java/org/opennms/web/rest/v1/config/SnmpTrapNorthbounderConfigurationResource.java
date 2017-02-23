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
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapMappingGroup;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapNorthbounderConfig;
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
         * Gets the trap sinks.
         *
         * @return the trap sinks
         */
        @XmlElement(name="trap-sink")
        public List<String> getTrapSinks() {
            return getObjects();
        }
    }

    /**
     * The Class ImportMappings.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="import-mappings")
    public static class ImportMappings extends JaxbListWrapper<String> {

        /**
         * Instantiates a new import mappings.
         */
        public ImportMappings() {}

        /**
         * Instantiates a new import mappings.
         *
         * @param mappings the mappings
         */
        public ImportMappings(List<String> mappings) {
            addAll(mappings);
        }

        /**
         * Gets the import mappings.
         *
         * @return the import mappings
         */
        @XmlElement(name="import-mapping")
        public List<String> getImportMappings() {
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
     * Gets the configuration.
     *
     * @return the configuration
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getConfiguration() {
        return Response.ok(m_snmpTrapNorthbounderConfigDao.getConfig()).build();
    }

    /**
     * Sets the configuration.
     *
     * @param config the full configuration object
     * @return the response
     */
    @POST
    public Response setConfiguration(final SnmpTrapNorthbounderConfig config) {
        writeLock();
        try {
            if (config == null) {
                throw getException(Status.BAD_REQUEST, "SNMP NBI configuration object cannot be null");
            }
            try {
                File configFile = m_snmpTrapNorthbounderConfigDao.getConfigResource().getFile();
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
    public SnmpTrapSink getSnmpTrapSink(@PathParam("trapsinkName") final String trapSinkName) {
        SnmpTrapSink trapSink = m_snmpTrapNorthbounderConfigDao.getConfig().getSnmpTrapSink(trapSinkName);
        if (trapSink == null) {
            throw getException(Status.NOT_FOUND, "SNMP Trap sink {} was not found.", trapSinkName);
        }
        return trapSink;
    }

    /**
     * Gets the import mappings.
     *
     * @param trapSinkName the trap sink name
     * @return the import mappings
     */
    @GET
    @Path("trapsinks/{trapsinkName}/import-mappings")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getImportMappings(@PathParam("trapsinkName") final String trapSinkName) {
        SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
        return Response.ok(new ImportMappings(trapSink.getImportMappings())).build();

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
    public Response setSnmpTrapSink(final SnmpTrapSink snmpTrapSink) {
        writeLock();
        try {
            if (snmpTrapSink == null) {
                throw getException(Status.BAD_REQUEST, "SNMP Trap Sink object cannot be null");
            }
            m_snmpTrapNorthbounderConfigDao.getConfig().addSnmpTrapSink(snmpTrapSink);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Sets an import mapping.
     *
     * @param trapSinkName the trap sink name
     * @param mappingGroup the mapping group
     * @return the response
     */
    @POST
    @Path("trapsinks/{trapsinkName}/import-mappings")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setImportMapping(@PathParam("trapsinkName") final String trapSinkName, final SnmpTrapMappingGroup mappingGroup) {
        writeLock();
        try {
            SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
            try {
                trapSink.addImportMapping(mappingGroup);
            } catch (Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update a specific SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trapsinks/{trapsinkName}")
    public Response updateSnmpTrapSink(@PathParam("trapsinkName") final String trapSinkName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            boolean modified = false;
            SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(trapSink);
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
     * Update import mapping.
     *
     * @param trapSinkName the trap sink name
     * @param mappingName the mapping name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trapsinks/{trapsinkName}/import-mappings/{mappingName}")
    public Response updateImportMapping(@PathParam("trapsinkName") final String trapSinkName, @PathParam("mappingName") final String mappingName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
            SnmpTrapMappingGroup mappingGroup = null;
            try {
                mappingGroup = trapSink.getImportMapping(mappingName);
            } catch (Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            if (mappingGroup == null) {
                return Response.status(404).build();
            }
            boolean modified = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(mappingGroup);
            for (final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                try {
                    trapSink.addImportMapping(mappingGroup);
                } catch (Throwable t) {
                    throw getException(Status.INTERNAL_SERVER_ERROR, t);
                }
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes the import mapping.
     *
     * @param trapSinkName the trap sink name
     * @param mappingName the mapping name
     * @return the response
     */
    @DELETE
    @Path("trapsinks/{trapsinkName}/import-mappings/{mappingName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response removeImportMapping(@PathParam("trapsinkName") final String trapSinkName, @PathParam("mappingName") final String mappingName) {
        SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
        if (trapSink.removeImportMapping(mappingName)) {
            return saveConfiguration();
        }
        return Response.notModified().build();
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
        eb.addParam(EventConstants.PARM_DAEMON_NAME, "SnmpTrapNBI");
        m_eventProxy.send(eb.getEvent());
    }

}
