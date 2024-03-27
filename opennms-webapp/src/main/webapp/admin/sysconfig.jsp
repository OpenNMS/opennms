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
%>
<%@page import="org.opennms.core.resource.Vault"%>
<%@page import="org.opennms.core.spring.BeanUtils"%>
<%@page import="org.opennms.netmgt.config.SyslogdConfigFactory"%>
<%@page import="org.opennms.netmgt.config.TrapdConfigFactory"%>
<%@page import="java.time.Instant"%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@taglib uri="../WEB-INF/taglib.tld" prefix="onms" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("System Configuration")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("System Configuration")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
        function cancel()
        {
                document.snmpConfigForm.action="admin/index.jsp";
                document.snmpConfigForm.submit();
        }

        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function readystatechange() {
            try {
                if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                    var config = JSON.parse(xhr.responseText);
                    console.debug('got config:', config);
                    var services = document.getElementById('services');
                    var contents = [];
                    for (var key of Object.keys(config.services).sort()) {
                        if (config.services[key] == 'running') {
                            contents.push(key);
                        }
                    }
                    services.innerHTML = contents.join('<br>');
                }
            } catch (err) {
                console.error('Failed to get service info: ' + err);
                document.getElementById('services').innerHTML = 'Unknown';
            }
        };
        xhr.open('GET', 'rest/info');
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.send();
</script>

<%
   String trapPort = "Unknown";
   try {
       TrapdConfigFactory.init();
       trapPort = String.valueOf(TrapdConfigFactory.getInstance().getSnmpTrapPort());
   } catch (Throwable e) {
       // if factory can't be initialized, status is already 'Unknown'
   }

   String syslogPort = "Unknown";
   try {
       SyslogdConfigFactory syslogdConfig = BeanUtils.getBean("commonContext", "syslogdConfigFactory", SyslogdConfigFactory.class);
       syslogPort = String.valueOf(syslogdConfig.getSyslogPort());
   } catch (Throwable e) {
       // if factory can't be initialized, status is already 'Unknown'
   }
%>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>OpenNMS Configuration</span>
      </div>
      <table class="table table-sm">
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
          <td><%=(Boolean.valueOf(Vault.getProperty("org.opennms.rrd.storeByGroup")) ? "True" : "False")%></td>
        </tr>
        <tr>
          <th>RRD store by foreign source enabled?</th>
          <td><%=(Boolean.valueOf(Vault.getProperty("org.opennms.rrd.storeByForeignSource")) ? "True" : "False")%></td>
        </tr>
        <tr>
          <th>Reports directory:</th>
          <td><%=Vault.getProperty("opennms.report.dir")%></td>
        </tr>
        <tr>
          <th>Jetty HTTP host:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.host") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.host")%></td>
        </tr>
        <tr>
          <th>Jetty HTTP port:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.port") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.port")%></td>
        </tr>
         <tr>
          <th>Jetty HTTPS host:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.https-host") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.https-host")%></td>
        </tr>
        <tr>
          <th>Jetty HTTPS port:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.https-port") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.https-port")%></td>
        </tr>
        <tr>
          <th>SNMP trap port:</th>
          <td><%=trapPort%></td>
        </tr>
        <tr>
          <th>Syslog port:</th>
          <td><%=syslogPort%></td>
        </tr>
        <tr>
            <th>Running services:</th>
            <td id="services"></td>
        </tr>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>System Configuration</span>
      </div>
      <table class="table table-sm">
        <tr>
          <th>Server&nbsp;Time:</th>
          <td><onms:datetime instant="${Instant.now()}"/></td>
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
          <th>OSGi&nbsp;Container:</th>
          <td>Apache Karaf <%=System.getProperty( "karaf.version" )%></td>
        </tr>
        <tr>
          <th>Servlet&nbsp;Container:</th>
          <td><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
        </tr>
        <tr>
          <th>User&nbsp;Agent:</th>
          <td><%=WebSecurityUtils.sanitizeString(request.getHeader( "User-Agent" ))%></td>
        </tr>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
