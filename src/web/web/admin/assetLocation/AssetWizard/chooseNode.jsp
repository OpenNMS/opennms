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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.element.*,org.opennms.web.admin.assetLocation.assetWizard.*,org.opennms.netmgt.config.*,org.opennms.netmgt.config.assetLocation.*" %>

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
    Node[] nodes = null;
        nodes = NetworkElementFactory.getAllNodes();

%>

<html>
<head>
  <title>Select Node And Building for Asset Location | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<script language="Javascript" type="text/javascript" >

    function next()
    {
        if (document.AssetLocation.node.selectedIndex==-1)
        {
            alert("Please select a node to associate Asset Location.");
        }
        else if (document.AssetLocation.building.selectedIndex==-1) 
        {
	    alert("Please select a Building to associate Asset Location.");
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
<% String breadcrumb3 = "Choose Node"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Node and Building" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <h3>Adding Node to Asset Location</h3></td>
    <td> &nbsp; </td>
  </tr>

  <tr>
    <td> &nbsp; </td>
	<td>
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
      <form METHOD="POST" NAME="AssetLocation" ACTION="admin/assetLocation/AssetWizard/assetLocationWizard">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_CHOOSE_NODE%>">
  <tr>
    <td> <h4>Select a Node </h4> </td>
    <td> <h4>Select a Building</h4> </td>
   </tr> 
  <tr>
  <% if( nodes.length > 0 ) { %>
    <td>
      <select NAME="node" SIZE="10">
      <% for( int i=0; i < nodes.length; i++ ) { %>
        <option VALUE="<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></option>
      <% } %>
      </select>
    </td>
  <% } else { %>
    <td>
      None found.
    </td>    
  <% } %>
	<td>
      <select NAME="building" SIZE="10">

            <% Map buildingMap = new TreeMap(AssetLocationFactory.getInstance().getBuildings());
               Iterator iterator = buildingMap.keySet().iterator();
               while(iterator.hasNext())
               {
                 String key = (String)iterator.next();
                 Building curBuild = (Building)buildingMap.get(key);
            %>
        <option VALUE="<%=key%>"><%=key%></option>
            <% } %>
        </select>
	</td>
        </tr>

        <tr>
          <td colspan="2">
            <input type="reset"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
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
