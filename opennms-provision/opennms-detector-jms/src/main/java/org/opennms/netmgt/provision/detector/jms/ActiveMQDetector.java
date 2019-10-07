/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.jms;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.AgentBasedSyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ActiveMQDetector class.
 * </p>
 *
 * @author roskens
 * @version $Id: $
 */

public class ActiveMQDetector extends AgentBasedSyncAbstractDetector<URI> {
	private static final Logger LOG = LoggerFactory.getLogger(ActiveMQDetector.class);

	private static final String DEFAULT_SERVICE_NAME = "ActiveMQ";
	private static final String DEFAULT_BROKERURL = "vm://localhost?create=false";

	private String m_brokerURL;
	private String m_user;
	private String m_password;
	private Boolean m_useNodeLabel;
	private final ActiveMQConnectionFactory m_connectionFactory;
	private Connection m_connection;
	private Session m_session;

	/**
	 * <p>
	 * Constructor for ActiveMQDetector.
	 * </p>
	 */
	public ActiveMQDetector() {
		super(DEFAULT_SERVICE_NAME, 0);
		setBrokerURL(DEFAULT_BROKERURL);
		m_connectionFactory = new ActiveMQConnectionFactory();
		m_connection = null;
		m_session = null;
	}

	public ActiveMQDetector(String serviceName, int port) {
		super(serviceName, port);
		setBrokerURL(DEFAULT_BROKERURL);
		m_connectionFactory = new ActiveMQConnectionFactory();
		m_connection = null;
		m_session = null;
	}

	@Override
	public URI getAgentConfig(DetectRequest request) {
		LOG.debug("getAgentConfig(request=[{}]", request);
		URI uri = null;
		try {
			uri = new URI(m_brokerURL);
			Map<String, String> map = request.getRuntimeAttributes();
			LOG.info("runtimeAttributes: {}", map);
			if (map != null) {
				// uri = new URI(uri.getScheme(), uri.getUserInfo(),
				// map.getNodeLabel(), uri.getPort(), uri.getPath(),
				// uri.getQuery(), uri.getFragment());
			}
		} catch (URISyntaxException e) {
			LOG.error("Invalid BrokerURL: {}", m_brokerURL, e);
		}
		return uri;
	}

	@Override
	public boolean isServiceDetected(InetAddress address, URI agentConfig) {
		LOG.debug("isServiceDetected(address=[{}], agentConfig=[{}])", address, agentConfig);
		try {
			m_connectionFactory.setBrokerURL(agentConfig.toString());
			m_connection = m_connectionFactory.createConnection(m_user, m_password);
			LOG.info("connection created");
			m_session = m_connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			LOG.info("session created");
			m_connection.start();
			LOG.info("connection started");

			return true;
		} catch (Throwable t) {
			throw new UndeclaredThrowableException(t);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void onInit() {
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
		if (m_session != null) {
			try {
				m_session.close();
			} catch (JMSException e) {
				LOG.debug("JMSException while closing the session.", e);
			}
			m_session = null;
		}
		if (m_connection != null) {
			try {
				m_connection.close();
			} catch (JMSException e) {
				LOG.debug("JMSException while closing the connection.", e);
			}
			m_connection = null;
		}
	}

	public String getBrokerURL() {
		return m_brokerURL;
	}

	public final void setBrokerURL(final String brokerURL) {
		m_brokerURL = brokerURL;
	}

	public String getUser() {
		return m_user;
	}

	public final void setUser(final String user) {
		m_user = user;
	}

	public String getPassword() {
		return m_password;
	}

	public final void setPassword(final String password) {
		m_password = password;
	}

	public Boolean getUseNodeLabel() {
		return m_useNodeLabel;
	}

	public final void setUseNodeLabel(final Boolean useNodeLabel) {
		m_useNodeLabel = useNodeLabel;
	}
}
