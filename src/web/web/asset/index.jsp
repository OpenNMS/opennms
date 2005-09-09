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
// 2003 Feb 07: Fixed URLEncoder issues.
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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.asset.*,java.util.*,org.opennms.web.element.NetworkElementFactory,org.opennms.netmgt.config.*,org.opennms.netmgt.config.assetLocation.*" %>

<%!
    protected AssetModel model;
    protected String[][] columns;

    public void init() throws ServletException {
        this.model = new AssetModel();
        this.columns = this.model.getColumns();
	try {
            AssetLocationFactory.init();
    }
        catch( Exception e ) {
           throw new ServletException( e.getMessage(), e );
       }    
	}
%>

<%
    Asset[] allAssets = this.model.getAllAssets();
    ArrayList assetsList = new ArrayList();

    for( int i=0; i < allAssets.length; i++ ) {
        if( !"".equals(allAssets[i].getAssetNumber()) ) {
            assetsList.add( allAssets[i] );
        }
    }

    int assetCount = assetsList.size();
    int middle = assetCount/2;  //integer division so it should round down
    if( assetCount%2 == 1 ) {
        middle++;  //make sure the one odd entry is on the left side
    }
%>

<html>
<head>
  <title>Assets | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="JavaScript">
	var Buildings = new Array();
	var indexCmd = 0;

function BuildingObj()
{
	this.BuildingName = new String();
	this.Rooms = new Array();
	return this;
}

<% int i = 0;
Map buildingMap = new TreeMap(AssetLocationFactory.getInstance().getBuildings());
              Iterator iterator = buildingMap.keySet().iterator();
               while(iterator.hasNext()) { 
                 String building = (String)iterator.next();
				Building curBuild = (Building)buildingMap.get(building);
				%>
Buildings[<%=i%>] = new BuildingObj();
Buildings[<%=i%>].BuildingName= "<%=curBuild.getName()%>";
<% 
int indexCmd  = 0;
Map roomMap = new TreeMap(AssetLocationFactory.getInstance().getRooms(curBuild));
               Iterator iterator1 = roomMap.keySet().iterator();
               while(iterator1.hasNext()) 
               { 
                 String room = (String)iterator1.next();
                 Room curRoom = (Room)roomMap.get(room);
            %>
			Buildings[<%=i%>].Rooms[<%=indexCmd%>] = "<%=curRoom.getRoomID()%>";
			<% indexCmd++; %>
<%
	}
i++;
}
%>

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "Assets"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Assets" />
  <jsp:param name="location" value="asset" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">
      <h3>Search Asset Information</h3>
      <p>
        <form action="asset/nodelist.jsp" method="GET">
          Assets in category: <br>
          <input type="hidden" name="column" value="category" />
          <select name="searchvalue" size="1">
            <% for( i=0; i < Asset.CATEGORIES.length; i++ ) { %>
              <option><%=Asset.CATEGORIES[i]%></option> 
            <% } %>
          </select>
          <input type="submit" value="Search" />
        </form>

        <form action="asset/nodelist.jsp" method="GET">
          <table width="50%" cellspacing="0" cellpadding="2" border="0">
            <tr>
              <td>Asset Field:</td>
              <td>Containing Text:</td>
            </tr>
            <tr>
              <td>
                <select name="column" size="1">
                  <% for( i=0; i < this.columns.length; i++ ) { %>
                    <option value="<%=this.columns[i][1]%>"><%=this.columns[i][0]%></option>
                  <% } %>
                </select>
              </td>
              <td>
                <input type="text" name="searchvalue" />
              </td>
            </tr>
            <tr>
              <td colspan="2"><input type="submit" value="Search" /></td>
            </tr>
          </table>
        </form>
		</p>
	<p>	
        <a href="asset/nodelist.jsp?column=<%=this.columns[0][1]%>&searchvalue=">List all nodes with asset info</a>
      </p>
      <h3>Search Asset Location Information</h3>
      <p>
        <!--form action="asset/alnodelist.jsp" method="POST" name="AssetLocation" onsubmit="r=checkFields(this);return r;"-->
        <form action="asset/alnodelist.jsp" method="POST" name="AssetLocation">
          <table width="50%" cellspacing="0" cellpadding="2" border="0">
            <tr>
              <td>
                <select name="building" size="1">
                    <option value="0" selected>Building</option>
                </select>
              </td>
              <td>
<td>
                <select name="room" size="1">
                    <option value="0" selected>Room</option>
                </select>
              </td>              </td>
            </tr>
            <tr>
              <td colspan="2"><input type="submit" value="Search" /></td>
            </tr>
          </table>
        </form>
		</p>
