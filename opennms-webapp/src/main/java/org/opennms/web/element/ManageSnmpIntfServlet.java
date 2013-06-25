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

/*
 * Creato il 17-feb-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ManageSnmpIntfServlet class.</p>
 *
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato,
 * aprire Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e
 * commenti
 *
 * La servlet prende i seguenti parametri dal file web.xml
 * @version $Id: $
 * @since 1.8.1
 */
public final class ManageSnmpIntfServlet extends HttpServlet {
	private static final Logger LOG = LoggerFactory.getLogger(ManageSnmpIntfServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = 996461881276250543L;

    protected int snmpServiceId;

    protected SnmpPeerFactory snmpPeerFactory;

    protected String pageToRedirect;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        try {
            this.snmpServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("SNMP");
            SnmpPeerFactory.init();
        } catch (Throwable e) {
            throw new ServletException(
                    "Could not determine the SNMP service ID", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        doPost(request, response);
    }

    /** {@inheritDoc} */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {


		HttpSession userSession = request.getSession(false);
        if (userSession == null)
            throw new ServletException("Session exceeded");

        String nodeIdString = request.getParameter("node");
        if (nodeIdString == null) {
            throw new org.opennms.web.servlet.MissingParameterException("node");
        }
        int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

        String intfIdString = request.getParameter("intf");
        if (intfIdString == null) {
            throw new org.opennms.web.servlet.MissingParameterException("intf");
        }
        int intfId = WebSecurityUtils.safeParseInt(intfIdString);

        String statusString = request.getParameter("status");
        if (statusString == null) {
            throw new org.opennms.web.servlet.MissingParameterException("status");
        }
        int status = WebSecurityUtils.safeParseInt(statusString);

        LOG.debug("ManageSnmpIntfServlet.doPost: parameters - node {} intf {} status {}", nodeId, intfId, status);
        String snmpIp = null;
        Service[] snmpServices = null;
        try {
            snmpServices = NetworkElementFactory.getInstance(getServletContext()).getServicesOnNode(nodeId, this.snmpServiceId);
            if (snmpServices != null && snmpServices.length > 0) {
                List<InetAddress> ips = new ArrayList<InetAddress>();
                for (int i = 0; i < snmpServices.length; i++) {
                    ips.add(InetAddressUtils.addr(snmpServices[i]
                            .getIpAddress()));
                }

                InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

                if (lowest != null) {
                    snmpIp = InetAddressUtils.str(lowest);
                }
            }

            InetAddress[] inetAddress = InetAddress.getAllByName(snmpIp);
            SnmpAgentConfig agent = SnmpPeerFactory.getInstance().getAgentConfig(inetAddress[0]);
            
            LOG.debug("ManageSnmpIntfServlet.doPost: agent SNMP version/write community {}/{}", agent.getVersion(), agent.getWriteCommunity());
            SnmpIfAdmin snmpIfAdmin = new SnmpIfAdmin(nodeId, agent);
            if (snmpIfAdmin.setIfAdmin(intfId, status)) {
                LOG.debug("ManageSnmpIntfServlet.doPost: snmpIAdmin return OK ");
            } else {
            	LOG.debug("ManageSnmpIntfServlet.doPost: snmpIAdmin return error ");
            }
            redirect(request, response);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (UnknownHostException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private void redirect(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
    }

}
