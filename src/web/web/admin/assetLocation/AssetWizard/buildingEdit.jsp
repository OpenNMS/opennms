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
    Building newBuild = (Building)user.getAttribute("newBuild");
    String Action = (String) user.getAttribute("Action");
%>

<html>
<head>
  <title>Edit Building Properties  | Admin | OpenNMS Web Console</title>
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
<% String breadcrumb2 = "<a href='admin/assetLocation/index.jsp'>Configure Asset Location</a>"; %>
<% String breadcrumb3 = "<a href='admin/assetLocation/AssetWizard/building.jsp" +  "'>Buildings</a>"; %>
<% String breadcrumb4 = Action + " Building"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Building Info" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h3>Set Building parameters value</h3>
      <form METHOD="POST" NAME="info" ACTION="admin/assetLocation/AssetWizard/assetLocationWizard">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_EDIT_BUILDING%>"/>
      <input type="hidden" name="userAction" value="<%=Action%>"/>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
	

          <tr>
            <td>Building's Name&nbsp;</td>
            <td colspan="5"><input type="text" value="<%=(newBuild.getName()!=null ? newBuild.getName() : "")%>" name="name" size="20" maxlength="64"/></td>
          </tr>
          <tr>
            <td>Address&nbsp;1</td>
            <td colspan="5"><input type="text" value="<%=(newBuild.getAddress1()!=null ? newBuild.getAddress1() : "")%>" name="address1" size="100" maxlength="256"/></td>
          </tr>
          <tr>
            <td>Address&nbsp;2</td>
            <td colspan="5"><input type="text" value="<%=(newBuild.getAddress2()!=null ? newBuild.getAddress2() : "")%>" name="address2" size="100" maxlength="256"/></td>
          </tr>
          <tr>
            <td>City</td>
            <td><input type="text" value="<%=(newBuild.getCity()!=null ? newBuild.getCity() : "")%>" name="city" size="20" maxlength="64"/></td>
            <td>State</td>
            <td><input type="text" value="<%=(newBuild.getState()!=null ? newBuild.getState() : "")%>" name="state" size="20" maxlength="64"/></td>
            <td>ZIP</td>
            <td><input type="text" value="<%=(newBuild.getZIP()!=null ? newBuild.getZIP() : "")%>" name="zip" size="20" maxlength="64"/></td>
          </tr>

        <tr>
          <td colspan="6">
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
