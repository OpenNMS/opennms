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
//      http://www.opennms.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.*" %>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<html>
<head>
  <title>Destination Paths | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function editPath() 
    {
        if (document.path.paths.selectedIndex==-1)
        {
            alert("Please select a path to edit.");
        }
        else
        {
            document.path.userAction.value="edit";
            document.path.submit();
        }
    }
    
    function newPath()
    {
        document.path.userAction.value="new";
        document.path.submit();
    }
    
    function deletePath()
    {
        if (document.path.paths.selectedIndex==-1)
        {
            alert("Please select a path to delete.");
        }
        else
        {
            message = "Are you sure you want to delete the path " + document.path.paths.options[document.path.paths.selectedIndex].value + "?";
            if (confirm(message))
            {
                document.path.userAction.value="delete";
                document.path.submit();
            }
        }
    }
    
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/notification/index.jsp'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "Destination Paths"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Destination Paths" />
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
      <h2>Destination Paths</h2>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
      <form METHOD="POST" NAME="path" ACTION="admin/notification/destinationWizard">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="sourcePage" value="destinationPaths.jsp"/>
        <tr>
          <td><h3>Create a new Destination Path or edit an existing path.</h3></td>
        </tr>
        <tr>
          <td>
            <input type="button" value="New Path" onclick="javascript:newPath()"/>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <h4>Existing Paths</h4>
            <select NAME="paths" SIZE="10">
            <% Map pathsMap = new TreeMap(DestinationPathFactory.getInstance().getPaths());
               Iterator iterator = pathsMap.keySet().iterator();
               while(iterator.hasNext()) 
               { 
                 String key = (String)iterator.next();
            %>
                <option VALUE=<%=key%>><%=key%></option>
            <% } %>
            </select>
            <br>
            <input type="button" value="Edit" onclick="javascript:editPath()"/>
            <input type="button" value="Delete" onclick="javascript:deletePath()"/>
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
