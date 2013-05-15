<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
		import="org.opennms.netmgt.config.PollerConfigFactory,
				org.opennms.netmgt.config.PollerConfig,
				org.opennms.netmgt.config.SnmpInterfacePollerConfigFactory,
				org.opennms.netmgt.config.SnmpInterfacePollerConfig,
				java.util.*,
                org.opennms.core.utils.SIUtils,
                org.opennms.netmgt.model.OnmsResource,
                org.opennms.web.api.Util,
                org.opennms.web.springframework.security.Authentication,
                org.opennms.web.element.*,
                org.opennms.web.event.*,
                org.opennms.web.svclayer.ResourceService,
                org.opennms.netmgt.utils.IfLabel,
                org.springframework.web.context.WebApplicationContext,
                org.springframework.web.context.support.WebApplicationContextUtils"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<%!protected int telnetServiceId;
    protected int httpServiceId;
    private WebApplicationContext m_webAppContext;
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

	    m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) m_webAppContext.getBean("resourceService", ResourceService.class);
    }%>

<%
    Interface intf_db = ElementUtil.getInterfaceByParams(request, getServletContext());
    int nodeId = intf_db.getNodeId();
    String ipAddr = intf_db.getIpAddress();
	int ifIndex = -1;    
	if (intf_db.getIfIndex() > 0) {
		ifIndex = intf_db.getIfIndex();
	}

    String telnetIp = null;
    Service telnetService = NetworkElementFactory.getInstance(getServletContext()).getService(nodeId, ipAddr, this.telnetServiceId);
    
    if( telnetService != null  ) {
        telnetIp = ipAddr;
    }    

    String httpIp = null;
    Service httpService = NetworkElementFactory.getInstance(getServletContext()).getService(nodeId, ipAddr, this.httpServiceId);

    if( httpService != null  ) {
        httpIp = ipAddr;
    }
    
    PollerConfigFactory.init();
    PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
    pollerCfgFactory.rebuildPackageIpListMap();
    
%>
<c:url var="eventUrl1" value="event/list.htm">
    <c:param name="filter" value="<%="node=" + nodeId%>"/>
    <c:param name="filter" value="<%="interface=" + ipAddr%>"/>
</c:url>
<c:url var="eventUrl2" value="event/list.htm">
    <c:param name="filter" value="<%="node=" + nodeId%>"/>
    <c:param name="filter" value="<%="ifindex=" + ifIndex%>"/>
</c:url>

<%
String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Interface" />
  <jsp:param name="headTitle" value="<%= ipAddr %>" />
  <jsp:param name="headTitle" value="Interface" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
  <jsp:param name="breadcrumb" value="Interface" />
</jsp:include>

