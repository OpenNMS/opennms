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

package org.opennms.web.rest;

import java.util.Date;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.opennms.web.rest.measurements.ExpressionEngine;
import org.opennms.web.rest.measurements.ExpressionException;
import org.opennms.web.rest.measurements.JEXLExpressionEngine;
import org.opennms.web.rest.measurements.fetch.FetchResults;
import org.opennms.web.rest.measurements.fetch.MeasurementFetchStrategy;
import org.opennms.web.rest.measurements.model.Expression;
import org.opennms.web.rest.measurements.model.QueryRequest;
import org.opennms.web.rest.measurements.model.QueryResponse;
import org.opennms.web.rest.measurements.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private MeasurementFetchStrategy m_fetchStrategy;

    private final ExpressionEngine expressionEngine = new JEXLExpressionEngine();

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
            @DefaultValue("AVERAGE") @QueryParam("aggregation") final String aggregation) {

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

        // Use the attribute name as the label
        Source source = new Source(attribute, resourceId, attribute, false);
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
        Preconditions.checkState(m_fetchStrategy != null);
        validateQueryRequest(request);

        LOG.debug("Executing query with {}", request);

        // Fetch the measurements
        FetchResults results;
        try {
            results = m_fetchStrategy.fetch(
                        request.getStart(),
                        request.getEnd(),
                        request.getStep(),
                        request.getMaxRows(),
                        request.getSources()
                        );
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, e, "Fetch failed");
        }

        // Return a 404 when null
        if (results == null) {
            throw getException(Status.NOT_FOUND, "Resource or attribute not found for {}", request);
        }

        // Apply the expression to the fetch results
        try {
            expressionEngine.applyExpressions(request, results);
        } catch (ExpressionException e) {
            throw getException(Status.BAD_REQUEST, e, "An error occured while evaluating an expression.");
        }

        final Map<String, double[]> columns = results.getColumns();

        // Remove any transient values belonging to sources
        for (final Source source : request.getSources()) {
            if (source.getTransient()) {
                columns.remove(source.getLabel());
            }
        }

        // Return a 204 if there are no columns
        if (columns.keySet().size() == 0) {
            throw getException(Status.NO_CONTENT, "No content.");
        }

        // Build the response
        final QueryResponse response = new QueryResponse();
        response.setStart(request.getStart());
        response.setEnd(request.getEnd());
        response.setStep(results.getStep());
        response.setTimestamps(results.getTimestamps());
        response.setColumns(columns);

        return response;
    }

    /**
     * Validates the query request, in order to avoid triggering
     * internal server errors for invalid input.
     *
     * @throws WebApplicationException if validation fails.
     */
    private static void validateQueryRequest(final QueryRequest request) {
        if (request.getEnd() < 0) {
            throw getException(Status.BAD_REQUEST, "Query end must be >= 0: {}", request.getEnd());
        }
        if (request.getStep() <= 0) {
            throw getException(Status.BAD_REQUEST, "Query step must be > 0: {}", request.getStep());
        }
        for (final Source source : request.getSources()) {
            if (source.getResourceId() == null
                    || source.getAttribute() == null
                    || source.getLabel() == null
                    || source.getAggregation() == null) {
                throw getException(Status.BAD_REQUEST, "Query source fields must be set: {}", source);
            }
        }
        for (final Expression expression : request.getExpressions()) {
            if (expression.getExpression() == null
                    || expression.getLabel() == null) {
                throw getException(Status.BAD_REQUEST, "Query expression fields must be set: {}", expression);
            }
        }
    }

    protected static <T> WebApplicationException getException(final Status status, String msg, Object... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    protected static <T> WebApplicationException getException(final Status status, Throwable t, String msg, Object... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg, t);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }
}
