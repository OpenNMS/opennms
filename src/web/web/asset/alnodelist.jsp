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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.asset.*,org.opennms.netmgt.config.*" %>

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
    String building = request.getParameter( "building" );
    String room = request.getParameter( "room" );

    if( building == null ) {
        throw new org.opennms.web.MissingParameterException( "building", new String[] {"building","room"} );
    }

    if( room == null ) {
        throw new org.opennms.web.MissingParameterException( "room", new String[] {"building","room"} );
    }

    if( room.equals("0")) {
		room = "";
    }

    if( building.equals("0")) {
		building = "";
    }

	Map nodeMap = new TreeMap(AssetLocationFactory.getInstance().getAssetNode(building,room));%>

<html>
<head>
  <title>Asset List | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='asset/index.jsp' >Assets</a>"; %>
<% String breadcrumb2 = "Asset List"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Asset List" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;  </td>
    <td> <h3>Assets</h3>
    <td>&nbsp;  </td>
  </tr>

  <tr>
    <td>&nbsp;  </td>
    <td> 

	<table cellspacing="0" cellpadding="2" border="0">

	<tr>
	<td width="50%" valign="top">&nbsp;</td>
	<td width="50%" valign="top">
<% if (building.equals("") && room.equals("")){ %>
	<p><b>No Search Constraint</b><br><br><br>
<% } else { %>
	<p>Current Search Constraint:<br>
	<% if (!building.equals("")){ %>
	<ul><li>Building is <%=building%></ul>
	<% } %>
	<% if (!room.equals("")){ %>
	<ul><li>Room is <%=room%></ul>
	<% }
} %>

	</td>
	</tr>
	</table>
    <td>&nbsp;  </td>
  </tr>

  <tr>
    <td> &nbsp; </td>

  <% int i = 0;
  if( nodeMap.size()> 0 ) { %>
    <td valign="top" colspan="2">
      <table width="100%" cellspacing="0" cellpadding="2" border="1"> 
        <tr bgcolor="#999999">
          <td width="15%" align="center"><b>Asset</b></td>
          <td align="center"><b>Asset Number</b></td>
          <td align="center"><b>Category</b></td>
          <td align="center"><b>Department</b></td>
          <td align="center"><b>Building</b></td>
          <td align="center"><b>Room</b></td>
          <td align="center"><b>Vendor</b></td>
        </tr>

      <% Iterator iterator = nodeMap.keySet().iterator();
               while(iterator.hasNext()) 
               { 
                 String key = (String)iterator.next();
                 Asset curAsset = (Asset)nodeMap.get(key);
%>
        <tr bgcolor="<%=(i%2 == 0) ? "white" : "#cccccc"%>">
          <td><a href="asset/detail.jsp?node=<%=curAsset.getNodeId()%>"><%=org.opennms.web.element.NetworkElementFactory.getNodeLabel( curAsset.getNodeId() )%></a></td>
          <td><%=curAsset.getAssetNumber()%>&nbsp;</td>
          <td><%=curAsset.getCategory()%>&nbsp;</td>
          <td><%=curAsset.getDepartment()%>&nbsp;</td>
          <td><%=curAsset.getBuilding()%>&nbsp;</td>
          <td><%=curAsset.getRoom()%>&nbsp;</td>
          <td><%=curAsset.getVendor()%>&nbsp;</td>
        </tr>
      <% i++;
      }
       %>
      </table>
   </td>
  <% } else { %>
    <td>
      None found.
    </td>    
  <% } %>
    
    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
