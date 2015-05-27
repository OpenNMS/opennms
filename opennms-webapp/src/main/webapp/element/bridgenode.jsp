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
		import="
				org.opennms.web.element.*,
				org.opennms.netmgt.model.OnmsNode,
				org.opennms.core.utils.WebSecurityUtils,
				java.util.*,
				java.net.*,
        org.opennms.core.utils.InetAddressUtils,
        org.opennms.web.svclayer.api.ResourceService,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils
        "
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%!
    protected int telnetServiceId;
    protected int httpServiceId;
    private ResourceService m_resourceService;
    
    public void init() throws ServletException {
        try {
            this.telnetServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("Telnet");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the Telnet service ID", e );
        }
        
        try {
            this.httpServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("HTTP");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the HTTP service ID", e );
        }

        WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
    }
%>

<%
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.servlet.MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    //get the database node info
    OnmsNode node_db = NetworkElementFactory.getInstance(getServletContext()).getNode( nodeId );
    if( node_db == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }
    String parentRes = Integer.toString(nodeId);
    String parentResType = "node";
    if (!(node_db.getForeignSource() == null) && !(node_db.getForeignId() == null)) {
        parentRes = node_db.getForeignSource() + ":" + node_db.getForeignId();
        parentResType = "nodeSource";
    }

    //find the telnet interfaces, if any
    String telnetIp = null;
    Service[] telnetServices = NetworkElementFactory.getInstance(getServletContext()).getServicesOnNode(nodeId, this.telnetServiceId);
    
    if (telnetServices != null && telnetServices.length > 0) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for (Service service : telnetServices) {
            ips.add(InetAddressUtils.addr(service.getIpAddress()));
        }
        
        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);
        
        if (lowest != null) {
            telnetIp = lowest.getHostAddress();
        }
    }    

    //find the HTTP interfaces, if any
    String httpIp = null;
    Service[] httpServices = NetworkElementFactory.getInstance(getServletContext()).getServicesOnNode(nodeId, this.httpServiceId);

    if (httpServices != null && httpServices.length > 0) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for (Service service : httpServices) {
            ips.add(InetAddressUtils.addr(service.getIpAddress()));
        }

        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

        if (lowest != null) {
            httpIp = lowest.getHostAddress();
        }
    }

    boolean isRouteIP = NetworkElementFactory.getInstance(getServletContext()).isRouteInfoNode(nodeId);
%>

<% pageContext.setAttribute("nodeId", nodeId); %>
<% pageContext.setAttribute("nodeLabel", node_db.getLabel()); %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="headTitle" value="${nodeLabel}" />
  <jsp:param name="headTitle" value="Bridge Node Info" />
  <jsp:param name="title" value="Bridge Node Info" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${nodeId}'>Node</a>" />
  <jsp:param name="breadcrumb" value="Bridge Info" />
</jsp:include>

<!-- Body -->

<h4>Node: <%=node_db.getLabel()%></h4>

<ul class="list-inline">
  <li>
    <a href="event/list.htm?filter=node%3D<%=nodeId%>">View Events</a>
  </li>
  <li>
    <a href="asset/modify.jsp?node=<%=nodeId%>">Asset Info</a>
  </li>
  <% if( telnetIp != null ) { %>
  <li>
    <a href="telnet://<%=telnetIp%>">Telnet</a>
  </li>
  <% } %>
  <% if( httpIp != null ) { %>
  <li>
    <a href="http://<%=httpIp%>">HTTP</a>
  </li>
  <% } %>
  <% if (m_resourceService.findNodeChildResources(node_db).size() > 0) { %>
    <li>
      <c:url var="resourceGraphsUrl" value="graph/chooseresource.htm">
        <c:param name="parentResourceType" value="<%=parentResType%>"/>
        <c:param name="parentResource" value="<%=parentRes%>"/>
        <c:param name="reports" value="all"/>
      </c:url>
      <a href="${resourceGraphsUrl}">Resource Graphs</a>
    </li>
  <% } %>
  <li>
    <a href="element/rescan.jsp?node=<%=nodeId%>">Rescan</a>
  </li>
</ul>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">General (Status: <%=(node_db == null ? "Unknown" : ElementUtil.getNodeStatusString(node_db))%>)</h3>
      </div>
      <div class="panel-body">
        <ul class="list-unstyled">
          <% if( isRouteIP ) { %>
          <li>
            <a href="element/routeipnode.jsp?node=<%=nodeId%>">View Node IP Route Info</a>
          </li>
          <% }%>
          <li>
            <a href="element/linkednode.jsp?node=<%=nodeId%>">View Node Link Detailed Info</a>
          </li>
        </ul>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <jsp:include page="/includes/nodeBridge-box.jsp" flush="false" >
      <jsp:param name="node" value="<%=nodeId%>" />
    </jsp:include>
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <jsp:include page="/includes/nodeSTPint-box.jsp" flush="false" >
      <jsp:param name="node" value="<%=nodeId%>" />
    </jsp:include>
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
