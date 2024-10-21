<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.api.Authentication,
		org.opennms.core.db.DataSourceFactory,
		org.opennms.core.resource.Vault,
		org.opennms.core.utils.TimeSeries,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.core.utils.DBUtils,
		org.opennms.core.rpc.common.RpcStrategy,
		org.opennms.core.ipc.sink.common.SinkStrategy,
		java.sql.Connection
	"
%>

<%
    boolean role = request.isUserInRole(Authentication.ROLE_ADMIN);

    final DBUtils d = new DBUtils();
    String dbName;
    String dbVersion;
    try {
      Connection conn = DataSourceFactory.getInstance().getConnection();
      d.watch(conn);
      dbName = conn.getMetaData().getDatabaseProductName();
      dbVersion = conn.getMetaData().getDatabaseProductVersion();
   	} catch (Exception e) {
   	  dbName = "Unknown";
      dbVersion = "Unknown";
   	} finally {
   	  d.cleanUp();
   	}
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("About")
          .breadcrumb("About")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

  <div class="card">
    <div class="card-header">
      <span>Version Details</span>
    </div>
<table class="table table-sm">
  <tr>
    <th>Version:</th>
    <td><a href="https://docs.opennms.com/meridian/<%="2024"%>/releasenotes/whatsnew.html" target="_blank" title="Release Notes"><%=Vault.getProperty("version.display")%></a></td>
  </tr>

  <tr>
    <th>Server Time:</th>
    <td><%=new java.util.Date()%></td>
  </tr>
  <tr>
    <th>Client Time:</th>
    <td><script type="text/javascript"> document.write( new Date().toString()) </script></td>
  </tr>
  <tr>
    <th>Java Version:</th>
    <td><%=System.getProperty( "java.version" )%> (<%=System.getProperty( "java.vendor" )%>)</td>
  </tr>
  <tr>
    <th>Java Runtime:</th>
    <td><%=System.getProperty( "java.runtime.name" )%> (<%=System.getProperty( "java.runtime.version" )%>)</td>
  </tr>
  <tr>
    <th>Java Specification:</th>
    <td><%=System.getProperty( "java.specification.name" )%> (<%=System.getProperty( "java.specification.vendor" )%>, <%=System.getProperty( "java.specification.version" )%>)</td>
  </tr>
  <tr>
    <th>Java Virtual Machine:</th>
    <td><%=System.getProperty( "java.vm.name" )%> (<%=System.getProperty( "java.vm.vendor" )%>, <%=System.getProperty( "java.vm.version" )%>)</td>
    <%-- java.vm.info doesn't appear to be part of the standard Java system properties--%>
    <%-- <%=System.getProperty( "java.vm.info" )%> --%>
  </tr>
  <tr>
    <th>Java Virtual Machine Specification:</th>
    <td><%=System.getProperty( "java.vm.specification.name" )%> (<%=System.getProperty( "java.vm.specification.vendor" )%>, <%=System.getProperty( "java.vm.specification.version" )%>)</td>
  </tr>
  <tr>
    <th>Operating System:</th>
    <td><%=System.getProperty( "os.name" )%> <%=System.getProperty( "os.version" )%> (<%=System.getProperty( "os.arch" )%>)</td>
  </tr>
  <tr>
    <th>OSGi Container:</th>
    <td>Apache Karaf <%=System.getProperty( "karaf.version" )%></td>
  </tr>
  <tr>
    <th>Servlet Container:</th>
    <td><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
  </tr>
  <tr>
    <th>User Agent:</th>
    <td><%=WebSecurityUtils.sanitizeString(request.getHeader( "User-Agent" ), false)%></td>
  </tr>
  <tr>
    <th>Database Type:</th>
    <td><%=dbName%></td>
  </tr>
  <tr>
    <th>Database Version:</th>
    <td><%=dbVersion%></td>
  </tr>
  <tr>
    <th>Time-Series Strategy:</th>
    <td><%=TimeSeries.getTimeseriesStrategy().getDescr()%></td>
  </tr>
  <tr>
    <th>IPC Strategy:</th>
    <td>Sink: <%=SinkStrategy.getSinkStrategy().getName()%>; RPC: <%=RpcStrategy.getRpcStrategy().getName()%></td>
  </tr>
