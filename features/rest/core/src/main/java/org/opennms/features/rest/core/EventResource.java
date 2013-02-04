package org.opennms.features.rest.core;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.ws.rs.ext.Providers;

import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.NodeDao;

import org.opennms.netmgt.model.OnmsEvent;

@Path("/events")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class EventResource {

    /** injected by BLUEPRINT */
    private NodeDao nodeDao;

    /** injected by BLUEPRINT */
    private EventDao eventDao;

    /** injected by JAX-RS framework */
    @Context
    private UriInfo uriInfo;

    /** injected by JAX-RS framework */
    @Context
    private HttpHeaders httpHeaders;

    /** injected by JAX-RS framework */
    @Context
    private SecurityContext securityContext;

    /** injected by JAX-RS framework */
    @Context
    private Request request;

    /** injected by JAX-RS framework */
    @Context
    private Providers providers;

    @GET
    public List<OnmsEvent> getEvents() {
        return eventDao.findAll();
    }

    @GET
    @Path("{eventId}")
    public OnmsEvent getEventById(@PathParam("eventId") final Integer eventId) {
        return eventDao.get(eventId);
    }

    @GET
    @Path("severity/{eventSeverity}")
    public OnmsEvent getEventsBySeverity(@PathParam("eventSeverity") final String eventSeverity) {
        //TODO all event with the given severity
        return eventDao.get(1);
    }

    @GET
    @Path("timeframe/{beginTime}/{endTime}")
    public OnmsEvent getEventsFromTimeframe(@PathParam("beginTime") final String beginTime, @PathParam("endTime") final String endTime) {
        //TODO all events with an eventtime between beginTime and endTime
        return eventDao.get(2);
    }

    @GET
    @Path("timeframe/{beginTime}/{endTime}/severity/{eventSeverity}")
    public OnmsEvent getEventsFromTimeframe(@PathParam("beginTime") final String beginTime, @PathParam("endTime") final String endTime, @PathParam("eventSeverity") final String eventSeverity) {
        //TODO all events with an eventtime between beginTime and endTime with the given severity
        uriInfo.getQueryParameters();
        return eventDao.get(3);
    }

    /** required for injection via BLUEPRINT */
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    /** required for injection via BLUEPRINT */
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }
}
