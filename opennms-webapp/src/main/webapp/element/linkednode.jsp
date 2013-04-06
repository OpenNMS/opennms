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

<%@page
	language="java"
	contentType="text/html"
	session="true"
	import="
		java.net.*,
		java.util.*,
		org.springframework.web.context.WebApplicationContext,
		org.springframework.web.context.support.WebApplicationContextUtils,
		org.opennms.core.utils.InetAddressUtils,
		org.opennms.netmgt.model.OnmsNode,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.web.event.*,
		org.opennms.web.springframework.security.Authentication,
		org.opennms.web.svclayer.ResourceService
	"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%!
    protected int telnetServiceId;
    protected int httpServiceId;
    protected int dellServiceId;
    protected int snmpServiceId;
    private ResourceService m_resourceService;

    public void init() throws ServletException {

        NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());
        
        try {
            this.telnetServiceId = factory.getServiceIdFromName("Telnet");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the Telnet service ID", e );
        }        

        try {
            this.httpServiceId = factory.getServiceIdFromName("HTTP");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the HTTP service ID", e );
        }

        try {
            this.dellServiceId = factory.getServiceIdFromName("Dell-OpenManage");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the Dell-OpenManage service ID", e );
        }

        try {
            this.snmpServiceId = factory.getServiceIdFromName("SNMP");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the SNMP service ID", e );
        }

        WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
    }%>

<%
    NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());

    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.servlet.MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    //get the database node info
    OnmsNode node_db = factory.getNode( nodeId );
    if( node_db == null ) {
		throw new ElementNotFoundException("No such node in database", "node", "element/linkednode.jsp", "node", "element/nodeList.htm");
    }

    //find the telnet interfaces, if any
    String telnetIp = null;
    Service[] telnetServices = factory.getServicesOnNode(nodeId, this.telnetServiceId);
    
    if( telnetServices != null && telnetServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < telnetServices.length; i++ ) {
            ips.add(InetAddressUtils.addr(telnetServices[i].getIpAddress()));
        }
        
        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);
        
        if( lowest != null ) {
            telnetIp = lowest.getHostAddress();
        }
    }    

    //find the HTTP interfaces, if any
    String httpIp = null;
    Service[] httpServices = factory.getServicesOnNode(nodeId, this.httpServiceId);

    if( httpServices != null && httpServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < httpServices.length; i++ ) {
            ips.add(InetAddressUtils.addr(httpServices[i].getIpAddress()));
        }

        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

        if( lowest != null ) {
            httpIp = lowest.getHostAddress();
        }
    }

    //find the Dell-OpenManage interfaces, if any
    String dellIp = null;
    Service[] dellServices = factory.getServicesOnNode(nodeId, this.dellServiceId);

    if( dellServices != null && dellServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < dellServices.length; i++ ) {
            ips.add(InetAddressUtils.addr(dellServices[i].getIpAddress()));
        }

        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

        if( lowest != null ) {
            dellIp = lowest.getHostAddress();
        }
    }

    //find if SNMP is on this node 
    boolean isSnmp = false;
    Service[] snmpServices = factory.getServicesOnNode(nodeId, this.snmpServiceId);

    if( snmpServices != null && snmpServices.length > 0 ) 
	isSnmp = true;
    
    boolean isBridge = factory.isBridgeNode(nodeId);
    boolean isRouteIP = factory.isRouteInfoNode(nodeId);

%>
<script type="text/javascript">
  function setDown(node, intf){
	document.setStatus.action="element/ManageSnmpIntf?node="+node+"&intf="+intf+"&status="+2;
	document.setStatus.submit();
	}
  function setUp(node, intf){
        document.setStatus.action="element/ManageSnmpIntf?node="+node+"&intf="+intf+"&status="+1;
        document.setStatus.submit();
	}

</script>


<% pageContext.setAttribute("nodeId", nodeId); %>
<% pageContext.setAttribute("nodeLabel", node_db.getLabel()); %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="headTitle" value="${nodeLabel}" />
  <jsp:param name="headTitle" value="Linked Node Info" />
  <jsp:param name="title" value="Linked Node Info" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${nodeId}'>Node</a>" />
  <jsp:param name="breadcrumb" value="Links" />
</jsp:include>



