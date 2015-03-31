/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.config;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;
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
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@PerRequest
@Scope("prototype")
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

    @Context
    private ResourceContext m_context;

    @Context 
    private UriInfo m_uriInfo;

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
    public Response getAgentsXmlWithExtension(@PathParam("filterName") final String filterName, @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
        return getAgentsXml(filterName, serviceName);
    }

    @GET
    @Path("{filterName}/{serviceName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response getAgentsXmlWithoutExtension(@PathParam("filterName") final String filterName, @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
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
    public Response getAgentsJsonWithExtension(@PathParam("filterName") final String filterName, @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
        return getAgentsJson(filterName, serviceName);
    }

    @GET
    @Path("{filterName}/{serviceName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentsJsonWithoutExtension(@PathParam("filterName") final String filterName, @PathParam("serviceName") final String serviceName) throws ConfigurationResourceException {
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
            throw new IllegalArgumentException("You must specify a filter name and service name!");
        }

        final Filter filter = m_collectdConfigurationResource.get().getFilter(filterName);
        if (filter == null) {
            LOG.warn("No filter matching {} could be found.", filterName);
            throw new WebApplicationException(404);
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

        final List<AgentResponse> responses = new ArrayList<AgentResponse>();

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
                final SnmpAgentConfig config = m_agentConfigFactory.getAgentConfig(ipAddress);
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
