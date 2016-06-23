/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.opennms.web.svclayer.model.SnmpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * A servlet that handles configuring SNMP.
 * 
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class SnmpConfigServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(SnmpConfigServlet.class);


	public static enum SnmpConfigServletAction {
		Default("default"), 
		GetConfigForIp("get"), 
		Save("add");

		private final String actionName;

		private SnmpConfigServletAction(String actionName) {
			this.actionName = actionName;
		}

		public String getActionName() {
			return actionName;
		}
	}
	
	private static final long serialVersionUID = -2298118339644843598L;
	private static final String ACTION_PARAMETER_NAME = "action";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/** {@inheritDoc} */
        @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/*
	 * Processes the request.
	 */
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SnmpInfo snmpInfo = createFromRequest(request);
		String firstIPAddress = request.getParameter("firstIPAddress");
		String lastIPAddress = request.getParameter("lastIPAddress");
		String ipAddress = request.getParameter("ipAddress");
		LOG.debug("doPost: snmpInfo:{}, firstIpAddress:{}, lastIpAddress:{}", snmpInfo.toString(), firstIPAddress, lastIPAddress);

		final SnmpConfigServletAction action = determineAction(request);
		boolean sendEvent = parseCheckboxValue(request.getParameter("sendEventOption"));
		boolean saveLocally = parseCheckboxValue(request.getParameter("saveLocallyOption"));
		switch (action) {
			case GetConfigForIp:
				request.setAttribute("snmpConfigForIp",
						new SnmpInfo(
								SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ipAddress))));
				request.setAttribute("firstIPAddress", ipAddress);
				break;
			case Save:
				boolean success = false;
				SnmpEventInfo eventInfo = snmpInfo.createEventInfo(firstIPAddress, lastIPAddress);
				if (saveLocally) {
					SnmpPeerFactory.getInstance().define(eventInfo);
					SnmpPeerFactory.getInstance().saveCurrent();
					success |= true;
				}
				if (sendEvent) {
					success |= sendEvent(eventInfo.createEvent("web ui"));
				}
				if (success) request.setAttribute("success", "success"); // the value doesn't matter, but it must be not null
				break;
			default:
			case Default:
				break;
		}
		request.setAttribute("snmpConfig", Files.toString(SnmpPeerFactory.getFile(), Charsets.UTF_8));

		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/snmpConfig.jsp");
		dispatcher.forward(request, response);
	}
	
	private boolean parseCheckboxValue(String parameter) {
		if (Strings.isNullOrEmpty(parameter)) return false;
		return "on".equalsIgnoreCase(parameter) || "true".equalsIgnoreCase(parameter) || "checked".equalsIgnoreCase(parameter);
	}

	/**
	 * Sends the given event via the EventProxy to the system. If null no event is send.
	 * @param eventToSend The Event to send. If null, no event is send.
	 * @return <code>true</code> if the event was send successfully and no exception occured, <code>false</code> if eventToSend is null.
	 * @throws ServletException On error.
	 */
	private boolean sendEvent(Event eventToSend) throws ServletException {
		if (eventToSend == null) return false;
		try {
            EventProxy eventProxy = Util.createEventProxy();
            if (eventProxy == null) throw new ServletException("Event proxy object is null, unable to send event " + eventToSend.getUei()); 
           	eventProxy.send(eventToSend);
           	return true;
		} catch (Throwable e) {
            throw new ServletException("Could not send event " + eventToSend.getUei(), e);
		}
	}
	
	/**
	 * Creates an {@link SnmpInfo} object from the given request.
	 * @param request The http request.
	 * @return The object parsed from the http request.
	 */
	private SnmpInfo createFromRequest(HttpServletRequest request) {
		SnmpInfo snmpInfo = new SnmpInfo();

		// general parameters
		String version = request.getParameter("version");
		String timeout = request.getParameter("timeout");
		String retryCount = request.getParameter("retryCount");
		String port = request.getParameter("port");
		String maxRequestSize = request.getParameter("maxRequestSize");
		String maxVarsPerPdu = request.getParameter("maxVarsPerPdu");
		String maxRepetitions = request.getParameter("maxRepetitions");
		String proxyHost = request.getParameter("proxyHost");
		
		// v1/v2c specifics
		String readCommunityString = request.getParameter("readCommunityString");
		String writeCommunityString = request.getParameter("writeCommunityString");

		// v3 specifics
		String securityName = request.getParameter("securityName");
		String securityLevel = request.getParameter("securityLevel");
		String authPassPhrase = request.getParameter("authPassPhrase");
		String authProtocol = request.getParameter("authProtocol");
		String privPassPhrase = request.getParameter("privPassPhrase");
		String privProtocol = request.getParameter("privProtocol");
		String engineId = request.getParameter("engineId");
		String contextEngineId = request.getParameter("contextEngineId");
		String contextName = request.getParameter("contextName");
		String enterpriseId = request.getParameter("enterpriseId");

		// save in snmpInfo
		if (!Strings.isNullOrEmpty(authPassPhrase)) snmpInfo.setAuthPassPhrase(authPassPhrase);
		if (!Strings.isNullOrEmpty(authProtocol)) snmpInfo.setAuthProtocol(authProtocol);
		if (!Strings.isNullOrEmpty(contextEngineId)) snmpInfo.setContextEngineId(contextEngineId);
		if (!Strings.isNullOrEmpty(contextName)) snmpInfo.setContextName(contextName);
		if (!Strings.isNullOrEmpty(engineId)) snmpInfo.setEngineId(engineId);
		if (!Strings.isNullOrEmpty(enterpriseId)) snmpInfo.setEnterpriseId(enterpriseId);
		if (!Strings.isNullOrEmpty(maxRepetitions)) snmpInfo.setMaxRepetitions(Integer.parseInt(maxRepetitions));
		if (!Strings.isNullOrEmpty(maxRequestSize)) snmpInfo.setMaxRequestSize(Integer.parseInt(maxRequestSize));
		if (!Strings.isNullOrEmpty(maxVarsPerPdu)) snmpInfo.setMaxVarsPerPdu(Integer.parseInt(maxVarsPerPdu));
		if (!Strings.isNullOrEmpty(port)) snmpInfo.setPort(Integer.parseInt(port));
		if (!Strings.isNullOrEmpty(privPassPhrase)) snmpInfo.setPrivPassPhrase(privPassPhrase);
		if (!Strings.isNullOrEmpty(privProtocol)) snmpInfo.setPrivProtocol(privProtocol);
		if (!Strings.isNullOrEmpty(proxyHost)) snmpInfo.setProxyHost(proxyHost);
		if (!Strings.isNullOrEmpty(readCommunityString)) snmpInfo.setReadCommunity(readCommunityString);
		if (!Strings.isNullOrEmpty(retryCount)) snmpInfo.setRetries(Integer.parseInt(retryCount));
		if (!Strings.isNullOrEmpty(securityLevel)) snmpInfo.setSecurityLevel(Integer.parseInt(securityLevel));
		if (!Strings.isNullOrEmpty(securityName)) snmpInfo.setSecurityName(securityName);
		if (!Strings.isNullOrEmpty(timeout)) snmpInfo.setTimeout(Integer.parseInt(timeout));
		if (!Strings.isNullOrEmpty(version)) snmpInfo.setVersion(version);
		if (!Strings.isNullOrEmpty(writeCommunityString)) snmpInfo.setWriteCommunity(writeCommunityString);

		return snmpInfo;
	}

	private SnmpConfigServletAction determineAction(HttpServletRequest request) {
		if (request.getParameter(ACTION_PARAMETER_NAME) == null) return SnmpConfigServletAction.Default;
		for (SnmpConfigServletAction eachAction : SnmpConfigServletAction.values()) {
			if (eachAction.getActionName().equals(request.getParameter(ACTION_PARAMETER_NAME))) return eachAction;
		}
		return SnmpConfigServletAction.Default;
	}
}