</table>
</div>

<div class="card">
  <div class="card-header">
    <span>License and Copyright</span>
  </div>
  <div class="card-body">
  <p>
    <a href="http://www.opennms.org/">OpenNMS&reg;</a> is a registered
    trademark, and Horizon&trade;, Meridian&trade;, and Compass&trade; are
    trademarks, of <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
    Horizon&trade; software by OpenNMS&reg; and Meridian&trade; software by OpenNMS&reg;, as
    distributed here, are copyright &copy; 2002-2023
    <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a>
  </p>
  <p>
    OpenNMS is a derivative work, containing both original code, included
    code and modified code that was published under the GNU Affero General Public
    License. Please see the source for detailed copyright notices.
  </p>
  <p>
    The source code for this release can be downloaded
    <a href="source/opennms-<%=Vault.getProperty("version.display")%>-source.tar.gz">here</a>.
  </p>
  <p>
    This program is free software; you can redistribute it and/or
    modify it under the terms of the
    <a href="http://www.gnu.org/licenses/agpl.html">GNU Affero General Public License</a>
    as published by the
    <a href="http://www.gnu.org/">Free Software Foundation</a>; either version 3
    of the License, or (at your option) any later version.
  </p>
  <p>
    This program is distributed in the hope that it will be useful,
    but <strong>without any warranty</strong>; without even the implied
    warranty of <strong>merchantability</strong> or <strong>fitness for
    a particular purpose</strong>.  See the
    <a href="http://www.gnu.org/licenses/agpl.html">GNU Affero General Public License</a>
    for more details.
  </p>

  <p>
    You should have received a copy of the
    <a href="http://www.gnu.org/licenses/agpl.html">GNU Affero General Public License</a>
    along with this program; if not, write to the
  </p>
  <p>
    Free Software Foundation, Inc.<br/>
    59 Temple Place - Suite 330<br/>
    Boston, MA  02111-1307, USA
  </p>
  </div>
</div>

<div class="card">
  <div class="card-header">
    <span>OSI Certified Open Source Software</span>
  </div>
  <div class="card-body">

<a target="_new" href="http://www.opensource.org/"><img src="images/osi-certified.png" style="float: left;" alt="OSI Certified"/></a>
<p>This software is OSI Certified Open Source Software.<br/>
  OSI Certified is a certification mark of the
  <a href="http://www.opensource.org/">Open Source Initiative</a>.
</p>
<div style="clear:both;"></div>
</div>
</div>

<div class="card">
	<div class="card-header">
		<span>Supporting Applications and Frameworks</span>
	</div>
	<div class="card-body">
        <div class="row">
		<div class="col-md-3">
			<p align="center">
				Powered By Jetty<br />
				<a href="http://www.eclipse.org/jetty/" target="_new">
					<img src="images/powered_by_jetty.png" style="border:0;" alt="eclipse.org/jetty/">
				</a>
			</p>
		</div>

		<div class="col-md-3">
			<p align="center">
				Support for Tobi Oetiker's RRDTool<br />
				<a href="http://www.rrdtool.org/" target="_new">
					<img src="images/rrdtool-logo-dark.png" style="border:0;padding:1em;" alt="www.rrdtool.org">
				</a>
			</p>
		</div>

		<div class="col-md-3">
			<p align="center">
				PostgreSQL Powered<br /> <a href="http://www.postgresql.org/"
					target="_new"><img src="images/pg-power_95x51_4.gif" style="border:0;padding:1em;" alt="www.postgresql.org"></a>
			</p>
		</div>

		<div class="col-md-3">
			<p align="center">
				Powered by Hibernate ORM<br />
				<a href="http://hibernate.org/" target="_new">
					<img src="images/hibernate_logo.gif" style="border:0;" alt="hibernate.org">
				</a>
			</p>
		</div>
        </div>
	</div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
