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

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.ResponseHandlingUtils;
import org.opennms.core.wsman.utils.RetryNTimesLoop;
import org.opennms.netmgt.config.wsman.credentials.WsmanAgentConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.support.ParameterSubstitutingMonitor;
import org.opennms.netmgt.provision.detector.wsman.WsmanEndpointUtils;
import org.w3c.dom.Node;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

/**
 * WS-Man Monitor
 *
 * This monitor is not distributable since it relies on local configuration
 * provided by the {@link WSManConfigDao}.
 *
 * @author jwhite
 */
public class WsManMonitor extends ParameterSubstitutingMonitor {

    private static final String WSMAN_RETRY_KEY = "retry";

    public static final String RESOURCE_URI_PARAM = "resource-uri";

    public static final String RULE_PARAM = "rule";

    public static final String SELECTOR_PARAM_PREFIX = "selector.";

    private WSManClientFactory m_factory = new CXFWSManClientFactory();

    private WSManConfigDao m_wsManConfigDao;

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = super.getRuntimeAttributes(svc, parameters);

        if (m_wsManConfigDao == null) {
            m_wsManConfigDao = BeanUtils.getBean("daoContext", "wsManConfigDao", WSManConfigDao.class);
        }

        final WsmanAgentConfig config = m_wsManConfigDao.getAgentConfig(svc.getAddress());
        WsmanEndpointUtils.toMap(WSManConfigDao.getEndpoint(config, svc.getAddress())).forEach((key, value) -> runtimeAttributes.put(key, Interpolator.pleaseInterpolate(value)));

        runtimeAttributes.put(WSMAN_RETRY_KEY, config.getRetry());
        return runtimeAttributes;
    }

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // Fetch the monitor specific parameters
        final String resourceUri = getKeyedString(parameters, RESOURCE_URI_PARAM, null);
        if (resourceUri == null) {
            throw new IllegalArgumentException("'" + RESOURCE_URI_PARAM + "' parameter is required.");
        }

        final String rule = resolveKeyedString(parameters, RULE_PARAM, null);
        if (rule == null) {
            throw new IllegalArgumentException("'" + RULE_PARAM + "' parameter is required.");
        }

        final Map<String, String> selectors = Maps.newHashMap();
        for (Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getKey().startsWith(SELECTOR_PARAM_PREFIX)) {
                final String selectorKey = parameter.getKey().substring(SELECTOR_PARAM_PREFIX.length());
                final Object selectorValue = parameter.getValue();
                if (selectorValue == null) {
                    continue;
                }
                selectors.put(selectorKey, selectorValue instanceof String ? (String)selectorValue : selectorValue.toString());
            }
        }

        final WSManEndpoint endpoint;
        try {
            final Map<String, String> filteredMap = parameters.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
            endpoint = WsmanEndpointUtils.fromMap(filteredMap);
        } catch (MalformedURLException e) {
            return PollStatus.down(e.getMessage());
        }
        final WSManClient client = m_factory.getClient(endpoint);
        final RetryNTimesLoop retryLoop = new RetryNTimesLoop(ParameterMap.getKeyedInteger(parameters, WSMAN_RETRY_KEY, 0));

        // Issue a GET
        Node node = null;
        try {
            while (retryLoop.shouldContinue()) {
                try {
                    node = client.get(resourceUri, selectors);
                    break;
                } catch (WSManException e) {
                    retryLoop.takeException(e);
                }
            }
        } catch (WSManException e) {
            return PollStatus.down(e.getMessage());
        }

        if (node == null) {
            return PollStatus.down(String.format("No resource was found at URI: '%s' with selectors: '%s'.", resourceUri, selectors));
        }

        // Verify the results
        final ListMultimap<String, String> elementValues = ResponseHandlingUtils.toMultiMap(node);
        try {
            ResponseHandlingUtils.getMatchingIndex(rule, elementValues);
            // We've successfully matched an index
            return PollStatus.up();
        } catch (NoSuchElementException e) {
            return PollStatus.down(String.format("No index was matched by rule: '%s' with values '%s'.", rule, elementValues));
        }
    }
    
    public void setWSManConfigDao(WSManConfigDao wsManConfigDao) {
        m_wsManConfigDao = Objects.requireNonNull(wsManConfigDao);
    }

    public void setWSManClientFactory(WSManClientFactory factory) {
        m_factory = Objects.requireNonNull(factory);
    }
}
