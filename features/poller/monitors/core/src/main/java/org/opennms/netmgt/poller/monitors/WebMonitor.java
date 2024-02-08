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
package org.opennms.netmgt.poller.monitors;

import static org.opennms.core.web.HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined;

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
        setUseSystemProxyIfDefined(clientWrapper, map);

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
            final long start = System.currentTimeMillis();
            CloseableHttpResponse response = clientWrapper.execute(getMethod);
            final long end = System.currentTimeMillis();
            int statusCode = response.getStatusLine().getStatusCode();
            String statusText = response.getStatusLine().getReasonPhrase();
            String expectedText = ParameterMap.getKeyedString(map,"response-text",null);

            final double responseTime = (end - start);

            LOG.debug("returned results are:");

            if(!inRange(ParameterMap.getKeyedString(map, "response-range", DEFAULT_HTTP_STATUS_RANGE),statusCode)){
                pollStatus = PollStatus.unavailable(statusText);
            }
            else {
                pollStatus = PollStatus.available(responseTime);
            }

            if (expectedText!=null){
                String responseText = EntityUtils.toString(response.getEntity()); 
                if(expectedText.charAt(0)=='~'){
                    if(!responseText.matches(expectedText.substring(1))){
                        pollStatus = PollStatus.unavailable("Regex Failed");
                    }
                    else 
                        pollStatus = PollStatus.available(responseTime);
                }
                else {
                    if(expectedText.equals(responseText))
                        pollStatus = PollStatus.available(responseTime);
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
