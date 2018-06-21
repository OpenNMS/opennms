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

<%@page import="java.util.Collection"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@page import="org.opennms.netmgt.model.OnmsNode"%>
<%@page import="org.opennms.web.element.ElementNotFoundException"%>
<%@page import="org.opennms.web.element.NetworkElementFactory"%>
<%@page import="org.opennms.web.element.NetworkElementFactoryInterface"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkNode"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkRemoteNode"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkSharedHost"%>
<%@page import="org.opennms.web.enlinkd.CdpLinkNode"%>
<%@ page import="org.opennms.web.enlinkd.EnLinkdElementFactory" %>
<%@ page import="org.opennms.web.enlinkd.EnLinkdElementFactoryInterface" %>
<%@ page import="org.opennms.web.enlinkd.IsisLinkNode" %>
<%@ page import="org.opennms.web.enlinkd.LldpLinkNode" %>
<%@ page import="org.opennms.web.enlinkd.NodeLinkBridge" %>
<%@ page import="org.opennms.web.enlinkd.OspfLinkNode" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    final NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());
    final EnLinkdElementFactoryInterface enlinkdfactory = EnLinkdElementFactory.getInstance(getServletContext());

    final String nodeIdString = request.getParameter( "node" );
    if( nodeIdString == null ) {
        throw new org.opennms.web.servlet.MissingParameterException( "node" );
    }
    final int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    //get the database node info
    final OnmsNode node_db = factory.getNode( nodeId );
    if( node_db == null ) {
		throw new ElementNotFoundException("No such node in database", "node", "element/linkednode.jsp", "node", "element/nodeList.htm");
    }

	pageContext.setAttribute("nodeId", nodeId);
	pageContext.setAttribute("nodeLabel", node_db.getLabel());

	Collection<LldpLinkNode> lldpLinks = enlinkdfactory.getLldpLinks(nodeId);
	Collection<BridgeLinkNode> bridgelinks = enlinkdfactory.getBridgeLinks(nodeId);
	Collection<CdpLinkNode> cdpLinks = enlinkdfactory.getCdpLinks(nodeId);
	Collection<NodeLinkBridge> nodelinks = enlinkdfactory.getNodeLinks(nodeId);
	Collection<OspfLinkNode> ospfLinks = enlinkdfactory.getOspfLinks(nodeId);
	Collection<IsisLinkNode> isisLinks = enlinkdfactory.getIsisLinks(nodeId);
%>

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
  <h4>Node: ${nodeLabel}</h4>

<div class="row">
<div class="col-md-12">

<!--  BRIDGE Links -->

