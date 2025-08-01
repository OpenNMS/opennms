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
package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeIpInterfacesRestService extends AbstractNodeDependentRestService<OnmsIpInterface,OnmsIpInterface,Integer,String> {

    @Autowired
    private IpInterfaceDao m_dao;

    @Override
    protected IpInterfaceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsIpInterface> getDaoClass() {
        return OnmsIpInterface.class;
    }

    @Override
    protected Class<OnmsIpInterface> getQueryBeanClass() {
        return OnmsIpInterface.class;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.IP_INTERFACE_SERVICE_PROPERTIES;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());

        // 1st level JOINs
        builder.alias("snmpInterface", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("monitoredServices", Aliases.monitoredService.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("monitoredService.serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // TODO: Remove this once the join conditions are in place
        builder.distinct();

        updateCriteria(uriInfo, builder);

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsIpInterface> createListWrapper(Collection<OnmsIpInterface> list) {
        return new OnmsIpInterfaceList(list);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsIpInterface ipInterface) {
        OnmsNode node = getNode(uriInfo);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node was not found.");
        } else if (ipInterface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface object cannot be null");
        } else if (ipInterface.getIpAddress() == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress cannot be null");
        } else if (ipInterface.getIpAddress().getAddress() == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress bytes cannot be null");
        }
        node.addIpInterface(ipInterface);
        getDao().save(ipInterface);

        final Event event = EventUtils.createNodeGainedInterfaceEvent("ReST", node.getId(), ipInterface.getIpAddress());
        sendEvent(event);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, ipInterface.getIpAddress().getHostAddress())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsIpInterface targetObject, MultivaluedMapImpl params) {
        if (params.getFirst("ipAddress") != null) {
            throw getException(Status.BAD_REQUEST, "Cannot change the IP address.");
        }
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsIpInterface intf) {
        intf.getNode().getIpInterfaces().remove(intf);
        getDao().delete(intf);
        final Event e = EventUtils.createDeleteInterfaceEvent("ReST", intf.getNodeId(), intf.getIpAddress().getHostAddress(), -1, -1L);
        sendEvent(e);
    }

    @Override
    protected OnmsIpInterface doGet(UriInfo uriInfo, String ipAddress) {
        final OnmsNode node = getNode(uriInfo);
        final OnmsIpInterface iface = node == null ? null : node.getIpInterfaceByIpAddress(ipAddress);
        if (iface != null) {
            getDao().initialize(iface.getSnmpInterface());
        }
		return iface;
    }

    @Path("{id}/services")
    public NodeMonitoredServiceRestService getMonitoredServicesResource(@Context final ResourceContext context) {
        return context.getResource(NodeMonitoredServiceRestService.class);
    }

    protected OnmsIpInterface getInterface(final UriInfo uriInfo, final String ipAddress) {
        final OnmsNode node = getNode(uriInfo);
        return node.getIpInterfaceByIpAddress(ipAddress);
    }

    @GET
    @Path("{ipAddress}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public OnmsMetaDataList getMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") String ipAddress) {
        final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

        if (intf == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find interface " + ipAddress);
        }

        return new OnmsMetaDataList(intf.getMetaData());
    }

    @GET
    @Path("{ipAddress}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public OnmsMetaDataList getMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") String ipAddress, @PathParam("context") String context) {
        final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

        if (intf == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find interface " + ipAddress);
        }

        return new OnmsMetaDataList(intf.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()))
                .collect(Collectors.toList()));
    }

    @GET
    @Path("{ipAddress}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public OnmsMetaDataList getMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") String ipAddress, @PathParam("context") String context, @PathParam("key") String key) {
        final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

        if (intf == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find interface " + ipAddress);
        }

        return new OnmsMetaDataList(intf.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()) && key.equals(e.getKey()))
                .collect(Collectors.toList()));
    }

    @DELETE
    @Path("{ipAddress}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response deleteMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") final String ipAddress, @PathParam("context") final String context) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find interface " + ipAddress);
            }
            intf.removeMetaData(context);
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{ipAddress}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response deleteMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") final String ipAddress, @PathParam("context") String context, @PathParam("key") final String key) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find interface " + ipAddress);
            }
            intf.removeMetaData(context, key);
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("{ipAddress}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response postMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") final String ipAddress, final OnmsMetaData entity) {
        checkUserDefinedMetadataContext(entity.getContext());

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "postMetaData: Can't find interface " + ipAddress);
            }
            intf.addMetaData(entity.getContext(), entity.getKey(), entity.getValue());
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{ipAddress}/metadata/{context}/{key}/{value}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response putMetaData(@Context final UriInfo uriInfo, @PathParam("ipAddress") final String ipAddress, @PathParam("context") final String context, @PathParam("key") final String key, @PathParam("value") final String value) {        checkUserDefinedMetadataContext(context);
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "putMetaData: Can't find interface " + ipAddress);
            }
            intf.addMetaData(context, key, value);
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }
}
