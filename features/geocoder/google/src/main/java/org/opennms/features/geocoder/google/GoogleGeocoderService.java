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
package org.opennms.features.geocoder.google;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.opennms.features.geocoder.GeocoderConfigurationException;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class GoogleGeocoderService implements GeocoderService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleGeocoderService.class);

    private final GoogleConfiguration configuration;

    public GoogleGeocoderService(GoogleConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public String getId() {
        return "google";
    }

    @Override
    public synchronized GeocoderResult resolveAddress(final String address) throws GeocoderConfigurationException {
        configuration.validate();
        LOG.debug("Configuration: {}", configuration.asMap());
        final GeoApiContext.Builder builder = new GeoApiContext.Builder()
                .readTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .maxRetries(1);
        if (configuration.isUseSystemProxy()) {
            final String targetUrl = "https://maps.googleapis.com";
            try {
                final Proxy proxy = selectProxy(targetUrl);
                builder.proxy(proxy);
            } catch (URISyntaxException e) {
                return GeocoderResult.error("Couldn't find proxy for URL: '" + targetUrl + "'").build();
            }
        }
        if (configuration.isUseEnterpriseCredentials()) {
            builder.enterpriseCredentials(configuration.getClientId(), configuration.getSignature());
        } else {
            builder.apiKey(configuration.getApiKey());
        }
        try {
            final GeoApiContext context = builder.build();
            final GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            LOG.trace("API returned {} results. If multiple, first is used.", results.length);
            if (results.length > 0 && results[0].geometry != null && results[0].geometry.location != null) {
                final LatLng location = results[0].geometry.location;
                LOG.trace("API returned a result with valid long/lat fields: {}/{}", location.lng, location.lat);
                return GeocoderResult.success(address, location.lng, location.lat).build();
            } else {
                LOG.debug("API returned a result, but long/lat fields were missing: {}", results[0]);
            }
            LOG.debug("Couldn't resolve coordinates for address {}", address);
            return GeocoderResult.noResult(address).build();
        } catch (ApiException | InterruptedException | IOException e) {
            return GeocoderResult.error(e).build();
        }
    }

    @Override
    public GoogleConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void validateConfiguration(Map<String, Object> properties) throws GeocoderConfigurationException {
        GoogleConfiguration.fromMap(properties).validate();
    }

    private Proxy selectProxy(String targetUri) throws URISyntaxException {
        final List<Proxy> proxies = ProxySelector.getDefault().select(new URI(targetUri));
        return proxies.stream()
                .filter(proxy -> proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP)
                .findFirst()
                .orElse(Proxy.NO_PROXY);
    }
}
