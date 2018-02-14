/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
        // Validate Strategy
        if (query.getStrategy() != null) {
            boolean valid = isValid(query.getStrategy(), NodeStatusCalculationStrategy.values());
            if (!valid) {
                throw new InvalidQueryException("Strategy '" + query.getStrategy() + "' is not supported");
            }
        }

        // Validate Severity
        if (query.getSeverityFilter() != null) {
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

