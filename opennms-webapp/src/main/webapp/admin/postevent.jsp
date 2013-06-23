<%--
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

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		java.io.*,
		java.util.*,
		java.net.InetAddress,
                java.net.UnknownHostException,
                org.opennms.web.api.Util,
                org.opennms.netmgt.EventConstants,
                org.opennms.netmgt.xml.event.Event,
                org.opennms.netmgt.xml.event.Parm,
                org.opennms.netmgt.xml.event.Value,
                org.apache.commons.lang.StringUtils
	"
%>
<%
    HttpSession user = request.getSession(true);

    String uei = StringUtils.trimToEmpty(request.getParameter("uei"));
    if (StringUtils.isBlank(uei)) {
        response.sendRedirect("sendevent.jsp");
        return;
    }

    Event event = new Event();
    event.setSource("Web UI");
    event.setUei(uei);
    event.setTime(EventConstants.formatToString(new java.util.Date()));

    String nodeID = StringUtils.trimToEmpty(request.getParameter("nodeid"));
    if (StringUtils.isNotBlank(nodeID)) {
        try {
            long nodeid = Long.parseLong(nodeID);
            event.setNodeid(nodeid);
        } catch (NumberFormatException e) {
            throw new ServletException("Invalid node id value: " + nodeID, e);
        }
    }

    String host = StringUtils.trimToEmpty(request.getParameter("host"));
    if (StringUtils.isNotBlank(host)) {
        event.setHost(host);
    }

    String intface = StringUtils.trimToEmpty(request.getParameter("interface"));
    if (StringUtils.isNotBlank(host)) {
        event.setInterface(intface);
    }

    String service = StringUtils.trimToEmpty(request.getParameter("service"));
    if (StringUtils.isNotBlank(service)) {
        event.setService(service);
    }

    String severity = StringUtils.trimToEmpty(request.getParameter("severity"));
    if (StringUtils.isNotBlank(severity)) {
        event.setSeverity(severity);
    }

    String description = StringUtils.trimToEmpty(request.getParameter("description"));
    if (StringUtils.isNotBlank(description)) {
        event.setDescr(description);
    }

    String operinstruct = StringUtils.trimToEmpty(request.getParameter("operinstruct"));
    if (StringUtils.isNotBlank(operinstruct)) {
        event.setOperinstruct(operinstruct);
    }

    StringBuffer sb = new StringBuffer();

    Enumeration<String> pNames = request.getParameterNames();
    for(String pName : Collections.list(pNames)) {
        if (pName.matches("^parm(\\d+)\\.name$")) {
            String parmName = StringUtils.trimToEmpty(request.getParameter(pName));
            if (StringUtils.isNotBlank(parmName)) {
                String vName = pName.replaceAll("\\.name$", ".value");
                String parmValue = StringUtils.trimToEmpty(request.getParameter(vName));
                sb.append("  &lt;parm name=\""+parmName+"\" value=\""+parmValue+"\" /&gt;\n");

                final Value value = new Value();
                value.setContent(parmValue);

                final Parm parm = new Parm();
                parm.setParmName(parmName);
                parm.setValue(value);

                event.addParm(parm);
            }
        }
    }

    try {
        Util.createEventProxy().send(event);
    } catch (Throwable e) {
        throw new ServletException("Could not send event " + event.getUei(), e);
    }
%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Post Event" />
  <jsp:param name="headTitle" value="Post Event" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Post Event" />
</jsp:include>

<h3>Event Sent...</h3>

<pre>
&lt;event&gt;
  &lt;uei&gt;<%=uei%>&lt;/uei&gt;
  &lt;nodeid&gt;<%=nodeID%>&lt;/nodeid&gt;
  &lt;host&gt;<%=host%>&lt;/host&gt;
  &lt;interface&gt;<%=intface%>&lt;/interface&gt;
  &lt;service&gt;<%=service%>&lt;/service&gt;
  &lt;severity&gt;<%=severity%>&lt;/severity&gt;
  &lt;descr&gt;<%=description%>&lt;/descr&gt;
  &lt;operinstr&gt;<%=operinstruct%>&lt;/operinstr&gt;
<%=sb%>
&lt;/event&gt;
</pre>

<jsp:include page="/includes/footer.jsp" flush="false" />
