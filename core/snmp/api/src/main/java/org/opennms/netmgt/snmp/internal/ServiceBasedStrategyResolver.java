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
	
	Map<String, SnmpStrategy> m_strategies = new ConcurrentHashMap<String, SnmpStrategy>();

	public void onBind(SnmpStrategy strategy, Map<String, String> props) {
		String key = props.get("implementation");
		if (key == null) {
			LOG.error("SnmpStrategy class published as service with out 'implementation' key.  Ignoring.");
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
