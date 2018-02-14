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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
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
 * The Class JavamailConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class JavamailConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The Javamail configuration DAO. */
    @Resource(name="javamailConfigDao")
    private JavaMailConfigurationDao m_javamailConfigurationDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class SendmailConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="sendmail-configs")
    public static class SendmailConfigList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new sendmail configuration list.
         */
        public SendmailConfigList() {}

        /**
         * Instantiates a new sendmail configuration list.
         *
         * @param sendmailConfigs the sendmail configurations
         */
        public SendmailConfigList(List<SendmailConfig> sendmailConfigs) {
            sendmailConfigs.forEach(d -> {
                if (d.getName() != null) {
                    add(d.getName());
                }
            });
        }

        /**
         * Gets the sendmail configurations.
         *
         * @return the sendmail configurations
         */
        @XmlElement(name="sendmail-config")
        public List<String> getSendmailConfigs() {
            return getObjects();
        }
    }

    /**
     * The Class ReadmailConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="sendmail-configs")
    public static class ReadmailConfigList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new readmail configuration list.
         */
        public ReadmailConfigList() {}

        /**
         * Instantiates a new readmail configuration list.
         *
         * @param sendmailConfigs the sendmail configurations
         */
        public ReadmailConfigList(List<ReadmailConfig> sendmailConfigs) {
            sendmailConfigs.forEach(d -> {
                if (d.getName() != null) {
                    add(d.getName());
                }
            });
        }

        /**
         * Gets the readmail configurations.
         *
         * @return the readmail configurations
         */
        @XmlElement(name="readmail-config")
        public List<String> getReadmailConfigs() {
            return getObjects();
        }
    }

    /**
     * The Class End2endConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="end2end-configs")
    public static class End2endConfigList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new end2end configuration list.
         */
        public End2endConfigList() {}

        /**
         * Instantiates a new end2end configuration list.
         *
         * @param end2endConfigs the end2end configurations
         */
        public End2endConfigList(List<End2endMailConfig> end2endConfigs) {
            end2endConfigs.forEach(d -> {
                if (d.getName() != null) {
                    add(d.getName());
                }
            });
        }

        /**
         * Gets the end2end configurations.
         *
         * @return the end2end configurations
         */
        @XmlElement(name="end2end-config")
        public List<String> getEnd2endConfigs() {
            return getObjects();
        }
    }

    /**
     * After properties set.
     *
     * @throws Exception the exception
     */
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_javamailConfigurationDao, "javamailConfigurationDao must be set!");
        Assert.notNull(m_eventProxy, "eventProxy must be set!");
    }

    /**
     * Gets the default readmail configuration.
     *
     * @return the default readmail configuration
     */
    @GET
    @Path("default/readmail")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDefaultReadmailConfiguration() {
        ReadmailConfig config = m_javamailConfigurationDao.getDefaultReadmailConfig();
        if (config == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(config.getName()).build();
    }

    /**
     * Gets the default sendmail configuration.
     *
     * @return the default sendmail configuration
     */
    @GET
    @Path("default/sendmail")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDefaultSendmailConfiguration() {
        SendmailConfig config = m_javamailConfigurationDao.getDefaultSendmailConfig();
        if (config == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(config.getName()).build();
    }

    /**
     * Sets the default readmail configuration.
     *
     * @param readmailConfigName the readmail configuration name
     * @return the response
     */
    @PUT
    @Path("default/readmail/{readmailConfig}")
    public Response setDefaultReadmailConfiguration(@PathParam("readmailConfig") final String readmailConfigName) {
        m_javamailConfigurationDao.setDefaultReadmailConfig(readmailConfigName);
        return saveConfiguration();
    }

    /**
     * Sets the default sendmail configuration.
     *
     * @param sendmailConfigName the sendmail configuration name
     * @return the response
     */
    @PUT
    @Path("default/sendmail/{sendmailConfig}")
    public Response setDefaultSendmailConfiguration(@PathParam("sendmailConfig") final String sendmailConfigName) {
        m_javamailConfigurationDao.setDefaultSendmailConfig(sendmailConfigName);
        return saveConfiguration();
    }

    /**
     * Gets all the readmail configurations.
     *
     * @return the readmail configuration list
     */
    @GET
    @Path("readmails")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getReadmailConfigurations() {
        ReadmailConfigList readmails = new ReadmailConfigList(m_javamailConfigurationDao.getReadmailConfigs());
        return Response.ok(readmails).build();
    }

    /**
     * Gets all the sendmail configurations.
     *
     * @return the sendmail configuration list
     */
    @GET
    @Path("sendmails")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getSendmailConfigurations() {
        SendmailConfigList sendmails = new SendmailConfigList(m_javamailConfigurationDao.getSendmailConfigs());
        return Response.ok(sendmails).build();
    }

    /**
     * Gets all the end2end configurations.
     *
     * @return the end2end configuration list
     */
    @GET
    @Path("end2ends")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getEnd2endConfigurations() {
        End2endConfigList sendmails = new End2endConfigList(m_javamailConfigurationDao.getEnd2EndConfigs());
        return Response.ok(sendmails).build();
    }

    /**
     * Gets a specific readmail configuration.
     *
     * @param readmailConfig the readmail configuration
     * @return the readmail configuration
     */
    @GET
    @Path("readmails/{readmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ReadmailConfig getReadmailConfiguration(@PathParam("readmailConfig") final String readmailConfig) {
        ReadmailConfig readmail = "default".equals(readmailConfig) ? m_javamailConfigurationDao.getDefaultReadmailConfig() : m_javamailConfigurationDao.getReadMailConfig(readmailConfig);
        if (readmail == null) {
            throw getException(Status.NOT_FOUND, "Readmail configuration {} was not found.", readmailConfig);
        }
        return readmail;
    }

    /**
     * Gets a specific sendmail configuration.
     *
     * @param sendmailConfig the sendmail configuration
     * @return the sendmail configuration
     */
    @GET
    @Path("sendmails/{sendmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public SendmailConfig getSendmailConfiguration(@PathParam("sendmailConfig") final String sendmailConfig) {
        SendmailConfig sendmail = "default".equals(sendmailConfig) ? m_javamailConfigurationDao.getDefaultSendmailConfig() : m_javamailConfigurationDao.getSendMailConfig(sendmailConfig);
        if (sendmail == null) {
            throw getException(Status.NOT_FOUND, "Sendmail configuration {} was not found.", sendmailConfig);
        }
        return sendmail;
    }

    /**
     * Gets a specific end2end mail configuration.
     *
     * @param end2endConfig the end2end configuration
     * @return the end2end mail configuration
     */
    @GET
    @Path("end2ends/{end2endConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public End2endMailConfig getEnd2EndMailConfiguration(@PathParam("end2endConfig") final String end2endConfig) {
        End2endMailConfig end2end = m_javamailConfigurationDao.getEnd2endConfig(end2endConfig);
        if (end2end == null) {
            throw getException(Status.NOT_FOUND, "End2End configuration {} was not found.", end2endConfig);
        }
        return end2end;
    }

    /**
     * Sets the readmail configuration.
     * <p>If there is a readmail configuration with the same name, the existing one will be overridden.</p>
     *
     * @param readmailConfig the readmail configuration
     * @return the response
     */
    @POST
    @Path("readmails")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setReadmailConfiguration(final ReadmailConfig readmailConfig) {
        writeLock();
        try {
            if (readmailConfig == null) {
                throw getException(Status.BAD_REQUEST, "Readmail configuration object cannot be null");
            }
            m_javamailConfigurationDao.addReadMailConfig(readmailConfig);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Sets the sendmail configuration.
     * <p>If there is a sendmail configuration with the same name, the existing one will be overridden.</p>
     *
     * @param sendmailConfig the sendmail configuration
     * @return the response
     */
    @POST
    @Path("sendmails")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setSendmailConfiguration(final SendmailConfig sendmailConfig) {
        writeLock();
        try {
            if (sendmailConfig == null) {
                throw getException(Status.BAD_REQUEST, "Sendmail configuration object cannot be null");
            }
            m_javamailConfigurationDao.addSendMailConfig(sendmailConfig);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Sets the end2end mail configuration.
     * <p>If there is a end2end configuration with the same name, the existing one will be overridden.</p>
     *
     * @param end2endMailConfig the end2end mail configuration
     * @return the response
     */
    @POST
    @Path("end2ends")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setEnd2EndMailConfiguration(final End2endMailConfig end2endMailConfig) {
        writeLock();
        try {
            if (end2endMailConfig == null) {
                throw getException(Status.BAD_REQUEST, "End2End configuration object cannot be null");
            }
            m_javamailConfigurationDao.addEnd2endMailConfig(end2endMailConfig);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update readmail configuration.
     *
     * @param readmailConfigName the readmail configuration name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("readmails/{readmailConfig}")
    public Response updateReadmailConfiguration(@PathParam("readmailConfig") final String readmailConfigName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            ReadmailConfig readmailConfig = getReadmailConfiguration(readmailConfigName);
            if (updateConfiguration(readmailConfig, params)) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update sendmail configuration.
     *
     * @param sendmailConfigName the sendmail configuration name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("sendmails/{sendmailConfig}")
    public Response updateSendmailConfiguration(@PathParam("sendmailConfig") final String sendmailConfigName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            SendmailConfig sendmailConfig = getSendmailConfiguration(sendmailConfigName);
            if (updateConfiguration(sendmailConfig, params)) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update end2end configuration.
     *
     * @param end2endConfigName the end2end configuration name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("end2ends/{end2endConfig}")
    public Response updateEnd2endConfiguration(@PathParam("end2endConfig") final String end2endConfigName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            End2endMailConfig end2endConfig = getEnd2EndMailConfiguration(end2endConfigName);
            if (updateConfiguration(end2endConfig, params)) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes the readmail configuration.
     *
     * @param readmailConfig the readmail configuration name
     * @return the response
     */
    @DELETE
    @Path("readmails/{readmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response removeReadmailConfig(@PathParam("readmailConfig") final String readmailConfig) {
        if (m_javamailConfigurationDao.removeReadMailConfig(readmailConfig)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Removes the sendmail configuration.
     *
     * @param sendmailConfig the sendmail configuration name
     * @return the response
     */
    @DELETE
    @Path("sendmails/{sendmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response removeSendmailConfig(@PathParam("sendmailConfig") final String sendmailConfig) {
        if (m_javamailConfigurationDao.removeSendMailConfig(sendmailConfig)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Removes the end2end configuration.
     *
     * @param end2endConfig the end2end configuration name
     * @return the response
     */
    @DELETE
    @Path("end2ends/{end2endConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response removeEnd2endConfig(@PathParam("end2endConfig") final String end2endConfig) {
        if (m_javamailConfigurationDao.removeEnd2endConfig(end2endConfig)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Update configuration.
     *
     * @param config the configuration object
     * @param params the parameters
     * @return true, if successful
     */
    private boolean updateConfiguration(final Object config, final MultivaluedMapImpl params) {
        boolean modified = false;
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(config);
        for (final String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                final String stringValue = params.getFirst(key);
                final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Saves the configuration.
     *
     * @return the response
     */
    public Response saveConfiguration() {
        writeLock();
        try {
            // FIXME Validate configuration.
            m_javamailConfigurationDao.saveConfiguration();
            EventBuilder eb = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "ReST");
            eb.addParam(EventConstants.PARM_DAEMON_NAME, "EmailNBI");
            m_eventProxy.send(eb.getEvent());
            return Response.noContent().build();
        } catch (Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
        } finally {
            writeUnlock();            
        }
    }

}
