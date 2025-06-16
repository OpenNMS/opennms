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

import static org.opennms.core.web.HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.hawkular.agent.prometheus.PrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.text.TextPrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.walkers.MetricCollectingWalker;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.collection.api.ServiceParameters;

public class PrometheusScraper {
    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final int DEFAULT_SO_TIMEOUT_MS = 10000;

    private static final String HEADER_PREFIX_PARM_KEY = "header-";
    public static final String DEFAULT_ACCEPT_HEADER = "application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1";

    public static void scrape(URI uri, Map<String, Object> parameters, MetricCollectingWalker walker) throws IOException {
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
                PrometheusMetricsProcessor<?> processor = new TextPrometheusMetricsProcessor(entity.getContent(), walker);
                processor.walk();
            }
        }
    }

    public static HttpClientWrapper createHttpClientFromParmMap(Map<String, Object> parameters) {
        // Timeouts and retries
        HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .setConnectionTimeout(ParameterMap.getKeyedInteger(parameters, ServiceParameters.ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT_MS))
                .setSocketTimeout(ParameterMap.getKeyedInteger(parameters, ServiceParameters.ParameterName.TIMEOUT.toString(), DEFAULT_SO_TIMEOUT_MS))
                .setRetries(ParameterMap.getKeyedInteger(parameters, ServiceParameters.ParameterName.RETRY.toString(), DEFAULT_RETRY_COUNT));
        // Proxy support
        setUseSystemProxyIfDefined(clientWrapper, parameters);
        return clientWrapper;
    }
}
