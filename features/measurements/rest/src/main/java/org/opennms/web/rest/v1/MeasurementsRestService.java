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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Measurements", description = """
        Read-only access to the values persisted by the collectors.

        A measurement is addressed by the combination of a resource id and an attribute name. JEXL
        expressions and filters can be layered on top of the fetched series. Every unit of time in
        this API, in requests and in responses alike, is milliseconds, and every timestamp is epoch
        milliseconds.

        Both `application/json` and `application/xml` are accepted and produced. The JSON and XML
        spellings of a query body differ. With no `Accept` header the response comes back as XML, the
        first entry in the produced list.

        Error bodies on this resource are `text/plain`, whatever the request asked for.""")
public class MeasurementsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementsRestService.class);

    @Autowired
    private MeasurementsService service;

    @Autowired
    private FilterEngine filterEngine;

    @GET
    @Path("filters")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the available filters",
            description = """
        Metadata for every filter the engine has registered, including the parameters each one takes
        and their defaults. `name` is the value a query body puts in `filter[].name`; `canonicalName`
        is the implementing class. `backend` is `Java` for filters evaluated in-process and `R` for
        filters that hand off to an R installation, which has to be present for those to run.""",
            operationId = "getMeasurementFilters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The registered filters. The list is never empty in a default installation.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = FilterMetaData.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "canonicalName": "org.opennms.netmgt.measurements.filters.impl.Chomp",
                        "name": "Chomp",
                        "description": "Strips leading and trailing rows that contain nothing but NaNs/null values.",
                        "backend": "Java",
                        "parameter": [
                          {
                            "key": "stripNaNs",
                            "type": "boolean",
                            "displayName": "Strip",
                            "description": "When set, leading and trailing rows containing NaNs will be removed",
                            "required": false,
                            "default": "true"
                          },
                          {
                            "key": "cutoffDate",
                            "type": "double",
                            "displayName": "Cutoff",
                            "description": "Timestamp in milliseconds. Any rows before this time will be removed.",
                            "required": false,
                            "default": "0"
                          }
                        ]
                      }
                    ]""")))
    })
    public List<FilterMetaData> getFilterMetadata() {
        return filterEngine.getFilterMetaData();
    }

    @GET
    @Path("filters/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one filter's metadata",
            description = """
        Metadata for a single filter, looked up by its registered name. The lookup is on `name`, not on
        the implementing class: `HoltWinters` resolves,
        `org.opennms.netmgt.measurements.filters.impl.HWForecast` does not.""",
            operationId = "getMeasurementFilterByName"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata for the named filter.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = FilterMetaData.class),
                            examples = @ExampleObject(value = """
                    {
                      "canonicalName": "org.opennms.netmgt.measurements.filters.impl.Chomp",
                      "name": "Chomp",
                      "description": "Strips leading and trailing rows that contain nothing but NaNs/null values.",
                      "backend": "Java",
                      "parameter": [
                        {
                          "key": "stripNaNs",
                          "type": "boolean",
                          "displayName": "Strip",
                          "description": "When set, leading and trailing rows containing NaNs will be removed",
                          "required": false,
                          "default": "true"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No filter is registered under that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No filter with name 'Nope' was found.")))
    })
    public FilterMetaData getFilterMetadata(
            @Parameter(description = "Registered filter name, as returned in the `name` field of the filter list.",
                    example = "Chomp", required = true)
            @PathParam("name") final String name) {
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
    @Operation(
            summary = "Query a single attribute",
            description = """
        Fetches one attribute of one resource as a single-column series. The query-string form of
        `POST /measurements` with exactly one source: no expressions, no filters, no second series.

        The resource id is one path segment even though it contains `[`, `]`, `:` and `.`, which are
        reserved in a URI, so it has to be percent-encoded:
        `node[loopback-lab:lb-001].responseTime[127.0.0.1]` becomes
        `node%5Bloopback-lab%3Alb-001%5D.responseTime%5B127.0.0.1%5D`. The literal, unencoded form was
        also accepted on the instance under test.

        `end` defaults to 0, which is read as "now". `start` defaults to -14400000, and a negative
        `start` is an offset back from `end`, so the default window is the last four hours. A resulting
        start below zero is clamped to zero.

        The response is the same `QueryResponse` shape the POST form returns, with a single label equal
        to the attribute name. Missing samples come back as the JSON string `"NaN"`, not as `null`.

        A query-string value that will not parse as its declared type, `step=abc` for instance, is
        rejected by the JAX-RS layer as a 404 rather than a 400.""",
            operationId = "getMeasurementsForAttribute"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One column of samples for the attribute.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = QueryResponse.class),
                                    examples = @ExampleObject(value = """
                    {
                      "step": 300000,
                      "start": 1787715836087,
                      "end": 1787730236087,
                      "timestamps": [1787729700000, 1787730000000, 1787730300000],
                      "labels": ["http-8080"],
                      "columns": [
                        {"values": [14.329565633333331, 10.33385595, "NaN"]}
                      ],
                      "constants": [],
                      "metadata": {
                        "resources": [
                          {
                            "id": "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                            "label": "Response Time for 127.0.0.1",
                            "name": "127.0.0.1",
                            "parent-id": "node[loopback-lab:lb-001]",
                            "node-id": 2
                          }
                        ],
                        "nodes": [
                          {"id": 2, "label": "loopback-001", "foreign-source": "loopback-lab", "foreign-id": "lb-001"}
                        ]
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = QueryResponse.class),
                                    examples = @ExampleObject(value = """
                    <query-response end="1787730236087" start="1787715836087" step="300000">
                      <columns>
                        <values>14.329565633333331</values>
                        <values>10.33385595</values>
                        <values>NaN</values>
                      </columns>
                      <labels>http-8080</labels>
                      <metadata>
                        <resources>
                          <resource id="node[loopback-lab:lb-001].responseTime[127.0.0.1]"
                                    parent-id="node[loopback-lab:lb-001]"
                                    label="Response Time for 127.0.0.1" name="127.0.0.1" node-id="2"/>
                        </resources>
                        <nodes>
                          <node id="2" foreign-source="loopback-lab" foreign-id="lb-001" label="loopback-001"/>
                        </nodes>
                      </metadata>
                      <timestamps>1787729700000</timestamps>
                      <timestamps>1787730000000</timestamps>
                      <timestamps>1787730300000</timestamps>
                    </query-response>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "The query produced no columns. Reached with `relaxed=true` and an attribute that does not exist."),
            @ApiResponse(responseCode = "404", description = "The resource or the attribute does not exist and `relaxed` is false, or a query-string value did not parse.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Resource or attribute not found for QueryRequest{Step=300000, Start=1787715836162, End=1787730236162, Relaxed=false, Max Rows=0, Interval=null, Heartbeat=null, Sources=[Source{Label=bogus, Resource ID=node[loopback-lab:lb-001].responseTime[127.0.0.1], Attribute=bogus, Datasource=bogus, Transient=false}], Expressions=[], Filters=[]}"))),
            @ApiResponse(responseCode = "500", description = "The fetch itself failed. Observed with an unknown `aggregation`, and with a `maxrows` too small for the requested window.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Fetch failed: Xport failed.")))
    })
    public QueryResponse simpleQuery(
            @Parameter(description = "Resource id of the resource holding the attribute, percent-encoded. Resource ids are listed by `GET /resources`.",
                    example = "node%5Bloopback-lab%3Alb-001%5D.responseTime%5B127.0.0.1%5D", required = true)
            @PathParam("resourceId") final String resourceId,
            @Parameter(description = "Name of the attribute on that resource. Also used as the datasource and as the column label.",
                    example = "http-8080", required = true)
            @PathParam("attribute") final String attribute,
            @Parameter(description = "Start of the window, epoch milliseconds. A negative value is an offset back from `end`; the default of -14400000 means four hours before `end`.",
                    example = "-14400000")
            @DefaultValue("-14400000") @QueryParam("start") final long start,
            @Parameter(description = "End of the window, epoch milliseconds. Zero or negative means now.",
                    example = "0")
            @DefaultValue("0") @QueryParam("end") final long end,
            @Parameter(description = "Requested sample interval in milliseconds. May be widened by the persistence layer or by `maxrows`.",
                    example = "300000")
            @DefaultValue("300000") @QueryParam("step") final long step,
            @Parameter(description = "Upper bound on the number of rows returned. The step is widened until the window fits. Zero disables the bound. Values far below what the window can be reduced to make the fetch fail with a 500.",
                    example = "0")
            @DefaultValue("0") @QueryParam("maxrows") final int maxrows,
            @Parameter(description = "Attribute to fall back to when `attribute` is not present on the resource. Empty disables the fallback. This form hard-wires the datasource to `attribute`, so a fallback to a differently named attribute fails the fetch with a 500. The POST form takes `datasource` alongside `fallback-attribute`.",
                    example = "")
            @DefaultValue("") @QueryParam("fallback-attribute") final String fallbackAttribute,
            @Parameter(description = "Consolidation function applied when several stored samples fall into one step. The accepted set is defined by the persistence layer; a value it does not know fails the fetch with a 500 rather than a 400.",
                    example = "AVERAGE",
                    schema = @Schema(type = "string", allowableValues = {"AVERAGE", "MIN", "MAX", "LAST", "TOTAL"}, defaultValue = "AVERAGE"))
            @DefaultValue("AVERAGE") @QueryParam("aggregation") final String aggregation,
            @Parameter(description = "When false, an attribute that does not exist is a 404. When true it is tolerated and filled with NaN, which for a single-source query leaves no columns and so yields a 204.",
                    example = "false")
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
    @Operation(
            summary = "Query measurements",
            description = """
        Fetches one or more series, optionally derives further series from them with JEXL expressions,
        and optionally runs filters over the result. The query is read-only.

        The JSON body keys for the three collections are singular: `source`, `expression` and `filter`,
        each an array. The plural spellings are not accepted, and an unknown key is not ignored: sending
        `sources` fails with a 500 carrying a Jackson `Unrecognized field` message, as does any body
        Jackson cannot parse. The scalars are `start`, `end`, `step`, `maxrows`, `interval`,
        `heartbeat` and `relaxed`, all at the top level, and all times are epoch milliseconds.

        Each `source` needs `resourceId`, `attribute` and `label`; a source missing one of them is a
        400. `aggregation` defaults to `AVERAGE` and `transient` to false. A transient source is
        fetched and made available to expressions but is left out of the response. A source that sets
        `fallback-attribute` also needs `datasource` set to the datasource the fallback is stored
        under, otherwise the fetch fails with a 500.

        The response holds `timestamps` and `columns` as parallel arrays: `columns[i].values[j]` is the
        sample of series `labels[i]` at `timestamps[j]`, so the two always have the same length. A
        missing sample is the JSON string `"NaN"`, not `null` and not a number. `constants` was empty in
        every response observed, with and without a filter applied.

        In the XML body the scalars are attributes on `<query-request>`, each source is a `<source/>`
        element carrying its fields as attributes, an expression's JEXL text is the element body rather
        than a `value` attribute, and a filter parameter's value is likewise the body of
        `<parameter key="...">`. A filter parameter written as
        `<parameter key="stripNaNs" value="true"/>` binds nothing and the filter runs with its
        default.""",
            operationId = "queryMeasurements"
    )
    @RequestBody(
            required = true,
            description = "The query. Sources, expressions and filters are evaluated in that order.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QueryRequest.class),
                            examples = @ExampleObject(value = """
                    {
                      "start": 1787726591000,
                      "end": 1787730191000,
                      "step": 300000,
                      "relaxed": false,
                      "source": [
                        {
                          "aggregation": "AVERAGE",
                          "attribute": "http-8080",
                          "label": "resp",
                          "resourceId": "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                          "transient": true
                        }
                      ],
                      "expression": [
                        {"label": "respSeconds", "value": "resp / 1000.0", "transient": false}
                      ],
                      "filter": [
                        {"name": "Chomp", "parameter": [{"key": "stripNaNs", "value": "true"}]}
                      ]
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = QueryRequest.class),
                            examples = @ExampleObject(value = """
                    <query-request start="1787726591000" end="1787730191000" step="300000" relaxed="false">
                      <source label="resp" resourceId="node[loopback-lab:lb-001].responseTime[127.0.0.1]"
                              attribute="http-8080" aggregation="AVERAGE" transient="true"/>
                      <expression label="respSeconds" transient="false">resp / 1000.0</expression>
                      <filter name="Chomp">
                        <parameter key="stripNaNs">true</parameter>
                      </filter>
                    </query-request>"""))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The requested series. One entry in `labels` and `columns` per non-transient source and expression.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = QueryResponse.class),
                                    examples = @ExampleObject(value = """
                    {
                      "step": 300000,
                      "start": 1787726591000,
                      "end": 1787730191000,
                      "timestamps": [1787729700000, 1787730000000, 1787730300000],
                      "labels": ["respSeconds"],
                      "columns": [
                        {"values": [0.014329565633333331, 0.01033385595, "NaN"]}
                      ],
                      "constants": [],
                      "metadata": {
                        "resources": [
                          {
                            "id": "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                            "label": "Response Time for 127.0.0.1",
                            "name": "127.0.0.1",
                            "parent-id": "node[loopback-lab:lb-001]",
                            "node-id": 2
                          }
                        ],
                        "nodes": [
                          {"id": 2, "label": "loopback-001", "foreign-source": "loopback-lab", "foreign-id": "lb-001"}
                        ]
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = QueryResponse.class),
                                    examples = @ExampleObject(value = """
                    <query-response end="1787730191000" start="1787726591000" step="300000">
                      <columns>
                        <values>0.014329565633333331</values>
                        <values>0.01033385595</values>
                        <values>NaN</values>
                      </columns>
                      <labels>respSeconds</labels>
                      <metadata>
                        <resources>
                          <resource id="node[loopback-lab:lb-001].responseTime[127.0.0.1]"
                                    parent-id="node[loopback-lab:lb-001]"
                                    label="Response Time for 127.0.0.1" name="127.0.0.1" node-id="2"/>
                        </resources>
                        <nodes>
                          <node id="2" foreign-source="loopback-lab" foreign-id="lb-001" label="loopback-001"/>
                        </nodes>
                      </metadata>
                      <timestamps>1787729700000</timestamps>
                      <timestamps>1787730000000</timestamps>
                      <timestamps>1787730300000</timestamps>
                    </query-response>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "The query produced no columns. Reached with `relaxed=true` and a source whose attribute does not exist."),
            @ApiResponse(responseCode = "400", description = "A source is incomplete, an expression will not parse, or a named filter is not registered.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "source missing resourceId",
                                            value = "Query source fields must be set: Source{Label=resp, Resource ID=null, Attribute=http-8080, Datasource=null, Transient=false}"),
                                    @ExampleObject(name = "unparsable expression",
                                            value = "An error occurred while evaluating an expression: Failed to parse expression. Label = 'x', Expression'resp / '. Please check also the Jexl documentation for details: https://commons.apache.org/proper/commons-jexl/reference/syntax.html"),
                                    @ExampleObject(name = "unknown filter",
                                            value = "No filter implementation found for NoSuchFilter")
                            })),
            @ApiResponse(responseCode = "404", description = "A source names a resource or attribute that does not exist and `relaxed` is false.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Resource or attribute not found for QueryRequest{Step=300000, Start=1787726604000, End=1787730204000, Relaxed=false, Max Rows=0, Interval=null, Heartbeat=null, Sources=[Source{Label=resp, Resource ID=node[loopback-lab:nope].responseTime[9.9.9.9], Attribute=http-8080, Datasource=null, Transient=false}], Expressions=[], Filters=[]}"))),
            @ApiResponse(responseCode = "500", description = "The body could not be deserialized, or the fetch failed. An empty `source` array reaches this rather than a 400.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "empty source array", value = "Fetch failed: Xport failed."),
                                    @ExampleObject(name = "unknown body key",
                                            value = "Unrecognized field \"sources\" (Class org.opennms.netmgt.measurements.model.QueryRequest), not marked as ignorable")
                            }))
    })
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
