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

package org.opennms.features.geocoder.google;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class GoogleGeocoderService implements GeocoderService {
    private int timeout; // in ms
    private boolean useEnterpriseCredentials;
    private boolean useSystemProxy;
    private String clientId;
    private String signature;
    private String apiKey;

    @Override
    public String getId() {
        return "google";
    }

    @Override
    public synchronized GeocoderResult resolveAddress(final String address) throws GeocoderException {
        final GeoApiContext.Builder builder = new GeoApiContext.Builder()
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .maxRetries(1);
        if (useSystemProxy) {
            try {
                final Proxy proxy = selectProxy("https://maps.googleapis.com");
                builder.proxy(proxy);
            } catch (URISyntaxException e) {
                return new GeocoderResult(e);
            }
        }
        if (useEnterpriseCredentials) {
            builder.enterpriseCredentials(clientId, signature);
        } else {
            builder.apiKey(apiKey);
        }
        try {
            final GeoApiContext context = builder.build();
            final GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results.length == 0) {
                return new GeocoderResult(new GeocoderException("No result found")); // TODO MVR maybe empty response?
            }
            final LatLng location = results[0].geometry.location;
            return new GeocoderResult(new Coordinates(location.lng, location.lat));
        } catch (ApiException e) {
            return new GeocoderResult(e);
        } catch (InterruptedException e) {
            return new GeocoderResult(e);
        } catch (IOException e) {
            return new GeocoderResult(e);
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", timeout);
        properties.put("useEnterpriseCredentials", useEnterpriseCredentials);
        properties.put("clientId", clientId);
        properties.put("signature", signature);
        properties.put("apiKey", apiKey);
        properties.put("useSystemProxy", useSystemProxy);
        return properties;
    }

    private Proxy selectProxy(String targetUri) throws URISyntaxException {
        final List<Proxy> proxies = ProxySelector.getDefault().select(new URI(targetUri));
        return proxies.stream()
                .filter(proxy -> proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP)
                .findFirst()
                .orElse(Proxy.NO_PROXY);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setUseEnterpriseCredentials(boolean useEnterpriseCredentials) {
        this.useEnterpriseCredentials = useEnterpriseCredentials;
    }

    public void setUseSystemProxy(boolean useSystemProxy) {
        this.useSystemProxy = useSystemProxy;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
