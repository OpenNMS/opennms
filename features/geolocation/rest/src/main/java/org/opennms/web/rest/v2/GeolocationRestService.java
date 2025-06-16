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
public class GeolocationRestService {

    /**
     * Is required to get access to services within the osgi container.
     */
    private ServiceRegistry serviceRegistry;

    @POST
    @Path("/")
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

