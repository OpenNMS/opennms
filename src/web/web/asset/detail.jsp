<%@page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.utils.*,java.net.*,java.util.*,org.opennms.web.asset.*,org.opennms.web.element.*,org.opennms.web.authenticate.Authentication" %>

<%!
    AssetModel model = new AssetModel();
	
	protected int vncServiceId;

    public void init() throws ServletException {
        try {
            this.vncServiceId = NetworkElementFactory.getServiceIdFromName("VNC");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the VNC service ID", e );
        }
	}
%>

<%
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = Integer.parseInt( nodeIdString );
    String nodeLabel = org.opennms.web.element.NetworkElementFactory.getNodeLabel( nodeId );
    Asset asset = this.model.getAsset( nodeId );
    Node node_db = NetworkElementFactory.getNode( nodeId );
    boolean isNew = false;

    if( asset == null ) {
        asset = new Asset();
        isNew = true;        
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
%>

<html>
<head>
  <title>Modify | Asset | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='asset/index.jsp' >Assets</a>"; %>
<% String breadcrumb2="Detail"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Asset" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>
    <td colspan="3">
      <h2><%=nodeLabel%></h2>
<table><tr><td>
      <a href="element/node.jsp?node=<%=nodeId%>">General Information</a>&nbsp;&nbsp;<% if( vncIp != null ) { %><a href="http://<%=vncIp%>:5800/"  target="_blank">VNC</a>&nbsp;<% } %></td><td>
<%
 if(request.isUserInRole(Authentication.ADMIN_ROLE)){
%>
      <a href="asset/modify.jsp?node=<%=nodeId%>">Modify Asset</a></td></tr></table><br>
<% } %>
      <%-- Handle the SNMP information if any --%> 
      <% if( node_db.getNodeSysId() != null ) { %>
        <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
          <tr>
            <td> System Id </td>
            <td> <%=node_db.getNodeSysId()%> </td>
            <td> System Name </td>
            <td> <%=node_db.getNodeSysName()%> </td>
          </tr>
          <tr>
            <td> System Location </td>
            <td> <%=node_db.getNodeSysLocn()%> </td>
            <td> System Contact </td>
            <td> <%=node_db.getNodeSysContact()%> </td>
          </tr>
          <tr>
            <td> System Description </td>
            <td> <%=node_db.getNodeSysDescr()%> </td>
	            <td>&nbsp; </td>
    	        <td>&nbsp; </td>
          </tr>
        </table>
      <% } %>      
    </td>
    <td>&nbsp;</td>
  </tr>

  <tr>
    <td>&nbsp;</td>

    <td>
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
	<tr>
            <td valign="bottom"><br><h3>Identification</h3></td>
	<tr>
          <tr>
          <td>
        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
          <tr>
            <td width="5%" bgcolor="#999999">Description</td>
            <td colspan="3">&nbsp;<%=asset.getDescription()%></td>
            <td width="5%" bgcolor="#999999">Category</td>
            <td>&nbsp;<%=asset.getCategory()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Manufacturer</td>
            <td>&nbsp;<%=asset.getManufacturer()%></td>
            <td bgcolor="#999999">Model Number</td>
            <td>&nbsp;<%=asset.getModelNumber()%></td>
            <td bgcolor="#999999">Serial Number</td>
            <td>&nbsp;<%=asset.getSerialNumber()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Asset Number</td>
            <td>&nbsp;<%=asset.getAssetNumber()%></td>
            <td bgcolor="#999999">Date Installed</td>
            <td>&nbsp;<%=asset.getDateInstalled()%></td>
            <td bgcolor="#999999">Operating System</td>
	    <% String os = asset.getOperatingSystem();
		if(os == null || os.equals(""))
			os = node_db.getOperatingSystem();
		if (os == null)
			os = "";
	     %>
            <td>&nbsp;<%=os%></td>
          </tr>
	</table>
	</td>
	</tr>
          <tr>
            <td valign="bottom"><br><h3>Location</h3></td>
          </tr>
	<tr>
	<td>
        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
          <tr>
            <td bgcolor="#999999">Region</td>
            <td>&nbsp;<%=asset.getRegion()%></td>
            <td bgcolor="#999999">Division</td>
            <td>&nbsp;<%=asset.getDivision()%></td>
            <td bgcolor="#999999">Department</td>
            <td>&nbsp;<%=asset.getDepartment()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Address&nbsp;1</td>
            <td colspan="5">&nbsp;<%=asset.getAddress1()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Address&nbsp;2</td>
            <td colspan="5">&nbsp;<%=asset.getAddress2()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">City</td>
            <td>&nbsp;<%=asset.getCity()%></td>
            <td bgcolor="#999999">State</td>
            <td>&nbsp;<%=asset.getState()%></td>
            <td bgcolor="#999999">ZIP</td>
            <td>&nbsp;<%=asset.getZip()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Building</td>
            <td>&nbsp;<%=asset.getBuilding()%></td>
            <td bgcolor="#999999">Floor</td>
            <td>&nbsp;<%=asset.getFloor()%></td>
            <td bgcolor="#999999">Room</td>
            <td>&nbsp;<%=asset.getRoom()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Rack</td>
            <td>&nbsp;<%=asset.getRack()%></td>
            <td bgcolor="#999999">Slot</td>
            <td>&nbsp;<%=asset.getSlot()%></td>
            <td bgcolor="#999999">Port</td>
            <td>&nbsp;<%=asset.getPort()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Circuit&nbsp;ID</td>
            <td>&nbsp;<%=asset.getCircuitId()%></td>
            <td colspan="4" bgcolor="FFFFFF">&nbsp;</td>
          </tr>
	</table>
	</td>
	</tr>
          <tr>
            <td valign="bottom"><br><h3>Vendor</h3></td>
          </tr>
	  <tr><td>
        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
          <tr>
            <td bgcolor="#999999">Name</td>
            <td>&nbsp;<%=asset.getVendor()%></td>
            <td bgcolor="#999999">Phone</td>
            <td>&nbsp;<%=asset.getVendorPhone()%></td>
            <td bgcolor="#999999">Fax</td>
            <td>&nbsp;<%=asset.getVendorFax()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Lease</td>
            <td>&nbsp;<%=asset.getLease()%></td>
            <td bgcolor="#999999">Lease Expires</td>
            <td>&nbsp;<%=asset.getLeaseExpires()%></td>
            <td bgcolor="#999999">Vendor Asset</td>
            <td>&nbsp;<%=asset.getVendorAssetNumber()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Maint Contract</td>
            <td>&nbsp;<%=asset.getMaintContract()%></td>
            <td bgcolor="#999999">Contract Expires</td>
            <td>&nbsp;<%=asset.getMaintContractExpires()%></td>
            <td bgcolor="#999999">Maint Phone</td>
            <td>&nbsp;<%=asset.getSupportPhone()%></td>
          </tr>
		</table>
	 </td></tr>
	 <!-- Add block -->
	<tr>
		<td valign="bottom"><br><h3>Configuration Categories</h3></td>
	</tr>
	 <tr><td>
        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
          <tr>
            <td bgcolor="#999999">Display Category</td>
            <td>&nbsp;<%=asset.getDisplayCategory()%></td>
            <td bgcolor="#999999">Notification Category</td>
            <td>&nbsp;<%=asset.getNotifyCategory()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Poller Category</td>
            <td>&nbsp;<%=asset.getPollerCategory()%></td>
            <td bgcolor="#999999">Threshold Category</td>
            <td>&nbsp;<%=asset.getThresholdCategory()%></td>
          </tr>
		</table>	 
	 </td></tr>
	 <!-- End Add block -->

	 
          <tr>
            <td valign="bottom"><br><h3>Comments</h3></td>
          </tr>
	<tr><td>
        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
          <tr>
            <td>&nbsp;<%=asset.getComments()%></td>
          </tr>
        </table>
	</td>
          </tr>
        </table>

    </td>

    <td>&nbsp;</td>
  </tr>
<td>&nbsp;</td>
<td align="right">
              <font size="-1">
              <% if( isNew ) { %>
                  <em>New Record</em>
              <% } else { %>
                  <em>Last Modified: <%=asset.getLastModifiedDate()%> by <%=asset.getUserLastModified()%></em>
              <% } %>
              </font>  
</td>
    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br>
<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
