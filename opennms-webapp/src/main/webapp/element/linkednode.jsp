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
<%@page import="org.opennms.web.enlinkd.IsisElementNode"%>
<%@page import="org.opennms.web.enlinkd.OspfElementNode"%>
<%@page import="org.opennms.web.enlinkd.CdpElementNode"%>
<%@page import="org.opennms.web.enlinkd.LldpElementNode"%>
<%@page import="java.util.Collection"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@page import="org.opennms.netmgt.model.OnmsNode"%>
<%@page import="org.opennms.web.element.ElementNotFoundException"%>
<%@page import="org.opennms.web.element.NetworkElementFactory"%>
<%@page import="org.opennms.web.element.NetworkElementFactoryInterface"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkNode"%>
<%@page import="org.opennms.web.enlinkd.BridgeLinkRemoteNode"%>
<%@page import="org.opennms.web.enlinkd.CdpLinkNode"%>
<%@ page import="org.opennms.web.enlinkd.EnLinkdElementFactory" %>
<%@ page import="org.opennms.web.enlinkd.EnLinkdElementFactoryInterface" %>
<%@ page import="org.opennms.web.enlinkd.IsisLinkNode" %>
<%@ page import="org.opennms.web.enlinkd.LldpLinkNode" %>
<%@ page import="org.opennms.web.enlinkd.BridgeLinkNode" %>
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
	Collection<OspfLinkNode> ospfLinks = enlinkdfactory.getOspfLinks(nodeId);
	Collection<IsisLinkNode> isisLinks = enlinkdfactory.getIsisLinks(nodeId);
	LldpElementNode lldpelem = enlinkdfactory.getLldpElement(nodeId);
	CdpElementNode cdpelem = enlinkdfactory.getCdpElement(nodeId);
	OspfElementNode ospfelem = enlinkdfactory.getOspfElement(nodeId);
	IsisElementNode isiselem = enlinkdfactory.getIsisElement(nodeId);
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("${nodeLabel}")
          .headTitle("Linked Node Info")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "element/node.jsp?node=${nodeId}")
          .breadcrumb("Links")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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

<div class="card">
	<div class="card-header"><span>
<% if (bridgelinks.isEmpty()) { %>
		No Bridge Forwarding Table Links found on ${nodeLabel} by Enhanced Linkd
<% } else { %>
        ${nodeLabel} Shared Segments found by Enhanced Linkd
<% } %>
 </span></div>
		<!-- Link box -->
	<table class="table table-sm">
	
	<thead>
		<tr>
		<th width="30%">Local Port</th> 
		<th width="30%">Remote Port</th>
		<th width="30%">Info</th>
		<th width="10%">Last Poll</th>
		
		</tr>
	</thead>				
<% for( BridgeLinkNode bridgelink: bridgelinks) { %>
	<tr>
		<td width="30%">
<% if (bridgelink.getBridgeLocalPortUrl() == null) {%>
			<%=bridgelink.getBridgeLocalPort()%>
<% } else { %>
 			<a href="<%=bridgelink.getBridgeLocalPortUrl()%>"><%=bridgelink.getBridgeLocalPort()%></a>
<% } %>
	    </td>
       	<td width="30%">
<% if (bridgelink.getBridgeLinkRemoteNodes().isEmpty()) {%>
            	            	&nbsp;
<% } else { %>
         	<table>
           	<% for (BridgeLinkRemoteNode remote: bridgelink.getBridgeLinkRemoteNodes()) {%>
            	<tr><td>
         		<% if (remote.getBridgeRemoteUrl() != null) { %>
            		<a href="<%=remote.getBridgeRemoteUrl()%>"><%=remote.getBridgeRemote()%></a>
	            <% } else { %> 
		            <%=remote.getBridgeRemote()%>
    			<% } %> 
    				&nbsp;
         		<% if (remote.getBridgeRemotePortUrl() != null) { %>
            		<a href="<%=remote.getBridgeRemotePortUrl()%>"><%=remote.getBridgeRemotePort()%></a>
	            <% } else if (remote.getBridgeRemotePort() != null){ %> 
		            <%=remote.getBridgeRemotePort()%>
            	<% }%>
            	</td><tr>
           	<% }%>
           	</table>
<% }%>
       	</td>
       	<td width="30%">
<% if (bridgelink.getBridgeInfo() == null) {%>
            	            	&nbsp;
<% } else { %>
          <%=bridgelink.getBridgeInfo()%>
<% } %>
		</td>
		<td width="10%"><%=bridgelink.getBridgeLinkLastPollTime() %></td>
       </tr>
<% } %>
   </table>

