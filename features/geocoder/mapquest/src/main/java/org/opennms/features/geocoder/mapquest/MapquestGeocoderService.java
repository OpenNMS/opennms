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

package org.opennms.features.geocoder.mapquest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.features.geocoder.GeocoderConfiguration;
import org.opennms.features.geocoder.GeocoderConfigurationException;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapquestGeocoderService implements GeocoderService {

    private static final Logger LOG = LoggerFactory.getLogger(MapquestGeocoderService.class);

    private final MapquestConfiguration configuration;

    public MapquestGeocoderService(MapquestConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public String getId() {
        return "mapquest";
    }

    @Override
    public GeocoderResult resolveAddress(final String address) throws GeocoderConfigurationException {
        configuration.validate();
        LOG.debug("Configuration: {}", configuration.asMap());
        try (HttpClientWrapper clientWrapper = HttpClientWrapper.create().dontReuseConnections()) {
            if(configuration.isUseSystemProxy()) {
                clientWrapper.useSystemProxySettings();
            }
            final String requestUrl = buildURL(configuration.getUrlTemplate(), configuration.getApiKey(), address);
            final HttpUriRequest request = new HttpGet(requestUrl);
            try (CloseableHttpResponse response = clientWrapper.execute(request)) {
                final StatusLine statusLine = response.getStatusLine();
                LOG.trace("Invoking URL {} returned {}:{} => {}", requestUrl, statusLine.getStatusCode(), statusLine.getReasonPhrase(), statusLine.getStatusCode() == 200 ? "OK" : "NOK" );
                if (statusLine.getStatusCode() != 200) {
                    return GeocoderResult.error(String.format("MapQuest returned a non-OK response code: %s: %s",
                            statusLine.getStatusCode(),
                            statusLine.getReasonPhrase())).build();
                }
                final InputStream responseStream = response.getEntity().getContent();
                final JSONTokener jsonTokener = new JSONTokener(responseStream);
                final JSONObject jsonObject = new JSONObject(jsonTokener);
                if (jsonObject.has("results")
                        && jsonObject.getJSONArray("results").length() > 0
                        && jsonObject.getJSONArray("results").getJSONObject(0).has("locations")) {
                    final JSONArray locationResults = jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("locations");
                    if (locationResults.length() > 0) {
                        LOG.trace("API returned {} of results. If multiple, the first is used.", locationResults.length());
                        final JSONObject location = (JSONObject) locationResults.get(0);
                        if (location.has("latLng")) {
                            final double lat = location.getJSONObject("latLng").getDouble("lat");
                            final double lng = location.getJSONObject("latLng").getDouble("lng");
                            LOG.trace("API returned a result with valid long/lat fields: {}/{}", lng, lat);
                            return GeocoderResult.success(address, lng, lat).build();
                        } else {
                            LOG.trace("API returned a result which does not contain lon/lat fields: {}", location);
                        }
                    } else {
                        LOG.trace("API returned an empty result");
                    }
                }
            }
            LOG.debug("Couldn't resolve coordinates for address {}", address);
            return GeocoderResult.noResult(address).build();
        } catch (IOException e) {
            return GeocoderResult.error(e).build();
        }
    }

    @Override
    public GeocoderConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void validateConfiguration(Map<String, Object> properties) throws GeocoderConfigurationException {
        MapquestConfiguration.fromMap(properties).validate();
    }

    private String buildURL(String urlTemplate, String apiKey, String addressToResolve) throws UnsupportedEncodingException {
        Objects.requireNonNull(urlTemplate);
        Objects.requireNonNull(apiKey);
        Objects.requireNonNull(addressToResolve);
        final String url = urlTemplate.replaceAll("\\{apiKey\\}", apiKey)
                .replaceAll("\\{query\\}", URLEncoder.encode(addressToResolve, "UTF-8"));
        return url;
    }

}
