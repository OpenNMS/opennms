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
    String pageSizeString = request.getParameter( "pagesize" );	
    int pageSize = (pageSizeString == null) ? 8 : Integer.parseInt( pageSizeString );

    String offsetString = request.getParameter( "offset" );	
    int offset = (offsetString == null) ? 0 : Integer.parseInt( offsetString ); 
%>
<html>
<head>
  <title>Asset Location Building Configuration | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function editBuilding(name)
    {
        document.AssetLocation.userAction.value="edit";
        document.AssetLocation.building.value=name;
        document.AssetLocation.submit();
    }
    function copyBuilding(name)
    {
        document.AssetLocation.userAction.value="copy";
        document.AssetLocation.building.value=name;
        document.AssetLocation.submit();
    }
    
    
    function addRoom(name)
    {
        document.AssetLocation.userAction.value="addRoom";
        document.AssetLocation.building.value=name;
        document.AssetLocation.submit();
    }
    
    function deleteBuilding(name, nBuild)
    {
        if(nBuild==1)
        {
         alert("Must be at least one building configured. Cannot cancel last building.");
         return;
        }
        if (confirm("Are you sure you want to delete the Building " + name + "?"))
        {
          document.AssetLocation.userAction.value="delete";
          document.AssetLocation.building.value=name;
          document.AssetLocation.submit();
        }
    }
    
    function newBuilding()
    {
        document.AssetLocation.userAction.value="new";
        document.AssetLocation.submit();
    }
    
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/assetLocation/index.jsp" +  "'>Configure Asset Location</a>"; %>
<% String breadcrumb3 = "Building Configuration"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Buildings" />
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
      <h2>Asset Location - Building's List</h2>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
      <form METHOD="POST" NAME="AssetLocation" ACTION="admin/assetLocation/AssetWizard/assetLocationWizard">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_BUILDING%>">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="building" value=""/>

        <tr>
          <td valign="top">
            <h4>Current Defined Buildings</h4>
            <table width="100%" cellspacing="2" cellpadding="2" border="1">
              <tr bgcolor="#999999">
                <td colspan="4">
                    <b>Actions</b>
                </td>
                <td>
                    <b>Building</b>
                </td>
                <td>
                    <b>City</b>
                </td>
                <td>
                    <b>State</b>
                </td>
                <td>
                    <b>Address</b>
                </td>
                <td>
                    <b>Zip Code</b>
                </td>
              </tr>
            <% Map buildingMap = new TreeMap(AssetLocationFactory.getInstance().getBuildings());
               Iterator iterator = buildingMap.keySet().iterator();
               int i = 0;
               int nBuildings = buildingMap.size();
               while(iterator.hasNext()) 
               { 
                 String key = (String)iterator.next();
                 Building curBuild = (Building)buildingMap.get(key);
                 if (  i >= offset && i < offset + pageSize ) {
            %>
                <tr>
                  <td>
                    <input type="button" value="Edit" onclick="javascript:editBuilding('<%=key%>')"/>
                  </td>
                  <td>
                    <input type="button" value="Copy" onclick="javascript:copyBuilding('<%=key%>')"/>
                  </td>
                  <td>
                    <input type="button" value="Delete"  onclick="javascript:deleteBuilding('<%=key%>', <%=nBuildings%>)"/>
                  </td>
                  <td>
                    <input type="button" value="Add/Edit Room"  onclick="javascript:addRoom('<%=key%>')"/>
                  </td>
                  <td bgcolor="green">
                    <%=key%>
                  </td>
                  <td bgcolor="orange">
                    <%=(curBuild.getCity()!=null ? curBuild.getCity() : "Not Present")%>
                  </td>
                  <td bgcolor="orange">
                    <%=(curBuild.getState()!=null ? curBuild.getState() : "Not Present")%>
                  </td>
                  <td bgcolor="orange">
                    <%=(curBuild.getAddress1()!=null ? curBuild.getAddress1() : "Not Present")%>
                  </td>
                  <td bgcolor="orange">
                    <%=(curBuild.getZIP()!=null ? curBuild.getZIP() : "Not Present")%>
                  </td>
                </tr>
            <% }
            i++;
} %>
<tr>
<td colspan="9">
<%
int npage = i / pageSize;
		if (i%pageSize != 0) npage++;
		int currentpage = 1 + offset / pageSize;
		if (offset%pageSize != 0) currentpage++;
		if (currentpage > 1)
		{
%> 
            <a href="admin/assetLocation/AssetWizard/building.jsp?pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage-2)%>">&lt;&lt;&nbsp;Prev&nbsp;</a> 
            <%
		}
		else
		{
%> 
&lt;&lt;&nbsp;Prev&nbsp; 
            <%
		}
		for (int j = 1; j <= npage; j++)
		{
			if (j != currentpage) {
%> 
&nbsp;<a href="admin/assetLocation/AssetWizard/building.jsp?pagesize=<%=pageSize%>&offset=<%=pageSize*(j-1)%>"><%=j%></a> 
            <%
			}
			else
			{
%> 
&nbsp;<%=j%> 
            <%		
			}
		}
		if (currentpage < npage)
		{
%> 
            <a href="admin/assetLocation/AssetWizard/building.jsp?pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage)%>">&nbsp;Next&nbsp;&gt;&gt;</a> 
            <%
		}
		else
		{
%> 
&nbsp;Next&nbsp;&gt;&gt; 
            <%
		}

%>
</td>
</tr>
            </table>
          </td>
        </tr>
        <tr>
          <td><h3><br></h3></td>
        </tr>
        <tr>
          <td> <input type="button" value="Add New Building" onclick="javascript:newBuilding()"/>
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
