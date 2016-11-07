/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.web.rest.v1.config.AgentConfigurationResource;
import org.opennms.web.rest.v1.config.CollectionConfigurationResource;
import org.opennms.web.rest.v1.config.DataCollectionConfigResource;
import org.opennms.web.rest.v1.config.EmailNorthbounderConfigurationResource;
import org.opennms.web.rest.v1.config.JavamailConfigurationResource;
import org.opennms.web.rest.v1.config.JmxDataCollectionConfigResource;
import org.opennms.web.rest.v1.config.PollerConfigurationResource;
import org.opennms.web.rest.v1.config.SnmpConfigurationResource;
import org.opennms.web.rest.v1.config.SnmpTrapNorthbounderConfigurationResource;
import org.opennms.web.rest.v1.config.SyslogNorthbounderConfigurationResource;
import org.opennms.web.rest.v1.config.TrapdConfigurationResource;
import org.springframework.stereotype.Component;

/**
 * ReST service for (JAXB) ConfigurationResource files.
 */

@Component("configRestService")
@Path("config")
public class ConfigRestService extends OnmsRestService {

    @Path("{location}/polling")
    public PollerConfigurationResource getPollerConfiguration(@Context final ResourceContext context) {
        return context.getResource(PollerConfigurationResource.class);
    }

    @Path("{location}/collection")
    public CollectionConfigurationResource getCollectionConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(CollectionConfigurationResource.class);
    }

    @Path("datacollection")
    public DataCollectionConfigResource getDatacollectionConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(DataCollectionConfigResource.class);
    }

    @Path("agents")
    public AgentConfigurationResource getAgentConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(AgentConfigurationResource.class);
    }

    @Path("snmp")
    public SnmpConfigurationResource getSnmpConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(SnmpConfigurationResource.class);
    }
    
    @Path("trapd")
    public TrapdConfigurationResource getTrapdConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(TrapdConfigurationResource.class);
    }
    
    @Path("jmx")
    public JmxDataCollectionConfigResource getJmxDataCollectionConfigResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(JmxDataCollectionConfigResource.class);
    }

    @Path("javamail")
    public JavamailConfigurationResource getJavamailConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(JavamailConfigurationResource.class);
    }

    @Path("syslog-nbi")
    public SyslogNorthbounderConfigurationResource getSyslogNorthbounderConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(SyslogNorthbounderConfigurationResource.class);
    }

    @Path("snmptrap-nbi")
    public SnmpTrapNorthbounderConfigurationResource getSnmpTrapNorthbounderConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(SnmpTrapNorthbounderConfigurationResource.class);
    }

    @Path("email-nbi")
    public EmailNorthbounderConfigurationResource getEmailNorthbounderConfigurationResource(@Context final ResourceContext context) throws ConfigurationResourceException {
        return context.getResource(EmailNorthbounderConfigurationResource.class);
    }

}