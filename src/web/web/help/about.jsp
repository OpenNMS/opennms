<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Dec 01: Moved items on this page around.
// 2003 Mar 18: Another copyright update.
// 2003 Mar 03: Updated copyright info.
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
//      http://www.blast.com///

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

    <h3>License and Copyright</h3>
      
      <p>
        The <a href="http://www.opennms.org/">OpenNMS&reg;</a> software, as distributed here, is copyright &copy; 2002-2004 <a href="http://www.blast.com">Blast Internet Services, Inc.</a> <a href="http://www.opennms.org/">OpenNMS&reg;</a> is a registered trademark of <a href="http://www.blast.com">Blast Internet Services, Inc.</a>
      </p>

      <p>
      OpenNMS is a derivative work, containing both original code, included code and modified code that was published under the GNU General Public License. Please see the source for detailed copyright notices, but some notable copyright owners are listed below:
      </p>
	<table>
	<tr>
	<td>
	Original code base for OpenNMS version 1.0.0 &copy; 1999-2001 <a href="http://www.oculan.com">Oculan Corporation</a>.
	</td>
	</tr>
	<tr>
	<td>
	Mapping code Copyright &copy; 2003 <a href="http://www.nksi.com">Networked Knowledge Systems, Inc.</a>
	</td>
	</tr>
        <tr>
        <td>
        ScriptD code Copyright &copy; 2003 <a href="http://www.tavve.com">Tavve Software Company</a>.
        </td>
        </tr>
	</table>

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

      <h3>Supporting Applications</h3>

	<table>
	   <tr>
      
      	   <td>
		<p>
        	Powered By Tomcat<br>
		<a href="http://jakarta.apache.org/tomcat/" target="_new"><img src="images/tomcat.gif" hspace="0" vspace="0" 
      		   border="0" alt="jakarta.apache.org"></a>.
      		</p>
	   </td>
           <td>
		<p>
        	<a href="http://www.rrdtool.org/" target="_new"><img src="images/rrdtool.gif" hspace="0" vspace="0"
           	border="0" alt="www.rrdtool.org"></a>
      		</p>      
	   </td>
           <td>
		<p>
        	<a href="http://www.postgresql.org/" target="_new"><img src="images/pg-power.jpg" hspace="0" vspace="0"
           	border="0" alt="www.postgresql.org"></a>
      		</p>      
	   </td>
	   </tr>
	</table>
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
