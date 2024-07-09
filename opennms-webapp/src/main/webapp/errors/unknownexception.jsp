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
	isErrorPage="true"
	import="org.opennms.core.resource.Vault,
                java.lang.StackTraceElement,
                java.lang.StringBuilder"
 %>
<%@ page import="org.opennms.web.utils.ExceptionUtils" %>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<% Bootstrap.with(pageContext)
          .headTitle("Unexpected Error")
          .headTitle("Error")
          .breadcrumb("Error ")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<%

    if (exception == null) {
        exception = (Throwable)request.getAttribute("javax.servlet.error.exception");
    }

    HttpSession userSession = request.getSession();
%>

<script type="text/javascript">
function toggleDiv(divName) {
    var targetDiv = document.getElementById(divName);
    if (! targetDiv) {
    	return;
	}
	targetDiv.style.display = (targetDiv.style.display == "none" ? "block" : "none");
}
</script>

<div class="card">
<div class="card-header">
  <span>The OpenNMS Web User Interface Has Experienced an Error</span>
</div>
<div class="card-body">

<p>
  The OpenNMS web UI has encountered an error that it does
  not know how to handle.
</p>

<p>
  Possible causes could be that the database is not responding,
  the OpenNMS application has stopped or is not running, or there
  is an issue with the servlet container.
</p>

<p>
  Please bring this message to the attention of the
  person responsible for maintaining OpenNMS for your organization,
  and have him or her check that OpenNMS, the external servlet container
  (if applicable), and the database are all running without errors.
</p>

<%
final StringBuilder stBuilder = new StringBuilder();
boolean showStrackTrace = Boolean.getBoolean("org.opennms.ui.show_stacktrace");
if(showStrackTrace) {
  stBuilder.append(ExceptionUtils.getFullStackTrace(exception));
} else {
  stBuilder.append("Print of stack trace is disabled");
}

final Logger LOG = LoggerFactory.getLogger(this.getClass());

String errorDetails = "";
if (LOG.isDebugEnabled()) {
  errorDetails += "System Details\n" +
    "--------------\n" +
    "OpenNMS Version: " + Vault.getProperty("version.display") + "\n" +
    "Java Version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n" +
    "Java Virtual Machine: " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor") + "\n" +
    "Operating System: " + System.getProperty("os.name") + " " +  System.getProperty("os.version") + " " + (System.getProperty("os.arch")) + "\n" +
    "Servlet Container: " + application.getServerInfo() + " (Servlet Spec " + application.getMajorVersion() + "." + application.getMinorVersion() + ")\n" +
    "User Agent: " + request.getHeader("User-Agent") + "\n" +
    "\n\n";
}

errorDetails += "Request Details\n" +
  "---------------\n" +
  "Locale: " + request.getLocale() + "\n" +
  "Method: " + request.getMethod() + "\n" +
  "Path Info: " + request.getPathInfo() + "\n" +
  "Path Info (translated): " + request.getPathTranslated() + "\n" +
  "Protocol: " + request.getProtocol() + "\n" +
  "URI: " + request.getRequestURI() + "\n" +
  "URL: " + request.getRequestURL() + "\n" +
  "Scheme: " + request.getScheme() + "\n" +
  "Server Name: " + request.getServerName() + "\n" +
  "Server Port: " + request.getServerPort() + "\n" +
  "\n" +
  "Exception Stack Trace\n" +
  "---------------------\n" + stBuilder.toString();

userSession.setAttribute("errorReportSubject", "Uncaught " + exception.getClass().getSimpleName() + " in webapp");
userSession.setAttribute("errorReportDetails", errorDetails);

%>

<p>
  To reveal details of the error encountered and instructions for
  reporting it, click
  <strong><a href="javascript:toggleDiv('errorDetails')">here</a></strong>.
</p>

</div> <!-- card-body -->
</div> <!-- panel -->

<div id="errorDetails" style="display: none;">

<div class="card">
  <div class="card-header">
    <span>Error Details</span>
  </div>
  <div class="card-body">
    <p>
    Please include the information below when reporting problems.
    </p>
  </div> <!-- card-body -->
</div> <!-- panel -->
<% if(showStrackTrace){ %>
  <div class="card">
    <div class="card-header">
      <span>Exception Trace</span>
  </div>
  <div class="card-body">
    <pre id="exceptionTrace"><%=stBuilder.toString()%></pre>
  </div> <!-- card-body -->
</div> <!-- panel -->
<% } %>

