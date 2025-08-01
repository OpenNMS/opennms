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
	import="org.opennms.netmgt.events.api.EventConstants,
		org.opennms.netmgt.xml.event.Event,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.servlet.MissingParameterException,
		org.opennms.web.api.Util
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%!
    private void sendSNMPRestartEvent(int nodeid, String primeInt) throws ServletException {
        Event snmpRestart = new Event();
        snmpRestart.setUei("uei.opennms.org/nodes/reinitializePrimarySnmpInterface");
        snmpRestart.setNodeid(Long.valueOf(nodeid));
        snmpRestart.setInterface(primeInt);
        snmpRestart.setSource("web ui");
        snmpRestart.setTime(new java.util.Date());

        try {
                Util.createEventProxy().send(snmpRestart);
        } catch (Throwable e) {
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

<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>
<c:url var="interfaceLink" value="element/interface.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
  <c:param name="intf" value="<%=ipAddr%>"/>
</c:url>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Rescan")
          .headTitle("SNMP Information")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "${nodeLink}")
          .breadcrumb("Interface", "${interfaceLink}")
          .breadcrumb("Update SNMP Information")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Update SNMP Information</span>
  </div>
  <div class="card-body">
    <p>
      The interface has had its SNMP information updated. This should not cause any
      changes in SNMP community names or collection to take effect.
    </p>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
