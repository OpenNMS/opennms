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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joda.time.Duration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("monitoringLocationsRestService")
@Path("monitoringLocations")
@Tag(name = "MonitoringLocations", description = """
        Monitoring Locations API. A location is what ties a Minion, a node and a polling package together;
        every installation has a `Default` location.

        The primary key is the location name, chosen by the caller. That has two consequences worth knowing
        before writing: `POST` with a name that already exists overwrites the existing row and still answers
        201, and a body with no `location-name` fails with 500 rather than 400.

        JSON and XML bodies are not interchangeable here. In JSON, `tags` is a plain array of strings. In
        XML, a `<tags>` element cannot be unmarshalled at all and fails with 500, so XML bodies have to omit
        it; the rest of the fields are attributes, spelled `location-name` and `monitoring-area`.

        `PUT` is separate again: it takes form-encoded parameters, not a JSON or XML body, and the parameter
        names are the Java bean property names (`monitoringArea`, `priority`, `geolocation`, `latitude`,
        `longitude`), not the hyphenated wire names.""")
public class MonitoringLocationsRestService extends OnmsRestService {

	private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationsRestService.class);
	private static final String POLLING_PACKAGE_NAMES = "pollingPackageNames";

	@Autowired
	@Qualifier("eventProxy")
	private EventProxy m_eventProxy;

	@Autowired
	private MonitoringLocationDao m_monitoringLocationDao;

	@GET
	@Path("default")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	@Operation(
			summary = "Get the default monitoring location",
			description = """
        Return the first monitoring location in the table's natural order, which on an untouched installation
        is `Default`. The choice is not tied to the name `Default`, so on a system whose locations have been
        edited this can return something else.""",
			operationId = "getDefaultMonitoringLocation"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The location.",
					content = {
							@Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = OnmsMonitoringLocation.class),
									examples = @ExampleObject(value = """
                    {
                      "tags": [],
                      "latitude": null,
                      "geolocation": null,
                      "longitude": null,
                      "priority": 100,
                      "location-name": "Default",
                      "monitoring-area": "localhost"
                    }""")),
							@Content(mediaType = MediaType.APPLICATION_XML,
									schema = @Schema(implementation = OnmsMonitoringLocation.class))
					})
	})
	public OnmsMonitoringLocation getDefaultMonitoringLocation() throws ParseException {
		return m_monitoringLocationDao.findAll().get(0);
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	@Operation(
			summary = "List monitoring locations",
			description = """
        List every monitoring location, sorted by name. The response is not paged: `totalCount` and `count`
        are equal and `offset` is always 0.

        In XML the entries are `location` elements inside `locations`; in JSON the array is keyed
        `location`.""",
			operationId = "getMonitoringLocations"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The monitoring locations.",
					content = {
							@Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = OnmsMonitoringLocationDefinitionList.class),
									examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "location": [
                        {
                          "tags": [],
                          "latitude": null,
                          "geolocation": null,
                          "longitude": null,
                          "priority": 100,
                          "location-name": "Default",
                          "monitoring-area": "localhost"
                        }
                      ]
                    }""")),
							@Content(mediaType = MediaType.APPLICATION_XML,
									schema = @Schema(implementation = OnmsMonitoringLocationDefinitionList.class))
					})
	})
	public OnmsMonitoringLocationDefinitionList getForeignSources() throws ParseException {
		final List<OnmsMonitoringLocation> onmsMonitoringLocationList = m_monitoringLocationDao.findAll();
		Collections.sort(onmsMonitoringLocationList, Comparator.comparing(OnmsMonitoringLocation::getLocationName));
		return new OnmsMonitoringLocationDefinitionList(onmsMonitoringLocationList);
	}

	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(
			summary = "Count monitoring locations",
			description = """
        Return the number of monitoring locations as a plain-text integer.

        This operation only produces `text/plain`. A request with `Accept: application/json` does not match it
        and falls through to `GET /monitoringLocations/{monitoringLocation}`, which then answers 404 for the
        literal name `count`.""",
			operationId = "getMonitoringLocationCount"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The number of locations.",
					content = @Content(mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(type = "string"),
							examples = @ExampleObject(value = "1")))
	})
	public String getTotalCount() throws ParseException {
		return Integer.toString(m_monitoringLocationDao.findAll().size());
	}

	@GET
	@Path("{monitoringLocation}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	@Operation(
			summary = "Get one monitoring location",
			description = """
        Return a single monitoring location by name. The lookup is exact and case sensitive.""",
			operationId = "getMonitoringLocation"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The location.",
					content = {
							@Content(mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = OnmsMonitoringLocation.class),
									examples = @ExampleObject(value = """
                    {
                      "location-name": "ApiDoc-Loc",
                      "monitoring-area": "raleigh",
                      "priority": 50,
                      "geolocation": "Raleigh, NC",
                      "latitude": 35.7796,
                      "longitude": -78.6382,
                      "tags": [
                        "east"
                      ]
                    }""")),
							@Content(mediaType = MediaType.APPLICATION_XML,
									schema = @Schema(implementation = OnmsMonitoringLocation.class))
					}),
			@ApiResponse(responseCode = "404", description = "No location with that name exists.",
					content = @Content(mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(type = "string"),
							examples = @ExampleObject(value = "Monitoring location Nowhere was not found.")))
	})
	public OnmsMonitoringLocation getMonitoringLocation(
			@Parameter(description = "Location name. Exact and case sensitive.", required = true, example = "Default")
			@PathParam("monitoringLocation") String monitoringLocation) {
	    final OnmsMonitoringLocation loc = m_monitoringLocationDao.get(monitoringLocation);
            if (loc == null) {
                throw getException(Status.NOT_FOUND, "Monitoring location {} was not found.", monitoringLocation);
            }
            return loc;
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	@Transactional
	@Operation(
			summary = "Create a monitoring location",
			description = """
        Create a monitoring location. `location-name` is the primary key and has to be supplied in the body;
        a body without it fails with 500 on the missing identifier rather than with 400.

        This is a save, not an insert. Posting a name that already exists overwrites that location and still
        answers 201, so it is worth checking with `GET` first if that is not the intent.

        The XML form differs from the JSON form: XML fields are attributes and a `<tags>` element cannot be
        unmarshalled, so it has to be omitted. Use JSON when tags are needed.

        `Location` on the 201 points at `GET /monitoringLocations/{monitoringLocation}` for the new
        location.""",
			operationId = "addMonitoringLocation"
	)
	@RequestBody(
			required = true,
			description = "The location to create. `location-name` is required. In XML, omit `tags`.",
			content = {
					@Content(mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = OnmsMonitoringLocation.class),
							examples = @ExampleObject(value = """
                    {
                      "location-name": "ApiDoc-Loc",
                      "monitoring-area": "raleigh",
                      "priority": 50,
                      "geolocation": "Raleigh, NC",
                      "latitude": 35.7796,
                      "longitude": -78.6382,
                      "tags": [
                        "east"
                      ]
                    }""")),
					@Content(mediaType = MediaType.APPLICATION_XML,
							schema = @Schema(implementation = OnmsMonitoringLocation.class),
							examples = @ExampleObject(value = """
                    <location location-name="ApiDoc-Loc"
                              monitoring-area="raleigh"
                              priority="50"
                              geolocation="Raleigh, NC"
                              latitude="35.7796"
                              longitude="-78.6382"/>"""))
			}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "The location was created or overwritten. `Location` carries its URI."),
			@ApiResponse(responseCode = "500", description = "`location-name` was missing, or an XML body contained a `tags` element.",
					content = @Content(mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(type = "string"),
							examples = @ExampleObject(value = "ids for this class must be manually assigned before calling save(): org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation")))
	})
	public Response addMonitoringLocation(@Context final UriInfo uriInfo, OnmsMonitoringLocation monitoringLocation) {
		writeLock();
		try {
			LOG.debug("addMonitoringLocation: Adding monitoringLocation {}", monitoringLocation.getLocationName());
			m_monitoringLocationDao.save(monitoringLocation);
			return Response.created(getRedirectUri(uriInfo, monitoringLocation.getLocationName())).build();
		} finally {
			writeUnlock();
		}
	}

	@PUT
	@Path("{monitoringLocation}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	@Operation(
			summary = "Update a monitoring location",
			description = """
        Update fields on an existing location from form-encoded parameters. Each parameter name is matched
        against the writable bean properties of the location, so the spelling is the Java property name
        (`monitoringArea`, `priority`, `geolocation`, `latitude`, `longitude`), not the hyphenated name that
        appears in a JSON or XML representation.

        Parameters that do not match a writable property are ignored. If nothing matched, or the body was
        empty, the answer is 304 and nothing is written. The location name itself cannot be changed this way.

        A name that does not exist fails with 500, not 404: the handler does not check the lookup result
        before writing to it.""",
			operationId = "updateMonitoringLocation"
	)
	@RequestBody(
			required = true,
			description = "Form-encoded properties to change, named after the bean properties.",
			content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
					schema = @Schema(type = "object"),
					examples = @ExampleObject(value = "monitoringArea=raleigh&priority=75&geolocation=Raleigh%2C+NC"))
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "At least one property was changed."),
			@ApiResponse(responseCode = "304", description = "The body was empty, or no parameter named a writable property. Nothing was written."),
			@ApiResponse(responseCode = "500", description = "No location with that name exists, or a value did not convert to the property type.",
					content = @Content(mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(type = "string"),
							examples = @ExampleObject(value = "Target object must not be null")))
	})
	public Response updateMonitoringLocation(
			@Parameter(description = "Location name. Exact and case sensitive.", required = true, example = "ApiDoc-Loc")
			@PathParam("monitoringLocation") String monitoringLocation, MultivaluedMapImpl params) {
		writeLock();
		try {
			boolean sendEvent = false;

			OnmsMonitoringLocation def = m_monitoringLocationDao.get(monitoringLocation);
			LOG.debug("updateMonitoringLocation: updating monitoring location {}", monitoringLocation);

			if (params.isEmpty()) return Response.notModified().build();

			boolean modified = false;
			final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(def);
			wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
			for(final String key : params.keySet()) {
				if (wrapper.isWritableProperty(key)) {
					String stringValue = params.getFirst(key);
					Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
					wrapper.setPropertyValue(key, value);
					modified = true;
				}
			}
			if (modified) {
			    LOG.debug("updateMonitoringLocation: monitoring location {} updated", monitoringLocation);
			    m_monitoringLocationDao.save(def);
				return Response.noContent().build();
			}
			return Response.notModified().build();
		} finally {
			writeUnlock();
		}
	}

	@DELETE
	@Path("{monitoringLocation}")
	@Transactional
	@Operation(
			summary = "Delete a monitoring location",
			description = """
        Delete a monitoring location by name. Nodes and Minions still referencing it are not checked for, so
        deleting a location that is in use leaves those references dangling.

        A name that does not exist fails with 500, not 404.""",
			operationId = "deleteMonitoringLocation"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "The location was deleted."),
			@ApiResponse(responseCode = "500", description = "No location with that name exists.",
					content = @Content(mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(type = "string"),
							examples = @ExampleObject(value = "attempt to create delete event with null entity")))
	})
	public Response deleteMonitoringLocation(
			@Parameter(description = "Location name. Exact and case sensitive.", required = true, example = "ApiDoc-Loc")
			@PathParam("monitoringLocation") String monitoringLocation) {
		writeLock();
		try {
			LOG.debug("deleteMonitoringLocation: deleting monitoring location {}", monitoringLocation);

			m_monitoringLocationDao.delete(monitoringLocation);

			return Response.noContent().build();
		} finally {
			writeUnlock();
		}
	}
}