</div>

<!-- LLDP Links -->

<div class="card">

<div class="card-header"><span>
<%  if (lldpLinks.isEmpty()) { %>
No LLDP Remote Table Links found on ${nodeLabel} by Enhanced Linkd
<% } else { %>
${nodeLabel} (ChassidId <%=lldpelem.getLldpChassisId() %>) LLDP Remote Table Links found by Enhanced Linkd
<% } %>
</span></div>
		<!-- Link box -->
<table class="table table-sm">
		
	<thead>
		<tr>
		<th width="30%">Local Port</th> 
		<th width="30%">Remote Port</th> 
        <th width="30%">Info</th>
		<th width="10%">Last Poll</th>
		</tr>
	</thead>
				
<% for( LldpLinkNode lldplink: lldpLinks) { %>
    <tr>
	    <td width="30%">
	 	<% if (lldplink.getLldpLocalPortUrl() != null) { %>
           	<a href="<%=lldplink.getLldpLocalPortUrl()%>"><%=lldplink.getLldpLocalPort()%></a>
           <% } else { %> 
                   <%=lldplink.getLldpLocalPort()%>
   		<% } %> 
           </td>
           <td width="30%">
           <% if (lldplink.getLldpRemChassisIdUrl() != null) { %>
           	<a href="<%=lldplink.getLldpRemChassisIdUrl()%>"><%=lldplink.getLldpRemChassisId()%></a>
           <% } else { %> 
                   <%=lldplink.getLldpRemChassisId()%>
   			<% } %> 
   			&nbsp;
    	<% if (lldplink.getLldpRemPortUrl() != null) { %>
           	<a href="<%=lldplink.getLldpRemPortUrl()%>"><%=lldplink.getLldpRemPort()%></a>
           <% } else { %> 
                   <%=lldplink.getLldpRemPort()%>
   		<% } %> 
           </td>
	    <td width="30%"><%=lldplink.getLldpRemInfo()%></td>
	    <td width="10%"><%=lldplink.getLldpLastPollTime()%></td>
    </tr>
<% } %>
		    
</table>
</div>

<!-- CDP Links -->

<div class="card">
<div class="card-header"><span>
<% if (cdpLinks.isEmpty()) { %>
No CDP Cache Table Links found on ${nodeLabel} by Enhanced Linkd
<% } else { %>
${nodeLabel} (Device Id <%=cdpelem.getCdpGlobalDeviceId() %>)CDP Cache Table Links found by Enhanced Linkd
<% } %>
</span></div>
<table class="table table-sm">
	<thead>
	<tr>
		<th width="30%">Local Port</th> 
		<th width="30%">Remote Port</th>
        <th width="30%">Info</th>
		<th width="10%">Last Poll</th>
	</tr>
	</thead>
<% for( CdpLinkNode cdplink: cdpLinks) { %>
    <tr>
	    <td width="30%">
 	  <% if (cdplink.getCdpLocalPortUrl() != null) { %>
        <a href="<%=cdplink.getCdpLocalPortUrl()%>"><%=cdplink.getCdpLocalPort()%></a>
      <% } else { %> 
        <%=cdplink.getCdpLocalPort()%>
      <% } %> 
        </td>
        <td width="30%">
        <% if (cdplink.getCdpCacheDeviceUrl() != null) { %>
          <a href="<%=cdplink.getCdpCacheDeviceUrl()%>"><%=cdplink.getCdpCacheDevice()%></a>
        <% } else { %> 
          <%=cdplink.getCdpCacheDevice()%>
   		<% } %> 
   		&nbsp;
    <% if (cdplink.getCdpCacheDevicePortUrl() != null) { %>
          <a href="<%=cdplink.getCdpCacheDevicePortUrl()%>"><%=cdplink.getCdpCacheDevicePort()%></a>
      <% } else { %> 
          <%=cdplink.getCdpCacheDevicePort()%>
	  <% } %> 
        </td>
	    <td width="30%"><%=cdplink.getCdpCachePlatform()%></td>
	    <td width="10%"><%=cdplink.getCdpLastPollTime()%></td>
    </tr>
<% } %>
  </table>
