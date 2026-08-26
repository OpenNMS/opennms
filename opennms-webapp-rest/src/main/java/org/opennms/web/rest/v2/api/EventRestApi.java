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
package org.opennms.web.rest.v2.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("events")
@Tag(name = "Events", description = """
        Events API V2.

        Events are the immutable record of everything OpenNMS and its agents report. This resource
        reads them and publishes new ones. It does not modify or delete them: the inherited write
        operations on `/events` and `/events/{id}` are reachable but fail, see below.

        Timestamps are rendered differently per representation: JSON carries epoch milliseconds
        (`1787685470949`), XML carries an ISO-8601 string with offset
        (`2026-08-25T15:17:50.949-04:00`). The generated schema shows the XML form for both.

        `POST /events` and `POST /events/{tiebreaker}` are the same operation. The tiebreaker template
        matches the empty string, and CXF resolves `POST /events` to it, so a POST to the collection
        publishes an event and the `EventDTO` request body the document shows for `POST /events` is not
        what the handler reads. Publish an `Event` document instead, as shown under
        `POST /events/{tiebreaker}`.

        `PUT /events`, `DELETE /events`, `POST /events/{id}`, `PUT /events/{id}` and
        `DELETE /events/{id}` are inherited from the generic DAO resource and answer 500 with
        `object is not an instance of declaring class`. This implementation is proxied on its interface,
        so the inherited methods cannot be invoked on the proxy. Do not build against them.

        `GET /events/{id}` is missing from this document but works: it returns one event by database id,
        200 with the same schema as an element of the `GET /events` list, or 404. The generator drops it
        because both this interface and the abstract superclass declare that signature, and neither
        declaration can be removed without breaking the endpoint.

        Collection reads accept a CXF FIQL expression in `_s` together with `limit`, `offset`,
        `orderBy` and `order`. The default page size is 10 and the default sort is `eventTime`
        descending. Property names usable in `_s` and `orderBy` are listed by
        `GET /events/properties`; naming a property the entity does not have fails with 500 rather
        than 400.""")
public interface EventRestApi {

    // These five declarations have to stay. <tx:annotation-driven/> proxies this implementation on its
    // interfaces, so a method absent here is absent from the proxy and answers 500 at runtime, which is
    // what already happens to the inherited write operations. Their OpenAPI detail lives on the
    // EventRestService overrides, because swagger resolves annotations from the superclass first.

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final Long id) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;


    @POST
    @Path("{tiebreaker: $}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Publish a new event",
            description = """
        Hand an event to eventd. `POST /events` resolves to this same handler, so both forms of the path
        behave alike.

        `uei` is the only field worth treating as required: `time` defaults to now and `source` defaults
        to `ReST` when they are absent. Nothing else is validated here. An event whose UEI matches no
        event configuration is still stored, under `uei.opennms.org/default/event`, so a body as small as
        `{}` yields 204 and leaves a row behind.

        The 204 says only that eventd accepted the event for processing. Whether it is persisted, and
        whether it raises an alarm, is decided by the matching event configuration afterwards.

        The two representations are not interchangeable. In XML the parameter value is element text with
        `type` and `encoding` as attributes; in JSON the same value is an object whose text sits in a
        `value` field. Sending the XML shape as JSON fails.""",
            operationId = "EventRestAPIPostTiebreaker")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "The event to publish.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Event.class),
                            examples = @ExampleObject(value = """
                    {
                      "uei": "uei.opennms.org/internal/droolsEngineException",
                      "source": "my-integration",
                      "parms": [
                        {
                          "parmName": "enginename",
                          "value": { "value": "north-uplink", "type": "string", "encoding": "text" }
                        }
                      ]
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Event.class),
                            examples = @ExampleObject(value = """
                    <event>
                      <uei>uei.opennms.org/internal/droolsEngineException</uei>
                      <source>my-integration</source>
                      <parms>
                        <parm>
                          <parmName>enginename</parmName>
                          <value type="string" encoding="text">north-uplink</value>
                        </parm>
                      </parms>
                    </event>"""))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The event was handed to eventd. No body is returned."),
            @ApiResponse(responseCode = "415", description = "The request declared a content type other than JSON or XML. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "malformed XML", value = "Failed to marshal/unmarshal XML file while unmarshalling an object (Event)"),
                                    @ExampleObject(name = "wrong JSON field type", value = "Can not construct instance of java.lang.Long from String value notanumber: not a valid Long value")
                            }))
    })
    Response create(@RequestBody Event event) ;
}
