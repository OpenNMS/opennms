/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
