<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 27: Organize imports, cleanup breadcrumbs. - dj@opennms.org
// 2007 Feb 01: Don't display the "Site Status" link if the building
//              column in assets is a zero-length string. - dj@opennms.org
// 2006 Oct 30: Convert to use Java 5 generics and clean up warnings.
//              - dj@opennms.org
// 2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor
// 2004 Jan 15: Added node admin function.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 01: Added response time link (Bug #684) and HTTP link (Bug #469).
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.element.*,
		java.util.*,
		java.net.*,
      	org.opennms.netmgt.dao.CategoryDao,
	    org.opennms.netmgt.dao.NodeDao,
        org.opennms.netmgt.model.OnmsResource,
        org.opennms.core.utils.IPSorter,
        org.opennms.web.Util,
        org.opennms.web.acegisecurity.Authentication,
        org.opennms.web.event.*,
        org.opennms.web.svclayer.ResourceService,
        org.opennms.web.asset.Asset,
        org.opennms.web.asset.AssetModel,
        org.springframework.transaction.support.TransactionTemplate,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<%!

    private WebApplicationContext m_webAppContext;

    protected int telnetServiceId;
    protected int httpServiceId;
    protected int dellServiceId;
    protected int snmpServiceId;
    private ResourceService m_resourceService;
	protected AssetModel model = new AssetModel();

	public static HashMap<Character, String> m_statusMap;

	static {
        m_statusMap = new HashMap<Character, String>();
        m_statusMap.put(new Character('A'), "Active");
        m_statusMap.put(new Character(' '), "Unknown");
        m_statusMap.put(new Character('D'), "Deleted");
	}
    
    public void init() throws ServletException {
        
        try {
            this.telnetServiceId = NetworkElementFactory.getServiceIdFromName("Telnet");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the Telnet service ID", e );
        }        

        try {
            this.httpServiceId = NetworkElementFactory.getServiceIdFromName("HTTP");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the HTTP service ID", e );
        }

        try {
            this.dellServiceId = NetworkElementFactory.getServiceIdFromName("Dell-OpenManage");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the Dell-OpenManage service ID", e );
        }

        try {
            this.snmpServiceId = NetworkElementFactory.getServiceIdFromName("SNMP");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the Dell-OpenManage service ID", e );
        }

	    m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		m_resourceService = (ResourceService) m_webAppContext.getBean("resourceService", ResourceService.class);
    }
    
    public static String getStatusString(char c) {
        return m_statusMap.get(new Character(c));
    }%>

<%
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = Integer.parseInt( nodeIdString );

    //get the database node info
    Node node_db = NetworkElementFactory.getNode( nodeId );
    if( node_db == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }

    //get the child interfaces
    Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode( nodeId );
    if( intfs == null ) { 
        intfs = new Interface[0]; 
    }

    //find the telnet interfaces, if any
    String telnetIp = null;
    Service[] telnetServices = NetworkElementFactory.getServicesOnNode(nodeId, this.telnetServiceId);
    
    if( telnetServices != null && telnetServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < telnetServices.length; i++ ) {
            ips.add(InetAddress.getByName(telnetServices[i].getIpAddress()));
        }
        
        InetAddress lowest = IPSorter.getLowestInetAddress(ips);
        
        if( lowest != null ) {
            telnetIp = lowest.getHostAddress();
        }
    }    

    //find the HTTP interfaces, if any
    String httpIp = null;
    Service[] httpServices = NetworkElementFactory.getServicesOnNode(nodeId, this.httpServiceId);

    if( httpServices != null && httpServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < httpServices.length; i++ ) {
            ips.add(InetAddress.getByName(httpServices[i].getIpAddress()));
        }

        InetAddress lowest = IPSorter.getLowestInetAddress(ips);

        if( lowest != null ) {
            httpIp = lowest.getHostAddress();
        }
    }

    //find the Dell-OpenManage interfaces, if any
    String dellIp = null;
    Service[] dellServices = NetworkElementFactory.getServicesOnNode(nodeId, this.dellServiceId);

    if( dellServices != null && dellServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < dellServices.length; i++ ) {
            ips.add(InetAddress.getByName(dellServices[i].getIpAddress()));
        }

        InetAddress lowest = IPSorter.getLowestInetAddress(ips);

        if( lowest != null ) {
            dellIp = lowest.getHostAddress();
        }
    }

    //find if SNMP is on this node 
    boolean isSnmp = false;
    Service[] snmpServices = NetworkElementFactory.getServicesOnNode(nodeId, this.snmpServiceId);

    if( snmpServices != null && snmpServices.length > 0 ) 
	isSnmp = true;

    boolean isParent = NetworkElementFactory.isParentNode(nodeId);
    boolean isBridge = NetworkElementFactory.isBridgeNode(nodeId);
    boolean isRouteIP = NetworkElementFactory.isRouteInfoNode(nodeId);

    // Get vlans on node
    Vlan[] vlans = NetworkElementFactory.getVlansOnNode(nodeId);
    
    //Get Asset Info for this node
    Asset asset = this.model.getAsset( nodeId );
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node" />
  <jsp:param name="headTitle" value="<%= node_db.getLabel() %>" />
  <jsp:param name="headTitle" value="Node" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="Node" />
