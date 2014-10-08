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

import java.security.InvalidKeyException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geocoder.TemporaryGeocoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.geocoder.AdvancedGeoCoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;

public class GoogleGeocoderService implements GeocoderService {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleGeocoderService.class);

    private AdvancedGeoCoder m_geocoder = null;
    private String m_clientId = null;
    private String m_clientKey = null;

    public GoogleGeocoderService() {
    }

    public void setClientId(final String clientId) {
        m_clientId = clientId;
    }

    public void setClientKey(final String clientKey) {
        m_clientKey = clientKey;
    }

    public void ensureInitialized() throws GeocoderException {
        if (m_geocoder == null) {
            if (notEmpty(m_clientId) && notEmpty(m_clientKey)) {
                try {
                    LOG.info("Initializing Google Geocoder using Client ID and Key.");
                    m_geocoder = new AdvancedGeoCoder(m_clientId, m_clientKey);
                } catch (final InvalidKeyException e) {
                    throw new GeocoderException("Unable to initialize Google Geocoder.", e);
                }
            }

            if (m_geocoder == null) {
                LOG.info("Initializing Google Geocoder using default configuration.");
                m_geocoder = new AdvancedGeoCoder();
            }

            final HttpClient httpClient = m_geocoder.getHttpClient();

            /* Configure proxying, if necessary... */
            final String httpProxyHost = System.getProperty("http.proxyHost");
            final Integer httpProxyPort = Integer.getInteger("http.proxyPort");
            if (httpProxyHost != null && httpProxyPort != null) {
                LOG.info("Proxy configuration found, using {}:{} as HTTP proxy.", httpProxyHost, httpProxyPort);
                httpClient.getHostConfiguration().setProxy(httpProxyHost, httpProxyPort);
            } else {
                LOG.info("No proxy configuration found.");
            }

            /* Limit retries... */
            httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, true));

            LOG.info("Google Geocoder initialized.");
        }
    }

    private boolean notEmpty(final String value) {
        return value != null && !"".equals(value);
    }

    @Override
    public synchronized Coordinates getCoordinates(final String address) throws GeocoderException {
        ensureInitialized();

        final GeocoderRequest request = new GeocoderRequestBuilder().setAddress(address).setLanguage("en").getGeocoderRequest();
        final GeocodeResponse response = m_geocoder.geocode(request);

        switch (response.getStatus()) {
        case OK:
            return new GoogleCoordinates(response.getResults().get(0));
        case OVER_QUERY_LIMIT:
            throw new TemporaryGeocoderException("Failed to get coordinates for " + address + " using the Google Geocoder.  You have exceeded the daily usage limit.");
        case ERROR:
        case INVALID_REQUEST:
        case REQUEST_DENIED:
        case UNKNOWN_ERROR:
        case ZERO_RESULTS:
        default:
            throw new GeocoderException("Failed to get coordinates for " + address + " using Google Geocoder.  Response was: " + response.getStatus().toString());
        }
    }
}
