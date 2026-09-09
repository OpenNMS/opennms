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
package org.opennms.netmgt.collectd.prometheus;

import static org.opennms.core.web.HttpClientWrapperConfigHelper.setSSLContextIfConfigured;
import static org.opennms.core.web.HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.hawkular.agent.prometheus.PrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.text.OpenMetricsProcessor;
import org.hawkular.agent.prometheus.text.TextPrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.walkers.PrometheusMetricsWalker;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;

public class PrometheusScraper {
    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final int DEFAULT_SO_TIMEOUT_MS = 10000;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusScraper.class);

    private static final String HEADER_PREFIX_PARM_KEY = "header-";
    public static final String DEFAULT_ACCEPT_HEADER = "application/openmetrics-text;version=1.0.0, text/plain;version=0.0.4;q=0.5, */*;q=0.1";
    
    //Be lenient with the OpenMetrics Content-Type header value and don't include the version 
    static final MediaType OPENMETRICS_LENIENT = MediaType.parse("application/openmetrics-text");
    static final MediaType PROMETHEUS_V0_0_4 = MediaType.parse("text/plain;version=0.0.4");

    public static void scrape(URI uri, Map<String, Object> parameters, PrometheusMetricsWalker walker) throws IOException {
        try (HttpClientWrapper httpClientWrapper = createHttpClientFromParmMap(parameters)) {
            final HttpGet get = new HttpGet(uri);
            get.setHeader(HttpHeaders.ACCEPT, DEFAULT_ACCEPT_HEADER);
            // Derive additional headers from the parameter map
            parameters.forEach((k,v) -> {
                if (k.startsWith(HEADER_PREFIX_PARM_KEY) && v instanceof String) {
                    String headerName = k.substring(HEADER_PREFIX_PARM_KEY.length());
                    get.setHeader(headerName, (String)v);
                }
            });

            try(CloseableHttpResponse response = httpClientWrapper.execute(get)) {
                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new IOException("No HTTP response entity from URL " + uri);
                }                
                MediaType mediaType = PROMETHEUS_V0_0_4; // default: header absent or unparseable  
                Header contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);                 
                if (contentType != null) {
                    try {                        
                        mediaType = MediaType.parse(contentType.getValue());
                    }
                    catch (IllegalArgumentException e) {
                        LOGGER.warn("Invalid Content-Type '{}' from {} - assuming Prometheus text format",  
                                contentType.getValue(), uri);
                    } 
                } else {
                    LOGGER.warn("No Content-Type header from {} - assuming Prometheus text format", uri);
                }
                PrometheusMetricsProcessor<?> processor;
                if(mediaType.is(OPENMETRICS_LENIENT)) {
                    LOGGER.debug("Processing response as OpenMetrics v1 format");
                    processor = new OpenMetricsProcessor(entity.getContent(), walker);
                }
                else {
                    LOGGER.debug("Processing response as Prometheus text format (v0.0.4)");
                    processor = new TextPrometheusMetricsProcessor(entity.getContent(), walker);
                }
                processor.walk();
            }
        }
    }


    public static HttpClientWrapper createHttpClientFromParmMap(Map<String, Object> parameters) throws IOException {
        // Timeouts and retries
        HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .setConnectionTimeout(ParameterMap.getKeyedInteger(parameters, ServiceParameters.ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT_MS))
                .setSocketTimeout(ParameterMap.getKeyedInteger(parameters, ServiceParameters.ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT_MS))
                .setRetries(ParameterMap.getKeyedInteger(parameters, ServiceParameters.ParameterName.RETRY.toString(), DEFAULT_RETRY_COUNT));
        // Proxy support
        setUseSystemProxyIfDefined(clientWrapper, parameters);
        // Custom trust anchors and/or client certificate (mutual TLS)
        try {
            setSSLContextIfConfigured(clientWrapper, parameters);
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to configure TLS from the service parameters", e);
        }
        return clientWrapper;
    }
}
