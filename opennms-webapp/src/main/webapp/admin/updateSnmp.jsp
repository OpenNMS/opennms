<%--

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
// Modifications:
//
// 2005 Mar 18: Created this file from rescan.jsp
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.EventConstants,
		org.opennms.netmgt.xml.event.Event,
		org.opennms.web.*,
		org.opennms.web.api.Util"
%>

<%!
    private void sendSNMPRestartEvent(int nodeid, String primeInt) throws ServletException {
        Event snmpRestart = new Event();
        snmpRestart.setUei("uei.opennms.org/nodes/reinitializePrimarySnmpInterface");
        snmpRestart.setNodeid(nodeid);
        snmpRestart.setInterface(primeInt);
        snmpRestart.setSource("web ui");
        snmpRestart.setTime(EventConstants.formatToString(new java.util.Date()));

        try {
                Util.createEventProxy().send(snmpRestart);
        } catch (Exception e) {
                throw new ServletException("Could not send event " + snmpRestart.getUei(), e);
        }

    }
%>

<%
    String nodeIdString = request.getParameter("node");
    String ipAddr = request.getParameter("ipaddr");
    String[] requiredParameters = new String[] { "node", "ipaddr" };
    
    if (nodeIdString == null) {
        throw new MissingParameterException("node", requiredParameters);
    }
    
    if (ipAddr == null) {
        throw new MissingParameterException("ipaddr", requiredParameters);
    }

    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

    sendSNMPRestartEvent(nodeId, ipAddr);
    
        
%>

<%
  String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId
                       + "'>Node</a>";
  String breadcrumb3 = "<a href='element/interface.jsp?node=" + nodeId
		       + "&intf=" + ipAddr  + "'>Interface</a>";
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Update SNMP Information" />
  <jsp:param name="headTitle" value="Rescan" />
  <jsp:param name="headTitle" value="SNMP Information" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="Update SNMP Information" />
</jsp:include>

<h3>Update SNMP Information</h3>
      
<p>
  The interface has had its SNMP information updated. This should cause any
  changes in SNMP community names or collection to take effect.
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />
