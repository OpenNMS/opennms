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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*,org.opennms.web.acegisecurity.Authentication,org.opennms.web.event.*,java.net.*,org.opennms.netmgt.utils.IPSorter,org.opennms.web.performance.*,org.opennms.web.response.*" %>

<%!
    protected int telnetServiceId;
    protected int httpServiceId;
    protected int dellServiceId;
    protected int snmpServiceId;
    protected PerformanceModel perfModel;
    protected ResponseTimeModel rtModel;
    
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

        try {
            this.perfModel = new PerformanceModel( org.opennms.core.resource.Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
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

    //find the Dell-OpenManage interfaces, if any
    String dellIp = null;
    Service[] dellServices = NetworkElementFactory.getServicesOnNode(nodeId, this.dellServiceId);

    if( dellServices != null && dellServices.length > 0 ) {
        ArrayList ips = new ArrayList();
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
%>

<%
    // find links
    Map linkMap = new HashMap();
    DataLinkInterface[] dl_if = NetworkElementFactory.getDataLinksOnNode(nodeId);

    for (int i=0; i<dl_if.length;i++){
	   int nodelinkedId = 0;
  	   int nodelinkedIf = -1;
  	   Integer ifindexmap = null;
  	   String iplinkaddress = null;
       Vector ifs = new Vector();

       nodelinkedId = dl_if[i].get_nodeparentid();
       nodelinkedIf = dl_if[i].get_parentifindex();
   	   iplinkaddress = dl_if[i].get_parentipaddr();
   	   ifindexmap = new Integer(dl_if[i].get_ifindex());

       Interface iface = null;
       if (iplinkaddress != null) {
	       if (nodelinkedIf == -1 ) {
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
	   
   }

    boolean isBridge = NetworkElementFactory.isBridgeNode(nodeId);
    boolean isRouteIP = NetworkElementFactory.isRouteInfoNode(nodeId);

%>
<script  language="JavaScript">
  function setDown(node, intf){
	document.setStatus.action="element/ManageSnmpIntf?node="+node+"&intf="+intf+"&status="+2;
	document.setStatus.submit();
	}
  function setUp(node, intf){
        document.setStatus.action="element/ManageSnmpIntf?node="+node+"&intf="+intf+"&status="+1;
        document.setStatus.submit();
	}

</script>

<html>
<head>
  <title><%=node_db.getLabel()%> | Node | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='element/index.jsp'>Search</a>"; %>
<% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
<% String breadcrumb3 = "Node Links"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Linked Node Info" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
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
         
        <% if( telnetIp != null ) { %>
          &nbsp;&nbsp;&nbsp;<a href="telnet://<%=telnetIp%>">Telnet</a>
        <% } %>

        <% if( httpIp != null ) { %>
          &nbsp;&nbsp;&nbsp;<a href="http://<%=httpIp%>">HTTP</a>
        <% } %>

        <% if( dellIp != null ) { %>
          &nbsp;&nbsp;&nbsp;<a href="https://<%=dellIp%>:1311">OpenManage</a>
        <% } %>

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

           <% if ( isSnmp && request.isUserInRole("OpenNMS Administrator"))  { %>
              <% for( int i=0; i < intfs.length; i++ ) { %>
                <% if( "P".equals( intfs[i].getIsSnmpPrimary() )) { %>
                      &nbsp;&nbsp;&nbsp;<a href="admin/updateSnmp.jsp?node=<%=nodeId%>&ipaddr=<%=intfs[i].getIpAddress()%>">Update SNMP</a>
                <% } %>
              <% } %>
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
</td>
<td>&nbsp;</td>
 <td valign="top" width="48%">&nbsp;</td>

</tr>
      
<tr>
<td valign="top" colspan="3">

<!-- Interface box -->
<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">

<tr bgcolor="#999999">
<td><b>Interface </b></td> 
<td width="10%"><b>If Status (Adm/Op)</b></td> 
<% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %> 
<td width="10%"><b>Set Admin Status</b></td> 
<% } %>
<td>
	<table width="100%" border="0" cellspacing="0" cellpadding="2" bordercolor="white">
	<tr>
	<td width="35%"><b> Linked Nodes </b></td>
	<td width="35%"><b> Interface  </b></td> 
	<td width="15%"><b> If Status (Adm/Op) </b></td>

	<td>
	<% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %> 
		<b> Set Admin Status </b>
	<% } else { %>
		<b> &nbsp; </b>
	<% } %>
	</td> 
	</tr>
	</table>
</td>
</tr>

<% for( int i=0; i < intfs.length; i++ ) { 

	Vector ifl =null;
	if (intfs[i].getIfIndex() == 0 ) {
 		ifl =(Vector)linkMap.get(new Integer(-1));
	} else {
 		ifl =(Vector)linkMap.get(new Integer(intfs[i].getIfIndex()));
	}
%>

<tr>

<td>
<% if( "0.0.0.0".equals( intfs[i].getIpAddress() )) { %>
<a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=intfs[i].getIpAddress()%>&ifindex=<%=intfs[i].getIfIndex()%>">Non-IP</a>
<%=" (ifIndex: "+intfs[i].getIfIndex()+"-"+intfs[i].getSnmpIfDescription()+")"%>
<% } else { %>  
<a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=intfs[i].getIpAddress()%>"><%=intfs[i].getIpAddress()%></a>
<%=intfs[i].getIpAddress().equals(intfs[i].getHostname()) ? "" : "(" + intfs[i].getHostname() + ")"%>
<% } %>
</td>

<td>
	<% if( intfs[i].getSnmpIfAdminStatus() < 1 && intfs[i].getSnmpIfOperStatus() < 1 ) { %>
	&nbsp; 
	<% } else { %>
	&nbsp;
	<%=OPER_ADMIN_STATUS[intfs[i].getSnmpIfAdminStatus()]%>/<%=OPER_ADMIN_STATUS[intfs[i].getSnmpIfOperStatus()]%>
	<% } %>
</td>
			
<% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %>
	<% if(OPER_ADMIN_STATUS[intfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
<td align="center"><b> <input type="button" value="Down" onClick="setDown(<%=intfs[i].getNodeId()%>,<%=intfs[i].getIfIndex()%>);"> </b></td>
	<% } else if (OPER_ADMIN_STATUS[intfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
<td align="center"><b> <input type="button" value="Up" onClick="setUp(<%=intfs[i].getNodeId()%>,<%=intfs[i].getIfIndex()%>);"> </b></td> 
	<% } else { %>
<td><b>&nbsp;</b></td> 
	<% } %>
<% } %>
		
<td>
<% if (ifl == null || ifl.size() == 0) {%>
&nbsp;
<% } else { %>
<table width="100%" border="0" cellspacing="0" cellpadding="2" bordercolor="white" BGCOLOR="#cccccc">
	<% for (int j=0; j<ifl.size();j++) { 
		Interface curlkif =(Interface)ifl.elementAt(j); 
	%>
        
	<tr>
	<td width="35%">
       	<a href="element/linkednode.jsp?node=<%=curlkif.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(curlkif.getNodeId())%></a>
	</td>
	<td width="35%">
       	<% if( "0.0.0.0".equals( curlkif.getIpAddress() )) { %>
        <a href="element/interface.jsp?node=<%=curlkif.getNodeId()%>&intf=<%=curlkif.getIpAddress()%>&ifindex=<%=curlkif.getIfIndex()%>">Non-IP</a>
        <%=" (ifIndex: "+curlkif.getIfIndex()+"-"+curlkif.getSnmpIfDescription()+")"%>
        <% } else { %>  
        <a href="element/interface.jsp?node=<%=curlkif.getNodeId()%>&intf=<%=curlkif.getIpAddress()%>"><%=curlkif.getIpAddress()%></a>
        <% } %>
	</td>
	<td width="15%">
	<% if( request.isUserInRole( Authentication.ADMIN_ROLE ) && curlkif != null) { %>
		<% if( curlkif.getSnmpIfAdminStatus() < 1 && curlkif.getSnmpIfOperStatus() < 1 ) { %>
		&nbsp; 
		<% } else { %>
		&nbsp; (
		<%=OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()]%>/<%=OPER_ADMIN_STATUS[curlkif.getSnmpIfOperStatus()]%>
		)
		<% } %>
	<% } else { %>
		&nbsp;
	<% } %>
	</td>
	<% if( request.isUserInRole( Authentication.ADMIN_ROLE ) && curlkif != null) { %>
		<% if(OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
		<td align="center"><b> <input type="button" value="Down" onClick="setDown(<%=curlkif.getNodeId()%>,<%=curlkif.getIfIndex()%>);"> </b></td>
		<% } else if (OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
		<td align="center"><b> <input type="button" value="Up" onClick="setUp(<%=curlkif.getNodeId()%>,<%=curlkif.getIfIndex()%>);"> </b></td> 
		<% } else { %>
		<td><b>&nbsp;</b></td> 
		<% } %>
	<% } else {%>              
	<td><b>&nbsp;</b></td> 
	<% } %>
	<% } %>
    </tr>
</table>
<%}%>
</td>


</tr>
<% } %>
</table>

<br>
</td>
</tr>
</table>


  </td>
  <td>&nbsp;</td>
  </tr>
</table>

<br>
<form method="POST" name="setStatus" />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%!
    public static HashMap statusMap;

    
    public String getStatusString( char c ) {
        return( (String)this.statusMap.get( new Character(c) ));
    }
    
      public static final String[] OPER_ADMIN_STATUS = new String[] {
    "&nbsp;",          //0 (not supported)
    "Up",              //1
    "Down",            //2
    "Testing",         //3
    "Unknown",         //4
    "Dormant",         //5
    "NotPresent",      //6
    "LowerLayerDown"   //7
  };

    
%>
