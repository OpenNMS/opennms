package org.opennms.netmgt.provision.support;

import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * Wrapper class for doing ref counting on NioSocketConnectors
 * @author Duncan Mackintosh
 *
 */
class ConnectorWrapper {
	NioSocketConnector m_connector = new NioSocketConnector();
	// Start with 0 references because we always increment
	int m_references = 0;
	
	ConnectorWrapper(int timeoutInMillis) {
		m_connector.setConnectTimeoutMillis(timeoutInMillis);
	}
	
	
}