package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.poller.Distributable;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JMXSecureConnectionFactory;

@Distributable
/**
 * <p>JMXSecureMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JMXSecureMonitor extends JMXMonitor
{
	/** {@inheritDoc} */
	public ConnectionWrapper getMBeanServerConnection(Map parameterMap, InetAddress address)
	{
		return JMXSecureConnectionFactory.getMBeanServerConnection(parameterMap, address);
	}
}