<!-- Body -->

        <h2>Node: <%=node_db.getLabel()%></h2>

      <div id="linkbar">
      <ul>
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

        <% if( dellIp != null ) { %>
          <li>
          <a href="https://<%=dellIp%>:1311">OpenManage</a>
          </li>
        <% } %>

        <% if (m_resourceService.findNodeChildResources(nodeId).size() > 0) { %>
	  <li>
        <c:url var="resourceGraphsUrl" value="graph/chooseresource.htm">
          <c:param name="parentResourceType" value="node"/>
          <c:param name="parentResource" value="<%= Integer.toString(nodeId) %>"/>
          <c:param name="reports" value="all"/>
        </c:url>
          <a href="${fn:escapeXml(resourceGraphsUrl)}">Resource Graphs</a>
	  </li>
        <% } %>
        
         <li>
         <a href="element/rescan.jsp?node=<%=nodeId%>">Rescan</a>
         </li>
        <% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
           <li>
           <a href="admin/nodemanagement/index.jsp?node=<%=nodeId%>">Admin</a>
           </li>
        <% } %>
	  </ul>
	  </div>
	  
	<div class="TwoColLeft">
            <!-- general info box -->
			<h3>General (Status: <%=(node_db == null ? "Unknown" : ElementUtil.getNodeStatusString(node_db))%>)</h3>
			<% if( isRouteIP || isBridge ) { %>
			<div class="boxWrapper">
			     <ul class="plain">
		            <% if( isRouteIP ) { %>
		            <li>
		            	<a href="element/routeipnode.jsp?node=<%=nodeId%>">View Node IP Route Info</a>
		            </li>
		            <% }%>
		         
		            <% if( isBridge ) { %>
		            <li>
						<a href="element/bridgenode.jsp?node=<%=nodeId%>">View Node Bridge/STP Info</a>
					</li>
		            <% }%>		
		         </ul>	     
			</div>
			<% }%>
	</div>
<hr />        

<h3><%=node_db.getLabel()%> Links</h3>
		
		<!-- Link box -->
		<table class="standard">
		
		<thead>
			<tr>
			<th>L2 Interface</th> 
            <th>L3 Interfaces</th>
			<th width="10%">Link Type</th>
			<th width="10%">Status</th>
			<th>Last Scan</th>
			 
<%--
			// TODO - turning this off until the SET is verified.
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
			<th width="10%">Set Admin Status</th> 
			<% } %>
