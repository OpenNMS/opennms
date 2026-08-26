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
package org.opennms.web.rest.v2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.features.geolocation.api.GeolocationConfiguration;
import org.opennms.features.geolocation.api.GeolocationInfo;
import org.opennms.features.geolocation.api.GeolocationQuery;
import org.opennms.features.geolocation.api.GeolocationService;
import org.opennms.features.geolocation.api.GeolocationSeverity;
import org.opennms.features.geolocation.api.StatusCalculationStrategy;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("geolocation")
@Transactional
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
@Tag(name = "Geolocation", description = """
        Geolocation API.

        Backs the geographical map. `POST /geolocation` returns the nodes that have usable coordinates,
        optionally with a status derived from their alarms or outages, and `GET /geolocation/config`
        returns the tile-server settings the map front end needs.

        Both operations are served by whatever provider is registered in the OSGi service registry, and
        both answer 503 while none is. The class declares XML and Atom alongside JSON, but no XML writer
        is registered for either response type, so a request that asks for XML fails with 500. Send and
        accept JSON.""")
public class GeolocationRestService {

    /**
     * Is required to get access to services within the osgi container.
     */
    private ServiceRegistry serviceRegistry;

    @POST
    @Path("/")
    @Operation(
            summary = "Query node geolocations",
            description = """
                    Return one entry per node that has usable coordinates, with the node's identity, its
                    address, its unacknowledged alarm count and, when a strategy is given, a computed
                    severity. Nodes whose asset record holds no coordinates and no resolvable address are
                    left out.

                    `strategy` selects how the status is calculated and is matched case-insensitively.
                    Validation accepts `None`, `Alarms` and `Outages`, but the value is then converted
                    against a narrower set of `Alarms` and `Outages`, so `None` passes validation and
                    fails afterwards with 500. Omitting `strategy` skips the calculation and leaves
                    `severityInfo` null, which is the supported way to ask for no status.

                    `severityFilter` keeps only the nodes whose calculated severity is at least the one
                    named. Validation accepts every `OnmsSeverity` name, but conversion accepts only
                    `Normal`, `Warning`, `Minor`, `Major` and `Critical`, so `Indeterminate` and
                    `Cleared` pass validation and fail afterwards with 500.

                    Only `application/json` is produced. A request that accepts XML or Atom is answered
                    with 500, because no writer is registered for the returned list.""",
            operationId = "geolocationQuery")
    @RequestBody(description = """
            The query. An empty object is valid and returns every node with coordinates, with no status
            calculated.""",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GeolocationQueryDTO.class),
                            examples = {
                                    @ExampleObject(name = "everything", value = """
                    {}"""),
                                    @ExampleObject(name = "alarmStatus", value = """
                    {
                      "strategy": "Alarms",
                      "severityFilter": "Normal",
                      "includeAcknowledgedAlarms": false
                    }""")
                            }),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = GeolocationQueryDTO.class),
                            examples = @ExampleObject(value = """
                    <geolocationQueryDTO>
                      <strategy>Alarms</strategy>
                      <severityFilter>Normal</severityFilter>
                      <includeAcknowledgedAlarms>false</includeAcknowledgedAlarms>
                    </geolocationQueryDTO>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The matching nodes and their coordinates.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = GeolocationInfo.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "nodeInfo": {
                          "nodeId": 9001,
                          "nodeLabel": "esx-01.lab",
                          "foreignSource": "review-vmware",
                          "foreignId": "esx-01.lab",
                          "description": null,
                          "maintcontract": null,
                          "ipAddress": "10.20.0.11",
                          "location": "Default",
                          "categories": []
                        },
                        "coordinates": { "longitude": -84.3892, "latitude": 33.7466 },
                        "severityInfo": { "id": 3, "label": "Normal" },
                        "addressInfo": {
                          "address1": null,
                          "address2": null,
                          "city": "Atlanta",
                          "state": "GA",
                          "zip": null,
                          "country": "US"
                        },
                        "alarmUnackedCount": 0
                      }
                    ]"""))),
            @ApiResponse(responseCode = "204", description = "No node matched. No body is returned."),
            @ApiResponse(responseCode = "400", description = """
                    `strategy` or `severityFilter` is not one of the accepted names. The body is the \
                    message as a bare string, even though the response is labelled JSON.""",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "strategy", value = "Strategy 'bogus' is not supported"),
                                    @ExampleObject(name = "severity", value = "Severity ' bogus' is not valid. Supported values are: [INDETERMINATE, CLEARED, NORMAL, WARNING, MINOR, MAJOR, CRITICAL]")
                            })),
            @ApiResponse(responseCode = "500", description = """
                    A value passed validation but could not be converted, the body was absent or could \
                    not be deserialised, or the request asked for a media type the list cannot be \
                    written as.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "strategyNone", value = "No enum with value 'None' found in [Alarms, Outages]"),
                                    @ExampleObject(name = "severityCleared", value = "No enum with value 'Cleared' found in [Normal, Warning, Minor, Major, Critical]"),
                                    @ExampleObject(name = "noBody", value = "No content to map to Object due to end of input"),
                                    @ExampleObject(name = "xmlRequested", value = "No message body writer has been found for class java.util.ArrayList, ContentType: application/xml")
                            })),
            @ApiResponse(responseCode = "503", description = "No geolocation service is registered yet.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No service registered to handle your query. This is a temporary issue. Please try again later.")))
    })
    public Response getLocations(GeolocationQueryDTO queryDTO) {
        final GeolocationService service = getServiceRegistry().findProvider(GeolocationService.class);
        if (service == null) {
            return temporarilyNotAvailable();
        }
        try {
            validate(queryDTO);
            GeolocationQuery query = toQuery(queryDTO);
            final List<GeolocationInfo> locations = service.getLocations(query);
            if (locations.isEmpty()) {
                return Response.noContent().build();
            }
            return Response.ok(locations).build();
        } catch (InvalidQueryException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/config")
    @Operation(
            summary = "Get the map configuration",
            description = """
                    Return the tile-server settings the geographical map uses: the tile URL template, a
                    display name for the server, and the Leaflet tile-layer options, which carry the
                    attribution the tile provider requires.

                    The values come from `opennms.properties`; the response shows the built-in defaults
                    when none are set.

                    Only `application/json` is produced. A request that accepts XML or Atom is answered
                    with 500, because no writer is registered for the configuration type.""",
            operationId = "geolocationConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The map configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GeolocationConfiguration.class),
                            examples = @ExampleObject(value = """
                    {
                      "tileServerUrl": "https://tiles.opennms.org/{z}/{x}/{y}.png",
                      "tileServerName": "OpenNMS Default",
                      "options": {
                        "attribution": "Map data &copy; OpenStreetMap contributors under ODbL, CC BY-SA 2.0"
                      }
                    }"""))),
            @ApiResponse(responseCode = "500", description = "The request asked for a media type the configuration cannot be written as.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No message body writer has been found for class org.opennms.features.geolocation.services.DefaultGeolocationConfiguration, ContentType: application/xml"))),
            @ApiResponse(responseCode = "503", description = "No geolocation configuration service is registered yet.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No service registered to handle your query. This is a temporary issue. Please try again later.")))
    })
    public Response getConfiguration() {
        GeolocationConfiguration config = getServiceRegistry().findProvider(GeolocationConfiguration.class);
        if (config == null) {
            return temporarilyNotAvailable();
        }
        return Response.ok(config).build();
    }

     private ServiceRegistry getServiceRegistry() {
        if (serviceRegistry == null) {
            serviceRegistry = BeanUtils.getBean("soaContext", "serviceRegistry", ServiceRegistry.class);
            Objects.requireNonNull(serviceRegistry);
        }
        return serviceRegistry;
    }

    private static GeolocationQuery toQuery(GeolocationQueryDTO queryDTO) {
        if (queryDTO != null) {
            GeolocationQuery query = new GeolocationQuery();
            if (queryDTO.getSeverityFilter() != null) {
                query.setSeverity(getEnum(queryDTO.getSeverityFilter(), GeolocationSeverity.values()));
            }
            if (queryDTO.getStrategy() != null) {
                query.setStatusCalculationStrategy(getEnum(queryDTO.getStrategy(), StatusCalculationStrategy.values()));
            }
            query.setIncludeAcknowledgedAlarms(queryDTO.isIncludeAcknowledgedAlarms());
            return query;
        }
        return null;
    }

    private static <T> T getEnum(String input, Enum<?>[] values) {
        for (Enum<?> eachEnum : values) {
            if (input.equalsIgnoreCase(eachEnum.name())) {
                return (T) eachEnum;
            }
        }
        throw new IllegalArgumentException("No enum with value '" + input + "' found in " + Arrays.toString(values));
    }

    private static void validate(GeolocationQueryDTO query) throws InvalidQueryException {
        // Validate and sanitize Strategy
        if (query.getStrategy() != null) {
            query.setStrategy(WebSecurityUtils.sanitizeString(query.getStrategy()));
            boolean valid = isValid(query.getStrategy(), NodeStatusCalculationStrategy.values());
            if (!valid) {
                throw new InvalidQueryException("Strategy '" + query.getStrategy() + "' is not supported");
            }
        }

        // Validate and sanitize Severity
        if (query.getSeverityFilter() != null) {
            query.setSeverityFilter(WebSecurityUtils.sanitizeString(query.getSeverityFilter()));
            boolean valid = isValid(query.getSeverityFilter(), OnmsSeverity.values());
            if (!valid) {
                throw new InvalidQueryException("Severity ' " + query.getSeverityFilter() + "' is not valid. Supported values are: " + Arrays.toString(OnmsSeverity.values()));
            }
        }
    }

    private static boolean isValid(String input, Enum[] enumValues) {
        for (Enum eachEnum : enumValues) {
            if (input.equalsIgnoreCase(eachEnum.name())) {
                return true;
            }
        }
        return false;
    }

    private static Response temporarilyNotAvailable() {
        return Response
                .status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("No service registered to handle your query. This is a temporary issue. Please try again later.")
                .build();
    }
}

