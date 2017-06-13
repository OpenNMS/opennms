/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsNode} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("nodes")
@Transactional
public class NodeRestService extends AbstractDaoRestService<OnmsNode,Integer,String> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRestService.class);

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    protected NodeDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsNode> getDaoClass() {
        return OnmsNode.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);

        // 1st level JOINs
        builder.alias("assetRecord", "assetRecord", JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("categories", "categories", JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("ipInterfaces", "ipInterfaces", JoinType.LEFT_JOIN);
        builder.alias("location", "location", JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("snmpInterfaces", "snmpInterfaces", JoinType.LEFT_JOIN);

        // 2nd level JOINs
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("ipInterfaces.monitoredServices", "monitoredServices", JoinType.LEFT_JOIN);

        // 3rd level JOINs
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);

        // Order by label by default
        builder.orderBy("label").desc();

        // TODO: Remove this once the join conditions are in place
        builder.distinct();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsNode> createListWrapper(Collection<OnmsNode> list) {
        return new OnmsNodeList(list);
    }

    @Override
    protected Map<String, String> getBeanPropertiesMapping() {
        final Map<String, String> map = new HashMap<>();
        map.put("categoryName", "categories.name");
        return map;
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsNode object) {
        if (object == null) {
            throw getException(Status.BAD_REQUEST, "Node object cannot be null");
        }
        if (object.getLocation() == null) {
            OnmsMonitoringLocation location = m_locationDao.getDefaultLocation();
            LOG.debug("doCreate: Assigning new node to default location: {}", location.getLocationName());
            object.setLocation(location);
        }
        final Integer id = getDao().save(object);
        final Event e = EventUtils.createNodeAddedEvent("Rest", id, object.getLabel(), object.getLabelSource());
        sendEvent(e);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected Response doUpdate(SecurityContext securityContext, UriInfo uriInfo, OnmsNode targetObject, MultivaluedMapImpl params) {
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsNode node) {
        getDao().delete(node);
        final Event e = EventUtils.createDeleteNodeEvent("ReST", node.getId(), -1L);
        sendEvent(e);
    }

    @Override
    protected OnmsNode doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    @Path("{nodeCriteria}/ipinterfaces")
    public NodeIpInterfacesRestService getIpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(NodeIpInterfacesRestService.class);
    }

    @Path("{nodeCriteria}/snmpinterfaces")
    public NodeSnmpInterfacesRestService getSnmpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(NodeSnmpInterfacesRestService.class);
    }

    @Path("{nodeCriteria}/hardwareInventory")
    public NodeHardwareInventoryRestService getHardwareInventoryResource(@Context final ResourceContext context) {
        return context.getResource(NodeHardwareInventoryRestService.class);
    }

    @Path("{nodeCriteria}/categories")
    public NodeCategoriesRestService getCategoriesResource(@Context final ResourceContext context) {
        return context.getResource(NodeCategoriesRestService.class);
    }

}
