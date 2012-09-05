<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
		import="java.util.*,
				org.opennms.netmgt.config.SnmpInterfacePollerConfigFactory,
				org.opennms.netmgt.config.SnmpInterfacePollerConfig,
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

<%!
    private WebApplicationContext m_webAppContext;
    private ResourceService m_resourceService;
    
    public void init() throws ServletException {

        m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) m_webAppContext.getBean("resourceService", ResourceService.class);
    }%>

<%
    Interface intf_db = ElementUtil.getSnmpInterfaceByParams(request, getServletContext());
    int nodeId = intf_db.getNodeId();
    String ipAddr = intf_db.getIpAddress();
	int ifIndex = -1;    
	if (intf_db.getSnmpIfIndex() > 0) {
		ifIndex = intf_db.getSnmpIfIndex();
	}
    


    String eventUrl2 = "event/list.htm?filter=node%3D" + nodeId + "&filter=ifindex%3D" + ifIndex;
    
%>

<%
String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Snmp Interface" />
  <jsp:param name="headTitle" value="Snmp Interface" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
  <jsp:param name="breadcrumb" value="SnmpInterface" />
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


      <h2>Interface: <%=(intf_db.getSnmpIfDescription() == null) ? "&nbsp;" : intf_db.getSnmpIfDescription()%>
      </h2>

        <%
        if (request.isUserInRole( Authentication.ROLE_ADMIN )) {
        %>
      <form method="post" name="delete" action="admin/deleteInterface">
      <input type="hidden" name="node" value="<%=nodeId%>"/>
      <input type="hidden" name="ifindex" value="<%=(ifIndex == -1 ? "" : String.valueOf(ifIndex))%>"/>
      <input type="hidden" name="intf" value="<%=ipAddr%>"/>
      <%
      }
      %>

      <div id="linkbar">
      <ul>
		
        <%
        if (ifIndex > 0 ) {
        %>
	<li>
        <a href="<%=eventUrl2%>">View Events by ifIndex</a>
	</li>
		<% } %>
		

      <%
                          String ifLabel;
                          if (ifIndex != -1) {
                              ifLabel = IfLabel.getIfLabelfromSnmpIfIndex(nodeId, ifIndex);
                          } else {
                              ifLabel = "no_ifLabel";
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
                <th>Physical Address</th>
                <td>
                  <% String macAddr = intf_db.getPhysicalAddress(); %>
                  <% if( macAddr != null && macAddr.trim().length() > 0 && !macAddr.equals("000000000000")) { %>
                    <%=macAddr%>
                  <% } else { %>
                    &nbsp;
                  <% } %>
                </td>
              </tr>
              <% if( ipAddr != null && !ipAddr.equals("0.0.0.0")) { %>
                <tr>
                  <th>IP Address</th>
                  <td>
                    <%=ipAddr%>
                  </td>
                </tr>
              <% } %>
                <tr> 
	          	  <th>Last Snmp Table Scan</th>
    	          <td><%=intf_db.getSnmpLastCapsdPoll()%></td>
        	  	</tr>
              
				<tr>
	              <th>Snmp Polling Status</th>
	              <td><%=ElementUtil.getSnmpInterfaceStatusString(intf_db)%></td>
	            </tr>  
                <tr> 
	          	  <th>Last Snmp Poll</th>
        	          <td><%=(intf_db.getSnmpLastSnmpPoll() == null) ? "&nbsp;" : intf_db.getSnmpLastSnmpPoll()%></td>
        	  	</tr>              

            </table>
            
            <!-- Node Link box -->
            <jsp:include page="/includes/interfaceLink-box.jsp" flush="false">
                <jsp:param name="node" value="<%=nodeId%>" />
                <jsp:param name="ifindex" value="<%=ifIndex%>" />
            </jsp:include>
                        

            <!-- SNMP box, if info available -->
              <h3>SNMP Attributes</h3>
		  	<table>
                <tr>
                  <th>Interface Type</th>
                  <td><%=ElementUtil.getIfTypeString(intf_db.getSnmpIfType())%></td>
                </tr>
                <tr> 
                  <th>Status (Adm/Op)</th>
                  <td>
                    <% if( intf_db.getSnmpIfAdminStatus() < 1 || intf_db.getSnmpIfOperStatus() < 1 ) { %>
                      &nbsp;
                    <% } else { %>
                      <%=ElementUtil.getIfStatusString(intf_db.getSnmpIfAdminStatus())%>/<%=ElementUtil.getIfStatusString(intf_db.getSnmpIfOperStatus())%>
                    <% } %>
                  </td>
                </tr>
                <tr>
                  <th>Speed</th>
                  <td><%=(intf_db.getSnmpIfSpeed() > 0) ? SIUtils.getHumanReadableIfSpeed(intf_db.getSnmpIfSpeed()) : "&nbsp;"%></td>
                </tr>
                <tr> 
                  <th>Description</th>
                  <td><%=(intf_db.getSnmpIfDescription() == null) ? "&nbsp;" : intf_db.getSnmpIfDescription()%></td>
                </tr>
                <tr>
                  <th>Alias</th>
                  <td><%=(intf_db.getSnmpIfAlias() == null) ? "&nbsp;" : intf_db.getSnmpIfAlias()%></td>
                </tr>

              </table>


</div>
       

<div id="contentright">

            <!-- interface desktop information box -->
          
            <!-- events list box 2 using ifindex -->
			<% if (ifIndex > 0 ) { %>
            <% String eventHeader2 = "<a href='" + eventUrl2 + "'>Recent Events (Using Filter ifIndex = " + ifIndex + ")</a>"; %>
            <% String moreEventsUrl2 = eventUrl2; %>
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader2%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl2%>" />
              <jsp:param name="ifIndex" value="<%=ifIndex%>" />
            </jsp:include>
            <% } %>
            
            <!-- STP Info box -->
            <jsp:include page="/includes/interfaceSTP-box.jsp" flush="false" />
         

</div> <!-- id="contentright" -->

<jsp:include page="/includes/footer.jsp" flush="false" />