<div class="panel panel-default">
<%

   if (bridgelinks.isEmpty()) {
	   if (nodelinks.isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No Bridge Forwarding Table Links found on ${nodeLabel} by Enhanced Linkd</h3>
	</div>
	<% } else { %>
    <div class="panel-heading">
        <h3 class="panel-title">${nodeLabel} Shared Segments found by Enhanced Linkd using Bridge Forwarding Table</h3>
	</div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Port - Ip - Mac</th> 
			<th>Other Hosts on Segment</th> 
			<th>Bridge Ports on Segment</th>
			</tr>
		</thead>
				
		<% for( NodeLinkBridge nodelink: nodelinks) { %>
	    <tr>
		    <td><%=nodelink.getNodeLocalPort()%></td>
            <td>
			<% if (nodelink.getBridgeLinkSharedHost().isEmpty()) {%>
            	            	&nbsp;
			<% } else { %>
            	<table>
            	<% for (BridgeLinkSharedHost sharedhost: nodelink.getBridgeLinkSharedHost()) {%>
            	<tr>
            		<td>
		 			<% if (sharedhost.getSharedHostUrl() != null) { %>
            		<a href="<%=sharedhost.getSharedHostUrl()%>"><%=sharedhost.getSharedHost()%></a>
            		<% } else { %> 
            		<%=sharedhost.getSharedHost()%>
		    		<% } %> 
		 			<% if (sharedhost.getSharedHostPortUrl() != null) { %>
            		&nbsp;(<a href="<%=sharedhost.getSharedHostPortUrl()%>"><%=sharedhost.getSharedHostPort()%></a>)
            		<% } else if (sharedhost.getSharedHostPort() != null ){ %> 
            		&nbsp;(<%=sharedhost.getSharedHostPort()%>)
            		<% } else { %> 
            		&nbsp;
    				<% } %> 
            		</td>
            	<tr>
            	<% }%>
            	</table>
			<% } %>
            </td>
            <td>
		    <% if (nodelink.getBridgeLinkRemoteNodes().isEmpty()) {%>
            	            	&nbsp;
			<% } else { %>
            	<table>
            	<% for (BridgeLinkRemoteNode remote: nodelink.getBridgeLinkRemoteNodes()) {%>
            	<tr>
            		<td>
            		<a href="<%=remote.getBridgeRemoteUrl()%>"><%=remote.getBridgeRemoteNode() %></a>
            		</td>
            		<td>
            		<a href="<%=remote.getBridgeRemotePortUrl()%>"><%=remote.getBridgeRemotePort()%></a>
            		</td>
            		<td>
         			<% if (remote.getBridgeRemoteVlan() != null) { %>
            		VLAN(<%=remote.getBridgeRemoteVlan() %>)
            		<% } else { %> 
            		&nbsp;
    				<% } %> 
            		</td>
            	<tr>
            	<% }%>
            	</table>
			<% }%>
            </td>
	    </tr>
	    <% } %>
		    
	    </table>

	<% } %>
	
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title">${nodeLabel} Bridge Forwarding Table Links found by Enhanced Linkd</h3>
  </div>		
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Port</th> 
            <th>VLAN</th>
			<th>Hosts on Segment</th> 
			<th>Bridge Ports on Segment</th>
			</tr>
		</thead>
				
		<% for( BridgeLinkNode bridgelink: bridgelinks) { %>
		<tr>
			<td><%=bridgelink.getNodeLocalPort()%></td>
		    <td>
<% if (bridgelink.getBridgeLocalVlan() == null) {%>
            	            	&nbsp;
<% } else { %>
		    <%=bridgelink.getBridgeLocalVlan()%>
<%} %>
		    </td>
            <td>
<% if (bridgelink.getBridgeLinkSharedHost().isEmpty()) {%>
            	            	&nbsp;
<% } else { %>
            	<table>
            	<% for (BridgeLinkSharedHost sharedhost: bridgelink.getBridgeLinkSharedHost()) {%>
            	<tr>
            		<td>
		 			<% if (sharedhost.getSharedHostUrl() != null) { %>
            		<a href="<%=sharedhost.getSharedHostUrl()%>"><%=sharedhost.getSharedHost()%></a>
            		<% } else { %> 
            		<%=sharedhost.getSharedHost()%>
		    		<% } %> 
		 			
		 			<% if (sharedhost.getSharedHostPortUrl() != null) { %>
            		&nbsp;(<a href="<%=sharedhost.getSharedHostPortUrl()%>"><%=sharedhost.getSharedHostPort()%></a>)
            		<% } else if (sharedhost.getSharedHostPort() != null ){ %> 
            		&nbsp;(<%=sharedhost.getSharedHostPort()%>)
            		<% } else { %> 
            		&nbsp;
    				<% } %> 
            		</td>
            	<tr>
            	<% }%>
            	</table>
<% }%>
            </td>
            <td>
<% if (bridgelink.getBridgeLinkRemoteNodes().isEmpty()) {%>
            	            	&nbsp;
<% } else { %>
            	<table>
            	<% for (BridgeLinkRemoteNode remote: bridgelink.getBridgeLinkRemoteNodes()) {%>
            	<tr>
            		<td>
            		<a href="<%=remote.getBridgeRemoteUrl()%>"><%=remote.getBridgeRemoteNode()%></a>
            		</td>
            		<td>
            		<a href="<%=remote.getBridgeRemotePortUrl()%>"><%=remote.getBridgeRemotePort()%></a>
            		</td>
            		<td>
         	<% if (remote.getBridgeRemoteVlan() != null) { %>
            		
            		VLAN(<%=remote.getBridgeRemoteVlan() %>)
            <% } else { %> 
            	&nbsp;
    		<% } %> 
            		</td>
            	<tr>
            	<% }%>
            	</table>
<% }%>
            </td>
	    </tr>
		    <% } %>
	    <% } %>
		    
	    </table>

</div>

<!-- LLDP Links -->

<div class="panel panel-default">
<%
    if (lldpLinks.isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No LLDP Remote Table Links found on ${nodeLabel} by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title">${nodeLabel} LLDP Remote Table Links found by Enhanced Linkd</h3>
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
				
		<% for( LldpLinkNode lldplink: lldpLinks) { %>
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
<% if (cdpLinks.isEmpty()) { %>
	<div class="panel-heading">
		<h3 class="panel-title">No CDP Cache Table Links found on ${nodeLabel} by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title">${nodeLabel} CDP Cache Table Links found by Enhanced Linkd</h3>
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
		<% for( CdpLinkNode cdplink: cdpLinks) { %>
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
   if (ospfLinks.isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No OSPF Nbr Links found on ${nodeLabel} by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title">${nodeLabel} OSPF Nbr Table Links found by Enhanced Linkd</h3>
	</div>
		<!-- Link box -->
		<table class="table table-condensed">
		
		<thead>
			<tr>
			<th>Local Ip Address</th> 
            <th>Local AddressLessIfIndex</th>
			<th>Nbr Router Id</th>
			<th>Nbr Ip Address</th>
			<th>Nbr AddressLessIfIndex</th> 
			<th>Created</th>
			<th>Last Poll</th>
			</tr>
		</thead>
				
		<% for( OspfLinkNode ospflink: ospfLinks) { %>
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
   if (isisLinks.isEmpty()) {
%>
	<div class="panel-heading">
		<h3 class="panel-title">No IS-IS Adjacency Links found on ${nodeLabel} by Enhanced Linkd</h3>
	</div>
<% } else { %>
  <div class="panel-heading">
    <h3 class="panel-title">${nodeLabel} IS-IS Adj Table Links found by Enhanced Linkd</h3>
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
				
		<% for( IsisLinkNode isislink : isisLinks) { %>
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
