<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		java.util.*,
                org.opennms.web.api.Util,org.opennms.netmgt.events.api.EventConstants,
                org.opennms.netmgt.xml.event.Event,
                org.opennms.netmgt.xml.event.Parm,
                org.opennms.netmgt.xml.event.Value,
                org.opennms.core.xml.JaxbUtils,
                org.apache.commons.lang.StringUtils
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String uei = StringUtils.trimToEmpty(request.getParameter("uei"));
    if (StringUtils.isBlank(uei)) {
        response.sendRedirect("sendevent.jsp");
        return;
    }

    Event event = new Event();
    event.setSource("Web UI");
    event.setUei(uei);
    event.setTime(new java.util.Date());

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
    if (StringUtils.isNotBlank(intface)) {
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

    String uuid = StringUtils.trimToEmpty(request.getParameter("uuid"));
    if (StringUtils.isNotBlank(uuid)) {
        event.setUuid(uuid);
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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Post Event")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Post Event")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<%
    String eventXml = JaxbUtils.marshal(event);
    // Strip off xml version string
    eventXml = eventXml.replaceFirst("^<\\?xml[^\\>]+\\?\\>\\s*", "");
%>

<div class="card">
  <div class="card-header">
    <span>Event Sent...</span>
  </div>
  <div class="card-body">
    <pre><c:out value="<%=eventXml%>" /></pre>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
