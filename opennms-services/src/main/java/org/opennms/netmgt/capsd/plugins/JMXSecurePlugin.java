package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JMXSecureConnectionFactory;

/**
 * <p>JMXSecurePlugin class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JMXSecurePlugin extends JMXPlugin
{

	/** {@inheritDoc} */
	public ConnectionWrapper getMBeanServerConnection(Map parameterMap, InetAddress address)
	{
		return JMXSecureConnectionFactory.getMBeanServerConnection(parameterMap, address);
	}

	/** {@inheritDoc} */
	public String getProtocolName(Map map)
	{
		return ParameterMap.getKeyedString(map, "friendly-name", "ssl-jmxmp");
	}

	/** {@inheritDoc} */
	public boolean isProtocolSupported(InetAddress address)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("username", "jmxmp");
		map.put("password", "jmxmp");
		map.put("friendly-name", "ssl-jmxmp");

		return isProtocolSupported(address, map);
	}
}
