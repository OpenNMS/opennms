<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.WebSecurityUtils,
			org.opennms.web.asset.*,
			org.opennms.web.element.*,
			org.opennms.netmgt.model.OnmsNode,
            org.opennms.web.springframework.security.Authentication,
        	org.opennms.web.MissingParameterException
	"
%>

<%!
    AssetModel model = new AssetModel();
	
%>

<%
	boolean isAuthorizedUser = false;

	if( request.isUserInRole(Authentication.ROLE_PROVISION) || request.isUserInRole(Authentication.ROLE_ADMIN) ){
		isAuthorizedUser = true;
	}
    String nodeIdString = request.getParameter("node");

    if (nodeIdString == null) {
        throw new MissingParameterException("node", new String[] { "node" });
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );
    String nodeLabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel( nodeId );
    Asset asset = this.model.getAsset( nodeId );
    OnmsNode node_db = NetworkElementFactory.getInstance(getServletContext()).getNode( nodeId );
    boolean isNew = false;

    if( asset == null ) {
        asset = new Asset();
        isNew = true;        
    } 
%>


<%@page import="org.springframework.web.HttpRequestHandler"%><jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Asset" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Asset" />
  <jsp:param name="breadcrumb" value="<a href ='asset/index.jsp'>Assets</a>" />
  <jsp:param name="breadcrumb" value="Modify" />
</jsp:include>

<h2><%=nodeLabel%> (Node ID <%=nodeId%>)</h2>

<p>
  <a href="element/node.jsp?node=<%=nodeId%>">General Information</a>
</p>

<%-- Handle the SNMP information if any --%> 
<% if( node_db.getSysObjectId() != null ) { %>
  <table class="standard">
    <tr>
      <td class="standardheader"> System Id </td>
      <td class="standard"> <%=node_db.getSysObjectId()%> </td>
      <td class="standardheader"> System Name </td>
      <td class="standard"> <%=node_db.getSysName()%> </td>
    </tr>

    <tr>
      <td class="standardheader"> System Location </td>
      <td class="standard"> <%=node_db.getSysLocation()%> </td>
      <td class="standardheader"> System Contact </td>
      <td class="standard"> <%=node_db.getSysContact()%> </td>
    </tr>

    <tr>
      <td class="standardheader"> System Description </td>
      <td class="standard"> <%=node_db.getSysDescription()%> </td>
      <td class="standard" colspan="2">&nbsp; </td>
   </tr>
  </table>
<% } %>      

