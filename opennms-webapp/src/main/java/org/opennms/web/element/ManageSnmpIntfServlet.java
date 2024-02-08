/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
                List<InetAddress> ips = new ArrayList<>();
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
