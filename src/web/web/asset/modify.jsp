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
// 2004 Jan 06: Added support for Display, Notify, Poller and threshold categories
// 2003 Feb 07: Fixed URLEncoder issues.
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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.asset.*,org.opennms.web.element.*" %>

<%!
    AssetModel model = new AssetModel();
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
%>

<html>
<head>
  <title>Modify | Asset | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href ='asset/index.jsp'>Assets</a>"; %>
<% String breadcrumb2 = "Modify"; %>
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
      <h2><%=nodeLabel%> ( nodeID=<%=nodeId%> ) </h2>
      <p><a href="element/node.jsp?node=<%=nodeId%>">General Information</a></p>
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
      <form action="asset/modifyAsset" method="POST">
        <input type="hidden" name="node" value="<%=nodeId%>" />
        <input type="hidden" name="isnew" value="<%=isNew%>" />

        <table width="100%" cellspacing="0" cellpadding="2" border="0">
	  <tr>
	    <td colspan="6"><h3>Configuration Categories</h3></td>
	  </tr>
	  <tr>
	    <td>Display Category</td>
	    <td><input type="text" name="displaycategory" value="<%=asset.getDisplayCategory()%>" size="20" maxlength="64"/></td>
	    <td>Notification Category</td>
	    <td><input type="text" name="notifycategory"  value="<%=asset.getNotifyCategory()%>" size="20" maxlength="64"/></td>
	  </tr>
	  <tr>
	    <td>Poller Category</td>
	    <td><input type="text" name="pollercategory" value="<%=asset.getPollerCategory()%>" size="20" maxlength="64"/></td>
	    <td>Threshold Category</td>
	    <td><input type="text" name="thresholdcategory"  value="<%=asset.getThresholdCategory()%>" size="20" maxlength="64"/></td>
	  </tr>
          <tr>
            <td colspan="6"><h3>Identification</h3></td>
          </tr>
          <tr>
            <td width="5%">Description</td>
            <td colspan="3"><input type="text" name="description" value="<%=asset.getDescription()%>" size="70" maxlength="128"/></td>
            <td width="5%">Category</td>
            <td>
              <select name="category" size="1">
              <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
                <option <%=(Asset.CATEGORIES[i].equals(asset.getCategory()))?"selected":""%>><%=Asset.CATEGORIES[i]%></option> 
              <% } %>
              </select>
            </td>
          </tr>
          <tr>
            <td>Manufacturer</td>
            <td><input type="text" name="manufacturer" value="<%=asset.getManufacturer()%>" size="20" maxlength="64"/></td>
            <td>Model Number</td>
            <td><input type="text" name="modelnumber" value="<%=asset.getModelNumber()%>" size="20" maxlength="64"/></td>
            <td>Serial Number</td>
            <td><input type="text" name="serialnumber"  value="<%=asset.getSerialNumber()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Asset Number</td>
            <td><input type="text" name="assetnumber" value="<%=asset.getAssetNumber()%>" size="20" maxlength="64"/></td>
            <td>Date Installed</td>
            <td><input type="text" name="dateinstalled" value="<%=asset.getDateInstalled()%>" size="20" maxlength="64"/></td>
            <td>Operating System</td>
	    <% String os = asset.getOperatingSystem();
		if(os == null || os.equals(""))
			os = node_db.getOperatingSystem();
		if (os == null)
			os = "";
	     %>
            <td><input type="text" name="operatingsystem"  
			value="<%=os%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td colspan="6"><h3>Location</h3></td>
          </tr>
          <tr>
            <td>Region</td>
            <td><input type="text" name="region" value="<%=asset.getRegion()%>" size="20" maxlength="64"/></td>
            <td>Division</td>
            <td><input type="text" name="division" value="<%=asset.getDivision()%>" size="20" maxlength="64"/></td>
            <td>Department</td>
            <td><input type="text" name="department" value="<%=asset.getDepartment()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Address&nbsp;1</td>
            <td colspan="5"><input type="text" value="<%=asset.getAddress1()%>" name="address1" size="100" maxlength="256"/></td>
          </tr>
          <tr>
            <td>Address&nbsp;2</td>
            <td colspan="5"><input type="text" value="<%=asset.getAddress2()%>" name="address2" size="100" maxlength="256"/></td>
          </tr>
          <tr>
            <td>City</td>
            <td><input type="text" value="<%=asset.getCity()%>" name="city" size="20" maxlength="64"/></td>
            <td>State</td>
            <td><input type="text" value="<%=asset.getState()%>" name="state" size="20" maxlength="64"/></td>
            <td>ZIP</td>
            <td><input type="text" value="<%=asset.getZip()%>" name="zip" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Building</td>
            <td><input type="text" name="building" value="<%=asset.getBuilding()%>" size="20" maxlength="64"/></td>
            <td>Floor</td>
            <td><input type="text" name="floor" value="<%=asset.getFloor()%>" size="20" maxlength="64"/></td>
            <td>Room</td>
            <td><input type="text" name="room" value="<%=asset.getRoom()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Rack</td>
            <td><input type="text" name="rack" value="<%=asset.getRack()%>" size="20" maxlength="64"/></td>
            <td>Slot</td>
            <td><input type="text" name="slot" value="<%=asset.getSlot()%>" size="20" maxlength="64"/></td>
            <td>Port</td>
            <td><input type="text" name="port" value="<%=asset.getPort()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Circuit&nbsp;ID</td>
            <td><input type="text" name="circuitid" value="<%=asset.getCircuitId()%>" size="20" maxlength="64"/></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td colspan="6"><h3>Vendor</h3></td>
          </tr>
          <tr>
            <td>Name</td>
            <td><input type="text" name="vendor" value="<%=asset.getVendor()%>" size="20" maxlength="64"/></td>
            <td>Phone</td>
            <td><input type="text" name="vendorphone" value="<%=asset.getVendorPhone()%>" size="20" maxlength="64"/></td>
            <td>Fax</td>
            <td><input type="text" name="vendorfax"  value="<%=asset.getVendorFax()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Lease</td>
            <td><input type="text" name="lease" value="<%=asset.getLease()%>" size="20" maxlength="64"/></td>
            <td>Lease Expires</td>
            <td><input type="text" name="leaseexpires" value="<%=asset.getLeaseExpires()%>" size="20" maxlength="64"/></td>
            <td>Vendor Asset</td>
            <td><input type="text" name="vendorassetnumber" value="<%=asset.getVendorAssetNumber()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Maint Contract</td>
            <td><input type="text" name="maintcontract" value="<%=asset.getMaintContract()%>" size="20" maxlength="64"/></td>
            <td>Contract Expires</td>
            <td><input type="text" name="maintcontractexpires" value="<%=asset.getMaintContractExpires()%>" size="20" maxlength="64"/></td>
            <td>Maint Phone</td>
            <td><input type="text" name="supportphone" value="<%=asset.getSupportPhone()%>" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td colspan="6"><h3>Comments</h3></td>
          </tr>
          <tr>
            <td colspan="6"><textarea name="comments" cols="100" rows="15"><%=asset.getComments()%></textarea></td>
          </tr>
          <tr>
            <td colspan="3">
              <input type="submit" value="Submit"/>
              <input type="reset" />
            </td>
            <td colspan="3" align="right"> 
              <font size="-1">
              <% if( isNew ) { %>
                  <em>New Record</em>
              <% } else { %>
                  <em>Last Modified: <%=asset.getLastModifiedDate()%> by <%=asset.getUserLastModified()%></em>
              <% } %>
              </font>  
            </td>
          </tr>
        </table>
      </form>

      <p>Note that all commas and end of line markers will be removed when 
        submitted.  Please try to format your comments and other values without
        commas or hitting the return key to add new lines.
      </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br>
<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
