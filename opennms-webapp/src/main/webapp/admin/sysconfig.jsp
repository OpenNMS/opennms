<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
	import="
		org.opennms.core.resource.Vault
	"
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="OpenNMS System Configuration" />
  <jsp:param name="headTitle" value="System Configuration" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="System Configuration" />
</jsp:include>

<script type="text/javascript">
        function cancel()
        {
                document.snmpConfigForm.action="admin/index.jsp";
                document.snmpConfigForm.submit();
        }
</script>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">OpenNMS Configuration</h3>
      </div>
      <table class="table">
        <tr>
          <th>OpenNMS Version:</th>
          <td><%=Vault.getProperty("version.display")%></td>
        </tr>
        <tr>
          <th>Home Directory:</th>
          <td><%=Vault.getProperty("opennms.home")%></td>
        </tr>
        <tr>
          <th>RRD store by group enabled?</th>
          <td><%=Vault.getProperty("org.opennms.rrd.storeByGroup")%></td>
        </tr>
        <tr>
          <th>RRD store by foreign source enabled?</th>
          <td><%=Vault.getProperty("org.opennms.rrd.storeByForeignSource")%></td>
        </tr>
        <tr>
          <th>Reports directory:</th>
          <td><%=Vault.getProperty("opennms.report.dir")%></td>
        </tr>
        <tr>
          <th>Jetty HTTP host:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.host")%></td>
        </tr>
        <tr>
          <th>Jetty HTTP port:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.port")%></td>
        </tr>
         <tr>
          <th>Jetty HTTPS host:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.https-host")%></td>
        </tr>
        <tr>
          <th>Jetty HTTPS port:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.https-port")%></td>
        </tr>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">System Configuration</h3>
      </div>
      <table class="table">
        <tr>
          <th>Server&nbsp;Time:</th>
          <td><%=new java.util.Date()%></td>
        </tr>
        <tr>
          <th>Client&nbsp;Time:</th>
          <td><script type="text/javascript"> document.write( new Date().toString()) </script></td>
        </tr>
        <tr>
          <th>Java&nbsp;Version:</th>
          <td><%=System.getProperty( "java.version" )%> <%=System.getProperty( "java.vendor" )%></td>
        </tr>
        <tr>
          <th>Java&nbsp;Virtual&nbsp;Machine:</th>
          <td><%=System.getProperty( "java.vm.version" )%> <%=System.getProperty( "java.vm.vendor" )%></td>
        </tr>
        <tr>
          <th>Operating&nbsp;System:</th>
          <td><%=System.getProperty( "os.name" )%> <%=System.getProperty( "os.version" )%> (<%=System.getProperty( "os.arch" )%>)</td>
        </tr>
        <tr>
          <th>Servlet&nbsp;Container:</th>
          <td><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
        </tr>
        <tr>
          <th>User&nbsp;Agent:</th>
          <td><%=request.getHeader( "User-Agent" )%></td>
        </tr>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
