<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.core.utils.WebSecurityUtils,
                org.opennms.web.element.*,
                org.opennms.netmgt.model.OnmsNode,
                org.opennms.web.servlet.MissingParameterException"
%>
<%@ page import="org.opennms.netmgt.model.OnmsIpInterface" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
    int nodeId = -1;
    String nodeIdString = request.getParameter("node");
    if (nodeIdString == null) {
        throw new MissingParameterException("node");
    }
    try {
        nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    } catch (NumberFormatException numE) {
        throw new ServletException(numE);
    }

    if (nodeId < 0) {
        throw new ServletException("Invalid node ID.");
    }

    //get the database node info
    OnmsNode node_db = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
    if (node_db == null) {
        throw new ServletException("No such node in database.");
    }

    List<OnmsIpInterface> ipInterfaces = new ArrayList<>(node_db.getIpInterfaces());
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Remote Desktop"/>
    <jsp:param name="headTitle" value="Remote Desktop"/>
    <jsp:param name="headTitle" value="Node Management"/>
    <jsp:param name="location" value="Node Management"/>
    <jsp:param name="breadcrumb" value="Remote Desktop"/>
</jsp:include>

<script type="text/javascript" >
    function connect() {
        const protocolValue = document.getElementById("rdRrotocolSelect").value;
        const ipAddressValue = document.getElementById("ipAddressSelect").value;

        window.location.href="admin/nodemanagement/remoteConnection.jsp?node=<%=nodeId%>&protocol=" + protocolValue + "&ipAddress=" + ipAddressValue;
    }
</script>

<h3>Node: <%=node_db.getLabel()%></h3>

<form role="form" class="form mb-2 col-md-6">
    <div class="form-group">
        <label for="rdRrotocolSelect">Protocol:</label>
        <select id="rdRrotocolSelect" class="form-control custom-select" name="rdRrotocolSelect">
            <option value="vnc">VNC</option>
            <option value="rdp">RDP</option>
            <option value="ssh">SSH</option>
            <option value="telnet">Telnet</option>
        </select>
    </div>

    <div class="form-group">
        <label for="ipAddressSelect">IP Address:</label>
        <select id="ipAddressSelect" class="form-control custom-select" name="ipAddressSelect">
            <% for (OnmsIpInterface ipInterface : ipInterfaces) { %>
                <option value="<%=ipInterface.getIpAddress().getHostAddress()%>"><%=ipInterface.getIpAddress().getHostAddress()%></option>
            <% } %>
        </select>
    </div>

    <div class="form-group">
        <input type="button" class="btn btn-secondary" value="Connect" onclick="connect()"/>
    </div>

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>