--%>

			<th>Linked to</th>
			</tr>
		</thead>
				
		<% for( LinkInterface linkInterface: factory.getDataLinksOnNode(nodeId)) { %>
		    <tr>

		    <td class="standard">
		 	<% if (linkInterface.hasInterface()) { %>
                
                <% if (linkInterface.getInterface().getSnmpIfName() != null && !linkInterface.getInterface().getSnmpIfName().equals("")) { %>
            	<a href="element/snmpinterface.jsp?node=<%=nodeId%>&ifindex=<%=linkInterface.getInterface().getSnmpIfIndex()%>">
                    <%=linkInterface.getInterface().getSnmpIfName()%>
                </a>
                <% } else { %> 
                 	&nbsp;
    			<% } %> 
            	(ifindex <%=linkInterface.getIfindex()%>)
                
                <% if (linkInterface.getInterface().getSnmpIfAlias() != null && !linkInterface.getInterface().getSnmpIfAlias().equals("")) { %>
                    ifAlias <%=linkInterface.getInterface().getSnmpIfAlias()%>"
                <% } else { %> 
                 	&nbsp;
    			<% } %> 
    			
	            <% if (linkInterface.getInterface().getSnmpIfAdminStatus() > 0 && linkInterface.getInterface().getSnmpIfOperStatus() > 0) { %>
            		<%=ElementUtil.getIfStatusString(linkInterface.getInterface().getSnmpIfAdminStatus())%>/<%=ElementUtil.getIfStatusString(linkInterface.getInterface().getSnmpIfOperStatus())%>
		    	<% } %>
    			
			<% } else { %>
                 <c:out value="No Interface Associated"/>
            <% } %>
            </td>
            
            <td class="standard">
            <% if (linkInterface.hasInterface() && linkInterface.getInterface().hasIpAddresses()) { %>
                <% for (String ipaddress : linkInterface.getInterface().getIpaddresses()) { %>
                	<c:url var="interfaceLink" value="element/interface.jsp">
	            	<c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
    	        	<c:param name="intf" value="<%=ipaddress%>"/>
        			</c:url>
                	<a href="<c:out value="${interfaceLink}"/>"> <%=ipaddress%> </a> &nbsp;
        		<% } %> 
            <% } %>
            </td>
            
            <td class="standard">
            <% if (linkInterface.getLinkTypeIdString() != null ) { %>
             	<%=linkInterface.getLinkTypeIdString()%>
            <% } else if (linkInterface.hasInterface()) { %>
                <%=ElementUtil.getIfTypeString(linkInterface.getInterface().getSnmpIfType())%>
		    <% } else { %>
     			&nbsp;
            <% } %>
            </td>
            
		    <td class="standard">
		    <% if (linkInterface.getStatus() != null ) { %>
             	<%=linkInterface.getStatus()%>
            <% } else { %>
     			&nbsp;
		    <% } %>
		    </td>

		    <td class="standard">
		    <% if (linkInterface.getLastPollTime() != null ) { %>
             	<%=linkInterface.getLastPollTime()%>
		    <% } else { %>
     			&nbsp;
		    <% } %>
		    </td>
					
<%--
		    // TODO - turning this off until the SET is verified.
		    <% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
			<td class="standard" align="center"> 
				<% if(ElementUtil.getIfStatusString[linkInterface.getInterface().getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
		            <input type="button" value="Down" onClick="setDown(<%=linkInterface.getInterface().getNodeId()%>,<%=linkInterface.getInterface().getSnmpIfIndex()%>)"> 
		 		<% } else if (ElementUtil.getIfStatusString[snmpIntfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
		            <input type="button" value="Up" onClick="setUp(<%=linkInterface.getInterface().getNodeId()%>,<%=linkInterface.getInterface().getSnmpIfIndex()%>)"> 
				<% } else { %>
		            <b>&nbsp;</b> 
				<% } %>
			</td>
		    <% } %>
--%>
				
			<td class="standard" style="font-size:70%" width="35%">
		       	<a href="element/linkednode.jsp?node=<%=linkInterface.getLinkedNodeId()%>"><%=factory.getNodeLabel(linkInterface.getLinkedNodeId())%></a>
		       	&nbsp;
		       	<%	if (linkInterface.hasLinkedInterface()) { %>
		       	on 
                
                <% if (linkInterface.getLinkedInterface().getSnmpIfName() != null && !linkInterface.getLinkedInterface().getSnmpIfName().equals("")) { %>
            	<a href="element/snmpinterface.jsp?node=<%=linkInterface.getLinkedNodeId()%>&ifindex=<%=linkInterface.getLinkedInterface().getSnmpIfIndex()%>">
                    <%=linkInterface.getLinkedInterface().getSnmpIfName()%>
                </a>
                <% } else if (linkInterface.getLinkedInterface().hasIpAddresses() && linkInterface.getLinkedInterface().getIpaddresses().size() > 0 ) { %>
	                <% for (String ipaddress : linkInterface.getLinkedInterface().getIpaddresses()) { %>
                	<c:url var="interfaceLink" value="element/interface.jsp">
	            	<c:param name="node" value="<%=String.valueOf(linkInterface.getLinkedNodeId())%>"/>
    	        	<c:param name="intf" value="<%=ipaddress%>"/>
        			</c:url>
                	<a href="<c:out value="${interfaceLink}"/>"> <%=ipaddress%> </a> &nbsp;
    	    		<% } %>                 
                <% } else { %> 
                 	&nbsp;
    			<% } %> 
            	(ifindex <%=linkInterface.getLinkedIfindex()%>)
                
                <% if (linkInterface.getLinkedInterface().getSnmpIfAlias() != null && !linkInterface.getLinkedInterface().getSnmpIfAlias().equals("")) { %>
                    ifAlias <%=linkInterface.getLinkedInterface().getSnmpIfAlias()%>"
                <% } else { %> 
                 	&nbsp;
    			<% } %> 
    			
	            <% if (linkInterface.getLinkedInterface().getSnmpIfAdminStatus() > 0 && linkInterface.getLinkedInterface().getSnmpIfOperStatus() > 0) { %>
            		<%=ElementUtil.getIfStatusString(linkInterface.getLinkedInterface().getSnmpIfAdminStatus())%>/<%=ElementUtil.getIfStatusString(linkInterface.getLinkedInterface().getSnmpIfOperStatus())%>
		    	<% } %>
    			
			<% } else { %>
                 <c:out value="No Interface Associated"/>
            <% } %>
		       	
			</td>
	
		    </tr>
	    <% } %>
		    
	    </table>


<form method="post" name="setStatus" />

<jsp:include page="/includes/footer.jsp" flush="false" />