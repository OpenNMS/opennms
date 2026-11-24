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
package org.opennms.web.rest.v1.config;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.config.agents.AgentResponse;
import org.opennms.netmgt.config.agents.AgentResponseCollection;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Component("agentConfigurationResource")
public class AgentConfigurationResource implements InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(AgentConfigurationResource.class);

    @Resource(name="collectd-configuration.xml")
    private ConfigurationResource<CollectdConfiguration> m_collectdConfigurationResource;

    @Autowired
    private FilterDao m_filterDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private SnmpAgentConfigFactory m_agentConfigFactory;

    public void setCollectdConfigurationResource(final ConfigurationResource<CollectdConfiguration> resource) {
        m_collectdConfigurationResource = resource;
    }

    public void setFilterDao(final FilterDao dao) {
        m_filterDao = dao;
    }

    public void setMonitoredServiceDao(final MonitoredServiceDao dao) {
        m_monitoredServiceDao = dao;
    }

    public void setAgentConfigFactory(final SnmpAgentConfigFactory factory) {
        m_agentConfigFactory = factory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_collectdConfigurationResource, "CollectdConfigurationResource must not be null.");
        Assert.notNull(m_filterDao, "FilterDao must not be null.");
        Assert.notNull(m_monitoredServiceDao, "MonitoredServiceDao must not be null.");
        Assert.notNull(m_agentConfigFactory, "SnmpConfigDao must not be null.");
    }

    @GET
    @Path("{filterName}/{serviceName}.xml")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List collectable agents for a filter and service (XML)",
            description = """
                    `filterName` is matched first against the `name` attribute of a collectd `filter`, then against
                    the enclosing `package` name, so a package whose filter is unnamed can be addressed by the
                    package name. The filter rule is evaluated against the current database to get the matching
                    interfaces, and those are then narrowed to the ones that actually have `serviceName`
                    monitored.

                    Each entry carries the collectd service parameters verbatim, so `${requisition:...}`
                    placeholders are returned unexpanded. `nodeId`, `foreignSource` and `foreignId` are added
                    when known. For `SNMP` the port comes from snmp-config.xml for that address and location
                    rather than from the collectd `port` parameter, and `sysObjectId` is added when the node
                    has one.

                    The XML form wraps the entries in an `agents` element carrying `count`, `offset` and
                    `totalCount` attributes; the map is rendered as repeated `entry` elements.""",
            operationId = "getAgentsForFilterXml")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One entry per matching agent.",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = AgentResponseCollection.class),
                            examples = @ExampleObject(value = """
                                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <agents count="1" offset="0" totalCount="1">
                                              <agent>
                                                <address>127.0.0.1</address>
                                                <port>1161</port>
                                                <serviceName>SNMP</serviceName>
                                                <parameters>
                                                  <entry>
                                                    <key>collection</key>
                                                    <value>${requisition:collection|detector:collection|default}</value>
                                                  </entry>
                                                  <entry>
                                                    <key>nodeId</key>
                                                    <value>2</value>
                                                  </entry>
                                                </parameters>
                                              </agent>
                                            </agents>"""))),
            @ApiResponse(responseCode = "204", description = "The filter matched, but no interface has that service monitored. Bodiless."),
            @ApiResponse(responseCode = "404", description = "No collectd filter or package is named `filterName`. Bodiless."),
            @ApiResponse(responseCode = "500", description = "`serviceName` is not configured on the matched package. The body is the plain string `Service name not part of package!`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service name not part of package!")))
    })
    public Response getAgentsXmlWithExtension(@io.swagger.v3.oas.annotations.Parameter(description = "Name of a collectd filter, or of the collectd package that owns it.", required = true, example = "example1") @PathParam("filterName") final String filterName, @io.swagger.v3.oas.annotations.Parameter(description = "Name of the monitored service to restrict the result to. Must be one of the services configured on that package.", required = true, example = "SNMP") @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
        return getAgentsXml(filterName, serviceName);
    }

    @GET
    @Path("{filterName}/{serviceName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List collectable agents for a filter and service",
            description = """
                    `filterName` is matched first against the `name` attribute of a collectd `filter`, then against
                    the enclosing `package` name, so a package whose filter is unnamed can be addressed by the
                    package name. The filter rule is evaluated against the current database to get the matching
                    interfaces, and those are then narrowed to the ones that actually have `serviceName`
                    monitored.

                    Each entry carries the collectd service parameters verbatim, so `${requisition:...}`
                    placeholders are returned unexpanded. `nodeId`, `foreignSource` and `foreignId` are added
                    when known. For `SNMP` the port comes from snmp-config.xml for that address and location
                    rather than from the collectd `port` parameter, and `sysObjectId` is added when the node
                    has one.

                    This path is served by two handlers selected by content negotiation: `Accept: application/xml`
                    (or application/atom+xml) returns the wrapped XML form, `Accept: application/json` returns the
                    bare JSON array. Only one of the two appears in this document, so the JSON response of this
                    path is documented on `/config/agents/{filterName}/{serviceName}.json` instead.

                    The XML form wraps the entries in an `agents` element carrying `count`, `offset` and
                    `totalCount` attributes; the map is rendered as repeated `entry` elements.""",
            operationId = "getAgentsForFilter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One entry per matching agent.",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = AgentResponseCollection.class),
                            examples = @ExampleObject(value = """
                                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <agents count="1" offset="0" totalCount="1">
                                              <agent>
                                                <address>127.0.0.1</address>
                                                <port>1161</port>
                                                <serviceName>SNMP</serviceName>
                                                <parameters>
                                                  <entry>
                                                    <key>collection</key>
                                                    <value>${requisition:collection|detector:collection|default}</value>
                                                  </entry>
                                                  <entry>
                                                    <key>nodeId</key>
                                                    <value>2</value>
                                                  </entry>
                                                </parameters>
                                              </agent>
                                            </agents>"""))),
            @ApiResponse(responseCode = "204", description = "The filter matched, but no interface has that service monitored. Bodiless."),
            @ApiResponse(responseCode = "404", description = "No collectd filter or package is named `filterName`. Bodiless."),
            @ApiResponse(responseCode = "500", description = "`serviceName` is not configured on the matched package. The body is the plain string `Service name not part of package!`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service name not part of package!")))
    })
    public Response getAgentsXmlWithoutExtension(@io.swagger.v3.oas.annotations.Parameter(description = "Name of a collectd filter, or of the collectd package that owns it.", required = true, example = "example1") @PathParam("filterName") final String filterName, @io.swagger.v3.oas.annotations.Parameter(description = "Name of the monitored service to restrict the result to. Must be one of the services configured on that package.", required = true, example = "SNMP") @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
        return getAgentsXml(filterName, serviceName);
    }

    public Response getAgentsXml(final String filterName, final String serviceName) throws ConfigurationResourceException {
        final List<AgentResponse> responses = getResponses(filterName, serviceName);

        if (responses.size() == 0) {
            return Response.noContent().build();
        }

        return Response.ok(new AgentResponseCollection(responses)).build();
    }

    @GET
    @Path("{filterName}/{serviceName}.json")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List collectable agents for a filter and service (JSON)",
            description = """
                    `filterName` is matched first against the `name` attribute of a collectd `filter`, then against
                    the enclosing `package` name, so a package whose filter is unnamed can be addressed by the
                    package name. The filter rule is evaluated against the current database to get the matching
                    interfaces, and those are then narrowed to the ones that actually have `serviceName`
                    monitored.

                    Each entry carries the collectd service parameters verbatim, so `${requisition:...}`
                    placeholders are returned unexpanded. `nodeId`, `foreignSource` and `foreignId` are added
                    when known. For `SNMP` the port comes from snmp-config.xml for that address and location
                    rather than from the collectd `port` parameter, and `sysObjectId` is added when the node
                    has one.

                    The JSON form is a bare array with no wrapper, and each `parameters` map is rendered as a
                    JAXB-style `{"entry": [{"key": ..., "value": ...}]}` object rather than as a plain
                    object.""",
            operationId = "getAgentsForFilterJson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One entry per matching agent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = AgentResponse.class)),
                            examples = @ExampleObject(value = """
                                    [
                                              {
                                                "address": "127.0.0.1",
                                                "port": 1161,
                                                "serviceName": "SNMP",
                                                "parameters": {
                                                  "entry": [
                                                    {
                                                      "key": "collection",
                                                      "value": "${requisition:collection|detector:collection|default}"
                                                    },
                                                    {
                                                      "key": "nodeId",
                                                      "value": "2"
                                                    }
                                                  ]
                                                }
                                              }
                                            ]"""))),
            @ApiResponse(responseCode = "204", description = "The filter matched, but no interface has that service monitored. Bodiless."),
            @ApiResponse(responseCode = "404", description = "No collectd filter or package is named `filterName`. Bodiless."),
            @ApiResponse(responseCode = "500", description = "`serviceName` is not configured on the matched package. The body is the plain string `Service name not part of package!`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service name not part of package!")))
    })
    public Response getAgentsJsonWithExtension(@io.swagger.v3.oas.annotations.Parameter(description = "Name of a collectd filter, or of the collectd package that owns it.", required = true, example = "example1") @PathParam("filterName") final String filterName, @io.swagger.v3.oas.annotations.Parameter(description = "Name of the monitored service to restrict the result to. Must be one of the services configured on that package.", required = true, example = "SNMP") @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
        return getAgentsJson(filterName, serviceName);
    }

    @GET
    @Path("{filterName}/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List collectable agents for a filter and service (JSON, negotiated)",
            description = """
                    Identical to `/config/agents/{filterName}/{serviceName}.json`, reached by sending
                    `Accept: application/json` to the extensionless path. Two handlers share that path and only
                    one of them appears in this document.

                    `filterName` is matched first against the `name` attribute of a collectd `filter`, then against
                    the enclosing `package` name, so a package whose filter is unnamed can be addressed by the
                    package name. The filter rule is evaluated against the current database to get the matching
                    interfaces, and those are then narrowed to the ones that actually have `serviceName`
                    monitored.

                    Each entry carries the collectd service parameters verbatim, so `${requisition:...}`
                    placeholders are returned unexpanded. `nodeId`, `foreignSource` and `foreignId` are added
                    when known. For `SNMP` the port comes from snmp-config.xml for that address and location
                    rather than from the collectd `port` parameter, and `sysObjectId` is added when the node
                    has one.

                    The JSON form is a bare array with no wrapper, and each `parameters` map is rendered as a
                    JAXB-style `{"entry": [{"key": ..., "value": ...}]}` object rather than as a plain
                    object.""",
            operationId = "getAgentsForFilterNegotiatedJson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One entry per matching agent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = AgentResponse.class)),
                            examples = @ExampleObject(value = """
                                    [
                                              {
                                                "address": "127.0.0.1",
                                                "port": 1161,
                                                "serviceName": "SNMP",
                                                "parameters": {
                                                  "entry": [
                                                    {
                                                      "key": "collection",
                                                      "value": "${requisition:collection|detector:collection|default}"
                                                    },
                                                    {
                                                      "key": "nodeId",
                                                      "value": "2"
                                                    }
                                                  ]
                                                }
                                              }
                                            ]"""))),
            @ApiResponse(responseCode = "204", description = "The filter matched, but no interface has that service monitored. Bodiless."),
            @ApiResponse(responseCode = "404", description = "No collectd filter or package is named `filterName`. Bodiless."),
            @ApiResponse(responseCode = "500", description = "`serviceName` is not configured on the matched package. The body is the plain string `Service name not part of package!`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service name not part of package!")))
    })
    public Response getAgentsJsonWithoutExtension(@io.swagger.v3.oas.annotations.Parameter(description = "Name of a collectd filter, or of the collectd package that owns it.", required = true, example = "example1") @PathParam("filterName") final String filterName, @io.swagger.v3.oas.annotations.Parameter(description = "Name of the monitored service to restrict the result to. Must be one of the services configured on that package.", required = true, example = "SNMP") @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
        return getAgentsJson(filterName, serviceName);
    }

    public Response getAgentsJson(final String filterName, final String serviceName) throws ConfigurationResourceException {
        final List<AgentResponse> responses = getResponses(filterName, serviceName);

        if (responses.size() == 0) {
            return Response.noContent().build();
        }

        return Response.ok(new GenericEntity<List<AgentResponse>>(responses){}).build();
    }

    protected List<AgentResponse> getResponses(final String filterName, final String serviceName) throws ConfigurationResourceException {
        LOG.debug("getAgentsForService(): filterName={}, serviceName={}", filterName, serviceName);

        if (filterName == null || serviceName == null) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("You must specify a filter name and service name!").build());
        }

        final Filter filter = m_collectdConfigurationResource.get().getFilter(filterName);
        if (filter == null) {
            LOG.warn("No filter matching {} could be found.", filterName);
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        final List<InetAddress> addresses = m_filterDao.getActiveIPAddressList(filter.getContent());
        LOG.debug("Matched {} IP addresses for filter {}", addresses == null? 0 : addresses.size(), filterName);

        if (addresses == null || addresses.size() == 0) {
            return Collections.emptyList();
        }

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class);
        builder.createAlias("ipInterface", "iface");
        builder.createAlias("serviceType", "type");
        builder.createAlias("iface.node", "node");
        builder.in("iface.ipAddress", addresses);
        builder.eq("type.name", serviceName);
        final List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(builder.toCriteria());
        int defaultPort = -1;

        // TODO: We shouldn't have to hardcode like this; what's the right way to know the port to return?
        final CollectdConfiguration collectdConfiguration = m_collectdConfigurationResource.get();
        org.opennms.netmgt.config.collectd.Package pack = collectdConfiguration.getPackage(filterName);
        if (pack == null) {
            for (final org.opennms.netmgt.config.collectd.Package p : collectdConfiguration.getPackages()) {
                if (filterName.equals(p.getFilter().getName())) {
                    pack = p;
                    break;
                }
            }
        }
        if (pack != null) {
            final Service svc = pack.getService(serviceName);
            final String port = svc.getParameter("port");
            if (port != null) {
                try {
                    defaultPort = Integer.valueOf(port);
                } catch (final NumberFormatException e) {
                    LOG.debug("Unable to turn port {} from service {} into a number.", port, serviceName);
                }
            }
        }

        final List<AgentResponse> responses = new ArrayList<>();

        for (final OnmsMonitoredService service : services) {
            final InetAddress ipAddress = service.getIpAddress();
            final OnmsIpInterface iface = service.getIpInterface();
            OnmsNode node = null;
            if (iface != null) {
                node = iface.getNode();
            }
            final Map<String,String> parameters = new TreeMap<String,String>();

            // all service parameters from collectd configuration to parameters map
            for (Parameter eachParameter : pack.getService(serviceName).getParameters()) {
                parameters.put(eachParameter.getKey(), eachParameter.getValue());
            }

            int port = defaultPort;
            if ("SNMP".equals(serviceName)) {
                final String sysObjectId = node == null? null : node.getSysObjectId();
                if (sysObjectId != null) {
                    parameters.put("sysObjectId", sysObjectId);
                }
                OnmsMonitoringLocation location = (node == null) ? null : node.getLocation();
                String locationName = (location == null) ? null : location.getLocationName();

                final SnmpAgentConfig config = m_agentConfigFactory.getAgentConfig(ipAddress, locationName);
                if (config != null) {
                    port = config.getPort();
                }
            }
            if (node != null) {
                if (node.getNodeId() != null && !node.getNodeId().trim().isEmpty()) {
                    parameters.put("nodeId", node.getNodeId());
                }
                if (node.getForeignSource() != null && !node.getForeignSource().trim().isEmpty()) {
                    parameters.put("foreignSource", node.getForeignSource());
                }
                if (node.getForeignId() != null && !node.getForeignId().trim().isEmpty()) {
                    parameters.put("foreignId", node.getForeignId());
                }
            }

            responses.add(new AgentResponse(ipAddress, port, service.getServiceName(), parameters));
        }
        return responses;
    }
}
