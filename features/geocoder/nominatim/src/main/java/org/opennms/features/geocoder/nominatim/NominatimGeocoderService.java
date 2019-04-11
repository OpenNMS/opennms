/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.geocoder.nominatim;

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
import org.opennms.features.geocoder.GeocoderConfigurationException;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class NominatimGeocoderService implements GeocoderService {

    private static final Logger LOG = LoggerFactory.getLogger(NominatimGeocoderService.class);

    private final NominatimConfiguration configuration;

    public NominatimGeocoderService(NominatimConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public String getId() {
        return "nominatim";
    }

    @Override
    public GeocoderResult resolveAddress(final String address) throws GeocoderConfigurationException {
        configuration.validate();
        LOG.debug("Configuration: {}", configuration.asMap());
        try (HttpClientWrapper clientWrapper = HttpClientWrapper.create().dontReuseConnections()) {
            if (configuration.isUseSystemProxy()) {
                clientWrapper.useSystemProxySettings();
            }
            final String url = buildUrl(configuration.getEmailAddress(), address);
            final HttpUriRequest method = new HttpGet(url);
            if (!Strings.isNullOrEmpty(configuration.getUserAgent())) {
                method.addHeader("User-Agent", configuration.getUserAgent());
            }
            if (!Strings.isNullOrEmpty(configuration.getReferer())) {
                method.addHeader("Referer", configuration.getReferer());
            }

            try (CloseableHttpResponse response = clientWrapper.execute(method)) {
                final StatusLine statusLine = response.getStatusLine();
                LOG.trace("Invoking URL {} returned {}:{} => {}", url, statusLine.getStatusCode(), statusLine.getReasonPhrase(), statusLine.getStatusCode() == 200 ? "OK" : "NOK" );
                if (statusLine.getStatusCode() != 200) {
                    return GeocoderResult.error(String.format("Nominatim returned a non-OK response code: %s: %s",
                            statusLine.getStatusCode(),
                            statusLine.getReasonPhrase())).build();
                }
                final InputStream responseStream = response.getEntity().getContent();
                final JSONTokener tokener = new JSONTokener(responseStream);
                final JSONArray results = new JSONArray(tokener);
                if (results.length() > 0) {
                    LOG.trace("API returned {} of results. If multiple, the first is used.", results.length());
                    final JSONObject result = results.getJSONObject(0);
                    if (result.has("lat") && result.has("lon")) {
                        final Float longitude = result.getFloat("lon");
                        final Float latitude = result.getFloat("lat");
                        LOG.trace("API returned a result with valid long/lat fields: {}/{}", longitude, latitude);
                        return GeocoderResult.success(address, longitude, latitude).build();
                    } else {
                        LOG.trace("API returned a result which does not contain lon/lat fields: {}", result);
                    }
                } else {
                    LOG.trace("API returned an empty result");
                }
                LOG.debug("Couldn't resolve coordinates for address {}", address);
                return GeocoderResult.noResult(address).build();
            }
        } catch (IOException e) {
            return GeocoderResult.error(e).build();
        }
    }

    @Override
    public NominatimConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void validateConfiguration(Map<String, Object> properties) throws GeocoderConfigurationException {
        NominatimConfiguration.fromMap(properties).validate();
    }

    private String buildUrl(final String emailAddress, final String addressToResolve) throws UnsupportedEncodingException {
        Objects.requireNonNull(emailAddress);
        Objects.requireNonNull(addressToResolve);
        final String url = configuration.getUrlTemplate()
                                .replaceAll("\\{email\\}", URLEncoder.encode(emailAddress, "UTF-8"))
                                .replaceAll("\\{query\\}", URLEncoder.encode(addressToResolve, "UTF-8"));
        return url;
    }

}
