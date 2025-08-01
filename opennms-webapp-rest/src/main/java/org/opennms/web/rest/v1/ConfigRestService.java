/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v1;

import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.web.rest.v1.config.AgentConfigurationResource;
import org.opennms.web.rest.v1.config.DataCollectionConfigResource;
import org.opennms.web.rest.v1.config.EmailNorthbounderConfigurationResource;
import org.opennms.web.rest.v1.config.JavamailConfigurationResource;
import org.opennms.web.rest.v1.config.JmxDataCollectionConfigResource;
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
@Tag(name = "Config", description = "Config API")
public class ConfigRestService extends OnmsRestService {

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
