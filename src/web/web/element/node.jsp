<!--

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

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*,org.opennms.web.authenticate.Authentication,java.net.*,org.opennms.netmgt.utils.IPSorter,org.opennms.web.performance.*,org.opennms.web.response.*" %>

<%!
    protected int telnetServiceId;
    protected int httpServiceId;
    protected int rdpServiceId;
    protected int vncServiceId;
    protected PerformanceModel perfModel;
    protected ResponseTimeModel rtModel;

    public static HashMap statusMap;
    public String getStatusString( char c ) {
        return( (String)this.statusMap.get( new Character(c) ));
    }
    
    public void init() throws ServletException {
        this.statusMap = new HashMap();
        this.statusMap.put( new Character('A'), "Active" );
        this.statusMap.put( new Character(' '), "Unknown" );
        this.statusMap.put( new Character('D'), "Deleted" );
        
        try {
            this.telnetServiceId = NetworkElementFactory.getServiceIdFromName("Telnet");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the Telnet service ID", e );
        }        
        
        try {
            this.perfModel = new PerformanceModel( org.opennms.core.resource.Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
        }        

        try {
            this.httpServiceId = NetworkElementFactory.getServiceIdFromName("HTTP");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the HTTP service ID", e );
        }

        try {
            this.rdpServiceId = NetworkElementFactory.getServiceIdFromName("RDP");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the RDP service ID", e );
        }

        try {
            this.vncServiceId = NetworkElementFactory.getServiceIdFromName("VNC");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the VNC service ID", e );
        }

        try {
            this.rtModel = new ResponseTimeModel( org.opennms.core.resource.Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the ResponseTimeModel", e );
        }

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

    //get the child interfaces
    Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode( nodeId );
    if( intfs == null ) { 
        intfs = new Interface[0]; 
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

    //find the RDP interfaces, if any
    String rdpIp = null;
    Service[] rdpServices = NetworkElementFactory.getServicesOnNode(nodeId, this.rdpServiceId);

    if( rdpServices != null && rdpServices.length > 0 ) {
        ArrayList ips = new ArrayList();
        for( int i=0; i < rdpServices.length; i++ ) {
            ips.add(InetAddress.getByName(rdpServices[i].getIpAddress()));
        }

        InetAddress lowest = IPSorter.getLowestInetAddress(ips);

        if( lowest != null ) {
            rdpIp = lowest.getHostAddress();
        }
    }

    //find the VNC interfaces, if any
    String vncIp = null;
    Service[] vncServices = NetworkElementFactory.getServicesOnNode(nodeId, this.vncServiceId);

    if( vncServices != null && vncServices.length > 0 ) {
        ArrayList ips = new ArrayList();
        for( int i=0; i < vncServices.length; i++ ) {
            ips.add(InetAddress.getByName(vncServices[i].getIpAddress()));
        }

        InetAddress lowest = IPSorter.getLowestInetAddress(ips);

        if( lowest != null ) {
            vncIp = lowest.getHostAddress();
        }
    }

    // find links
    Map linkMap = new HashMap();
    DataLinkInterface[] dl_if = null;
    Interface[] intf_dbs = null;
    boolean isParent = ExtendedNetworkElementFactory.isParentNode(nodeId);
	    
    if ( isParent ) {
       dl_if = ExtendedNetworkElementFactory.getDataLinksFromNodeParent(nodeId);
    } else {
       dl_if = ExtendedNetworkElementFactory.getDataLinks(nodeId);
    }

    for (int i=0; i<dl_if.length;i++){
	   int nodelinkedId = 0;
  	   int nodelinkedIf = 0;
  	   Integer ifindexmap = null;
  	   String iplinkaddress = null;
       Vector ifs = new Vector();

       if (isParent) {
           nodelinkedId = dl_if[i].get_nodeId();
           nodelinkedIf = dl_if[i].get_ifindex();
       	   iplinkaddress = dl_if[i].get_ipaddr();
       	   ifindexmap = new Integer(dl_if[i].get_parentifindex());
       } else {
           nodelinkedId = dl_if[i].get_nodeparentid();
           nodelinkedIf = dl_if[i].get_parentifindex();
       	   iplinkaddress = dl_if[i].get_parentipaddr();
       	   ifindexmap = new Integer(dl_if[i].get_ifindex());
       }
       Interface iface = null;
       if (nodelinkedIf == 0) {
       		iface = NetworkElementFactory.getInterface(nodelinkedId,iplinkaddress);
       } else {
      		iface = NetworkElementFactory.getInterface(nodelinkedId,iplinkaddress,nodelinkedIf);
       }
       if (linkMap.containsKey(ifindexmap)){
	        ifs = (Vector)linkMap.get(ifindexmap);
	   } 
	   ifs.addElement(iface);
	   linkMap.put(ifindexmap,ifs);
    }

    boolean isBridge = ExtendedNetworkElementFactory.isBridgeNode(nodeId);

    boolean isRouteIP = ExtendedNetworkElementFactory.isRouteInfoNode(nodeId);

%>

<html>
<head>
  <title><%=node_db.getLabel()%> | Node | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
<% if( rdpIp != null ) { %>
  <script type="text/javascript">
  // only for iexplorer 
  // 1 - mstsc application must be installed on windows client 
  // 2 - mstsc path must be in PATH environment variable
  // 3 - activex control execution not signed must be enable or enable it for "intranet zone" only on internet explorer settings
  var clientRDPNAme = "mstsc.exe";
  var paramPrefix = "/v:";
  var paramSuffix = "";
  
  function launchRdpCLient()
  {
	if (typeof ActiveXObject != "undefined")
	{
		var oShell = new ActiveXObject("WScript.Shell");
		oShell.run (clientRDPNAme + " " + paramPrefix + "<%=rdpIp%>" + paramSuffix,1);  
	}
	else
		alert("ActiveXObject is not supported\nThis features is supported only by Internet Explorer on windows clients.");
  }
  </script>
<% } %>  
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='element/index.jsp'>Search</a>"; %>
<% String breadcrumb2 = "Node"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td width="100%" valign="top" >
      <h2>Node: <%=node_db.getLabel()%></h2>

      <p>
        <a href="event/list?filter=node%3D<%=nodeId%>">View Events</a>
        &nbsp;&nbsp;&nbsp;<a href="conf/inventorylist.jsp?node=<%=nodeId%>">Inventory</a>
        &nbsp;&nbsp;&nbsp;<a href="asset/detail.jsp?node=<%=nodeId%>">Asset Info</a>
         
        <% if(this.rtModel.isQueryableNode(nodeId)) { %>
          &nbsp;&nbsp;&nbsp;<a href="response/addIntfFromNode?endUrl=response%2FaddReportsToUrl&node=<%=nodeId%>&relativetime=lastday">Response Time</a>
        <% } %>
        
        <% if(this.perfModel.isQueryableNode(nodeId)) { %>
          &nbsp;&nbsp;&nbsp;<a href="performance/addIntfFromNode?endUrl=performance%2FaddReportsToUrl&node=<%=nodeId%>&relativetime=lastday">SNMP Performance</a>
        <% } %>
        
        &nbsp;&nbsp;&nbsp;<a href="element/rescan.jsp?node=<%=nodeId%>">Rescan</a>      
        <% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %> 
          &nbsp;&nbsp;&nbsp;<a href="admin/nodemanagement/index.jsp?node=<%=nodeId%>">Admin</a>
        <% } %>
        
        <br>
        
        <% if( telnetIp != null ) { %>
          <a href="telnet://<%=telnetIp%>">Telnet</a>&nbsp;&nbsp;&nbsp;
        <% } %>

        <% if( httpIp != null ) { %>
          <a href="http://<%=httpIp%>">HTTP</a>&nbsp;&nbsp;&nbsp;
        <% } %>

        <% if( rdpIp != null ) { %>
          <a href="javascript:launchRdpCLient();">RDP</a>&nbsp;&nbsp;&nbsp;
        <% } %>

        <% if( vncIp != null ) { %>
          <a href="http://<%=vncIp%>:5800">VNC</a>&nbsp;&nbsp;&nbsp;
        <% } %>
        
      </p>

      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td valign="top" width="48%">

            <!-- general info box -->
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
              <tr bgcolor="#999999">
                <td colspan="2" ><b>General</b></td> 
              </tr>
              <tr> 
                <td>Status</td>
                <td><%=(this.getStatusString(node_db.getNodeType())!=null ? this.getStatusString(node_db.getNodeType()) : "Unknown")%></td>
              </tr>
         <% if( isRouteIP ) { %>
              <tr>
              <td colspan="2" ><b><a href="element/routeipnode.jsp?node=<%=nodeId%>"> View Node Ip Route Info</a></b></td>
		</tr>
         <% }%>
         <% if( isBridge ) { %>
              <tr>
              <td colspan="2" ><b><a href="element/bridgenode.jsp?node=<%=nodeId%>">View Node Bridge/STP Info</a></b></td>
		</tr>
         <% }%>

            </table>
            <br>
            
            <!-- Interface box -->
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
              <tr bgcolor="#999999">
                <td><b>Interfaces</b></td> 
                <td><b>Linked Node/Interface</b></td> 
              </tr>
              <% for( int i=0; i < intfs.length; i++ ) { %>
					<% Vector ifl =(Vector)linkMap.get(new Integer(intfs[i].getIfIndex()));%>
				<tr>
                <% if( "0.0.0.0".equals( intfs[i].getIpAddress() )) { %>
                    <td>
                      <a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=intfs[i].getIpAddress()%>&ifindex=<%=intfs[i].getIfIndex()%>">Non-IP</a>
                      <%=" (ifIndex: "+intfs[i].getIfIndex()+"-"+intfs[i].getSnmpIfDescription()+")"%>
                    </td>
                <% } else { %>  
                    <td>
                      <a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=intfs[i].getIpAddress()%>"><%=intfs[i].getIpAddress()%></a>
                      <%=intfs[i].getIpAddress().equals(intfs[i].getHostname()) ? "" : "(" + intfs[i].getHostname() + ")"%>
                    </td>
                <% } %>
				<% if (ifl == null || ifl.size() == 0) {%>
			    <td>&nbsp;</td>
				<% } else { %>
                    <td>
			            <table width="100%" border="0" cellspacing="0" cellpadding="2" bordercolor="white" BGCOLOR="#cccccc">
                        <tr>
					<% for (int j=0; j<ifl.size();j++) { 
						Interface lkif =(Interface)ifl.elementAt(j); 
					%>
                        <tr>
                         <td width="48%">
                    	  <a href="element/node.jsp?node=<%=lkif.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(lkif.getNodeId())%></a>
                    	 </td>
		    			 <td>&nbsp;</td>
                         <td width="48%">
            		    <% if( "0.0.0.0".equals( lkif.getIpAddress() )) { %>
                    	  <a href="element/interface.jsp?node=<%=lkif.getNodeId()%>&intf=<%=lkif.getIpAddress()%>&ifindex=<%=lkif.getIfIndex()%>">Non-IP</a>
                      	  <%=" (ifIndex: "+lkif.getIfIndex()+"-"+lkif.getSnmpIfDescription()+")"%>
                		<% } else { %>  
                      	  <a href="element/interface.jsp?node=<%=lkif.getNodeId()%>&intf=<%=lkif.getIpAddress()%>"><%=lkif.getIpAddress()%></a>
                	    <% } %>
                    <%}%>
                         </td>
                        </tr>
                    	</table>
                    </td>
                <%}%>
               </tr>
            <% } %>
            </table>

            <br>

            <!-- Availability box -->
            <jsp:include page="/includes/nodeAvailability-box.jsp" flush="false" />
            <br>
            
            <!-- node desktop information box -->

            <!-- SNMP box, if info available --> 
            <% if( node_db.getNodeSysId() != null ) { %>
              <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
                <tr bgcolor="#999999">
                  <td colspan="2"><b>SNMP Attributes</b></td>
                </tr>
                <tr>
                  <td width="10%">Name:</td>
                  <td><%=(node_db.getNodeSysName() == null) ? "&nbsp;" : node_db.getNodeSysName()%></td>
                </tr>
                <tr>
                  <td width="10%">Object&nbsp;ID:</td>
                  <td><%=(node_db.getNodeSysId() == null) ? "&nbsp;" : node_db.getNodeSysId()%></td>
                </tr>
                <tr>
                  <td width="10%">Location:</td>
                  <td><%=(node_db.getNodeSysLocn() == null) ? "&nbsp;" : node_db.getNodeSysLocn()%></td>
                </tr>
                <tr>
                  <td width="10%">Contact:</td>
                  <td><%=(node_db.getNodeSysContact() == null) ? "&nbsp;" : node_db.getNodeSysContact()%></td>
                </tr>
                <tr>
                  <td valign="top" width="10%">Description:</td>
                  <td valign="top" colspan="3"><%=(node_db.getNodeSysDescr() == null) ? "&nbsp;" : node_db.getNodeSysDescr()%> </td>
                </tr>
              </table>  
              <br>
            <% } %>
            
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
              
            </table>
            
          </td>

          <td>&nbsp;</td>

          <td valign="top" width="48%">
            <!-- events list  box -->
            <% String eventHeader = "<a href='event/list?filter=" + URLEncoder.encode("node=" + nodeId) + "'>Recent Events</a>"; %>
            <% String moreEventsUrl = "event/list?filter=" + URLEncoder.encode("node=" + nodeId); %>

            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl%>" />
            </jsp:include>
            <br>
            
            <!-- Recent outages box -->
            <jsp:include page="/includes/nodeOutages-box.jsp" flush="false" />
            <br>
            <!-- Active Inventory box -->
            <jsp:include page="/includes/nodeInventory-box.jsp" flush="false">
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="nodelabel" value="<%=node_db.getLabel()%>" />
            </jsp:include>
         </td>
       </tr>
     </table>
    
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>