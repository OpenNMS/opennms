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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.ReadonlyContext;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.jexl.OnmsJexlEngine;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.jmx.JmxUtils;
import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxConnectors;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.impl.connection.connectors.DefaultConnectionManager;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.jmx.wrappers.ObjectNameWrapper;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;

/**
 * This class computes the response time of making a connection to the remote
 * server. If the connection is successful the reponse time RRD is updated.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class JMXMonitor extends AbstractServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(JMXMonitor.class);

    private static final OnmsJexlEngine JEXL_ENGINE;

    public static final String PARAM_BEAN_PREFIX = "beans.";
    public static final String PARAM_TEST_PREFIX = "tests.";
    public static final String PARAM_TEST = "test";
    public static final String PARAM_PORT = "port";

    static {
        JEXL_ENGINE = new OnmsJexlEngine();
        JEXL_ENGINE.white(ObjectNameWrapper.class.getName());
        JEXL_ENGINE.white(String.class.getName());
    }

    private class Timer {

        private long startTime;

        private Timer() {
            reset();
        }

        public void reset() {
            this.startTime = System.nanoTime();
        }

        public long getStartTime() {
            return startTime;
        }
    }

    private final Supplier<JmxConfigDao> jmxConfigDao = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "jmxConfigDao", JmxConfigDao.class));

    protected JmxConnectors getConnectionName() {
        return JmxConnectors.DEFAULT;
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        Map<String, String> convert = new HashMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            convert.put(entry.getKey(), (String) entry.getValue());
        }
        Map<String, String> attributes = JmxUtils.getRuntimeAttributes(jmxConfigDao.get(), InetAddressUtils.str(svc.getAddress()), convert);
        return new HashMap<>(Interpolator.pleaseInterpolate(attributes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> map) {
        final InetAddress ipv4Addr = svc.getAddress();

        PollStatus serviceStatus = PollStatus.unavailable();
        try {
            final Timer timer = new Timer();
            final JmxConnectionManager connectionManager = new DefaultConnectionManager(
                    ParameterMap.getKeyedInteger(map, "retry", 3));
            final JmxConnectionManager.RetryCallback retryCallback = new JmxConnectionManager.RetryCallback() {
                @Override
                public void onRetry() {
                    timer.reset();
                }
            };

            try (JmxServerConnectionWrapper connection = connectionManager.connect(getConnectionName(), ipv4Addr,
                    JmxUtils.convertToStringMap(map), retryCallback)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("connected to JMX server {} on {}", getConnectionName(), InetAddressUtils.str(ipv4Addr));
                }

                // Start with simple communication
                connection.getMBeanServerConnection().getMBeanCount();

                // Take time just here to get not influenced by test execution
                // time
                final long nanoResponseTime = System.nanoTime() - timer.getStartTime();

                // Find all variable definitions
                final Map<String, Object> variables = Maps.newHashMap();
                for (final String key : map.keySet()) {
                    // Skip fast if it does not start with the prefix
                    if (!key.startsWith(PARAM_BEAN_PREFIX)) {
                        continue;
                    }

                    // Get the variable name
                    final String variable = key.substring(PARAM_BEAN_PREFIX.length());

                    // Get the variable definition
                    final String definition = ParameterMap.getKeyedString(map, key, null);

                    // Store wrapper for variable definition
                    variables.put(variable,
                            ObjectNameWrapper.create(connection.getMBeanServerConnection(), definition));
                }

                // Find all test definitions
                final Map<String, Expression> tests = Maps.newHashMap();
                for (final String key : map.keySet()) {
                    // Skip fast if it does not start with the prefix
                    if (!key.startsWith(PARAM_TEST_PREFIX)) {
                        continue;
                    }

                    // Get the test name
                    final String variable = key.substring(PARAM_TEST_PREFIX.length());

                    // Get the test definition
                    final String definition = ParameterMap.getKeyedString(map, key, null);

                    // Build the expression from the definition
                    final Expression expression = JEXL_ENGINE.createExpression(definition);

                    // Store expressions
                    tests.put(variable, expression);
                }

                // Also handle a single test
                if (map.containsKey(PARAM_TEST)) {
                    // Get the test definition
                    final String definition = ParameterMap.getKeyedString(map, PARAM_TEST, null);

                    // Build the expression from the definition
                    final Expression expression = JEXL_ENGINE.createExpression(definition);

                    // Store expressions
                    tests.put(null, expression);
                }

                // Build the context for all tests
                final JexlContext context = new ReadonlyContext(new MapContext(variables));
                serviceStatus = PollStatus.up(nanoResponseTime / 1000000.0);

                // Execute all tests
                for (final Map.Entry<String, Expression> e : tests.entrySet()) {
                    try {
                        if (!(boolean) e.getValue().evaluate(context)) {
                            serviceStatus = PollStatus.down("Test failed: " + e.getKey());
                            break;
                        }
                    } catch (final Throwable t) {
                        LOG.warn("failed to execute test {}", e.getKey(), t);
                        serviceStatus = PollStatus.down("Test failed: " + e.getKey());
                        break;
                    }
                }

            } catch (JmxServerConnectionException mbse) {
                // Number of retries exceeded
                String reason = "IOException while polling address: " + ipv4Addr;
                LOG.debug(reason, mbse);
                serviceStatus = PollStatus.unavailable(reason);
            }
        } catch (Throwable e) {
            String reason = "Monitor - failed! " + InetAddressUtils.str(ipv4Addr);
            LOG.debug(reason, e);
            serviceStatus = PollStatus.unavailable(reason);
        }
        return serviceStatus;
    }
}
