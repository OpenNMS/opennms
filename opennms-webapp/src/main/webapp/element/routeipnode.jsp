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

<%@page
	language="java"
	contentType="text/html"
	session="true"
	import="
		org.opennms.web.element.*,
		java.util.*,
		java.net.*,
		org.opennms.netmgt.utils.IPSorter,
		org.opennms.web.performance.*,
		org.opennms.web.response.*,
		org.springframework.web.context.WebApplicationContext,
      	org.springframework.web.context.support.WebApplicationContextUtils
	"
%>

<%!
    protected int telnetServiceId;
    protected int httpServiceId;
    protected PerformanceModel perfModel;
    protected ResponseTimeModel rtModel;

	public static HashMap<Character, String> statusMap;

    public void init() throws ServletException {
        statusMap = new HashMap<Character, String>();
        statusMap.put( new Character('A'), "Active" );
        statusMap.put( new Character(' '), "Unknown" );
        statusMap.put( new Character('D'), "Deleted" );

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

	    WebApplicationContext m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		this.perfModel = (PerformanceModel) m_webAppContext.getBean("performanceModel", PerformanceModel.class);
		this.rtModel = (ResponseTimeModel) m_webAppContext.getBean("responseTimeModel", ResponseTimeModel.class);
    }
    
    public String getStatusString( char c ) {
        return statusMap.get(new Character(c));
    }
%>

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

    //find the telnet interfaces, if any
    String telnetIp = null;
    Service[] telnetServices = NetworkElementFactory.getServicesOnNode(nodeId, this.telnetServiceId);
    
    if( telnetServices != null && telnetServices.length > 0 ) {
        ArrayList ips = new ArrayList();
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
        ArrayList ips = new ArrayList();
        for( int i=0; i < httpServices.length; i++ ) {
            ips.add(InetAddress.getByName(httpServices[i].getIpAddress()));
        }

        InetAddress lowest = IPSorter.getLowestInetAddress(ips);

        if( lowest != null ) {
            httpIp = lowest.getHostAddress();
        }
    }

    boolean isBridgeIP = NetworkElementFactory.isBridgeNode(nodeId);

%>

<html>
<head>
  <title><%=node_db.getLabel()%> | Node | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='element/index.jsp'>Search</a>"; %>
<% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
<% String breadcrumb3 = "Bridge Info"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>
<!-- Body -->
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
        <% if(this.rtModel.isQueryableNode(nodeId)) { %>
		<li>
			<a href="response/addIntfFromNode?endUrl=response%2FaddReportsToUrl&node=<%=nodeId%>&relativetime=lastday">Response Time</a>
		</li>
        <% } %>        
        <% if(this.perfModel.isQueryableNode(nodeId)) { %>
        <li>
          	<a href="performance/addIntfFromNode?endUrl=performance%2FaddReportsToUrl&node=<%=nodeId%>&relativetime=lastday">SNMP Performance</a>
        </li>  	
        <% } %>        
        <li>
	        <a href="element/rescan.jsp?node=<%=nodeId%>">Rescan</a>    
        </li>
      </ul>
      </div>

	<div class="TwoColLeft">
            <!-- general info box -->
						<h3>General (Status: <%=(this.getStatusString(node_db.getNodeType())!=null ? this.getStatusString(node_db.getNodeType()) : "Unknown")%>)</h3>

			<div class="boxWrapper">
			     <ul class="plain">
		         
		            <% if( isBridgeIP ) { %>
		            <li>
						<a href="element/bridgenode.jsp?node=<%=nodeId%>">View Node Bridge/STP Info</a>
					</li>
		            <% }%>				     
		            <li>
		            	<a href="element/linkednode.jsp?node=<%=nodeId%>">View Node Link Detailed Info</a>
		            </li>
		         </ul>	     
			</div>
	</div>
	<hr />
<div>
		<h3>Node Ip Routes</h3>
			
         <!-- general Route info box -->
            <jsp:include page="/includes/nodeRouteInfo-box.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
			</jsp:include>
			
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
