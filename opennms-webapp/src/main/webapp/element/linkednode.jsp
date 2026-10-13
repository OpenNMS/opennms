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
  <h4>Node: <c:out value="${nodeLabel}"/></h4>

<div class="row">
<div class="col-md-12">

<!--  BRIDGE Links -->

<div class="card">
	<div class="card-header"><span>
<% if (bridgelinks.isEmpty()) { %>
		No Bridge Forwarding Table Links found on <c:out value="${nodeLabel}"/> by Enhanced Linkd
<% } else { %>
        <c:out value="${nodeLabel}"/> Shared Segments found by Enhanced Linkd
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
			<%=WebSecurityUtils.sanitizeString(bridgelink.getBridgeLocalPort())%>
<% } else { %>
 			<a href="<%=bridgelink.getBridgeLocalPortUrl()%>"><%=WebSecurityUtils.sanitizeString(bridgelink.getBridgeLocalPort())%></a>
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
            		<a href="<%=remote.getBridgeRemoteUrl()%>"><%=WebSecurityUtils.sanitizeString(remote.getBridgeRemote())%></a>
	            <% } else { %> 
		            <%=WebSecurityUtils.sanitizeString(remote.getBridgeRemote())%>
    			<% } %> 
    				&nbsp;
         		<% if (remote.getBridgeRemotePortUrl() != null) { %>
            		<a href="<%=remote.getBridgeRemotePortUrl()%>"><%=WebSecurityUtils.sanitizeString(remote.getBridgeRemotePort())%></a>
	            <% } else if (remote.getBridgeRemotePort() != null){ %> 
		            <%=WebSecurityUtils.sanitizeString(remote.getBridgeRemotePort())%>
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
          <%=WebSecurityUtils.sanitizeString(bridgelink.getBridgeInfo())%>
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
No LLDP Remote Table Links found on <c:out value="${nodeLabel}"/> by Enhanced Linkd
<% } else { %>
<c:out value="${nodeLabel}"/> (ChassidId <%=WebSecurityUtils.sanitizeString(lldpelem.getLldpChassisId()) %>) LLDP Remote Table Links found by Enhanced Linkd
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
           	<a href="<%=lldplink.getLldpLocalPortUrl()%>"><%=WebSecurityUtils.sanitizeString(lldplink.getLldpLocalPort())%></a>
           <% } else { %> 
                   <%=WebSecurityUtils.sanitizeString(lldplink.getLldpLocalPort())%>
   		<% } %> 
           </td>
           <td width="30%">
           <% if (lldplink.getLldpRemChassisIdUrl() != null) { %>
           	<a href="<%=lldplink.getLldpRemChassisIdUrl()%>"><%=WebSecurityUtils.sanitizeString(lldplink.getLldpRemChassisId())%></a>
           <% } else { %> 
                   <%=WebSecurityUtils.sanitizeString(lldplink.getLldpRemChassisId())%>
   			<% } %> 
   			&nbsp;
    	<% if (lldplink.getLldpRemPortUrl() != null) { %>
           	<a href="<%=lldplink.getLldpRemPortUrl()%>"><%=WebSecurityUtils.sanitizeString(lldplink.getLldpRemPort())%></a>
           <% } else { %> 
                   <%=WebSecurityUtils.sanitizeString(lldplink.getLldpRemPort())%>
   		<% } %> 
           </td>
	    <td width="30%"><%=WebSecurityUtils.sanitizeString(lldplink.getLldpRemInfo())%></td>
	    <td width="10%"><%=lldplink.getLldpLastPollTime()%></td>
    </tr>
<% } %>
		    
</table>
</div>

<!-- CDP Links -->

