package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JMXSecureConnectionFactory;

public class JMXSecurePlugin extends JMXPlugin
{

	public ConnectionWrapper getMBeanServerConnection(Map parameterMap, InetAddress address)
	{
		return JMXSecureConnectionFactory.getMBeanServerConnection(parameterMap, address);
	}

	public String getProtocolName(Map map)
	{
		return ParameterMap.getKeyedString(map, "friendly-name", "ssl-jmxmp");
	}

	public boolean isProtocolSupported(InetAddress address)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("username", "jmxmp");
		map.put("password", "jmxmp");
		map.put("friendly-name", "ssl-jmxmp");

		return isProtocolSupported(address, map);
	}
}