<div class="card">
  <div class="card-header">
    <span>Request Details</span>
  </div>
  <table class="table table-sm table-bordered">
    <tr>
      <th>Locale</th>
      <td><%=request.getLocale()%></td>
    </tr>
    <tr>
      <th>Method</th>
      <td><%=request.getMethod()%></td>
    </tr>
    <tr>
      <th>Path Info</th>
      <td><%=request.getPathInfo()%></td>
    </tr>
    <tr>
      <th>Path Info (translated)</th>
      <td><%=request.getPathTranslated()%></td>
    </tr>
    <tr>
      <th>Protocol</th>
      <td><%=request.getProtocol()%></td>
    </tr>
    <tr>
      <th>URI</th>
      <td><%=request.getRequestURI()%></td>
    </tr>
    <tr>
      <th>URL</th>
      <td><%=request.getRequestURL()%></td>
    </tr>
    <tr>
      <th>Scheme</th>
      <td><%=request.getScheme()%></td>
    </tr>
    <tr>
      <th>Server Name</th>
      <td><%=request.getServerName()%></td>
    </tr>
    <tr>
      <th>Server Port</th>
      <td><%=request.getServerPort()%></td>
    </tr>
  </table>
</div> <!-- panel -->

<%
  if (LOG.isDebugEnabled()) {
%>

<div class="card">
  <div class="card-header">
    <span>System Details</span>
  </div>
  <table class="table table-sm table-bordered">
    <tr>
      <th>OpenNMS Version:</th>
      <td><%=Vault.getProperty("version.display")%></td>
    </tr>
    <tr>
      <th>Java Version:</th>
      <td><%=System.getProperty( "java.version" )%> <%=System.getProperty( "java.vendor" )%></td>
    </tr>
    <tr>
      <th>Java Virtual Machine:</th>
      <td><%=System.getProperty( "java.vm.version" )%> <%=System.getProperty( "java.vm.vendor" )%></td>
    </tr>
    <tr>
      <th>Operating System:</th>
      <td><%=System.getProperty( "os.name" )%> <%=System.getProperty( "os.version" )%> (<%=System.getProperty( "os.arch" )%>)</td>
    </tr>
    <tr>
      <th>Servlet Container:</th>
      <td><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
    </tr>
    <tr>
      <th>User Agent:</th>
      <td><%=request.getHeader( "User-Agent" )%></td>
    </tr>
  </table>
</div> <!-- panel -->
<%
  }
%>

<div class="card">
  <div class="card-header">
    <span>Options for Reporting This Problem</span>
  </div>
  <div class="card-body">
    <p>
    There are two options for reporting this problem outside your own organization.
    </p>

    <strong>OpenNMS Bug Tracker</strong>
    <p>
    If you have an account on the <a href="http://issues.opennms.org/">OpenNMS issue tracker</a>,
    please consider reporting this problem. Bug reports help us make OpenNMS better, and are
    often the only way we become aware of problems. Please do search the tracker first to check
    that others have not already reported the problem that you have encountered.
    </p>

    <strong>OpenNMS Commercial Support</strong>
    <p>
    If you have a commercial support agreement with <a href="http://www.opennms.com/">The
    OpenNMS Group</a>, please consider opening a support ticket about this problem at
    <strong><a href="https://support.opennms.com/">support.opennms.com</a></strong> or via
    e-mail. Tickets from our customers receive priority treatment from our support staff.
    If you create a support ticket and the support engineer handling the ticket determines
    that you have found a bug, he or she will create a record in the bug tracker.
    </p>

    <p>
    For a plain-text version of the information above suitable for pasting into a bug report
    or support ticket, click
    <strong><a href="javascript:toggleDiv('plainTextErrorDetails');">here</a></strong>.
    </p>
  </div> <!-- card-body -->
</div> <!-- panel -->

</div> <!-- errorDetails -->

<div id="plainTextErrorDetails" style="display: none;">

<div class="card">
  <div class="card-header">
    <span>Plain Text Error Details</span>
  </div>
  <div class="card-body">
    <textarea id="plainTextArea" style="width: 100%; height: 300px;">Please take a few moments to include a description of what you were doing when you encountered this problem. Without knowing the context of the error, it's often difficult for the person looking at the problem to narrow the range of possible causes. Bug reports that do not include any information on the context in which the problem occurred will receive a lower priority and may even be closed as invalid. 

<%= errorDetails %>
    </textarea>
  </div> <!-- card-body -->
</div> <!-- panel -->

</div> <!-- plainTextErrorDetails -->

<script type="text/javascript">
var reportArea = document.getElementById("plainTextArea");
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
