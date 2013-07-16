/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import com.vaadin.server.VaadinRequest;
import org.opennms.web.api.OnmsHeaderProvider;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * This class creates an {@link HttpServletRequest} object that delegates all calls to
 * a {@link VaadinRequest} instance. This is used so that we can fetch the header HTML
 * from an {@link OnmsHeaderProvider} in {@link TopologyWidgetTestApplication}.
 * 
 * TODO: Refactor into a common class.
 */
public class HttpServletRequestVaadinImpl implements HttpServletRequest {

	private final VaadinRequest m_request;
	
	public HttpServletRequestVaadinImpl(VaadinRequest request) {
		m_request = request;
	}

	@Override
	public String getAuthType() {
		return m_request.getAuthType();
	}

	@Override
	public String getContextPath() {
		return m_request.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		return m_request.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return m_request.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return m_request.getHeader(name);
	}

	@Override
	public Enumeration getHeaderNames() {
		return m_request.getHeaderNames();
	}

	@Override
	public Enumeration getHeaders(String name) {
		return m_request.getHeaders(name);
	}

	@Override
	public int getIntHeader(String name) {
		return Integer.parseInt(m_request.getHeader(name));
	}

	@Override
	public String getMethod() {
		return m_request.getMethod();
	}

	@Override
	public String getPathInfo() {
		return m_request.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return m_request.getRemoteUser();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getRequestURI() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getRequestURI()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public StringBuffer getRequestURL() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getRequestURL()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getRequestedSessionId() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getRequestedSessionId()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getServletPath() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getServletPath()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public HttpSession getSession() {
		//return VaadinSession.getCurrent();
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getSession()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public HttpSession getSession(boolean create) {
		//return VaadinSession.getCurrent();
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getSession()");
	}

	@Override
	public Principal getUserPrincipal() {
		return m_request.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isUserInRole(String role) {
		return m_request.isUserInRole(role);
	}

	@Override
	public Object getAttribute(String name) {
		return m_request.getAttribute(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return m_request.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return m_request.getCharacterEncoding();
	}

	@Override
	public int getContentLength() {
		return m_request.getContentLength();
	}

	@Override
	public String getContentType() {
		return m_request.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		throw new IOException("Cannot get input stream from " + this.getClass().getName());
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getLocalAddr() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getLocalAddr()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getLocalName() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getLocalName()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public int getLocalPort() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getLocalPort()");
	}

	@Override
	public Locale getLocale() {
		return m_request.getLocale();
	}

	@Override
	public Enumeration getLocales() {
		return m_request.getLocales();
	}

	@Override
	public String getParameter(String name) {
		return m_request.getParameter(name);
	}

	@Override
	public Map getParameterMap() {
		return m_request.getParameterMap();
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(Collections.emptyList());
	}

	@Override
	public String[] getParameterValues(String name) {
		return null;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getProtocol() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getProtocol()");
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return m_request.getReader();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getRealPath(String path) {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getRealPath()");
	}

	@Override
	public String getRemoteAddr() {
		return m_request.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return m_request.getRemoteHost();
	}

	@Override
	public int getRemotePort() {
		return m_request.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getScheme() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getScheme()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getServerName() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getServerName()");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public int getServerPort() {
		throw new UnsupportedOperationException("Unimplemented: " + this.getClass().getName() + ".getServerPort()");
	}

	@Override
	public boolean isSecure() {
		return m_request.isSecure();
	}

	@Override
	public void removeAttribute(String name) {
		m_request.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		m_request.setAttribute(name, o);
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		// Do nothing
	}
}
