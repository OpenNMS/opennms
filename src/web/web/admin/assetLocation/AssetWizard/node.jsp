<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.admin.assetLocation.assetWizard.*,org.opennms.netmgt.config.assetLocation.*,org.opennms.web.asset.*,org.opennms.web.element.*,org.opennms.netmgt.config.*" %>

<%!
    public void init() throws ServletException {
        try {
            AssetLocationFactory.init();
        }
        catch( Exception e ) {
           throw new ServletException( e.getMessage(), e );
       }
    }

%>
<%
    HttpSession user = request.getSession(true);
    Building newBuild = (Building)user.getAttribute("newBuild");
    Room newRoom = (Room)user.getAttribute("newRoom");
    String building = newBuild.getName();
    String room = newRoom.getRoomID();
	
    if( room == null || building == null ) {
        throw new org.opennms.web.MissingParameterException( "searchvalue", new String[] {"column","searchvalue"} );
    }

    Node[] nodes = null;
    nodes = NetworkElementFactory.getAllNodes();
    Map nodeMap = new TreeMap(AssetLocationFactory.getInstance().getAssetNode(building,room));

%>


<html>
<head>
  <title>Asset Location Room Configuration | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function editNode(name)
    {
        document.AssetLocation.userAction.value="edit";
        document.AssetLocation.node.value=name;
        document.AssetLocation.submit();
    }
    
    function deleteNode(name)
    {
        if (confirm("Are you sure you want to delete Asset Info for Node  " + name + "?"))
        {
          document.AssetLocation.userAction.value="delete";
          document.AssetLocation.node.value=name;
          document.AssetLocation.submit();
        }
    }
   
    function newNode()
    {
    	if (document.AssetLocation.newnode.selectedIndex >= 0) 
    	{
		document.AssetLocation.userAction.value="new";
	        document.AssetLocation.submit();
	}
	else
		alert("Please select a node.");
    }
    
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/assetLocation/index.jsp" +  "'>Configure Asset Location</a>"; %>
<% String breadcrumb3 = "<a href='admin/assetLocation/AssetWizard/building.jsp" +  "'>Buildings</a>"; %>
<% String breadcrumb4 = "<a href='admin/assetLocation/AssetWizard/room.jsp" +  "'>Rooms</a>"; %>
<% String breadcrumb5 = "Room's Node Configuration"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Building's Room Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb5%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>

      <h2>Asset Location Building - Room - Node's List <br></h2>

        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
        <tr>
            <td bgcolor="#999999">Building</td>
            <td bgcolor="red">&nbsp;<%=building%></td>
            <td bgcolor="#999999">Floor</td>
            <td>&nbsp;<%=newRoom.getFloor()%></td>
            <td bgcolor="#999999">Room</td>
            <td bgcolor="red">&nbsp;<%=room%></td>
          </tr>

          <tr>
            <td bgcolor="#999999">Address&nbsp;1</td>
            <td colspan="5">&nbsp;<%=newBuild.getAddress1()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">City</td>
            <td>&nbsp;<%=newBuild.getCity()%></td>
            <td bgcolor="#999999">State</td>
            <td>&nbsp;<%=newBuild.getState()%></td>
            <td bgcolor="#999999">ZIP</td>
            <td>&nbsp;<%=newBuild.getZIP()%></td>
          </tr>
        </table>

      <table width="100%" cellspacing="2" cellpadding="2" border="0">
      <form METHOD="POST" NAME="AssetLocation" ACTION="admin/assetLocation/AssetWizard/assetLocationWizard">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_NODE%>">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="building" value="<%=building%>"/>
      <input type="hidden" name="room" value="<%=room%>"/>
      <input type="hidden" name="node" value=""/>

        <tr>
          <td><br>
<% if( nodeMap.size()> 0 ) { %>
            <h4>Room's Current Defined Nodes </h4>
            <table width="100%" cellspacing="2" cellpadding="2" border="1" bgcolor="cccccc">
              <tr bgcolor="#999999">
                <td colspan="2">
                    <b>Actions</b>
                </td>
                <td>
                    <b>Asset Info</b>
                </td>
                <td>
                    <b>General Information</b>
                </td>
                <td>
                    <b>Rack</b>
                </td>
                <td>
                    <b>Slot</b>
                </td>
                <td>
                    <b>Port</b>
                </td>
                <td>
                    <b>CircuitID</b>
                </td>
              </tr>
            <% Iterator iterator = nodeMap.keySet().iterator();
               while(iterator.hasNext()) 
               { 
                 String key = (String)iterator.next();
                 Asset curAsset = (Asset)nodeMap.get(key);
            %>
                <tr>
                  <td>
                    <input type="button" value="Edit" onclick="javascript:editNode('<%=key%>')"/>
                  </td>
                  <td>
                    <input type="button" value="Delete"  onclick="javascript:deleteNode('<%=key%>')"/>
                  </td>
                  <td bgcolor="green">
                    <a href="asset/detail.jsp?node=<%=key%>">Asset Info</a>
                  </td>
                  <td bgcolor="green">
                    <a href="element/node.jsp?node=<%=key%>"><%=org.opennms.web.element.NetworkElementFactory.getNodeLabel( curAsset.getNodeId() )%></a>
                  </td>
            <td bgcolor="orange">&nbsp;<%=curAsset.getRack()%></td>
            <td bgcolor="orange">&nbsp;<%=curAsset.getSlot()%></td>
            <td bgcolor="orange">&nbsp;<%=curAsset.getPort()%></td>
            <td bgcolor="orange">&nbsp;<%=curAsset.getCircuitId()%></td>
          </tr>
    
                </tr>
            <% } %>
            </table>
          </td>
<%	} else { %>
            <h4>No Defined Node in Room  </h4>
		</td>
<% } %>
        </tr>
	<tr>
  <% if( nodes.length > 0 ) { %>
    <td>
            <h3><br> </h3>

      <select NAME="newnode" SIZE="6">
      <% for( int i=0; i < nodes.length; i++ ) { %>
        <option VALUE="<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></option>
      <% } %>
      </select>
    </td>
</tr><tr>
          <td> <input type="button" value="Add/Edit Node" onclick="javascript:newNode()"/></td>
</tr>

  <% } else { %>
    <td>
      None found.
    </td>
  <% } %>
        </tr>

      </form>
      </table>
    
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