<p>
        <a href="asset/treeview.jsp">Building Asset Info  Tree View</a>

      </p>
      <br>
    </td>
    
    <td>&nbsp;</td>

    <td width="60%" valign="top">
        <h3>Assets Inventory</h3>

        <p>The OpenNMS system provides a means for you to easily track and share 
            important information about capital assets in your organization.  This 
            data, when coupled with the information about your network that the 
            OpenNMS system obtains during network discovery, can be a powerful tool not 
            only for solving problems, but in tracking the current state of 
            equipment repairs as well as network or system-related moves, additions, 
            or changes.
        </p>    
            
        <p>There are three ways to add or modify the asset data stored in the OpenNMS system:
            <ul>
              <li>Enter data by hand using Asset Location <em>Admin </em> page
              <li>Import the data from another source (Importing asset data is 
            described on the <em>Admin</em> page)
              <li>Enter the data by hand
            </ul>
            
            Once you begin adding data to the OpenNMS system's assets inventory page, 
            any node with an asset number (for example, bar code) will be displayed on the 
            lower half of this page, providing you a one-click mechanism for 
            tracking the current physical status of that device.  If you wish to 
            search for particular assets by category, simply click the drop-down box 
            labeled <b>Assets in category</b>, select the desired category, and click 
            <b>[Search]</b> to retrieve a list of all assets associated with that category. 
            If you wish to search asset using a particular Asset Field, simply click the
            drop-down box labeled <b>Asset Field</b> then insert the text data to find
            in the box labeled <b>Containing Text</b>, and click <b>[Search]</b> to retrieve
            a list of all assets associated with the search criteria.
            And for a complete list of nodes, whether or not they have associated 
            asset numbers, simply click on the <b>List all nodes with asset information</b> 
            link.
            Also is possible to get a TreeView Asset Info expanded on buildings and rooms.
			Finally is possible to get a list of Nodes selecting building and/or rooms .
        </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
<br>
<hr align="center" size="2" width="95%">
<br>
<table>
  <tr>
    <td>&nbsp;</td>

    <td colspan="3" valign="top">
      <h3>Assets with asset numbers</h3>

      <table width="100%" cellspacing="0" cellpadding="2" border="0">
        <tr>
          <td>
            <ul>
            <% for( i=0; i < middle; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li> <%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(asset.getNodeId())%></a>
            <% } %>
            </ul>
          </td>
          <td>
            <% for( i=middle; i < assetCount; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li> <%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(asset.getNodeId())%></a>
            <% } %>
          </td>
        </tr>
      </table>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="asset" />
</jsp:include>

</body>
</html>

<script language="JavaScript">

function fillCombo()
{
  var j = 0;
  var i = 0;
	
  for(i=0;i<Buildings.length;i++)
  {
    document.AssetLocation.building.options.length+=1;		
    document.AssetLocation.building.options[document.AssetLocation.building.options.length-1] = new Option(Buildings[i].BuildingName,Buildings[i].BuildingName, false, false);

    j=0;

    for(j=0;j<Buildings[i].Rooms.length;j++)
    {
      if(!isAlready(Buildings[i].Rooms[j],document.AssetLocation.room))
      {
        document.AssetLocation.room.options.length+=1;
	document.AssetLocation.room.options[document.AssetLocation.room.options.length-1] = new Option("Room " + Buildings[i].Rooms[j],Buildings[i].Rooms[j], false, false);			
      }
    }
  }

  document.AssetLocation.building.onchange = function()
  {
    var a = document.AssetLocation.room.selectedIndex;
    ReFillRoomCombo(document.AssetLocation.room);
    document.AssetLocation.room.selectedIndex = a;
  }

  document.AssetLocation.room.onchange = function()
  {
    var a = document.AssetLocation.building.selectedIndex;
    ReFillBuildingCombo(document.AssetLocation.building);
    document.AssetLocation.building.selectedIndex = a;

  }
}

function isAlready(StrCmd,Combo)
{
	var i=0;
	
	for(i=0;i<Combo.length;i++)
		if(Combo.options[i].value == StrCmd) return true;
	return false;
}

 function ReFillBuildingCombo(combo)
{
	var room;
			
	if(document.AssetLocation.room.selectedIndex>0)
	{	
		room = document.AssetLocation.room.options[document.AssetLocation.room.selectedIndex].value;
		combo.options.length=1;
		
		for(i=0;i<Buildings.length;i++)
		{

			for(v=0;v<Buildings[i].Rooms.length;v++)
			{
				if(Buildings[i].Rooms[v] == room)
				{	
					combo.options.length+=1;
					combo.options[combo.options.length-1] = new Option(Buildings[i].BuildingName,Buildings[i].BuildingName, false, false);
				}
			} 	
		}
	}
}
 
 function ReFillRoomCombo(combo)
{
	var building;
	var boolFound = false;
	var foundIndex = 0;
			
	if(document.AssetLocation.building.selectedIndex>0)
	{	
		building = document.AssetLocation.building.options[document.AssetLocation.building.selectedIndex].value;
			
		for(i=0;i<Buildings.length;i++)
		{
			if(Buildings[i].BuildingName == building)	
			{
				boolFound = true;
				foundIndex = i;
			}
			if(boolFound) break;
		} 	
 		
		combo.options.length=1;

		for(v=0;v<Buildings[foundIndex].Rooms.length;v++)
		{
			combo.options.length+=1;		
			combo.options[combo.options.length-1] = new Option("Room " + Buildings[foundIndex].Rooms[v],Buildings[foundIndex].Rooms[v], false, false);
		}
	}
}
fillCombo();
 
</script>