</div>

<!-- OSPF Links -->

<div class="card">
<div class="card-header"><span>
<%   if (ospfLinks.isEmpty()) { %>
No OSPF Links found on ${nodeLabel} by Enhanced Linkd
<% } else { %>
${nodeLabel} (Router id <%=ospfelem.getOspfRouterId() %>)OSPF Nbr Table Links found by Enhanced Linkd
<% } %>
</span></div>
<table class="table table-sm">
		
	<thead>
	<tr>
	<th width="30%">Local Port</th> 
	<th width="30%">Remote Port</th>
	<th width="30%">Info</th> 
	<th width="10%">Last Poll</th>
			</tr>
		</thead>
				
<% for ( OspfLinkNode ospflink: ospfLinks) { %>
    <tr>
	    <td width="30%">
 		<% if (ospflink.getOspfLocalPortUrl() != null ) { %>
          	<a href="<%=ospflink.getOspfLocalPortUrl()%>"><%=ospflink.getOspfLocalPort()%></a>
    	<% } else { %> 
             <%=ospflink.getOspfLocalPort()%>
		<% } %> 
    	</td>
     	<td width="30%">
    	<% if (ospflink.getOspfRemRouterUrl() != null) { %>
     		<a href="<%=ospflink.getOspfRemRouterUrl()%>"><%=ospflink.getOspfRemRouterId()%></a>
    	<% } else { %>
	    	<%=ospflink.getOspfRemRouterId()%>
		<% } %> 
		&nbsp;
 		<% if (ospflink.getOspfRemPortUrl() != null) { %>
     		<a href="<%=ospflink.getOspfRemPortUrl()%>"><%=ospflink.getOspfRemPort()%></a>
    	<% } else { %> 
            <%=ospflink.getOspfRemPort()%>
		<% } %> 
        </td>
	    <td width="30%"><%=ospflink.getOspfLinkInfo()%></td>
	    <td width="10%"><%=ospflink.getOspfLinkLastPollTime()%></td>
   </tr>
<% } %>
		    
</table>

</div>

<!-- ISIS Links -->

<div class="card">
	<div class="card-header">
		<span>
<%   if (isisLinks.isEmpty()) { %>
No IS-IS Adjacency Links found on ${nodeLabel} by Enhanced Linkd
<% } else { %>
${nodeLabel} (id <%=isiselem.getIsisSysID() %>) IS-IS Adj Table Links found by Enhanced Linkd
<% } %>
</span></div>
		<!-- Link box -->
<table class="table table-sm">

<thead>
	<tr>
	<th width="30%">Local Port</th> 
	<th width="30%">Remote Port</th>
	<th width="30%">Info</th> 
	<th width="10%">Last Poll</th>
	</tr>
</thead>
		
<% for( IsisLinkNode isislink : isisLinks) { %>
   <tr>
    <td width="30%">circuit:<%=isislink.getIsisCircIfIndex()%> status:<%=isislink.getIsisCircAdminState()%></td>
    <td width="30%">
          <% if (isislink.getIsisISAdjNeighSysUrl() != null) { %>
          	<a href="<%=isislink.getIsisISAdjNeighSysUrl()%>"><%=isislink.getIsisISAdjNeighSysID() %></a>
          <% } else { %> 
                 <%=isislink.getIsisISAdjNeighSysID()%>
  			<% } %> 
    	 type:<%=isislink.getIsisISAdjNeighSysType()%>
 	<% if (isislink.getIsisISAdjUrl() != null) { %>
          	<a href="<%=isislink.getIsisISAdjUrl()%>"><%=isislink.getIsisISAdjNeighPort()%></a>
          <% } else { %> 
		<%=isislink.getIsisISAdjNeighPort()%>
  		<% } %> 
     </td>
    <td width="30%">adjstate:<%=isislink.getIsisISAdjState()%> 
        adjSNPAaddr:<%=isislink.getIsisISAdjNeighSNPAAddress()%>
        adjNbrExtCircId:<%=isislink.getIsisISAdjNbrExtendedCircID()%>
    </td>
    <td width="10%"><%=isislink.getIsisLinkLastPollTime()%></td>
   </tr>
<% } %>
		    
</table>

</div>

</div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
