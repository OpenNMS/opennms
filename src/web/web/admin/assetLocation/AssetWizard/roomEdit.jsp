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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.admin.assetLocation.assetWizard.*,org.opennms.netmgt.config.assetLocation.*" %>

<%
    HttpSession user = request.getSession(true);
    Room newRoom = (Room)user.getAttribute("newRoom");
    String building = (String)user.getAttribute("building");
    String Action = (String)user.getAttribute("Action");
%>

<html>
<head>
  <title>Edit Room Properties  | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script LANGUAGE="JAVASCRIPT" >
  
    function trimString(str) 
    {
        while (str.charAt(0)==" ")
        {
          str = str.substring(1);
        }
        while (str.charAt(str.length - 1)==" ")
        {
          str = str.substring(0, str.length - 1);
        }
        return str;
    }
    
    function finish()
    {
        trimmedName = trimString(document.info.name.value);
        if (trimmedName=="")
        {
            alert("Please give this Building a name.");
        }
        else
        {
            document.info.submit();
        }
    }
  
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/assetLocation/index.jsp" +  "'>Configure Asset Location</a>"; %>
<% String breadcrumb3 = "<a href='admin/assetLocation/AssetWizard/building.jsp" +  "'>Buildings</a>"; %>
<% String breadcrumb4 = "<a href='admin/assetLocation/AssetWizard/room.jsp" +  "'>Rooms</a>"; %>
<% String breadcrumb5 = Action + " Room"; %>
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
      <h2>Asset Location Building - <%=Action%> Room</h2>
                <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
                <tr>
            <td bgcolor="#999999">Building</td>
            <td bgcolor="red">&nbsp;<%=building%></td>
          </tr>
        </table>

      <h3>Set Room parameters value</h3>
      <form METHOD="POST" NAME="info" ACTION="admin/assetLocation/AssetWizard/assetLocationWizard">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_EDIT_ROOM%>"/>
      <input type="hidden" name="userAction" value="<%=Action%>"/>
      <input type="hidden" name="building" value="<%=building%>"/>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
	
          <tr>
            <td>Room&nbsp;</td>
            <td colspan="5"><input type="text" value="<%=(newRoom.getRoomID()!=null ? newRoom.getRoomID() : "")%>" name="name" size="20" maxlength="64"/></td>
          </tr>

          <tr>
            <td>Floor&nbsp;</td>
            <td colspan="5"><input type="text" value="<%=(newRoom.getFloor()!=null ? newRoom.getFloor() : "")%>" name="floor" size="20" maxlength="64"/></td>
          </tr>

        <tr>
          <td colspan="2">
            <a HREF="javascript:finish()">Finish</a>
          </td>
        </tr>
      </table>
      </form>
      
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
