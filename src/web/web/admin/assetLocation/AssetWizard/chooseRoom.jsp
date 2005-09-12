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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.*,org.opennms.web.admin.assetLocation.assetWizard.*,org.opennms.netmgt.config.assetLocation.*" %>

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
    String node = (String)user.getAttribute("node");
    Building newBuild = (Building)user.getAttribute("newBuild");
    int nodeId = Integer.parseInt(node);
    String nodeLabel = org.opennms.web.element.NetworkElementFactory.getNodeLabel( nodeId );

%>


<html>
<head>
  <title>Select Room for Asset Location | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

       function next()
    {
        if (document.AssetLocation.room.selectedIndex==-1)
        {
            alert("Please select a Room to associate Asset Location.");
        }
        else
        {
            document.AssetLocation.submit();
        }
    }

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/assetLocation/index.jsp" +  "'>Configure Asset Location</a>"; %>
<% String breadcrumb3 = "Choose Room"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Building's Room Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h2>Asset Location Building - Room's List</h2>
        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
        <tr>
            <td bgcolor="#999999">Building</td>
            <td bgcolor="red">&nbsp;<%=newBuild.getName()%></td>
            <td bgcolor="#999999">Address&nbsp;1</td>
            <td colspan="3">&nbsp;<%=(newBuild.getAddress1()!=null)?newBuild.getAddress1():""%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">City</td>
            <td>&nbsp;<%=(newBuild.getCity()!=null)?newBuild.getCity():""%></td>
            <td bgcolor="#999999">State</td>
            <td>&nbsp;<%=(newBuild.getState()!=null)?newBuild.getState():""%></td>
            <td bgcolor="#999999">ZIP</td>
            <td>&nbsp;<%=(newBuild.getZIP()!=null)?newBuild.getZIP():""%></td>
          </tr>
        </table>


      <table width="100%" cellspacing="2" cellpadding="2" border="0">
      <form METHOD="POST" NAME="AssetLocation" ACTION="admin/assetLocation/AssetWizard/assetLocationWizard">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_CHOOSE_ROOM%>">
      <input type="hidden" name="building" value="<%=newBuild.getName()%>"/>
      <input type="hidden" name="node" value="<%=node%>"/>

        <tr>
          <td valign="top">
            <h4>Choose Rooms </h4>
      <select NAME="room" SIZE="10">

            <% Map roomMap = new TreeMap(AssetLocationFactory.getInstance().getRooms(newBuild));
               Iterator iterator = roomMap.keySet().iterator();
               while(iterator.hasNext()) 
               { 
                 String key = (String)iterator.next();
                 Room curRoom = (Room)roomMap.get(key);
            %>
        <option VALUE="<%=key%>">Room <%=key%></option>
            <% } %>
        </select>
	</td>
       </tr>
        <tr>
       <td>
            <input type="reset"/>
          </td>
        </tr>
        <tr>
          <td>
           <a HREF="javascript:next()">Next &#155;&#155;&#155;</a>
          </td>
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
