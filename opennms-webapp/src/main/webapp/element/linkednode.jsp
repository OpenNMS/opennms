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

<%@page import="org.opennms.web.enlinkd.EnLinkdElementFactory"%>
<%@page import="org.opennms.web.enlinkd.EnLinkdElementFactoryInterface"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkNode"%>
<%@page import="org.opennms.web.enlinkd.NodeLinkBridge"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkRemoteNode"%>
<%@page import="org.opennms.web.enlinkd.LldpLinkNode"%>
<%@page import="org.opennms.web.enlinkd.CdpLinkNode"%>
<%@page import="org.opennms.web.enlinkd.OspfLinkNode"%>
<%@page import="org.opennms.web.enlinkd.IsisLinkNode"%>
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
		org.opennms.web.api.Authentication,
		org.opennms.web.svclayer.api.ResourceService
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
    EnLinkdElementFactoryInterface enlinkdfactory = EnLinkdElementFactory.getInstance(getServletContext());

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
    String parentRes = Integer.toString(nodeId);
    String parentResType = "node";
    if (!(node_db.getForeignSource() == null) && !(node_db.getForeignId() == null)) {
        parentRes = node_db.getForeignSource() + ":" + node_db.getForeignId();
        parentResType = "nodeSource";
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
    Service[] snmpServices = factory.getServicesOnNode(nodeId, this.snmpServiceId);

    boolean isBridge = factory.isBridgeNode(nodeId);
    boolean isRouteIP = factory.isRouteInfoNode(nodeId);

%>

<% pageContext.setAttribute("nodeId", nodeId); %>
<% pageContext.setAttribute("nodeLabel", node_db.getLabel()); %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="headTitle" value="${nodeLabel}" />
  <jsp:param name="headTitle" value="Linked Node Info" />
  <jsp:param name="title" value="Linked Node Info" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${nodeId}'>Node</a>" />
  <jsp:param name="breadcrumb" value="Links" />
</jsp:include>

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

        <% if( dellIp != null ) { %>
          <li>
          <a href="https://<%=dellIp%>:1311">OpenManage</a>
          </li>
        <% } %>

        <% if (m_resourceService.findNodeChildResources(node_db).size() > 0) { %>
	  <li>
        <c:url var="resourceGraphsUrl" value="graph/chooseresource.htm">
          <c:param name="parentResourceType" value="<%=parentResType%>"/>
          <c:param name="parentResource" value="<%=parentRes%>"/>
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

<div class="row">
<div class="col-md-12">
	<div class="panel panel-default">
    <!-- general info box -->
    <div class="panel-heading">
			<h3 class="panel-title">General (Status: <%=(node_db == null ? "Unknown" : ElementUtil.getNodeStatusString(node_db))%>)</h3>
    </div>
			<% if( isRouteIP || isBridge ) { %>
			<div class="panel-body">
			     <ul class="list-inline">
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

<!-- LINKD Links -->

  <div class="panel panel-default">
<%
   if (factory.getDataLinksOnNode(nodeId).isEmpty()) {
%>
  <div class="panel-heading">
		<h3 class="panel-title">No Links found on <%=node_db.getLabel()%> by Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> Links found by Linkd</h3>
	</div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>L2 Interface</th> 
            <th>L3 Interfaces</th>
			<th width="10%">Link Type</th>
			<th width="10%">Status</th>
			<th>Discovery Protocol</th>
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

		    <td>
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
                    (ifAlias <%=linkInterface.getInterface().getSnmpIfAlias()%>)
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
            
            <td>
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
            
            <td>
            <% if (linkInterface.getLinkTypeIdString() != null ) { %>
             	<%=linkInterface.getLinkTypeIdString()%>
            <% } else if (linkInterface.hasInterface()) { %>
                <%=ElementUtil.getIfTypeString(linkInterface.getInterface().getSnmpIfType())%>
		    <% } else { %>
     			&nbsp;
            <% } %>
            </td>
            
		    <td>
		    <% if (linkInterface.getStatus() != null ) { %>
             	<%=linkInterface.getStatus()%>
            <% } else { %>
     			&nbsp;
		    <% } %>
		    </td>

		    <td>
             	<%=linkInterface.getProtocol()%>
		    </td>

		    <td>
		    <% if (linkInterface.getLastPollTime() != null ) { %>
             	<%=linkInterface.getLastPollTime()%>
		    <% } else { %>
     			&nbsp;
		    <% } %>
		    </td>
					
<%--
		    // TODO - turning this off until the SET is verified.
		    <% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
			<td align="center"> 
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
				
			<td style="font-size:70%" width="35%">
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

<% }  %>
</div>

<!--  BRIDGE Links -->

<div class="panel panel-default">
<%
   Collection<BridgeLinkNode> bridgelinks = enlinkdfactory.getBridgeLinks(nodeId);
   if (bridgelinks.isEmpty()) {
	   Collection<NodeLinkBridge> nodelinks = enlinkdfactory.getNodeLinks(nodeId);
	   if (nodelinks.isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No Bridge Forwarding Table Links found on <%=node_db.getLabel()%> by Enhanced Linkd</h3>
	</div>
	<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> Bridge Forwarding Table Links found by Enhanced Linkd</h3>
	</div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Local Port</th> 
			<th>Bridge Node</th>
			<th>Bridge Port</th> 
            <th>Bridge Vlan</th>
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
				
		<% for( NodeLinkBridge nodelink: nodelinks) { %>
			<% for( String localport: nodelink.getNodeLocalPorts()) { %>
	    <tr>
		    <td><%=localport%></td>
            <td>
            	<a href="<%=nodelink.getBridgeLinkRemoteNode().getBridgeRemoteUrl()%>"><%=nodelink.getBridgeLinkRemoteNode().getBridgeRemoteNode()%></a>
             </td>
            <td>
            	<a href="<%=nodelink.getBridgeLinkRemoteNode().getBridgeRemotePortUrl()%>"><%=nodelink.getBridgeLinkRemoteNode().getBridgeRemotePort()%></a>
            </td>
		    <td>
		 	<% if (nodelink.getBridgeLinkRemoteNode().getBridgeRemoteVlan() != null) { %>
            	<%=nodelink.getBridgeLinkRemoteNode().getBridgeRemoteVlan()%>
            <% } else { %> 
            	&nbsp;
    		<% } %> 
            </td>
		    <td><%=nodelink.getBridgeLinkCreateTime()%></td>
		    <td><%=nodelink.getBridgeLinkLastPollTime()%></td>
	    </tr>
		    <% } %>
	    <% } %>
		    
	    </table>

	<% } %>
	
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> Bridge Forwarding Table Links found by Enhanced Linkd</h3>
  </div>		
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Local Port</th> 
            <th>Local Vlan</th>
			<th>Remote Node</th>
			<th>Remote Port</th> 
            <th>Remote Vlan</th>
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
				
		<% for( BridgeLinkNode bridgelink: bridgelinks) { %>
			<% for( BridgeLinkRemoteNode remlink: bridgelink.getBridgeLinkRemoteNodes()) { %>
	    <tr>
		    <td><%=bridgelink.getBridgeLocalPort()%></td>
		    <td>
		 	<% if (bridgelink.getBridgeLocalVlan() != null) { %>
            	<%=bridgelink.getBridgeLocalVlan()%>
            <% } else { %> 
            	&nbsp;
    		<% } %> 
            </td>
            <td>
            <% if (remlink.getBridgeRemoteUrl() != null) { %>
            	<a href="<%=remlink.getBridgeRemoteUrl()%>"><%=remlink.getBridgeRemoteNode()%></a>
            <% } else { %> 
				<%=remlink.getBridgeRemoteNode()%>
    			<% } %> 
            </td>
            <td>
           <% if (remlink.getBridgeRemotePortUrl() != null) { %>
            	<a href="<%=remlink.getBridgeRemotePortUrl()%>"><%=remlink.getBridgeRemotePort()%></a>
            <% } else { %> 
				<%=remlink.getBridgeRemotePort() != null ? remlink.getBridgeRemotePort() : "" %>
    			<% } %> 
            </td>
		    <td>
		 	<% if (remlink.getBridgeRemoteVlan() != null) { %>
            	<%=remlink.getBridgeRemoteVlan()%>
            <% } else { %> 
            	&nbsp;
    		<% } %> 
            </td>
		    <td><%=bridgelink.getBridgeLinkCreateTime()%></td>
		    <td><%=bridgelink.getBridgeLinkLastPollTime()%></td>
	    </tr>
		    <% } %>
	    <% } %>
		    
	    </table>

<% }  %>
</div>

<!-- LLDP Links -->

<div class="panel panel-default">
<%
   if (enlinkdfactory.getLldpLinks(nodeId).isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No LLDP Remote Table Links found on <%=node_db.getLabel()%> by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> LLDP Remote Table Links found by Enhanced Linkd</h3>
  </div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Local Port</th> 
            <th>Local Port Descr</th>
			<th>Remote Chassis Id</th>
			<th>Remote Sysname</th>
			<th>Remote Port</th> 
            <th>Remote Port Descr</th>
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
				
		<% for( LldpLinkNode lldplink: enlinkdfactory.getLldpLinks(nodeId)) { %>
	    <tr>
		    <td>
		 	<% if (lldplink.getLldpPortUrl() != null) { %>
            	<a href="<%=lldplink.getLldpPortUrl()%>"><%=lldplink.getLldpPortString()%></a>
            <% } else { %> 
                    <%=lldplink.getLldpPortString()%>
    		<% } %> 
            </td>
		    <td><%=lldplink.getLldpPortDescr()%></td>
            <td>
            <% if (lldplink.getLldpRemChassisIdUrl() != null) { %>
            	<a href="<%=lldplink.getLldpRemChassisIdUrl()%>"><%=lldplink.getLldpRemChassisIdString()%></a>
            <% } else { %> 
                    <%=lldplink.getLldpRemChassisIdString()%>
    			<% } %> 
            </td>
            <td>
                    <%=lldplink.getLldpRemSysName()%>
            </td>
		    <td>
		 	<% if (lldplink.getLldpRemPortUrl() != null) { %>
            	<a href="<%=lldplink.getLldpRemPortUrl()%>"><%=lldplink.getLldpRemPortString()%></a>
            <% } else { %> 
                    <%=lldplink.getLldpRemPortString()%>
    		<% } %> 
            </td>
		    <td><%=lldplink.getLldpRemPortDescr()%></td>
		    <td><%=lldplink.getLldpCreateTime()%></td>
		    <td><%=lldplink.getLldpLastPollTime()%></td>
	    </tr>
	    <% } %>
		    
	    </table>

<% }  %>
</div>

<!-- CDP Links -->

<div class="panel panel-default">
<% if (enlinkdfactory.getCdpLinks(nodeId).isEmpty()) { %>
	<div class="panel-heading">
		<h3 class="panel-title">No CDP Cache Table Links found on <%=node_db.getLabel()%> by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> CDP Cache Table Links found by Enhanced Linkd</h3>
  </div>
	<table class="table table-condensed">		
		<thead>
			<tr>
			<th>Local Port</th> 
			<th>Address Type</th>
			<th>Address</th>
			<th>Version</th>
			<th>Device Id</th>
			<th>Device Port</th> 
      <th>Platform</th>
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
		<% for( CdpLinkNode cdplink: enlinkdfactory.getCdpLinks(nodeId)) { %>
	    <tr>
		    <td>
		 	  <% if (cdplink.getCdpLocalPortUrl() != null) { %>
        <a href="<%=cdplink.getCdpLocalPortUrl()%>"><%=cdplink.getCdpLocalPort()%></a>
        <% } else { %> 
        <%=cdplink.getCdpLocalPort()%>
        <% } %> 
        </td>
		    <td><%=cdplink.getCdpCacheAddressType()%></td>
		    <td><%=cdplink.getCdpCacheAddress()%></td>
		    <td><%=cdplink.getCdpCacheVersion()%></td>
        <td>
        <% if (cdplink.getCdpCacheDeviceUrl() != null) { %>
          <a href="<%=cdplink.getCdpCacheDeviceUrl()%>"><%=cdplink.getCdpCacheDeviceId()%></a>
        <% } else { %> 
          <%=cdplink.getCdpCacheDeviceId()%>
    		<% } %> 
        </td>
		    <td>
		 	  <% if (cdplink.getCdpCacheDevicePortUrl() != null) { %>
          <a href="<%=cdplink.getCdpCacheDevicePortUrl()%>"><%=cdplink.getCdpCacheDevicePort()%></a>
        <% } else { %> 
          <%=cdplink.getCdpCacheDevicePort()%>
    		<% } %> 
        </td>
		    <td><%=cdplink.getCdpCacheDevicePlatform()%></td>
		    <td><%=cdplink.getCdpCreateTime()%></td>
		    <td><%=cdplink.getCdpLastPollTime()%></td>
	    </tr>
    <% } %>
  </table>
<% } %>
</div>

<!-- OSPF Links -->

<div class="panel panel-default">
<%
   if (enlinkdfactory.getOspfLinks(nodeId).isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No OSPF Nbr Links found on <%=node_db.getLabel()%> by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> OSPF Nbr Table Links found by Enhanced Linkd</h3>
	</div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Local Ip Address</th> 
            <th>Local Address Less Index</th>
			<th>Nbr Router Id</th>
			<th>Nbr Ip Address</th>
			<th>Nbr Address Kess Index</th> 
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
				
		<% for( OspfLinkNode ospflink: enlinkdfactory.getOspfLinks(nodeId)) { %>
	    <tr>
		    <td><%=ospflink.getOspfIpAddr()%>(ifindex=<%=ospflink.getOspfIfIndex()%>)</td>
		    <td><%=ospflink.getOspfAddressLessIndex()%></td>
            <td>
            <% if (ospflink.getOspfRemRouterUrl() != null) { %>
            	<a href="<%=ospflink.getOspfRemRouterUrl()%>"><%=ospflink.getOspfRemRouterId()%></a>
            <% } else { %> 
                    <%=ospflink.getOspfRemRouterId()%>
    			<% } %> 
            </td>
		    <td>
		 	<% if (ospflink.getOspfRemPortUrl() != null) { %>
            	<a href="<%=ospflink.getOspfRemPortUrl()%>"><%=ospflink.getOspfRemIpAddr()%></a>
            <% } else { %> 
                    <%=ospflink.getOspfRemIpAddr()%>
    		<% } %> 
            </td>
		    <td><%=ospflink.getOspfRemAddressLessIndex()%></td>
		    <td><%=ospflink.getOspfLinkCreateTime()%></td>
		    <td><%=ospflink.getOspfLinkLastPollTime()%></td>
	    </tr>
	    <% } %>
		    
	    </table>

<% }  %>
</div>

<!-- ISIS Links -->

<div class="panel panel-default">
<%
   if (enlinkdfactory.getIsisLinks(nodeId).isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No IS-IS Adjacency Links found on <%=node_db.getLabel()%> by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title"><%=node_db.getLabel()%> IS-IS Adj Table Links found by Enhanced Linkd</h3>
	</div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Circuit IfIndex</th> 
			<th>Circuit Admin State</th>
			<th>Adj Neigh Sys ID</th>
			<th>Adj Neigh Sys Type</th> 
			<th>Adj Neigh Port</th> 
			<th>Adj Neigh State</th> 
			<th>Adj Neigh SNPA Address</th> 
			<th>Adj Neigh Extended Circ ID</th> 
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
				
		<% for( IsisLinkNode isislink: enlinkdfactory.getIsisLinks(nodeId)) { %>
	    <tr>
		    <td><%=isislink.getIsisCircIfIndex()%></td>
		    <td><%=isislink.getIsisCircAdminState()%></td>
            <td>
            <% if (isislink.getIsisISAdjNeighSysUrl() != null) { %>
            	<a href="<%=isislink.getIsisISAdjNeighSysUrl()%>"><%=isislink.getIsisISAdjNeighSysID() %></a>
            <% } else { %> 
                   <%=isislink.getIsisISAdjNeighSysID()%>
    			<% } %> 
            </td>
		    <td><%=isislink.getIsisISAdjNeighSysType()%></td>
		    <td>
		 	<% if (isislink.getIsisISAdjUrl() != null) { %>
            	<a href="<%=isislink.getIsisISAdjUrl()%>"><%=isislink.getIsisISAdjNeighPort()%></a>
            <% } else { %> 
				<%=isislink.getIsisISAdjNeighPort()%>
    		<% } %> 
            </td>
		    <td><%=isislink.getIsisISAdjState()%></td>
		    <td><%=isislink.getIsisISAdjNeighSNPAAddress()%></td>
		    <td><%=isislink.getIsisISAdjNbrExtendedCircID()%></td>
		    <td><%=isislink.getIsisLinkCreateTime()%></td>
		    <td><%=isislink.getIsisLinkLastPollTime()%></td>
	    </tr>
	    <% } %>
		    
	    </table>

<% }  %>
</div>

</div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
