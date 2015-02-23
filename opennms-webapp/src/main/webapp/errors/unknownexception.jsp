<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
	isErrorPage="true"
	import="org.opennms.core.resource.Vault,
                java.lang.StackTraceElement,
                java.lang.StringBuilder"
 %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Unexpected Error" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error " />
</jsp:include>

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

<div class="panel panel-default">
<div class="panel-heading">
  <h3 class="panel-title">The OpenNMS Web User Interface Has Experienced an Error</h3>
</div>
<div class="panel-body">

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
StringBuilder stBuilder = new StringBuilder();

if (exception != null) {
  if (exception instanceof ServletException && ((ServletException)exception).getRootCause() != null) {
    exception = ((ServletException) exception).getRootCause();
  } else if (exception.getCause() != null) {
    exception = exception.getCause();
  }
  stBuilder.append(exception.getClass().getName());
  /*
  NOTE: While this may look like a good idea, we want to omit the message
  to avoid transmitting sensitive information like file paths and other internal
  information on the error page. If the user needs more precise information,
  they can look at the webapp logs for the complete stack trace.

  String message = exception.getMessage();
  if (message != null && message.length() > 0) {
    stBuilder.append(": ").append(exception.getMessage());
  }
  */
  stBuilder.append("\n");
  for (StackTraceElement ste : exception.getStackTrace()) {
    stBuilder.append("\tat ").append(ste.toString()).append("\n");
  }
} else {
  stBuilder.append("No exception to see here, please move along.");
}

String errorDetails = 
"System Details\n" +
"--------------\n" +
"OpenNMS Version: " + Vault.getProperty("version.display") + "\n" +
"Java Version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n" +
"Java Virtual Machine: " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor") + "\n" +
"Operating System: " + System.getProperty("os.name") + " " +  System.getProperty("os.version") + " " + (System.getProperty("os.arch")) + "\n" +
"Servlet Container: " + application.getServerInfo() + " (Servlet Spec " + application.getMajorVersion() + "." + application.getMinorVersion() + ")\n" +
"User Agent: " + request.getHeader("User-Agent") + "\n" +
"\n" +
"\n" +
"Request Details\n" +
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

</div> <!-- panel-body -->
</div> <!-- panel -->

<div id="errorDetails" style="display: none;">

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Error Details</h3>
  </div>
  <div class="panel-body">
    <p>
    Please include the information below when reporting problems.
    </p>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Exception Trace</h3>
  </div>
  <div class="panel-body">
    <pre id="exceptionTrace"><%=stBuilder.toString()%></pre>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Request Details</h3>
  </div>
  <table class="table table-condensed table-bordered">
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

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">System Details</h3>
  </div>
  <table class="table table-condensed table-bordered">
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

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Options for Reporting This Problem</h3>
  </div>
  <div class="panel-body">
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
  </div> <!-- panel-body -->
</div> <!-- panel -->

</div> <!-- errorDetails -->

<div id="plainTextErrorDetails" style="display: none;">

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Plain Text Error Details</h3>
  </div>
  <div class="panel-body">
    <textarea id="plainTextArea" style="width: 100%; height: 300px;">Please take a few moments to include a description of what you were doing when you encountered this problem. Without knowing the context of the error, it's often difficult for the person looking at the problem to narrow the range of possible causes. Bug reports that do not include any information on the context in which the problem occurred will receive a lower priority and may even be closed as invalid. 

<%= errorDetails %>
    </textarea>
  </div> <!-- panel-body -->
</div> <!-- panel -->

</div> <!-- plainTextErrorDetails -->

<script type="text/javascript">
var reportArea = document.getElementById("plainTextArea");
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
