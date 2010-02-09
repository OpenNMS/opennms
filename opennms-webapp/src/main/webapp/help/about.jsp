<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Feb 09: Happy new year - jeffg@opennms.org
// 2009 Jan 14: Happy new year, copyright update. - jeffg@opennms.org
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.acegisecurity.Authentication,
		org.opennms.core.resource.Vault
	"
%>

<%
    boolean role = request.isUserInRole(Authentication.ADMIN_ROLE);
%> 

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="About" />
  <jsp:param name="headTitle" value="About" />
  <jsp:param name="breadcrumb" value="<a href='help/index.jsp'>Help</a>" />
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
    <td class="standard"><script language="javascript"> document.write( new Date().toString()) </script></td>
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
<hr />
<h3>License and Copyright</h3>
<div class="boxWrapper">
<p>
  The <a href="http://www.opennms.org/">OpenNMS&reg;</a> software, as
  distributed here, is copyright &copy; 2002-2010
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
    <li>Original code base for OpenNMS version 1.0.0 Copyright &copy; 1999-2001
      <a href="http://www.oculan.com/">Oculan Corporation</a>.</li>
    <li>Original (static) Mapping code Copyright &copy; 2003
      <a href="http://www.nksi.com">Networked Knowledge Systems, Inc.</a>.</li>
    <li>ScriptD code Copyright &copy; 2003
      <a href="http://www.tavve.com">Tavve Software Company</a>.</li>
  </ul>
<p>
  This program is free software; you can redistribute it and/or
  modify it under the terms of the 
  <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a>
  as published by the
  <a href="http://www.gnu.org/">Free Software Foundation</a>; either version 2
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
      
      	   <td>
		<p>
        	Powered By Jetty<br/>
		<a href="http://jetty.mortbay.org" target="_new"><img src="images/powered_by_jetty.gif" hspace="0" vspace="0"
      		   border="0" alt="jetty.mortbay.org"></a>.
      		</p>
	   </td>
           <td>
		<p>
		Support for Tobi Oetiker's RRDTool<br/>
        	<a href="http://www.rrdtool.org/" target="_new"><img src="images/rrdtool-logo-dark.png" hspace="0" vspace="0" width="121" height="48"
           	border="0" alt="www.rrdtool.org"></a>
      		</p>      
	   </td>
           <td>
		<p>
		PostgreSQL Powered<br/>
        	<a href="http://www.postgresql.org/" target="_new"><img src="images/pg-power_95x51_4.gif" hspace="0" vspace="0" width="95" height="51"
           	border="0" alt="www.postgresql.org"></a>
      		</p>      
	   </td>
      	   <td>
		<p>
        	Support for Tomcat<br/>
		<a href="http://tomcat.apache.org/" target="_new"><img src="images/tomcat.gif" hspace="0" vspace="0" width="77" height="80"
      		   border="0" alt="tomcat.apache.org"></a>.
      		</p>
	   </td>
	   </tr>
	</table>
 
                                   

<jsp:include page="/includes/bookmarkAll.jsp" flush="false" >
  <jsp:param name="adminrole" 
   value="<%= role %>" />
</jsp:include>

<jsp:include page="/includes/footer.jsp" flush="false"/>