</jsp:include>

      <h2>Node: <%=node_db.getLabel()%></h2>

      <div id="linkbar">
      <ul>
        <li>
          <a href="event/list?filter=node%3D<%=nodeId%>">View Events</a>
	    </li>
        <li>
         <a href="asset/modify.jsp?node=<%=nodeId%>">Asset Info</a>
        </li>
		<!-- li>
         <a href="conf/inventorylist.jsp?node=<%=nodeId%>">Inventory</a>
        </li -->

        <% if (asset != null && asset.getBuilding() != null && asset.getBuilding().length() > 0) { %>
          <li>
            <a href="siteStatusView.htm?statusSite=<%=Util.encode(asset.getBuilding())%>">Site Status</a>
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
          <a href="${resourceGraphsUrl}">Resource Graphs</a>
	  </li>
        <% } %>
        
        <% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %> 
	  <li>
            <a href="element/rescan.jsp?node=<%=nodeId%>">Rescan</a>      
	  </li>
        <% } %>
        <% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %> 
	  <li>
          <a href="admin/nodemanagement/index.jsp?node=<%=nodeId%>">Admin</a>
	  </li>
        <% } %>

           <% if ( isSnmp && request.isUserInRole( Authentication.ADMIN_ROLE ))  { %>
              <% for( int i=0; i < intfs.length; i++ ) { %>
                <% if( "P".equals( intfs[i].getIsSnmpPrimary() )) { %>
		      <li>
                      <a href="admin/updateSnmp.jsp?node=<%=nodeId%>&ipaddr=<%=intfs[i].getIpAddress()%>">Update SNMP</a>
		      </li>
                <% } %>
              <% } %>
           <% } %>


      </ul>
      </div>



	<div class="TwoColLeft">
            <!-- general info box -->
						<h3>General (Status: <%=(getStatusString(node_db.getNodeType())!=null ? getStatusString(node_db.getNodeType()) : "Unknown")%>)</h3>
			<div class="boxWrapper">
			     <ul class="plain">
		            <% if( isRouteIP ) { %>
		            <li>
		            	<a href="element/routeipnode.jsp?node=<%=nodeId%>"> View Node Ip Route Info</a>
		            </li>
		            <% }%>
		         
		            <% if( isBridge ) { %>
		            <li>
						<a href="element/bridgenode.jsp?node=<%=nodeId%>">View Node Bridge/STP Info</a>
					</li>
		            <% }%>	
		            <li>
						<a href="element/linkednode.jsp?node=<%=nodeId%>">View Node Link Detailed Info</a>
					</li>
		         </ul>	     

		</div>
 
            <!-- Availability box -->
            <jsp:include page="/includes/nodeAvailability-box.jsp" flush="false" />

            <!-- Asset box, if info available --> 
            <% if( asset != null && ((asset.getDescription() != null && asset.getDescription().length() > 0)
                    || (asset.getComments() != null && asset.getComments().length() > 0))) { %>
			  <h3>Asset Information</h3>
			  <table>
				<tr>
				  <td width="10%">Description:</td>
				  <td><%=(asset.getDescription() == null) ? "&nbsp;" : asset.getDescription()%></td>
				</tr>
				
				<tr>
				  <td width="10%">Comments:</td>
				  <td><%=(asset.getComments() == null) ? "&nbsp;" : asset.getComments()%></td>
				</tr>
			  </table>
            <% } %>

            <!-- SNMP box, if info available --> 
            <% if( node_db.getNodeSysId() != null ) { %>
		      <h3>SNMP Attributes</h3>
	          <table class="standard">
                <tr>
		          <!-- XXX should get rid of width... replace with a class? -->
                  <td class="standard" width="10%">Name:</td>
                  <td class="standard"><%=(node_db.getNodeSysName() == null) ? "&nbsp;" : node_db.getNodeSysName()%></td>
                </tr>
                <tr>
                  <td class="standard" width="10%">Object&nbsp;ID:</td>
                  <td class="standard"><%=(node_db.getNodeSysId() == null) ? "&nbsp;" : node_db.getNodeSysId()%></td>
                </tr>
                <tr>
                  <td class="standard" width="10%">Location:</td>
                  <td class="standard"><%=(node_db.getNodeSysLocn() == null) ? "&nbsp;" : node_db.getNodeSysLocn()%></td>
                </tr>
                <tr>
                  <td class="standard" width="10%">Contact:</td>
                  <td class="standard"><%=(node_db.getNodeSysContact() == null) ? "&nbsp;" : node_db.getNodeSysContact()%></td>
                </tr>
                <tr>
                  <td class="standard" valign="top" width="10%">Description:</td>
                  <td class="standard" valign="top"><%=(node_db.getNodeSysDescr() == null) ? "&nbsp;" : node_db.getNodeSysDescr()%> </td>
                </tr>
              </table>
            <% } %>
            
            <!-- Interface box -->
	        <h3>Interfaces</h3>
			<div class="boxWrapper">
			  <ul class="plain">
              <% for( int i=0; i < intfs.length; i++ ) { %>
                <% if( "0.0.0.0".equals( intfs[i].getIpAddress() )) { %>
                  <li><a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=intfs[i].getIpAddress()%>&ifindex=<%=intfs[i].getIfIndex()%>">Non-IP</a>
                <% } else { %>  
                  <li><a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=intfs[i].getIpAddress()%>"><%=intfs[i].getIpAddress()%></a>
                      <%=intfs[i].getIpAddress().equals(intfs[i].getHostname()) ? "" : "(" + intfs[i].getHostname() + ")"%>
                <% } %>
		       	<% if( intfs[i].getIfIndex() != 0 ) { %>
				<%=" (ifIndex: "+intfs[i].getIfIndex()+"-"+intfs[i].getSnmpIfDescription()+")"%>
		        <% } %>
		        </li>
              <% } %>
			  </ul>
			</div>

           <!-- Vlan box if available -->
            <% if ( vlans != null && vlans.length > 0) { %>
                          <h3>Vlan Information</h3>
                          <table>
                                <thead>
                                <tr>
                                  <th>Vlan ID</th>
                                  <th>Vlan name</th>
                                  <th>Vlan Type</th>
                                  <th>Vlan Status</th>
                                  <th>Status - Last Poll Time</th>
                                </tr>
                                </thead>
                          <% for ( int i=0; i< vlans.length ;i++ ) {
                                Vlan vlan = vlans[i];
                          %>
                                <tr>
                                  <td ><%=vlan.getVlanId()%></td>
                                  <td><%=vlan.getVlanName()%></td>
                                  <td><%=vlan.getVlanTypeString()%></td>
                                  <td><%=vlan.getVlanStatusString()%></td>
                                  <td><%=vlan.getStatusString()%> - <%=vlan.get_lastPollTime()%></td>
                                </tr>

                    <% } %>
                          </table>

            <% } %>

      <!-- Category box -->
	  <jsp:include page="/includes/nodeCategory-box.htm" flush="false" />
	</div>


	<div class="TwoColRight">

            <!-- notification box -->
            <jsp:include page="/includes/notification-box.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
            </jsp:include>

            <!-- events list  box -->
            <% String eventHeader = "<a href='event/list?filter=" + Util.encode("node=" + nodeId) + "'>Recent Events</a>"; %>
            <% String moreEventsUrl = "event/list?filter=" + Util.encode("node=" + nodeId); %>

            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl%>" />
            </jsp:include>

            <!-- Recent outages box -->
            <jsp:include page="/includes/nodeOutages-box.jsp" flush="false" />
       </div>
<hr />
            <%-- jsp:include page="/includes/nodeInventory-box.jsp" flush="false">
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="nodelabel" value="<%=node_db.getLabel()%>" />
            </jsp:include>
			<hr / --%>

<jsp:include page="/includes/footer.jsp" flush="false" />
