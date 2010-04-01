<%--

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
// 2003 Oct 27: created
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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.springframework.security.Authentication,
		org.opennms.core.resource.Vault
	"
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="OpenNMS System Configuration" />
  <jsp:param name="headTitle" value="System Configuration" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="System Configuration" />
</jsp:include>

<script language="JavaScript">
    
        function cancel()
        {
                document.snmpConfigForm.action="admin/index.jsp";
                document.snmpConfigForm.submit();
        }
</script>


  <div class="TwoColLeft">
  <h3>OpenNMS Configuration</h3>
  	<div class="boxWrapper">
 	<table class="standard">
 	  <tr>
    <td class="standardheader">OpenNMS Version:</td>
    <td class="standard"><%=Vault.getProperty("version.display")%></td>
  </tr>
 	<tr>
    <td class="standardheader">Home Directory:</td>
    <td class="standard"><%=Vault.getProperty("opennms.home")%></td>
  </tr>
  <tr>
    <td class="standardheader">RRD store by Group:</td>
    <td class="standard"><%=Vault.getProperty("org.opennms.rrd.storeByGroup")%></td>
  </tr>
  <tr>
    <td class="standardheader">Web-Application Logfiles:</td>
    <td class="standard"><%=Vault.getProperty("opennms.webapplogs.dir")%></td>
  </tr>
  <tr>
    <td class="standardheader">Reports directory:</td>
    <td class="standard"><%=Vault.getProperty("opennms.report.dir")%></td>
  </tr>
  <tr>
    <td class="standardheader">Jetty http host:</td>
    <td class="standard"><%=Vault.getProperty("org.opennms.netmgt.jetty.host")%></td>
  </tr>
  <tr>
    <td class="standardheader">Jetty http port:</td>
    <td class="standard"><%=Vault.getProperty("org.opennms.netmgt.jetty.port")%></td>
  </tr>
   <tr>
    <td class="standardheader">Jetty https host:</td>
    <td class="standard"><%=Vault.getProperty("org.opennms.netmgt.jetty.https-host")%></td>
  </tr>
  <tr>
    <td class="standardheader">Jetty https port:</td>
    <td class="standard"><%=Vault.getProperty("org.opennms.netmgt.jetty.https-port")%></td>
  </tr>

</table>
</div>
</div>
<div class="TwoColRight">
  <h3>System Configuration</h3>
  	<div class="boxWrapper">
 	<table class="standard">
  <tr>
    <td class="standardheader">Server Time:</td>
    <td class="standard"><%=new java.util.Date()%></td>
  </tr>
  <tr>
    <td class="standardheader">Client Time:</td>
    <td class="standard"><script type="text/javascript"> document.write( new Date().toString()) </script></td>
  </tr>
  <tr>
    <td class="standardheader">Java Version:</td>
    <td class="standard"><%=System.getProperty( "java.version" )%> <%=System.getProperty( "java.vendor" )%></td>
  </tr>  
  <tr>
    <td class="standardheader">Java Virtual Machine:</td>
    <td class="standard"><%=System.getProperty( "java.vm.version" )%> <%=System.getProperty( "java.vm.vendor" )%></td>
  </tr>
  <tr>
    <td class="standardheader">Operating System:</td>
    <td class="standard"><%=System.getProperty( "os.name" )%> <%=System.getProperty( "os.version" )%> (<%=System.getProperty( "os.arch" )%>)</td>
  </tr>
  <tr>
    <td class="standardheader">Servlet Container:</td>
    <td class="standard"><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
  </tr>
  <tr>
    <td class="standardheader">User Agent:</td>
    <td class="standard"><%=request.getHeader( "User-Agent" )%></td>
  </tr>    
</table>
</div>
  </div>

  <div class="TwoColRAdmin">
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />
