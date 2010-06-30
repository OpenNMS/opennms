package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JMXSecureConnectionFactory;

/**
 * <p>JMXSecureCollector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JMXSecureCollector extends JMXCollector
{

	/**
	 * <p>Constructor for JMXSecureCollector.</p>
	 */
	public JMXSecureCollector()
	{
		//The value of serviceName will be assumed as a collection name
		//from jmx-datacollection-config.xml if no "collection"
		//parameter will be specified in collectd-configuration.xml.
		//Service name also will be the relative directory to store RRD data in
		//if "useFriendlyName" variable set to false.
		//If "useFriendlyName" variable set to true, RRD data will be stored in
		//directory specified by "friendly-name" parameter in collectd-configuration.xml.
		//If "friendly-name" is not specified in collectd-configuration.xml, then directory
		//name will be the "port" parameter.
		setServiceName("ssl-jmxmp");
		setUseFriendlyName(true);
	}

    /** {@inheritDoc} */
    public ConnectionWrapper getMBeanServerConnection(Map parameterMap, InetAddress address)
	{
		return JMXSecureConnectionFactory.getMBeanServerConnection(parameterMap, address);
	}
}