<div class="card">
<div class="card-header"><span>
<% if (cdpLinks.isEmpty()) { %>
No CDP Cache Table Links found on <c:out value="${nodeLabel}"/> by Enhanced Linkd
<% } else { %>
<c:out value="${nodeLabel}"/> (Device Id <%=WebSecurityUtils.sanitizeString(cdpelem.getCdpGlobalDeviceId()) %>)CDP Cache Table Links found by Enhanced Linkd
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
        <a href="<%=cdplink.getCdpLocalPortUrl()%>"><%=WebSecurityUtils.sanitizeString(cdplink.getCdpLocalPort())%></a>
      <% } else { %> 
        <%=WebSecurityUtils.sanitizeString(cdplink.getCdpLocalPort())%>
      <% } %> 
        </td>
        <td width="30%">
        <% if (cdplink.getCdpCacheDeviceUrl() != null) { %>
          <a href="<%=cdplink.getCdpCacheDeviceUrl()%>"><%=WebSecurityUtils.sanitizeString(cdplink.getCdpCacheDevice())%></a>
        <% } else { %> 
          <%=WebSecurityUtils.sanitizeString(cdplink.getCdpCacheDevice())%>
   		<% } %> 
   		&nbsp;
    <% if (cdplink.getCdpCacheDevicePortUrl() != null) { %>
          <a href="<%=cdplink.getCdpCacheDevicePortUrl()%>"><%=WebSecurityUtils.sanitizeString(cdplink.getCdpCacheDevicePort())%></a>
      <% } else { %> 
          <%=WebSecurityUtils.sanitizeString(cdplink.getCdpCacheDevicePort())%>
	  <% } %> 
        </td>
	    <td width="30%"><%=WebSecurityUtils.sanitizeString(cdplink.getCdpCachePlatform())%></td>
	    <td width="10%"><%=cdplink.getCdpLastPollTime()%></td>
    </tr>
<% } %>
  </table>
</div>

<!-- OSPF Links -->

<div class="card">
<div class="card-header"><span>
<%   if (ospfLinks.isEmpty()) { %>
No OSPF Links found on <c:out value="${nodeLabel}"/> by Enhanced Linkd
<% } else { %>
<c:out value="${nodeLabel}"/> (Router id <%=WebSecurityUtils.sanitizeString(ospfelem.getOspfRouterId()) %>)OSPF Nbr Table Links found by Enhanced Linkd
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
          	<a href="<%=ospflink.getOspfLocalPortUrl()%>"><%=WebSecurityUtils.sanitizeString(ospflink.getOspfLocalPort())%></a>
    	<% } else { %> 
             <%=WebSecurityUtils.sanitizeString(ospflink.getOspfLocalPort())%>
		<% } %> 
    	</td>
     	<td width="30%">
    	<% if (ospflink.getOspfRemRouterUrl() != null) { %>
     		<a href="<%=ospflink.getOspfRemRouterUrl()%>"><%=WebSecurityUtils.sanitizeString(ospflink.getOspfRemRouterId())%></a>
    	<% } else { %>
	    	<%=WebSecurityUtils.sanitizeString(ospflink.getOspfRemRouterId())%>
		<% } %> 
		&nbsp;
 		<% if (ospflink.getOspfRemPortUrl() != null) { %>
     		<a href="<%=ospflink.getOspfRemPortUrl()%>"><%=WebSecurityUtils.sanitizeString(ospflink.getOspfRemPort())%></a>
    	<% } else { %> 
            <%=WebSecurityUtils.sanitizeString(ospflink.getOspfRemPort())%>
		<% } %> 
        </td>
	    <td width="30%"><%=WebSecurityUtils.sanitizeString(ospflink.getOspfLinkInfo())%></td>
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
No IS-IS Adjacency Links found on <c:out value="${nodeLabel}"/> by Enhanced Linkd
<% } else { %>
<c:out value="${nodeLabel}"/> (id <%=WebSecurityUtils.sanitizeString(isiselem.getIsisSysID()) %>) IS-IS Adj Table Links found by Enhanced Linkd
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
    <td width="30%">circuit:<%=isislink.getIsisCircIfIndex()%> status:<%=WebSecurityUtils.sanitizeString(isislink.getIsisCircAdminState())%></td>
    <td width="30%">
          <% if (isislink.getIsisISAdjNeighSysUrl() != null) { %>
          	<a href="<%=isislink.getIsisISAdjNeighSysUrl()%>"><%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjNeighSysID())%></a>
          <% } else { %> 
                 <%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjNeighSysID())%>
  			<% } %> 
    	 type:<%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjNeighSysType())%>
 	<% if (isislink.getIsisISAdjUrl() != null) { %>
          	<a href="<%=isislink.getIsisISAdjUrl()%>"><%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjNeighPort())%></a>
          <% } else { %> 
		<%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjNeighPort())%>
  		<% } %> 
     </td>
    <td width="30%">adjstate:<%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjState())%> 
        adjSNPAaddr:<%=WebSecurityUtils.sanitizeString(isislink.getIsisISAdjNeighSNPAAddress())%>
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
