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

import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("minionRestService")
@Path("minions")
@Tag(name = "Minions", description = """
        Read-only access to the Minions that have registered with this instance.

        Rows appear when a Minion checks in and are not created through this API. An instance with no Minions
        answers with an empty list and a count of 0.

        `date` and `lastCheckedIn` are serialised as epoch milliseconds, not as the date-time strings the
        derived schema shows. `properties` is whatever the Minion reported about itself, so its keys vary by
        version and deployment.""")
public class MinionRestService extends OnmsRestService {
    @Autowired
    private MinionDao m_minionDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{minionId}")
    @Transactional
    @Operation(
            summary = "Get one Minion",
            description = """
        Return a single registered Minion by its id. The id is the Minion's system id, normally a UUID, not
        its label.

        `date` and `lastCheckedIn` come back as epoch milliseconds.""",
            operationId = "getMinion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The Minion.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": "00000000-0000-0000-0000-000000ddba11",
                      "label": "minion-01",
                      "location": "Default",
                      "type": "Minion",
                      "date": 1787670300817,
                      "lastCheckedIn": 1787670300817,
                      "status": "Started",
                      "version": "36.0.4",
                      "properties": {
                        "org.opennms.instance.id": "OpenNMS"
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMinion.class))
                    }),
            @ApiResponse(responseCode = "404", description = "No Minion with that id has registered.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Minion 00000000-0000-0000-0000-000000ddba11 was not found.")))
    })
    public OnmsMinion getMinion(
            @Parameter(description = "Minion system id.", required = true,
                    example = "00000000-0000-0000-0000-000000ddba11")
            @PathParam("minionId") final String minionId) {
        final OnmsMinion minion = m_minionDao.get(minionId);
        if (minion == null) {
            throw getException(Status.NOT_FOUND, "Minion {} was not found.", minionId);
        }
        return minion;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{minionId}/{key}")
    @Transactional
    @Operation(
            summary = "Get one property of a Minion",
            description = """
        Return a single entry from a Minion's `properties` map as plain text. Which keys exist depends on
        what the Minion reported.

        A missing Minion and a missing key are both reported as 404, distinguishable only by the message.""",
            operationId = "getMinionProperty"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The property value.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "OpenNMS"))),
            @ApiResponse(responseCode = "404", description = "No such Minion, or the Minion has no such property.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Property org.opennms.instance.id was not found on Minion 00000000-0000-0000-0000-000000ddba11.")))
    })
    public String getMinionProperty(
            @Parameter(description = "Minion system id.", required = true,
                    example = "00000000-0000-0000-0000-000000ddba11")
            @PathParam("minionId") final String minionId,
            @Parameter(description = "Property key, as it appears in the Minion's `properties` map.", required = true,
                    example = "org.opennms.instance.id")
            @PathParam("key") final String key) {
        final OnmsMinion minion = getMinion(minionId);
        final String value = minion.getProperties().get(key);
        if (value == null) {
            throw getException(Status.NOT_FOUND, "Property {} was not found on Minion {}.", key, minionId);
        }
        return value;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    @Operation(
            summary = "Count registered Minions",
            description = """
        Return the number of registered Minions as a plain-text integer. This is the unfiltered total and
        ignores any query parameters.

        This operation only produces `text/plain`. A request with `Accept: application/json` does not match
        it and falls through to `GET /minions/{minionId}`, which then answers 404 for the literal id
        `count`.""",
            operationId = "getMinionCount"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The number of registered Minions.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "0")))
    })
    public String getCount() {
        return Integer.toString(m_minionDao.countAll());
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "List registered Minions",
            description = """
        List the registered Minions. Query parameters are turned into criteria against the Minion entity, so
        the usual v1 filtering, ordering and paging parameters apply: `limit`, `offset`, `orderBy`, `order`,
        and `<property>=<value>` for equality on a property of the entity.

        `totalCount` is the number of Minions matching the filter ignoring `limit` and `offset`, while
        `count` is how many are in this page. An instance with no Minions returns an empty `minion` array
        with `totalCount` 0 and `count` null.""",
            operationId = "getMinions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The matching Minions.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMinionCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 0,
                      "count": null,
                      "offset": 0,
                      "minion": []
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMinionCollection.class))
                    }),
            @ApiResponse(responseCode = "400", description = "A query parameter did not name a property of the Minion entity, or a value did not convert.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public OnmsMinionCollection getMinions(@Context final UriInfo uriInfo) throws ParseException {
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters());
        final OnmsMinionCollection coll = new OnmsMinionCollection(m_minionDao.findMatching(builder.toCriteria()));
        coll.setTotalCount(m_minionDao.countMatching(builder.clearOrder().toCriteria()));

        return coll;
    }

    private CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMinion.class);
        //builder.alias("properties", "property", JoinType.LEFT_JOIN);
        applyQueryFilters(params, builder);
        return builder;
    }

}
