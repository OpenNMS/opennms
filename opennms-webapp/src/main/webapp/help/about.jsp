<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


--%>

<%@page
	language="java"
	contentType="text/html"
	session="true"
	import="
		org.opennms.web.springframework.security.Authentication,
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
		The <a href="http://www.opennms.org/">OpenNMS&reg;</a> software, as distributed here, is copyright &copy; 2002-2009
		<a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>.  All rights reserved.
	</p>
	<p>
	  <a href="http://www.opennms.org/">OpenNMS&reg;</a> is a registered trademark of <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a>
	</p>
	<p>
		This program is free software; you can redistribute it and/or modify it under the terms of the 
		<a href="http://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GNU General Public License</a> as published by the
		<a href="http://www.gnu.org/">Free Software Foundation</a>; either version 2 of the License, or (at your option) any later version.
	</p>
	
	<p>        
		This program is distributed in the hope that it will be useful, but <strong>without any warranty</strong>; without even the implied
		warranty of <strong>merchantability</strong> or <strong>fitness for a particular purpose</strong>.  See the
		<a href="http://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GNU General Public License</a> for more details.
	</p>
		      
	<p>
		You should have received a copy of the <a href="http://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GNU General Public License</a>
		along with this program; if not, write to the Free Software Foundation, Inc.:
	</p>
	<p>
		Free Software Foundation, Inc.<br />
		51 Franklin Street<br />
		5th Floor<br />
		Boston, MA 02110-1301<br />
		USA
	</p>
	<p>
		For more information contact:
	</p>
	<p>
		OpenNMS Licensing &lt;<a href="mailto:license@opennms.org">license@opennms.org</a>&gt;<br />
		<a href="http://www.opennms.org/">http://www.opennms.org/</a><br />
		<a href="http://www.opennms.com/">http://www.opennms.com/</a>
	</p>
</div>

<hr />

<h3>OSI Certified Open Source Software</h3>

<div class="boxWrapper">
	<a target="_new" href="http://www.opensource.org/"><img src="images/osi-certified.png" style="float: left;" alt="OSI Certified"/></a>
	<p>
		This software is OSI Certified Open Source Software.<br/>
		OSI Certified is a certification mark of the <a href="http://www.opensource.org/">Open Source Initiative</a>.
	</p>
	<div style="clear:both;" />
</div>

<hr />

<h3>Supporting Applications</h3>

<dl>
	<dt>Jetty</dt>
		<dd>OpenNMS uses the <a href="http://jetty.mortbay.org/">Jetty servlet container</a>. (<a href="http://www.apache.org/licenses/LICENSE-2.0.html">APLv2</a>)</dd>

	<dt>Tomcat</dt>
		<dd>OpenNMS can be configured to use the <a href="http://tomcat.apache.org/">Apache Tomcat servlet container</a>. (<a href="http://www.apache.org/licenses/LICENSE-2.0.html">APLv2</a>)</dd>

	<dt>RRDTool</dt>
		<dd>OpenNMS can be configured to use Tobi Oetiker's <a href="http://www.rrdtool.org/">RRDTool</a> for collected time-sensitive data. (<a href="http://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GPLv2</a>)</dd>

	<dt>PostgreSQL</dt>
		<dd>OpenNMS uses the <a href="http://www.postgresql.org/">PostgreSQL database</a> for data storage.</dd>

	<dt>carto:net SVG ECMAScript</dt>
		<dd>The OpenNMS network topology map feature uses <a href="http://www.carto.net/papers/svg/gui/">carto:net's SVG GUI ECMAScript</a>. (<a href="http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html">LGPLv2</a>)</dd>

</dl>

<jsp:include page="/includes/bookmarkAll.jsp" flush="false" >
  <jsp:param name="adminrole" 
   value="<%= role %>" />
</jsp:include>

<jsp:include page="/includes/footer.jsp" flush="false"/>
