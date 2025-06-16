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
package org.opennms.netmgt.snmp.internal;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.netmgt.snmp.ClassBasedStrategyResolver;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.StrategyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceBasedStrategyResolver implements StrategyResolver {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(ServiceBasedStrategyResolver.class);

    private static final ClassBasedStrategyResolver s_classBasedStrategyResolver = new ClassBasedStrategyResolver();

    private final Map<String, SnmpStrategy> m_strategies = new ConcurrentHashMap<String, SnmpStrategy>();

	public static ServiceBasedStrategyResolver register() {
		ServiceBasedStrategyResolver resolver = new ServiceBasedStrategyResolver();
		SnmpUtils.setStrategyResolver(resolver);
		return resolver;
	}

	public static void unregister() {
		SnmpUtils.unsetStrategyResolver();
	}

	public void onBind(SnmpStrategy strategy, Map<String, String> props) {
		String key = props.get("implementation");
		if (key == null) {
			LOG.error("SnmpStrategy class '{}' published as service with out 'implementation' key.  Ignoring.", strategy.getClass());
			return;
		}
		m_strategies.put(key, strategy);
	}
	
	public void onUnbind(SnmpStrategy operation, Map<String, String> props) {
		if (props != null) {
			String key = props.get("implementation");
			if (key != null) {
				m_strategies.remove(key);
			}
		}
	}

	@Override
	public SnmpStrategy getStrategy() {
		final String strategyClass = SnmpUtils.getStrategyClassName();
		final SnmpStrategy strategy = m_strategies.get(strategyClass);
		if (strategy == null) {
			if (m_strategies.isEmpty()) {
			    LOG.warn("There is no SnmpStrategy registered. Unable to find strategy "+strategyClass + ". Falling back to ClassBasedStrategyResolver.");
			    return s_classBasedStrategyResolver.getStrategy();
			} else {
				Map.Entry<String, SnmpStrategy> entry = m_strategies.entrySet().iterator().next();
				LOG.error("SnmpStrategy {} was not found! Using strategy {} instead!", strategyClass, entry.getKey());
				return entry.getValue();
			}
		}
		return strategy;
	}

	/**
	 * @return an immutable copy of of the strategies that are currently registered
	 */
	protected Map<String, SnmpStrategy> getStrategies() {
	    return Collections.unmodifiableMap(m_strategies);
	}

}
