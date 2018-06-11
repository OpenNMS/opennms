/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.opennms.core.utils.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.utils.http.HttpResponseRange;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.http.HttpNorthbounderConfig.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards north bound alarms via HTTP.
 * <p>FIXME: Needs lots of work still :(</p>
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class HttpNorthbounder extends AbstractNorthbounder {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(HttpNorthbounder.class);

    /** The configuration. */
    private HttpNorthbounderConfig m_config;

    /**
     * Instantiates a new http northbounder.
     */
    protected HttpNorthbounder() {
        super("HttpNorthbounder");
    }

    //FIXME: This should be wired with Spring but is implmented as was in the PSM
    // Make sure that the {@link EmptyKeyRelaxedTrustSSLContext} algorithm
    // is available to JSSE
    static {
        //this is a safe call because the method returns -1 if it is already installed (by PageSequenceMonitor, etc.)
        java.security.Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }


    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#accepts(org.opennms.netmgt.alarmd.api.NorthboundAlarm)
     */
    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        if (m_config.getAcceptableUeis() == null || m_config.getAcceptableUeis().contains(alarm.getUei())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#forwardAlarms(java.util.List)
     */
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        LOG.info("Forwarding {} alarms", alarms.size());

        //Need a configuration bean for these

        int connectionTimeout = 3000;
        int socketTimeout = 3000;
        Integer retryCount = Integer.valueOf(3);

        URI uri = m_config.getURI();

        final HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .setConnectionTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .setRetries(retryCount)
                .useBrowserCompatibleCookies();

        if (m_config.getVirtualHost() != null && !m_config.getVirtualHost().trim().isEmpty()) {
            clientWrapper.setVirtualHost(m_config.getVirtualHost());
        }
        if (m_config.getUserAgent() != null && !m_config.getUserAgent().trim().isEmpty()) {
            clientWrapper.setUserAgent(m_config.getUserAgent());
        }

        if ("https".equals(uri.getScheme())) {
            try {
                clientWrapper.useRelaxedSSL("https");
            } catch (final GeneralSecurityException e) {
                throw new NorthbounderException("Failed to configure HTTP northbounder for relaxed SSL.", e);
            }
        }

        HttpUriRequest method = null;

        if (HttpMethod.POST == (m_config.getMethod())) {
            HttpPost postMethod = new HttpPost(uri);

            //TODO: need to configure these
            List<NameValuePair> postParms = new ArrayList<>();

            //FIXME:do this for now
            NameValuePair p = new BasicNameValuePair("foo", "bar");
            postParms.add(p);

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParms, StandardCharsets.UTF_8);
            postMethod.setEntity(formEntity);

            HttpEntity entity = null;
            try {
                //I have no idea what I'm doing here ;)
                entity = new StringEntity("XML HERE");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            postMethod.setEntity(entity);

            method = postMethod;
        } else if (HttpMethod.GET == m_config.getMethod()) {

            //TODO: need to configure these
            //List<NameValuePair> getParms = null;
            method = new HttpGet(uri);
        }
        HttpVersion httpVersion = determineHttpVersion(m_config.getHttpVersion());        
        clientWrapper.setVersion(httpVersion);

        HttpResponse response = null;
        try {
            response = clientWrapper.execute(method);

            int code = response.getStatusLine().getStatusCode();
            HttpResponseRange range = new HttpResponseRange("200-399");
            if (!range.contains(code)) {
                LOG.debug("response code out of range for uri:{}.  Expected {} but received {}", uri, range, code);
                throw new NorthbounderException("response code out of range for uri:" + uri + ".  Expected " + range + " but received " + code);
            }
            LOG.debug("HTTP Northbounder received response: {}", response.getStatusLine().getReasonPhrase());
        } catch (final ClientProtocolException e) {
            throw new NorthbounderException(e);
        } catch (final IOException e) {
            throw new NorthbounderException(e);
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }
    }

    /**
     * Determine HTTP version.
     *
     * @param version the version
     * @return the HTTP version
     */
    private static HttpVersion determineHttpVersion(String version) {
        HttpVersion httpVersion = null;
        if ("1.0".equals(version)) {
            httpVersion = HttpVersion.HTTP_1_0;
        } else {
            httpVersion = HttpVersion.HTTP_1_1;
        }
        return httpVersion;
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    public HttpNorthbounderConfig getConfig() {
        return m_config;
    }

    /**
     * Sets the configuration.
     *
     * @param config the new configuration
     */
    public void setConfig(HttpNorthbounderConfig config) {
        m_config = config;
    }

}