<%
if (request.isUserInRole( Authentication.ROLE_ADMIN )) {
%>

<script type="text/javascript" >
function doDelete() {
     if (confirm("Are you sure you want to proceed? This action will permanently delete this interface and cannot be undone."))
     {
         document.forms["delete"].submit();
     }
     return false;
}
</script>
<%
}
%>


      <h2>Interface: <%=intf_db.getIpAddress()%>
        <% if (intf_db.getHostname() != null && !intf_db.getIpAddress().equals(intf_db.getHostname())) { %>
          (<c:out value="<%=intf_db.getHostname()%>"/>)
        <% } %>
      </h2>

        <%
        if (request.isUserInRole( Authentication.ROLE_ADMIN )) {
        %>
      <form method="post" name="delete" action="admin/deleteInterface">
      <input type="hidden" name="node" value="<%=nodeId%>"/>
      <input type="hidden" name="ifindex" value="<%=(ifIndex == -1 ? "" : "" + ifIndex)%>"/>
      <input type="hidden" name="intf" value="<c:out value="<%=ipAddr%>"/>"/>
      <%
      }
      %>

      <div id="linkbar">
      <ul>
        <% if (! ipAddr.equals("0.0.0.0")) { %>
          <li>
            <a href="<c:out value="${eventUrl1}"/>">View Events by IP Address</a>
          </li>
        <% } %>

        <% if (ifIndex > 0 ) { %>
          <li>
            <a href="<c:out value="${eventUrl2}"/>">View Events by ifIndex</a>
          </li>
        <% } %>

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

        <%
          String ifLabel;
          if (ifIndex != -1) {
              ifLabel = IfLabel.getIfLabelfromIfIndex(nodeId, ipAddr, ifIndex);
          } else {
              ifLabel = IfLabel.getIfLabel(nodeId, ipAddr);
          }

          List<OnmsResource> resources = m_resourceService.findNodeChildResources(nodeId);
          for (OnmsResource resource : resources) {
              if (resource.getName().equals(ipAddr) || resource.getName().equals(ifLabel)) {
                  %>
                      <c:url var="graphLink" value="graph/results.htm">
                          <c:param name="reports" value="all"/>
                          <c:param name="resourceId" value="<%=resource.getId()%>"/>
                      </c:url>
                      <li>
                          <a href="<c:out value="${graphLink}"/>"><c:out value="<%=resource.getResourceType().getLabel()%>"/> Graphs</a>
                      </li>
                  <% 
              }
          }
        %>

        <% if (request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
          <li>
            <a href="admin/deleteInterface" onClick="return doDelete()">Delete</a>
          </li>
        <% } %>

        <% if (request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
          <li>
            <c:url var="rescanUrl" value="element/rescan.jsp">
              <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
              <c:param name="ipaddr" value="<%=ipAddr%>"/>
            </c:url>
            <a href="<c:out value="${rescanUrl}"/>">Rescan</a>
          </li>
        <% } %>

        <% if (request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
          <li>
            <c:url var="schedOutageUrl" value="admin/sched-outages/editoutage.jsp">
              <c:param name="newName" value="<%=ipAddr%>"/>
              <c:param name="addNew" value="true"/>
              <c:param name="ipAddr" value="<%=ipAddr%>"/>
            </c:url>
            <a href="<c:out value="${schedOutageUrl}"/>">Schedule Outage</a>      
          </li>
        <% } %>

      </ul>
      </div>

      <% if (request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
      </form>
      <% } %>

	<div id="contentleft">

        <h3>General</h3>
            <!-- general info box -->
	    <table>
              <tr>
                <th>Node</th>
                <td><a href="element/node.jsp?node=<%=intf_db.getNodeId()%>"><%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(intf_db.getNodeId())%></a></td>
              </tr>
              <tr> 
                <th>Polling Status</th>
                <td><%=ElementUtil.getInterfaceStatusString(intf_db)%></td>
              </tr>
              <% if(ElementUtil.getInterfaceStatusString(intf_db).equals("Managed") && request.isUserInRole( Authentication.ROLE_ADMIN )) {
                  List inPkgs = pollerCfgFactory.getAllPackageMatches(ipAddr);
                  Collections.sort(inPkgs);
                  Iterator pkgiter = inPkgs.iterator();
                  while (pkgiter.hasNext()) { %>
                      <tr>
                          <th>Polling Package</th>
                          <td><%= (String) pkgiter.next()%></td>
                      </tr>
                  <% } %>
              <% } %>
              <tr>
                <th>Interface Index</th>
                <td>
                  <% if( ifIndex != -1 ) {  %>
                    <%=ifIndex%>
                  <% } else { %>
                    &nbsp;
                  <% } %>
                </td>
              </tr>
              <tr> 
                <th>Last Service Scan</th>
                <td><%=(intf_db.getLastCapsdPoll() == null) ? "&nbsp;" : intf_db.getLastCapsdPoll()%></td>
              </tr>              
            </table>
            
            <% if (ifIndex > 0 ) { %>
            
            <!-- Node Link box -->
            <jsp:include page="/includes/interfaceLink-box.jsp" flush="false">
                <jsp:param name="node" value="<%=nodeId%>" />
                <jsp:param name="ifindex" value="<%=ifIndex%>" />
            </jsp:include>
            <% } %>

            <!-- Availability box -->
            <jsp:include page="/includes/interfaceAvailability-box.jsp" flush="false">
                <jsp:param name="node" value="<%=nodeId%>" />
                <jsp:param name="ipAddr" value="<%=ipAddr%>" />
                <jsp:param name="interfaceStatus" value="<%=ElementUtil.getInterfaceStatusString(intf_db)%>" />
            </jsp:include>

</div>
       

<div id="contentright">

            <!-- interface desktop information box -->
          
            <!-- events list box 1 using ipaddress-->
            <% if (!ipAddr.equals("0.0.0.0")) { %>
              <c:set var="eventHeader1">
                <a href="<c:out value="${eventUrl1}"/>">Recent Events (Using Filter IP Address: <c:out value="<%=ipAddr%>"/>)</a>
              </c:set>
              <jsp:include page="/includes/eventlist.jsp" flush="false" >
                <jsp:param name="node" value="<%=nodeId%>" />
                <jsp:param name="ipAddr" value="<%=ipAddr%>" />
                <jsp:param name="throttle" value="5" />
                <jsp:param name="header" value="${eventHeader1}" />
                <jsp:param name="moreUrl" value="${eventUrl1}" />
              </jsp:include>
            <% } %>
            <!-- events list box 2 using ifindex -->
            <% if (ifIndex > 0 ) { %>
              <c:set var="eventHeader2">
                <a href="<c:out value="${eventUrl2}"/>">Recent Events (Using Filter ifIndex: <c:out value="<%=ifIndex%>"/>)</a>
              </c:set>
              <jsp:include page="/includes/eventlist.jsp" flush="false" >
                <jsp:param name="node" value="<%=nodeId%>" />
                <jsp:param name="throttle" value="5" />
                <jsp:param name="header" value="${eventHeader2}" />
                <jsp:param name="moreUrl" value="${eventUrl2}" />
                <jsp:param name="ifIndex" value="<%=ifIndex%>" />
              </jsp:include>
            <% } %>
            <!-- Recent outages box -->
            <jsp:include page="/outage/interfaceOutages-box.htm" flush="false">
                <jsp:param name="node" value="<%=nodeId%>" />
                <jsp:param name="ipAddr" value="<%=ipAddr%>" />
            </jsp:include>         


</div> <!-- id="contentright" -->

<jsp:include page="/includes/footer.jsp" flush="false" />
