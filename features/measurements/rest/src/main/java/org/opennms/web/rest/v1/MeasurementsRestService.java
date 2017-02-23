/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.opennms.netmgt.measurements.api.FilterEngine;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.ExpressionException;
import org.opennms.netmgt.measurements.api.exceptions.FetchException;
import org.opennms.netmgt.measurements.api.exceptions.FilterException;
import org.opennms.netmgt.measurements.api.exceptions.ResourceNotFoundException;
import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.FilterMetaData;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Measurements API provides read-only access to values
 * persisted by the collectors.
 *
 * Measurements are referenced by combination of resource id
 * and attribute name.
 *
 * Calculations may then be performed on these measurements
 * using JEXL expressions.
 *
 * Units of time, including timestamps are expressed in milliseconds.
 *
 * This API is designed to be similar to the one provided
 * by Newts.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
@Component
@Scope("prototype")
@Path("measurements")
public class MeasurementsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementsRestService.class);

    @Autowired
    private MeasurementsService service;

    @Autowired
    private FilterEngine filterEngine;

    @GET
    @Path("filters")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public List<FilterMetaData> getFilterMetadata() {
        return filterEngine.getFilterMetaData();
    }

    @GET
    @Path("filters/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public FilterMetaData getFilterMetadata(@PathParam("name") final String name) {
        FilterMetaData metaData = filterEngine.getFilterMetaData(name);
        if (metaData == null) {
            throw getException(Status.NOT_FOUND, "No filter with name '{}' was found.", name);
        }
        return metaData;
    }

    /**
     * Retrieves the measurements for a single attribute.
     */
    @GET
    @Path("{resourceId}/{attribute}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public QueryResponse simpleQuery(@PathParam("resourceId") final String resourceId,
            @PathParam("attribute") final String attribute,
            @DefaultValue("-14400000") @QueryParam("start") final long start,
            @DefaultValue("0") @QueryParam("end") final long end,
            @DefaultValue("300000") @QueryParam("step") final long step,
            @DefaultValue("0") @QueryParam("maxrows") final int maxrows,
            @DefaultValue("") @QueryParam("fallback-attribute") final String fallbackAttribute,
            @DefaultValue("AVERAGE") @QueryParam("aggregation") final String aggregation,
            @DefaultValue("false") @QueryParam("relaxed") final boolean relaxed) {

        QueryRequest request = new QueryRequest();
        // If end is not strictly positive, use the current timestamp
        request.setEnd(end > 0 ? end : new Date().getTime());
        // If start is negative, subtract it from the end
        request.setStart(start >= 0 ? start : request.getEnd() + start);
        // Make sure the resulting start time is not negative
        if (request.getStart() < 0) {
            request.setStart(0);
        }

        request.setStep(step);
        request.setMaxRows(maxrows);
        request.setRelaxed(relaxed);

        // Use the attribute name as the datasource and label
        Source source = new Source(attribute, resourceId, attribute, attribute, false);
        source.setFallbackAttribute(fallbackAttribute);
        source.setAggregation(aggregation);
        request.setSources(Lists.newArrayList(source));

        return query(request);
    }

    /**
     * Retrieves the measurements of many resources and performs
     * arbitrary calculations on these.
     *
     * This a read-only query, however we use a POST instead of GET
     * since the request parameters are difficult to express in a query string.
     */
    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public QueryResponse query(final QueryRequest request) {
        Preconditions.checkState(service != null);
        LOG.debug("Executing query with {}", request);
        QueryResponse response = null;
        try {
            response = service.query(request);
        } catch (ExpressionException e) {
            throw getException(Status.BAD_REQUEST, e, "An error occurred while evaluating an expression: {}", e.getMessage());
        } catch (FilterException  | ValidationException e) {
            throw getException(Status.BAD_REQUEST, e, e.getMessage());
        } catch (ResourceNotFoundException e) {
            throw getException(Status.NOT_FOUND, e, e.getMessage());
        } catch (FetchException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, e, "Query failed: {}", e.getMessage());
        }

        // Return a 204 if there are no columns
        if (response.getColumns().length == 0) {
            throw getException(Status.NO_CONTENT, "No content.");
        }

        return response;
    }

    protected static WebApplicationException getException(final Status status, String msg, Object... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    protected static WebApplicationException getException(final Status status, Throwable t, String msg, Object... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg, t);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }
}
