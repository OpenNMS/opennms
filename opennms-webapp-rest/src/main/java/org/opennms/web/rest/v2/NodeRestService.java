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
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restrictions;
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
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
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
public class NodeRestService extends AbstractDaoRestService<OnmsNode,SearchBean,Integer,String> {

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
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class, Aliases.node.toString());

        // 1st level JOINs
        builder.alias("assetRecord", Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Add this alias via a CriteriaBehavior so that we can specify a join condition
        //builder.alias("categories", Aliases.category.toString(), JoinType.LEFT_JOIN);
        // Add this alias via a CriteriaBehavior so that we can specify a join condition
        //builder.alias("ipInterfaces", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("location", Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Add this alias via a CriteriaBehavior so that we can specify a join condition
        //builder.alias("snmpInterfaces", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        // TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties
        //builder.alias(Aliases.ipInterface.prop("monitoredServices"), Aliases.monitoredService.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        // TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties
        //builder.alias(Aliases.monitoredService.prop("serviceType"), Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // Order by label by default
        builder.orderBy("label").desc();

        return builder;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.NODE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.NODE_BEHAVIORS);

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));

        // Use join conditions for one-to-many aliases
        for (Map.Entry<String,CriteriaBehavior<?>> entry : CriteriaBehaviors.IP_INTERFACE_BEHAVIORS.entrySet()) {
            map.put(Aliases.ipInterface.prop(entry.getKey()), new CriteriaBehavior(entry.getValue().getPropertyName(), entry.getValue().getConverter(), (b,v,c,w)-> {
                b.alias(
                    "ipInterfaces",
                    Aliases.ipInterface.toString(),
                    JoinType.LEFT_JOIN,
                    Restrictions.or(Restrictions.eq(Aliases.ipInterface.prop(entry.getKey()), v), Restrictions.isNull(Aliases.ipInterface.prop(entry.getKey())))
                );
            }));
        }
        // Also add behaviors for the String properties (which is not normally necessary
        // but is necessary here because they add BeforeVisit operations to add JOINs)
        for (String prop : new String[] { "ipHostName", "isManaged" } ) {
            map.put(Aliases.ipInterface.prop(prop), new CriteriaBehavior<>((String)null, String::new, (b,v,c,w)-> {
                b.alias(
                    "ipInterfaces",
                    Aliases.ipInterface.toString(),
                    JoinType.LEFT_JOIN,
                    Restrictions.or(Restrictions.eq(Aliases.ipInterface.prop(prop), v), Restrictions.isNull(Aliases.ipInterface.prop(prop)))
                );
            }));
        }

        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));

        // Use join conditions for one-to-many aliases
        for (Map.Entry<String,CriteriaBehavior<?>> entry : CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS.entrySet()) {
            map.put(Aliases.snmpInterface.prop(entry.getKey()), new CriteriaBehavior(entry.getValue().getPropertyName(), entry.getValue().getConverter(), (b,v,c,w)-> {
                b.alias(
                    "snmpInterfaces",
                    Aliases.snmpInterface.toString(),
                    JoinType.LEFT_JOIN,
                    Restrictions.or(Restrictions.eq(Aliases.snmpInterface.prop(entry.getKey()), v), Restrictions.isNull(Aliases.snmpInterface.prop(entry.getKey())))
                );
            }));
        }
        // There are no extra String properties on node.snmpInterfaces 

        // TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties

        return map;
    }

    @Override
    protected JaxbListWrapper<OnmsNode> createListWrapper(Collection<OnmsNode> list) {
        return new OnmsNodeList(list);
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
        // See NMS-9855
        if (object.getAssetRecord() != null && object.getAssetRecord().getNode() == null) {
            object.getAssetRecord().setNode(object);
        }
        final Integer id = getDao().save(object);
        final Event e = EventUtils.createNodeAddedEvent("Rest", id, object.getLabel(), object.getLabelSource());
        sendEvent(e);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsNode targetObject, MultivaluedMapImpl params) {
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
