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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The Class JavamailConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class JavamailConfigurationResource implements InitializingBean {

    /** The Javamail configuration DAO. */
    @Resource(name="javamailConfigDao")
    private JavaMailConfigurationDao m_javamailConfigurationDao;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_javamailConfigurationDao, "javamailConfigurationDao must be set!");
    }

    /**
     * Gets the readmail configuration.
     *
     * @param readmailConfig the readmail configuration
     * @return the readmail configuration
     */
    @GET
    @Path("readmail/{readmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getReadmailConfiguration(@PathParam("readmailConfig") String readmailConfig) {
        ReadmailConfig readmail = m_javamailConfigurationDao.getReadMailConfig(readmailConfig);
        if (readmail == null) {
            return Response.status(404).build();
        }
        return Response.ok(readmail).build();
    }

    /**
     * Gets the sendmail configuration.
     *
     * @param sendmailConfig the sendmail configuration
     * @return the sendmail configuration
     */
    @GET
    @Path("sendmail/{sendmailConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getSendmailConfiguration(@PathParam("sendmailConfig") String sendmailConfig) {
        SendmailConfig sendmail = m_javamailConfigurationDao.getSendMailConfig(sendmailConfig);
        if (sendmail == null) {
            return Response.status(404).build();
        }
        return Response.ok(sendmail).build();
    }

    /**
     * Gets the end2 end mail configuration.
     *
     * @param end2endConfig the end2end configuration
     * @return the end2 end mail configuration
     */
    @GET
    @Path("end2end/{end2endConfig}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getEnd2EndMailConfiguration(@PathParam("end2endConfig") String end2endConfig) {
        End2endMailConfig end2end = m_javamailConfigurationDao.getEnd2EndConfig(end2endConfig);
        if (end2end == null) {
            return Response.status(404).build();
        }
        return Response.ok(end2end).build();
    }

    /**
     * Sets the readmail configuration.
     *
     * @param readmailConfig the readmail configuration
     * @return the response
     */
    @POST
    @Path("readmail/{readmailConfig}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setReadmailConfiguration(ReadmailConfig readmailConfig) {
        m_javamailConfigurationDao.addReadMailConfig(readmailConfig);
        m_javamailConfigurationDao.saveConfiguration();
        return Response.ok().build();
    }

    /**
     * Sets the sendmail configuration.
     *
     * @param sendmailConfig the sendmail configuration
     * @return the response
     */
    @POST
    @Path("sendmail/{sendmailConfig}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setSendmailConfiguration(SendmailConfig sendmailConfig) {
        m_javamailConfigurationDao.addSendMailConfig(sendmailConfig);
        m_javamailConfigurationDao.saveConfiguration();
        return Response.ok().build();
    }

    /**
     * Sets the end2 end mail configuration.
     *
     * @param end2endMailConfig the end2end mail configuration
     * @return the response
     */
    @POST
    @Path("end2end/{end2endConfig}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response setEnd2EndMailConfiguration(End2endMailConfig end2endMailConfig) {
        m_javamailConfigurationDao.addEnd2endMailConfig(end2endMailConfig);
        m_javamailConfigurationDao.saveConfiguration();
        return Response.ok().build();
    }

}
