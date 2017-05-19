/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>WebMonitor class.</p>
 *
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="mailto:cliles@capario.com">Chris Liles</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
@Distributable
public class WebMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(WebMonitor.class);
    static Integer DEFAULT_TIMEOUT = 3000;
    static Integer DEFAULT_PORT = 80;
    static String DEFAULT_USER_AGENT = "OpenNMS WebMonitor";
    static String DEFAULT_PATH = "/";
    static String DEFAULT_USER = "admin";
    static String DEFAULT_PASSWORD = "admin";
    static String DEFAULT_HTTP_STATUS_RANGE = "100-399";
    static String DEFAULT_SCHEME = "http";

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String,Object> map) {
        PollStatus pollStatus = PollStatus.unresponsive();
        HttpClientWrapper clientWrapper = HttpClientWrapper.create();

        try {
            final String hostAddress = InetAddressUtils.str(svc.getAddress());

            URIBuilder ub = new URIBuilder();
            ub.setScheme(ParameterMap.getKeyedString(map, "scheme", DEFAULT_SCHEME));
            ub.setHost(hostAddress);
            ub.setPort(ParameterMap.getKeyedInteger(map, "port", DEFAULT_PORT));
            ub.setPath(ParameterMap.getKeyedString(map, "path", DEFAULT_PATH));

            String queryString = ParameterMap.getKeyedString(map,"queryString",null);
            if (queryString != null && !queryString.trim().isEmpty()) {
                final List<NameValuePair> params = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
                if (!params.isEmpty()) {
                    ub.setParameters(params);
                }
            }

            final HttpGet getMethod = new HttpGet(ub.build());
            clientWrapper.setConnectionTimeout(ParameterMap.getKeyedInteger(map, "timeout", DEFAULT_TIMEOUT))
                .setSocketTimeout(ParameterMap.getKeyedInteger(map, "timeout", DEFAULT_TIMEOUT));

            final String userAgent = ParameterMap.getKeyedString(map,"user-agent",DEFAULT_USER_AGENT);
            if (userAgent != null && !userAgent.trim().isEmpty()) {
                clientWrapper.setUserAgent(userAgent);
            }

            final String virtualHost = ParameterMap.getKeyedString(map,"virtual-host", hostAddress);
            if (virtualHost != null && !virtualHost.trim().isEmpty()) {
                clientWrapper.setVirtualHost(virtualHost);
            }

            if(ParameterMap.getKeyedBoolean(map, "http-1.0", false)) {
                clientWrapper.setVersion(HttpVersion.HTTP_1_0);
            }

            for(final Object okey : map.keySet()) {
                final String key = okey.toString();
                if(key.matches("header_[0-9]+$")){
                    final String headerName  = ParameterMap.getKeyedString(map,key,null);
                    final String headerValue = ParameterMap.getKeyedString(map,key + "_value",null);
                    getMethod.setHeader(headerName, headerValue);
                }
            }

            if (ParameterMap.getKeyedBoolean(map, "use-ssl-filter", false)) {
                clientWrapper.trustSelfSigned(ParameterMap.getKeyedString(map, "scheme", DEFAULT_SCHEME));
            }

            if(ParameterMap.getKeyedBoolean(map,"auth-enabled",false)) {
                clientWrapper.addBasicCredentials(ParameterMap.getKeyedString(map, "auth-user", DEFAULT_USER), ParameterMap.getKeyedString(map, "auth-password", DEFAULT_PASSWORD));
                if (ParameterMap.getKeyedBoolean(map, "auth-preemptive", true)) {
                    clientWrapper.usePreemptiveAuth();
                }
            }

            LOG.debug("getMethod parameters: {}", getMethod);
            CloseableHttpResponse response = clientWrapper.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            String statusText = response.getStatusLine().getReasonPhrase();
            String expectedText = ParameterMap.getKeyedString(map,"response-text",null);

            LOG.debug("returned results are:");

            if(!inRange(ParameterMap.getKeyedString(map, "response-range", DEFAULT_HTTP_STATUS_RANGE),statusCode)){
                pollStatus = PollStatus.unavailable(statusText);
            }
            else {
                pollStatus = PollStatus.available();
            }

            if (expectedText!=null){
                String responseText = EntityUtils.toString(response.getEntity()); 
                if(expectedText.charAt(0)=='~'){
                    if(!responseText.matches(expectedText.substring(1))){
                        pollStatus = PollStatus.unavailable("Regex Failed");
                    }
                    else 
                        pollStatus = PollStatus.available();
                }
                else {
                    if(expectedText.equals(responseText))
                        pollStatus = PollStatus.available();
                    else
                        pollStatus = PollStatus.unavailable("Did not find expected Text");
                }
            }

        } catch (IOException e) {
            LOG.info(e.getMessage());
            pollStatus = PollStatus.unavailable(e.getMessage());
        } catch (URISyntaxException e) {
            LOG.info(e.getMessage());
            pollStatus = PollStatus.unavailable(e.getMessage());
        } catch (GeneralSecurityException e) {
            LOG.error("Unable to set SSL trust to allow self-signed certificates", e);
            pollStatus = PollStatus.unavailable("Unable to set SSL trust to allow self-signed certificates");
        } catch (Throwable e) {
            LOG.error("Unexpected exception while running " + getClass().getName(), e);
            pollStatus = PollStatus.unavailable("Unexpected exception: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }
        return pollStatus;
    }

    private boolean inRange(String range,Integer val){
        String[] boundries = range.split("-");
        if(val < Integer.valueOf(boundries[0]) || val > Integer.valueOf(boundries[1]))
            return false;
        else
            return true;
    }
}
