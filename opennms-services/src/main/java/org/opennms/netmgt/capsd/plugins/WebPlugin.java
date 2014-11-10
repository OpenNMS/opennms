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

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(WebPlugin.class);

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
    public String getProtocolName() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProtocolSupported(final InetAddress address, final Map<String, Object> map) {
        boolean retval=false;
        
        final HttpClientWrapper clientWrapper = HttpClientWrapper.create();

        try {
            final URIBuilder ub = new URIBuilder();
            ub.setScheme(ParameterMap.getKeyedString(map, "scheme", DEFAULT_SCHEME));
            ub.setHost(InetAddressUtils.str(address));
            ub.setPort(ParameterMap.getKeyedInteger(map, "port", DEFAULT_PORT));
            ub.setPath( ParameterMap.getKeyedString(map, "path", DEFAULT_PATH));
            final HttpGet getMethod = new HttpGet(ub.build());

            clientWrapper.setConnectionTimeout(ParameterMap.getKeyedInteger(map,"timeout", DEFAULT_TIMEOUT))
                    .setSocketTimeout(ParameterMap.getKeyedInteger(map,"timeout", DEFAULT_TIMEOUT))
                    .setUserAgent(ParameterMap.getKeyedString(map,"user-agent",DEFAULT_USER_AGENT))
                    .setVirtualHost(ParameterMap.getKeyedString(map,"virtual-host", InetAddressUtils.str(address)));

            if(ParameterMap.getKeyedBoolean(map, "http-1.0", false)) {
                getMethod.setProtocolVersion(HttpVersion.HTTP_1_0);
            }

            for(final Object okey : map.keySet()) {
                final String key = okey.toString();
                if(key.matches("header_[0-9]+$")){
                    String headerName  = ParameterMap.getKeyedString(map,key,null);
                    String headerValue = ParameterMap.getKeyedString(map,key + "_value",null);
                    getMethod.setHeader(headerName, headerValue);
                }
            }

            if(ParameterMap.getKeyedBoolean(map,"auth-enabled",false)){
                clientWrapper.addBasicCredentials(ParameterMap.getKeyedString(map, "auth-user", DEFAULT_USER), ParameterMap.getKeyedString(map, "auth-password", DEFAULT_PASSWORD));
                if (ParameterMap.getKeyedBoolean(map, "auth-preemptive", true)) {
                    clientWrapper.usePreemptiveAuth();
                }
            }

            final CloseableHttpResponse response = clientWrapper.execute(getMethod);
            final Integer statusCode = response.getStatusLine().getStatusCode();

            final String expectedText = ParameterMap.getKeyedString(map,"response-text",null);

            if(!inRange(ParameterMap.getKeyedString(map, "response-range", DEFAULT_HTTP_STATUS_RANGE),statusCode)){
                retval=false;
            }
            else {
                retval=true;
            }

            if (expectedText!=null) {
                String responseText = EntityUtils.toString(response.getEntity());
                if(expectedText.charAt(0)=='~'){
                    if(!responseText.matches(expectedText.substring(1)))
                        retval=false;
                    else 
                        retval=true;                
                }
                else {

                    if(responseText.equals(expectedText)){
                        retval=true;
                    }
                    else
                        retval=false;
                }


            }

        } catch (final IOException e) {
            LOG.info(e.getMessage(), e);
            retval = false;
        } catch (final URISyntaxException e) {
            LOG.info(e.getMessage(), e);
            retval = false;
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }

        return retval;
    }

    private boolean inRange(final String range, final Integer val) {
        final String[] boundries = range.split("-");
        if(val < Integer.valueOf(boundries[0]) || val > Integer.valueOf(boundries[1])) {
            return false;
        } else {
            return true;
        }
    }

}
