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
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.servlet.MissingParameterException
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
	
    String nodeIdString = request.getParameter( "node" );
    String ipAddr = request.getParameter( "intf" );
    String ifIndexString = request.getParameter("ifindex");

    if( nodeIdString == null ) {
        throw new MissingParameterException( "node", new String[] { "node", "intf or ifindex" } );
    }

    if( ipAddr == null && ifIndexString == null ) {
        throw new MissingParameterException( "intf or ifindex", new String[] { "node", "intf or ifindex" } );
    }

    int nodeId = -1;

    try {
        nodeId = WebSecurityUtils.safeParseInt( nodeIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type, should be integer but got '"+nodeIdString+"'", e );
    }
    
    int ifIndex = -1;
    if (ifIndexString != null && ifIndexString.length() != 0) {
        try {
            ifIndex = WebSecurityUtils.safeParseInt( ifIndexString );
        }
        catch( NumberFormatException e ) {
            //throw new WrongParameterDataTypeException
            throw new ServletException( "Wrong data type, should be integer but got '"+ifIndexString+"'", e );
        }
    }
	
%>

<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>
<c:url var="interfaceLink" value="element/interface.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
  <c:param name="intf" value="<%=WebSecurityUtils.sanitizeString(ipAddr)%>"/>
</c:url>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle(WebSecurityUtils.sanitizeString(ipAddr))
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "${nodeLink}")
          .breadcrumb("Interface", "${interfaceLink}")
          .breadcrumb("Interface Deleted")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <% if (ifIndex == -1) { %>
    <span>Finished Deleting Interface <%= WebSecurityUtils.sanitizeString(ipAddr) %></span>
    <% } else if (!"0.0.0.0".equals(ipAddr) && ipAddr != null && ipAddr.length() !=0){ %>
    <span>Finished Deleting Interface <%= WebSecurityUtils.sanitizeString(ipAddr) %> with ifIndex <%= ifIndex %></span>
    <% } else { %>
    <span>Finished Deleting Interface with ifIndex <%= ifIndex %></span>
    <% } %>
  </div>
  <div class="card-body">
    <p>
      OpenNMS should not need to be restarted, but it may take a moment for
      the Categories to be updated.
    </p>
  </div>
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
