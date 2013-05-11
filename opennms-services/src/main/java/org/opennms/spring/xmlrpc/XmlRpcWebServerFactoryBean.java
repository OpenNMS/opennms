/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.spring.xmlrpc;

import java.net.InetAddress;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.secure.SecureWebServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>
 * XmlRpcWebServerFactoryBean class.
 * </p>
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class XmlRpcWebServerFactoryBean implements FactoryBean<WebServer>,
		InitializingBean, DisposableBean {

	private WebServer m_webServer;
	private int m_port = -1;
	private InetAddress m_address = null;
	private XmlRpcServer m_xmlRpcServer = null;
	private boolean m_secure = false;

	/**
	 * <p>
	 * Getter for the field <code>port</code>.
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getPort() {
		return m_port;
	}

	/**
	 * <p>
	 * Setter for the field <code>port</code>.
	 * </p>
	 * 
	 * @param port
	 *            a int.
	 */
	public void setPort(final int port) {
		m_port = port;
	}

	/**
	 * <p>
	 * Getter for the field <code>address</code>.
	 * </p>
	 * 
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getAddress() {
		return m_address;
	}

	/**
	 * <p>
	 * Setter for the field <code>address</code>.
	 * </p>
	 * 
	 * @param addrress
	 *            a {@link java.net.InetAddress} object.
	 */
	public void setAddress(final InetAddress addrress) {
		m_address = addrress;
	}

	/**
	 * <p>
	 * Getter for the field <code>secure</code>.
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean getSecure() {
		return m_secure;
	}

	/**
	 * <p>
	 * Setter for the field <code>secure</code>.
	 * </p>
	 * 
	 * @param secure
	 *            a boolean.
	 */
	public void setSecure(final boolean secure) {
		m_secure = secure;
	}

	/**
	 * <p>
	 * Getter for the field <code>xmlRpcServer</code>.
	 * </p>
	 * 
	 * @return a {@link org.apache.xmlrpc.XmlRpcServer} object.
	 */
	public XmlRpcServer getXmlRpcServer() {
		return m_xmlRpcServer;
	}

	/**
	 * <p>
	 * Setter for the field <code>xmlRpcServer</code>.
	 * </p>
	 * 
	 * @param xmlRpcServer
	 *            a {@link org.apache.xmlrpc.XmlRpcServer} object.
	 */
	public void setXmlRpcServer(final XmlRpcServer xmlRpcServer) {
		m_xmlRpcServer = xmlRpcServer;
	}

	/**
	 * <p>
	 * getObject
	 * </p>
	 * 
	 * @return a {@link java.lang.Object} object.
	 * @throws java.lang.Exception
	 *             if any.
	 */
        @Override
	public WebServer getObject() throws Exception {
		return m_webServer;
	}

	/**
	 * <p>
	 * getObjectType
	 * </p>
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
        @Override
	public Class<? extends WebServer> getObjectType() {
		return WebServer.class;
	}

	/**
	 * <p>
	 * isSingleton
	 * </p>
	 * 
	 * @return a boolean.
	 */
        @Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * <p>
	 * afterPropertiesSet
	 * </p>
	 * 
	 * @throws java.lang.Exception
	 *             if any.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (m_port == -1)
			throw new IllegalArgumentException("port is required");

		if (m_xmlRpcServer == null)
			m_xmlRpcServer = new XmlRpcServer();

		if (m_secure) {
			m_webServer = new SecureWebServer(m_port, m_address, m_xmlRpcServer);
		} else {
			m_webServer = new WebServer(m_port, m_address, m_xmlRpcServer);
		}
		m_webServer.start();
	}

	/**
	 * <p>
	 * destroy
	 * </p>
	 * 
	 * @throws java.lang.Exception
	 *             if any.
	 */
        @Override
	public void destroy() throws Exception {
		m_webServer.shutdown();
	}

}