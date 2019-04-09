/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.geocoder.rest.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderConfigurationException;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geocoder.GeocoderServiceManager;
import org.opennms.features.geocoder.GeocoderServiceManagerConfiguration;
import org.opennms.features.geocoder.rest.GeocodingRestService;

public class GeocodingRestServiceImpl implements GeocodingRestService {

    private GeocoderServiceManager geocoderServiceManager;

    public GeocodingRestServiceImpl(GeocoderServiceManager geocoderServiceManager) {
        this.geocoderServiceManager = Objects.requireNonNull(geocoderServiceManager);
    }

    @Override
    public Response getConfiguration() throws IOException {
        final GeocoderServiceManagerConfiguration configuration = geocoderServiceManager.getConfiguration();
        final Map<String, Object> configurationMap = configuration.asMap();
        final JSONObject result = new JSONObject(configurationMap);
        return Response.ok(result.toString()).build();
    }

    @Override
    public Response updateConfiguration(InputStream inputStream) throws IOException {
        JSONTokener jsonTokener = new JSONTokener(inputStream);
        JSONObject configuration = new JSONObject(jsonTokener);
        // TODO MVR from JSON
        geocoderServiceManager.updateConfiguration(new GeocoderServiceManagerConfiguration(configuration.toMap()));
        return Response.accepted().build();
    }

    @Override
    public Response listGeocoderConfigurations() {
        final List<GeocoderService> services = geocoderServiceManager.getGeocoderServices();
        if (services.size() == 0) {
            return Response.noContent().build();
        }
        final JSONArray serviceArray = new JSONArray();
        services.stream().forEach(geocoderService -> {
            JSONObject eachService = new JSONObject();
            eachService.put("id", geocoderService.getId());
            eachService.put("config", geocoderService.getConfiguration().asMap());

            // Verify the configuration
            try {
                geocoderService.getConfiguration().validate();
            } catch (GeocoderConfigurationException ex) {
                eachService.put("error", new JSONObject()
                        .put("message", ex.getRawMessage())
                        .put("context", ex.getContext()));
            }
            serviceArray.put(eachService);
        });
        return Response.ok(serviceArray.toString()).build();
    }

    @Override
    public Response updateGeocoderConfiguration(String geocoderId, InputStream inputStream) {
        try {
            final JSONTokener tokener = new JSONTokener(inputStream);
            final Map<String, Object> properties = new JSONObject(tokener).getJSONObject("config").toMap();
            try {
                geocoderServiceManager.updateGeocoderConfiguration(geocoderId, properties);
            } catch (GeocoderConfigurationException ex) {
                final JSONObject errorObject = new JSONObject()
                        .put("message", ex.getRawMessage())
                        .put("context", ex.getContext());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorObject.toString())
                        .build();
            }
            return Response.noContent().build();
        } catch (IOException ex) {
            // TODO MVR add data
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public Response resolveAddress(final String addressToResolve) {
        // TODO MVR make this return a result or something like that, so we know if there is no coordinate or
        // if there was an error
        try {
            final GeocoderResult geocoderResult = geocoderServiceManager.getActiveGeocoderService().resolveAddress(addressToResolve);
            if (geocoderResult.isEmpty() && !geocoderResult.hasError()) {
                return Response.noContent().build();
            }
            if (geocoderResult.hasError()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("content-type", "application/json")
                        .entity("{\"error\": \"" + geocoderResult.getThrowable().getMessage() + "\"}")
                        .build();
            }
            // TODO MVR convert
            final Coordinates coordinates = geocoderResult.getCoordinates();
            if (coordinates != null) {
                return Response.ok()
                        .header("content-type", "application/json")
                        .entity("{ \"longitude\": " + coordinates.getLongitude() + ", \"latitude\": " + coordinates.getLatitude() + "}")
                        .build();
            }
            return Response.noContent().build();
        } catch (GeocoderException e) {
            // TODO MVR return error object
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                  .header("content-type", "application/json")
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
