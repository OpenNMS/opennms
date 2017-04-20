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

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.netmgt.model.events.EventBuilder;
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
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.orderBy("eventTime").desc(); // order by event time by default
        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsEvent> createListWrapper(Collection<OnmsEvent> list) {
        return new OnmsEventCollection(list);
    }

    @Override
    protected OnmsEvent doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    protected Response doCreate(UriInfo uriInfo, OnmsEvent event) {
        final Event e = convert(event);
        sendEvent(e);
        return Response.noContent().build();
    }

    // Experimental
    private Event convert(OnmsEvent event) {
        if (event == null || event.getEventUei() == null) {
            throw getException(Status.BAD_REQUEST, "Event UEI is required.");
        }
        EventBuilder builder = new EventBuilder(event.getEventUei(), "ReST");
        if (event.getNodeId() != null) builder.setNodeid(event.getNodeId());
        if (event.getIpAddr() != null) builder.setInterface(event.getIpAddr());
        if (event.getServiceType() != null && event.getServiceType().getName() != null) builder.setService(event.getServiceType().getName());
        if (event.getSeverityLabel() != null) builder.setSeverity(event.getSeverityLabel());
        if (event.getEventTime() == null) builder.setTime(new Date());
        event.getEventParameters().forEach(p -> builder.addParam(p.getName(), p.getValue()));
        return builder.getEvent();
    }
}
