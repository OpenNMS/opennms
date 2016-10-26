/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.StrategyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceBasedStrategyResolver implements StrategyResolver {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(ServiceBasedStrategyResolver.class);
	
	public static ServiceBasedStrategyResolver register() {
		ServiceBasedStrategyResolver resolver = new ServiceBasedStrategyResolver();
		SnmpUtils.setStrategyResolver(resolver);
		return resolver;
	}
	
	public static void unregister() {
		SnmpUtils.unsetStrategyResolver();
	}
	
	Map<String, SnmpStrategy> m_strategies = new ConcurrentHashMap<String, SnmpStrategy>();

	public void onBind(SnmpStrategy strategy, Map<String, String> props) {
		String key = props.get("implementation");
		if (key == null) {
			LOG.error("SnmpStrategy class published as service with out 'implementation' key.  Ignoring.");
			return;
		}
		m_strategies.put(key, strategy);
	}
	
	public void onUnbind(SnmpStrategy operation, Map<String, String> props) {
		String key = props.get("implementation");
		if (key != null) {
			m_strategies.remove(key);
		}
	}
	
	@Override
	public SnmpStrategy getStrategy() {
		String strategyClass = SnmpUtils.getStrategyClassName();
		SnmpStrategy strategy = m_strategies.get(strategyClass);
		if (strategy == null) {
			if (m_strategies.isEmpty()) {
				throw new RuntimeException("There is no SnmpStrategy registered.  Unable to find strategy "+strategyClass);
			} else {
				Map.Entry<String, SnmpStrategy> entry = m_strategies.entrySet().iterator().next();
				LOG.error("SnmpStrategy {} was not found! Using strategy {} instead!", strategyClass, entry.getKey());
				strategy = entry.getValue();
			}
		}
		return strategy;
	}
	
	

}
