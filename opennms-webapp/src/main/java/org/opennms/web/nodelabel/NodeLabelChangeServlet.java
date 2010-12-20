//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.nodelabel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.utils.NodeLabel;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.api.Util;

/**
 * Changes the label of a node, throws an event signalling that change, and then
 * redirects the user to a web page displaying that node's details.
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeLabelChangeServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -7766362068448931124L;
    protected EventProxy proxy;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        try {
            this.proxy = Util.createEventProxy();
        } catch (Exception e) {
            throw new ServletException("JMS Exception", e);
        }
    }

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nodeIdString = request.getParameter("node");
        String labelType = request.getParameter("labeltype");
        String userLabel = request.getParameter("userlabel");

        if (nodeIdString == null) {
            throw new MissingParameterException("node", new String[] { "node", "labeltype", "userlabel" });
        }
        if (labelType == null) {
            throw new MissingParameterException("labeltype", new String[] { "node", "labeltype", "userlabel" });
        }
        if (userLabel == null) {
            throw new MissingParameterException("userlabel", new String[] { "node", "labeltype", "userlabel" });
        }

        try {
            int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
            NodeLabel oldLabel = NodeLabel.retrieveLabel(nodeId);
            NodeLabel newLabel = null;

            if (labelType.equals("auto")) {
                newLabel = NodeLabel.computeLabel(nodeId);
            } else if (labelType.equals("user")) {
                newLabel = new NodeLabel(userLabel, NodeLabel.SOURCE_USERDEFINED);
            } else {
                throw new ServletException("Unexpected labeltype value: " + labelType);
            }

            NodeLabel.assignLabel(nodeId, newLabel);
            this.sendLabelChangeEvent(nodeId, oldLabel, newLabel);
            response.sendRedirect(Util.calculateUrlBase(request) + "/element/node.jsp?node=" + nodeIdString);
        } catch (SQLException e) {
            throw new ServletException("Database exception", e);
        } catch (Exception e) {
            throw new ServletException("Exception sending node label change event", e);
        }
    }

    /**
     * <p>sendLabelChangeEvent</p>
     *
     * @param nodeId a int.
     * @param oldNodeLabel a {@link org.opennms.netmgt.utils.NodeLabel} object.
     * @param newNodeLabel a {@link org.opennms.netmgt.utils.NodeLabel} object.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    protected void sendLabelChangeEvent(int nodeId, NodeLabel oldNodeLabel, NodeLabel newNodeLabel) throws EventProxyException {
        Event outEvent = new Event();
        outEvent.setSource("NodeLabelChangeServlet");
        outEvent.setUei(EventConstants.NODE_LABEL_CHANGED_EVENT_UEI);
        outEvent.setNodeid(nodeId);
        outEvent.setHost("host");
        outEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        Parms parms = new Parms();

        if (oldNodeLabel != null) {
            // old label
            Value value = new Value();
            value.setContent(oldNodeLabel.getLabel());
            Parm parm = new Parm();
            parm.setParmName(EventConstants.PARM_OLD_NODE_LABEL);
            parm.setValue(value);
            parms.addParm(parm);

            // old label source
            value = new Value();
            value.setContent(String.valueOf(oldNodeLabel.getSource()));
            parm = new Parm();
            parm.setParmName(EventConstants.PARM_OLD_NODE_LABEL_SOURCE);
            parm.setValue(value);
            parms.addParm(parm);
        }

        if (newNodeLabel != null) {
            // new label
            Value value = new Value();
            value.setContent(newNodeLabel.getLabel());
            Parm parm = new Parm();
            parm.setParmName(EventConstants.PARM_NEW_NODE_LABEL);
            parm.setValue(value);
            parms.addParm(parm);

            // old label source
            value = new Value();
            value.setContent(String.valueOf(newNodeLabel.getSource()));
            parm = new Parm();
            parm.setParmName(EventConstants.PARM_NEW_NODE_LABEL_SOURCE);
            parm.setValue(value);
            parms.addParm(parm);
        }

        outEvent.setParms(parms);

        this.proxy.send(outEvent);
    }

}
