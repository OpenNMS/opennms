/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgt.events.rest;

import org.opennms.web.rest.model.v2.EventCollectionDTO;
import org.opennms.web.rest.model.v2.EventDTO;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;

@Path("events")
public interface EventRestService {

    /**
     * <p>
     * getEvent
     * </p>
     * 
     * @param eventId
     *            a {@link String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{eventId}")
    @RolesAllowed({"user", "admin"})
    EventDTO getEvent(@PathParam("eventId") Integer eventId);

    /**
     * returns a plaintext string being the number of events
     *
     * @return a {@link String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @RolesAllowed({"user", "admin"})
    String getCount();

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     *
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @RolesAllowed({"user", "admin"})
    EventCollectionDTO getEvents(@Context UriInfo uriInfo) throws ParseException;

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     *
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("between")
    @RolesAllowed({"user", "admin"})
    EventCollectionDTO getEventsBetween(@Context UriInfo uriInfo) throws ParseException;

    /**
     * Updates the event with id "eventid" If the "ack" parameter is "true",
     * then acks the events as the current logged in user, otherwise unacks
     * the events
     *
     * @param eventId
     *            a {@link Integer} object.
     * @param ack
     *            a {@link Boolean} object.
     */
    @PUT
    @Path("{eventId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed({"admin"})
    Response updateEvent(@Context SecurityContext securityContext, @PathParam("eventId") Integer eventId, @FormParam("ack") Boolean ack);

    /**
     * Updates all the events that match any filter/query supplied in the
     * form. If the "ack" parameter is "true", then acks the events as the
     * current logged in user, otherwise unacks the events
     * 
     * @param formProperties
     *            Map of the parameters passed in by form encoding
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed({"admin"})
    Response updateEvents(@Context SecurityContext securityContext, MultivaluedHashMap<String, String> formProperties);


    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @RolesAllowed({"admin"})
    Response publishEvent(final org.opennms.netmgt.xml.event.Event event);
}
