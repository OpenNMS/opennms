/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.ResponseHandlingUtils;
import org.opennms.core.wsman.utils.RetryNTimesLoop;
import org.opennms.netmgt.config.wsman.WsmanAgentConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.support.ParameterSubstitutingMonitor;
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

    public static final String RESOURCE_URI_PARAM = "resource-uri";

    public static final String RULE_PARAM = "rule";

    public static final String SELECTOR_PARAM_PREFIX = "selector.";

    private WSManClientFactory m_factory = new CXFWSManClientFactory();

    private WSManConfigDao m_wsManConfigDao;

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

        // Setup the WS-Man Client
        if (m_wsManConfigDao == null) {
            m_wsManConfigDao = BeanUtils.getBean("daoContext", "wsManConfigDao", WSManConfigDao.class);
        }
        final WsmanAgentConfig config = m_wsManConfigDao.getAgentConfig(svc.getAddress());
        final WSManEndpoint endpoint = WSManConfigDao.getEndpoint(config, svc.getAddress());
        final WSManClient client = m_factory.getClient(endpoint);
        final RetryNTimesLoop retryLoop = new RetryNTimesLoop(config.getRetry() != null ? config.getRetry() : 0);

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
