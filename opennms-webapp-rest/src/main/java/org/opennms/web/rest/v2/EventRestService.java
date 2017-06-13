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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.netmgt.xml.event.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsEvent} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("events")
@Transactional
public class EventRestService extends AbstractDaoRestService<OnmsEvent,Integer,Integer> {

    @Autowired
    private EventDao m_dao;

    @Override
    protected EventDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsEvent> getDaoClass() {
        return OnmsEvent.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass(), "event");

        // 1st level JOINs
        builder.alias("alarm", "alarm", JoinType.LEFT_JOIN);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias("node.assetRecord", "assetRecord", JoinType.LEFT_JOIN);
        builder.alias("node.categories", "categories", JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias("node.ipInterfaces", "ipInterfaces", JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty("ipInterfaces.ipAddress", "event.ipAddr"), Restrictions.isNull("ipInterfaces.ipAddress")));
        builder.alias("node.location", "location", JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias("node.snmpInterfaces", "snmpInterfaces", JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty("snmpInterfaces.ifIndex", "event.ifIndex"), Restrictions.isNull("snmpInterfaces.ifIndex")));

        builder.orderBy("eventTime").desc(); // order by event time by default

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsEvent> createListWrapper(Collection<OnmsEvent> list) {
        return new OnmsEventCollection(list);
    }

    @Override
    protected Map<String, String> getBeanPropertiesMapping() {
        final Map<String, String> map = new HashMap<>();
        map.put("uei", "eventUei");
        map.put("nodeLabel", "node.label");
        map.put("categoryName", "node.categories.name");
        return map;
    }

    @Override
    protected Map<String, String> getCriteriaPropertiesMapping() {
        final Map<String, String> map = new HashMap<>();
        map.put("node.categories.name", "categories.name");
        return map;
    }

    @Override
    protected OnmsEvent doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, Event event) {
        if (event.getTime() == null) event.setTime(new Date());
        if (event.getSource() == null) event.setSource("ReST");

        sendEvent(event);
        return Response.noContent().build();
    }

}
