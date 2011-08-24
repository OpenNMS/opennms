/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * <p>ScheduledOutagesRestService class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("sched-outages")
public class ScheduledOutagesRestService extends OnmsRestService {

    @Autowired
    protected PollOutagesConfigFactory m_configFactory;

    @Autowired
    protected EventProxy m_eventProxy;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Outages getOutages() {
        Outages outages = new Outages();
        outages.setOutage(m_configFactory.getOutages());
        return outages;
    }

    @GET
    @Path("{outageName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Outage getOutages(@PathParam("outageName") String outageName) {
        return m_configFactory.getOutage(outageName);
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveOrUpdateOutage(Outage newOutage) {
        try {
            Outage oldOutage = m_configFactory.getOutage(newOutage.getName());
            if (oldOutage == null) {
                log().debug("saveOrUpdateOutage: adding outage " + newOutage.getName());
                m_configFactory.addOutage(newOutage);
            } else {
                log().debug("saveOrUpdateOutage: updating outage " + newOutage.getName());
                m_configFactory.replaceOutage(oldOutage, newOutage);
            }
            m_configFactory.saveCurrent();
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't save or update the scheduled outage " + newOutage.getName() + " because, " + e.getMessage());
        }
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @DELETE
    @Path("{outageName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteOutage(@PathParam("outageName") String outageName) {
        try {
            log().debug("deleteOutage: deleting outage " + outageName);
            m_configFactory.removeOutage(outageName);
            m_configFactory.saveCurrent();
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't delete the scheduled outage " + outageName + " because, " + e.getMessage());
        }
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    private void sendConfigChangedEvent() {
        EventBuilder builder = new EventBuilder(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI, "Web UI");
        try {
            m_eventProxy.send(builder.getEvent());
        } catch (Throwable e) {
            throwException(Status.BAD_REQUEST, "Could not send event " + builder.getEvent().getUei() + " because, " + e.getMessage());
        }
    }

}