<% if( isAuthorizedUser ) { %>
<form action="asset/modifyAsset" method="post">
  <input type="hidden" name="node" value="<%=nodeId%>" />
  <input type="hidden" name="isnew" value="<%=isNew%>" />
<% } %>      

  <table width="100%" cellspacing="0" cellpadding="2" border="0">
	  <tr>
	    <td colspan="6"><h3>Configuration Categories</h3></td>
	  </tr>
	  <tr>
	    <td>Display Category</td>
	    <td colspan="2"><%=getTextField(request, "text", "displaycategory", asset.getDisplayCategory(), "20", "64")%></td>
	    <td>Notification Category</td>
	    <td colspan="2"><%=getTextField(request, "text", "notifycategory", asset.getNotifyCategory(), "20", "64")%></td>
	  </tr>
	  <tr>
	    <td>Poller Category</td>
	    <td colspan="2"><%=getTextField(request, "text", "pollercategory", asset.getPollerCategory(), "20", "64")%></td>
	    <td>Threshold Category</td>
	    <td colspan="2"><%=getTextField(request, "text", "thresholdcategory", asset.getThresholdCategory(), "20", "64")%></td>
	  </tr>
          <tr>
            <td colspan="6"><h3>Identification</h3></td>
          </tr>
          <tr>
            <td width="5%">Description</td>
            <td colspan="3"><%=getTextField(request, "text", "description", asset.getDescription(), "70", "128")%></td>
            <td width="5%">Category</td>
            <td>
            <%if( isAuthorizedUser ) { %>
              <select name="category" size="1">
              <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
                <option<%=(Asset.CATEGORIES[i].equals(asset.getCategory()))?" selected":""%>><%=Asset.CATEGORIES[i]%></option> 
              <% } %>
			<% } else { getTextField(request, "text", "category", asset.getCategory(), "20", "64"); } %>
            </td>
          </tr>
          <tr>
            <td>Manufacturer</td>
            <td><%=getTextField(request, "text", "manufacturer", asset.getManufacturer(), "20", "64")%></td>
            <td>Model Number</td>
            <td><%=getTextField(request, "text", "modelnumber", asset.getModelNumber(), "20", "64")%></td>
            <td>Serial Number</td>
            <td><%=getTextField(request, "text", "serialnumber", asset.getSerialNumber(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Asset Number</td>
            <td><%=getTextField(request, "text", "assetnumber", asset.getAssetNumber(), "20", "64")%></td>
            <td>Date Installed</td>
            <td><%=getTextField(request, "text", "dateinstalled", asset.getDateInstalled(), "20", "64")%></td>
            <td>Operating System</td>
	    <% String os = asset.getOperatingSystem();
		if(os == null || os.equals(""))
			os = node_db.getOperatingSystem();
		if (os == null)
			os = "";
	     %>
            <td><%=getTextField(request, "text", "operatingsystem", os, "20", "64")%></td>
          </tr>
          <tr>
            <td colspan="6"><h3>Location</h3></td>
          </tr>
          <tr>
            <td>Region</td>
            <td><%=getTextField(request, "text", "region", asset.getRegion(), "20", "64")%></td>
            <td>Division</td>
            <td><%=getTextField(request, "text", "division", asset.getDivision(), "20", "64")%></td>
            <td>Department</td>
            <td><%=getTextField(request, "text", "department", asset.getDepartment(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Address&nbsp;1</td>
            <td colspan="3"><%=getTextField(request, "text", "address1", asset.getAddress1(), "100", "256")%></td>
            <td>Admin</td>
            <td><%=getTextField(request, "text", "admin", asset.getAdmin(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Address&nbsp;2</td>
            <td colspan="5"><%=getTextField(request, "text", "address2", asset.getAddress2(), "100", "256")%></td>
          </tr>
          <tr>
            <td>City</td>
            <td><%=getTextField(request, "text", "city", asset.getCity(), "20", "64")%></td>
            <td>State</td>
            <td><%=getTextField(request, "text", "state", asset.getState(), "20", "64")%></td>
            <td>ZIP</td>
            <td><%=getTextField(request, "text", "zip", asset.getZip(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Building</td>
            <td><%=getTextField(request, "text", "building", asset.getBuilding(), "20", "64")%></td>
            <td>Floor</td>
            <td><%=getTextField(request, "text", "floor", asset.getFloor(), "20", "64")%></td>
            <td>Room</td>
            <td><%=getTextField(request, "text", "room", asset.getRoom(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Rack</td>
            <td><%=getTextField(request,  "text", "rack", asset.getRack(), "20", "64")%></td>
            <td>Slot</td>
            <td><%=getTextField( request, "text", "slot", asset.getSlot(), "20", "64")%></td>
            <td>Port</td>
            <td><%=getTextField(request, "text", "port", asset.getPort(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Rack&nbsp;unit&nbsp;height</td>
            <td><%=getTextField(request, "text", "rackunitheight", asset.getRackunitheight(), "2", "2")%></td>
            <td>Circuit&nbsp;ID</td>
            <td><%=getTextField(request, "text", "circuitid", asset.getCircuitId(), "20", "64")%></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td colspan="6"><h3>Vendor</h3></td>
          </tr>
          <tr>
            <td>Name</td>
            <td><%=getTextField( request, "text", "vendor", asset.getVendor(), "20", "64")%></td>
            <td>Phone</td>
            <td><%=getTextField(request, "text", "vendorphone", asset.getVendorPhone(), "20", "64")%></td>
            <td>Fax</td>
            <td><%=getTextField(request, "text", "vendorfax", asset.getVendorFax(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Lease</td>
            <td><%=getTextField( request, "text", "lease", asset.getLease(), "20", "64")%></td>
            <td>Lease Expires</td>
            <td><%=getTextField( request, "text", "leaseexpires", asset.getLeaseExpires(), "20", "64")%></td>
            <td>Vendor Asset</td>
            <td><%=getTextField( request, "text", "vendorassetnumber", asset.getVendorAssetNumber(), "20", "64")%></td>
          </tr>
          <tr>
            <td>Maint Contract</td>
            <td><%=getTextField( request, "text", "maintcontract", asset.getMaintContract(), "20", "64")%></td>
            <td>Contract Expires</td>
            <td><%=getTextField( request, "text", "maintcontractexpires", asset.getMaintContractExpires(), "20", "64")%></td>
            <td>Maint Phone</td>
            <td><%=getTextField( request, "text", "supportphone", asset.getSupportPhone(), "20", "64")%></td>
          </tr>
          
          <% if((request.isUserInRole(Authentication.ROLE_PROVISION)) || (request.isUserInRole(Authentication.ROLE_ADMIN))){ %>
          <tr>
            <td colspan="6"><h3>Authentication</h3></td>
          </tr>
          <tr>
            <td>Username</td>
            <td><input type="text" name="username" value="<%=asset.getUsername()%>" size="20" maxlength="32"/></td>
            <td>Password</td>
            <td><input type="text" name="password" value="<%=asset.getPassword()%>" size="20" maxlength="32"/></td>
            <td>Enable Password</td>
            <td><input type="text" name="enable" value="<%=asset.getEnable()%>" size="20" maxlength="32"/></td>
          </tr>
          <% } %>
          
          <tr>
            <td>Connection</td>
            <td>
            <% if(isAuthorizedUser){ %>
              <select name="connection" size="1">
              	<option></option>
              <% for( int i=0; i < Asset.CONNECTIONS.length; i++ ) { %>
                <option <%=(Asset.CONNECTIONS[i].equals(asset.getConnection()))?"selected":""%>><%=Asset.CONNECTIONS[i]%></option> 
              <% } %>
              </select>
              <% }else{ getTextField(request,  "text", "connection", asset.getConnection(), "20", "64");}%>
            </td>
            <td>AutoEnable</td>
            <td>
            <% if(isAuthorizedUser){ %>
              <select name="autoenable" size="1">
              	<option></option>
              <% for( int i=0; i < Asset.AUTOENABLES.length; i++ ) { %>
                <option <%=(Asset.AUTOENABLES[i].equals(asset.getAutoenable()))?"selected":""%>><%=Asset.AUTOENABLES[i]%></option> 
              <% } %>
              </select>
              <% }else{ getTextField(request, "text", "autoEnable", asset.getAutoenable(), "20", "64"); } %>
             </td>
            <% if(isAuthorizedUser){ %>
            <td>SNMP community</td>
            <td><%=getTextField( request, "text", "snmpcommunity", asset.getSnmpcommunity(), "20", "32")%></td>
            <% } else { %>
	     <td>&nbsp;</td>
	     <td>&nbsp;</td>
	     <% } %>
          </tr>
          <tr>
            <td colspan="6"><h3>Hardware</h3></td>
          </tr>
          <tr>
            <td>Cpu</td>
            <td><%=getTextField( request, "text", "cpu", asset.getCpu(), "20", "64")%></td>
            <td>Ram</td>
            <td><%=getTextField( request, "text", "ram", asset.getRam(), "10", "10")%></td>
            <td>Storage Controller</td>
            <td><%=getTextField( request, "text", "storagectrl", asset.getStoragectrl(), "20", "64")%></td>
          </tr>
          <tr>
            <td>HDD 1</td>
            <td><%=getTextField( request, "text", "hdd1", asset.getHdd1(), "20", "64")%></td>
            <td>HDD 4</td>
            <td><%=getTextField( request, "text", "hdd4", asset.getHdd4(), "20", "64")%></td>
            <td>Additional hardware</td>
            <td><%=getTextField( request, "text", "additionalhardware", asset.getAdditionalhardware(), "20", "64")%></td>
          </tr>
          <tr>
            <td>HDD 2</td>
            <td><%=getTextField( request, "text", "hdd2", asset.getHdd2(), "20", "64")%></td>
            <td>HDD 5</td>
            <td><%=getTextField( request, "text", "hdd5", asset.getHdd5(), "20", "64")%></td>
            <td>Number of power supplies</td>
            <td><%=getTextField( request, "text", "numpowersupplies", asset.getNumpowersupplies(), "1", "1")%></td>
	  </tr>
          <tr>
            <td>HDD 3</td>
            <td><%=getTextField( request, "text", "hdd3", asset.getHdd3(), "20", "64")%></td>
            <td>HDD 6</td>
            <td><%=getTextField( request, "text", "hdd6", asset.getHdd6(), "20", "64")%></td>
            <td>Inputpower</td>
            <td><%=getTextField( request, "text", "inputpower", asset.getInputpower(), "11", "11")%></td>
	  </tr>
         <tr>
            <td colspan="6"><h3>Comments</h3></td>
          </tr>
          <tr>
            <td colspan="6"><%=getTextArea( request, "comments", asset.getComments())%></td>
          </tr>
          <tr>
          <% if(isAuthorizedUser) { %>
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
            <% } %>
          </tr>
        </table>
      <%if(isAuthorizedUser) { %>
      </form>

      <% } %>
      
      <%!      	
      	private String getTextField(HttpServletRequest request, String type, String name, String value, String size, String maxLength){
	    	  if(request.isUserInRole(Authentication.ROLE_PROVISION) || request.isUserInRole(Authentication.ROLE_ADMIN) ){
	    		  return String.format("<input type=\"%s\" name=\"%s\" value=\"%s\" size=\"%s\" maxlength=\"%s\"/>", type, name, value, size, maxLength);
	    	  }else{
	    	  	return String.format("<p>%s</p>", value);
	    	  }
      	}
      
	    private String getTextArea(HttpServletRequest request, String name, String value){
	    	  if(request.isUserInRole(Authentication.ROLE_PROVISION) || request.isUserInRole(Authentication.ROLE_ADMIN) ){
	    		  return String.format("<textarea name=\"%s\" cols=\"100\" rows=\"15\">%s</textarea>",  name, value);
	    	  }else{
	    	  	return String.format("<p>%s</p>", value);
	    	  }
	  	}
      %>

<jsp:include page="/includes/footer.jsp" flush="false" />
