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
	import="org.opennms.core.utils.WebSecurityUtils,
	org.opennms.web.element.*,
  org.opennms.web.api.Util,
	org.opennms.web.servlet.MissingParameterException
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
  String nodeIdString = request.getParameter("node");
  String ipAddr = request.getParameter("ipaddr");
  
  if( nodeIdString == null ) {
    throw new MissingParameterException("node");
  }
  
  int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
  String nodeLabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId);
%>

<c:url var="nodeLink" value="element/node.jsp">
	<c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>
<c:choose>
	<c:when test="<%=(ipAddr == null)%>">
		<c:set var="returnUrl" value="${nodeLink}"/>
		<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Rescan")
          .headTitle("Element")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "${nodeLink}")
          .breadcrumb("Rescan")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />
	</c:when>
	<c:otherwise>
		<c:url var="interfaceLink" value="element/interface.jsp">
			<c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
			<c:param name="intf" value="<%=WebSecurityUtils.sanitizeString(ipAddr)%>"/>
		</c:url>
		<c:set var="returnUrl" value="${interfaceLink}"/>
		<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Rescan")
          .headTitle("Element")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "${nodeLink}")
          .breadcrumb("Interface", "${fn:escapeXml(interfaceLink)}")
          .breadcrumb("Rescan")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />
	</c:otherwise>
</c:choose>

<div class="row">

  <div class="col-md-5">
    <div class="card">
      <div class="card-header">
        <span>Capability Rescan</span>
      </div>
      <div class="card-body">
        <p>Are you sure you want to rescan the <nobr><%=WebSecurityUtils.sanitizeString(nodeLabel)%></nobr>
          <% if( ipAddr==null ) { %>
            node?
          <% } else { %>
            interface <%= WebSecurityUtils.sanitizeString(ipAddr) %>?
          <% } %>
        </p>        
        <form method="post" action="element/rescan">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <p>
            <input type="hidden" name="node" value="<%=nodeId%>" />
            <input type="hidden" name="returnUrl" value="${fn:escapeXml(returnUrl)}" />
            <div class="btn-group" role="group">
              <button class="btn btn-secondary" type="submit">Rescan</button>
              <button class="btn btn-secondary" type="button" onClick="window.open('<%= Util.calculateUrlBase(request)%>${returnUrl}', '_self')">Cancel</button>
            </div>
          </p>
        </form>
      </div>
    </div>
  </div>

  <div class="col-md-7">
    <div class="card">
      <div class="card-header">
        <span>Rescan Node</span>
      </div>
      <div class="card-body">
        <p>
          <em>Rescanning</em> a node tells the provisioning subsystem to re-detect what <em>services</em> appear on the node's interfaces and to re-apply the appropriate set of <em>policies</em>.
          If the node is correctly configured for SNMP, a rescan will also cause the node's SNMP attributes (<em>sysLocation</em>, <em>sysContact</em>, <em>etc.</em>) to be refreshed.
        </p>
      </div>
    </div>
  </div>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
