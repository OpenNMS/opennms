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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.authenticate.Authentication" %>

<%
    boolean role = request.isUserInRole(Authentication.ADMIN_ROLE);
%> 

<html>
<head>
  <title>About | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='help/index.jsp'>Help</a>"; %>
<% String breadcrumb2 = "About"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="About" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="0"border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h3>OpenNMS Web Console</h3>
      <p>
        <a href="http://www.opennms.org/">OpenNMS</a>, Copyright &copy; 2003 <a href="http://www.sortova.com">Sortova Consulting Group, Inc.</a>
      </p>
        
      <p>
        This program is free software; you can redistribute it and/or
        modify it under the terms of the 
        <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a>
        as published by the <a href="http://www.gnu.org/">Free Software Foundation</a>; either version 2
        of the License, or (at your option) any later version.
      </p>

      <p>        
        This program is distributed in the hope that it will be useful,
        but <strong>without any warranty</strong>; without even the implied warranty of
        <strong>merchantability</strong> or <strong>fitness for a particular purpose</strong>.  See the
        <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a> for more details.
      </p>
        
      <p>
        You should have received a copy of the <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a>
        along with this program; if not, write to the 
        <pre>
          Free Software Foundation, Inc.
          59 Temple Place - Suite 330
          Boston, MA  02111-1307, USA
        </pre>
      </p>

      <table width="100%" cellspacing="0" cellpadding="0"border="0">
        <tr>
          <td>Version:</td>
          <td>@opennms.version.string@</td>
        </tr>
        <tr>
          <td>Server Time:</td>
          <td><%=new java.util.Date()%></td>
        </tr>
        <tr>
          <td>Client Time:</td>
          <td><script language="javascript"> document.write( new Date().toString()) </script></td>
        </tr>
        <tr>
          <td>Java Version</td>
          <td><%=System.getProperty( "java.version" )%> <%=System.getProperty( "java.vendor" )%></td>
        </tr>  
        <tr>
          <td>Java Virtual Machine:</td>
          <td><%=System.getProperty( "java.vm.version" )%> <%=System.getProperty( "java.vm.vendor" )%></td>
        </tr>
        <tr>
          <td>Operating System:</td>
          <td><%=System.getProperty( "os.name" )%> <%=System.getProperty( "os.version" )%> (<%=System.getProperty( "os.arch" )%>)</td>
        </tr>
        <tr>
          <td>Servlet Container:</td>
          <td><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
        </tr>
        <tr>
          <td>User Agent:</td>
          <td><%=request.getHeader( "User-Agent" )%></td>
        </tr>    
      </table>
      
      <p>
        This product includes software developed by the
        <a href="http://www.apache.org/" target="_new">Apache Software Foundation</a>.
      </p>
      <p>
       This product includes code licensed from RSA Security, Inc.
      </p>
      <p>
       Some portions licensed from IBM are available at
       <a href="http://oss.software.ibm.com/icu4j/" target="_new">http://oss.software.ibm.com/icu4j/</a>.
      </p>
      <p>
        <a href="http://www.rrdtool.org/" target="_new"><img src="images/rrdtool.gif" hspace="0" vspace="0"
           border="0" alt="www.rrdtool.org"></a>
      </p>      
    </td>

    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/bookmarkAll.jsp" flush="false" >
  <jsp:param name="adminrole" 
   value="<%= role %>" />
</jsp:include>
<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="about" />
</jsp:include>

</body>
</html>
