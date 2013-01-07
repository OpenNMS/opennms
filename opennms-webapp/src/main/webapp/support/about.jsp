<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.springframework.security.Authentication,
		org.opennms.core.resource.Vault,
		org.opennms.core.utils.DBUtils,
		java.sql.Connection
	"
%>

<%
    boolean role = request.isUserInRole(Authentication.ROLE_ADMIN);
    
    final DBUtils d = new DBUtils();
    String dbName;
    String dbVersion;
    try {
      Connection conn = Vault.getDbConnection();
      d.watch(conn);
      dbName = Vault.getDbConnection().getMetaData().getDatabaseProductName();
      dbVersion = Vault.getDbConnection().getMetaData().getDatabaseProductVersion();
   	} catch (Exception e) {
   	  dbName = "Unknown";
      dbVersion = "Unknown";
   	} finally {
   	  d.cleanUp();
   	}
%> 

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="About" />
  <jsp:param name="headTitle" value="About" />
  <jsp:param name="breadcrumb" value="<a href='support/index.htm'>Support</a>" />
  <jsp:param name="breadcrumb" value="About" />
</jsp:include>

<h3>OpenNMS Web Console</h3>

<table class="standard">
  <tr>
    <td class="standardheader">Version:</td>
    <td class="standard"><%=Vault.getProperty("version.display")%></td>
  </tr>

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
  <tr>
    <td class="standardheader">Database Type:</td>
    <td class="standard"><%=dbName%></td>
  </tr>
  <tr>
    <td class="standardheader">Database Version:</td>
    <td class="standard"><%=dbVersion%></td>
  </tr>
</table>
<hr />
<h3>License and Copyright</h3>
<div class="boxWrapper">
<p>
  The <a href="http://www.opennms.org/">OpenNMS&reg;</a> software, as
  distributed here, is copyright &copy; 2002-2013
  <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>.
  <a href="http://www.opennms.org/">OpenNMS&reg;</a> is a registered
  trademark of <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
</p>
<p>
  OpenNMS is a derivative work, containing both original code, included
  code and modified code that was published under the GNU General Public
  License. Please see the source for detailed copyright notices, but some
  notable copyright owners are listed below:
</p>
<ul>
    <li>Original (static) Mapping code Copyright &copy; 2003
      <a href="http://www.nksi.com">Networked Knowledge Systems, Inc.</a>.</li>
  </ul>
<p>
  This program is free software; you can redistribute it and/or
  modify it under the terms of the 
  <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a>
  as published by the
  <a href="http://www.gnu.org/">Free Software Foundation</a>; either version 3
  of the License, or (at your option) any later version.
</p>

<p>        
  This program is distributed in the hope that it will be useful,
  but <strong>without any warranty</strong>; without even the implied
  warranty of <strong>merchantability</strong> or <strong>fitness for
  a particular purpose</strong>.  See the
  <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a>
  for more details.
</p>
        
<p>
  You should have received a copy of the
  <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a>
  along with this program; if not, write to the</p>
    <p>Free Software Foundation, Inc.<br/>
    59 Temple Place - Suite 330<br/>
    Boston, MA  02111-1307, USA</p>
</div>
<hr />
<h3>OSI Certified Open Source Software</h3>
<div class="boxWrapper">
<a target="_new" href="http://www.opensource.org/"><img src="images/osi-certified.png" style="float: left;" alt="OSI Certified"/></a>
<p>This software is OSI Certified Open Source Software.<br/>
  OSI Certified is a certification mark of the
  <a href="http://www.opensource.org/">Open Source Initiative</a>.
</p>
<div style="clear:both;"></div>
</div>
<hr />
<h3>Supporting Applications</h3>

	<table>
	   <tr>
      
      	   <td style="border-right: none;">
		<p align="center">
        	Powered By Jetty<br/>
		<a href="http://jetty.mortbay.org" target="_new"><img src="images/powered_by_jetty.gif" hspace="0" vspace="0"
      		   border="0" alt="jetty.mortbay.org" align="center"></a>.
      		</p>
	   </td>
           <td style="border-left: none; border-right: none;">
		<p align="center">
		Support for Tobi Oetiker's RRDTool<br/>
        	<a href="http://www.rrdtool.org/" target="_new"><img src="images/rrdtool-logo-dark.png" hspace="0" vspace="0" width="121" height="48"
           	border="0" alt="www.rrdtool.org" align="center"></a>
      		</p>      
	   </td>
           <td style="border-left: none;">
		<p align="center">
		PostgreSQL Powered<br/>
        	<a href="http://www.postgresql.org/" target="_new"><img src="images/pg-power_95x51_4.gif" hspace="0" vspace="0" width="95" height="51"
           	border="0" alt="www.postgresql.org" align="center"></a>
      		</p>      
	   </td>
	   </tr>
	</table>
 
                                   

<jsp:include page="/includes/bookmarkAll.jsp" flush="false" >
  <jsp:param name="adminrole" 
   value="<%= role %>" />
</jsp:include>

<jsp:include page="/includes/footer.jsp" flush="false"/>
