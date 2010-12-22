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
// 2010 Mar 03: Made kinder and gentler. - jeffg@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
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
	isErrorPage="true"
	import="org.opennms.core.resource.Vault,
                java.lang.StackTraceElement,
                java.lang.StringBuilder"
 %>

<jsp:include page="/includes/header.jsp" flush="false" >
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

<h3>The OpenNMS Web User Interface Has Experienced an Error</h3>
<br/>

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
  stBuilder.append(exception.getClass().getName()).append("\n");
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

<div id="errorDetails" style="display: none;">
<h3>Error Details</h3>

<p>
Please include the information below when reporting problems.
</p>

<h3>Exception Trace</h3>
  <pre id="exceptionTrace">
<%=stBuilder.toString()%>
  </pre>
  
<h3>Request Details</h3>
<table class="standard">
  <tr>
    <td class="standardheader">Locale</td>
    <td class="standard"><%=request.getLocale()%></td>
  </tr>
  <tr>
    <td class="standardheader">Method</td>
    <td class="standard"><%=request.getMethod()%></td>
  </tr>
  <tr>
    <td class="standardheader">Path Info</td>
    <td class="standard"><%=request.getPathInfo()%></td>
  </tr>
  <tr>
    <td class="standardheader">Path Info (translated)</td>
    <td class="standard"><%=request.getPathTranslated()%></td>
  </tr>
  <tr>
    <td class="standardheader">Protocol</td>
    <td class="standard"><%=request.getProtocol()%></td>
  </tr>
  <tr>
    <td class="standardheader">URI</td>
    <td class="standard"><%=request.getRequestURI()%></td>
  </tr>
  <tr>
    <td class="standardheader">URL</td>
    <td class="standard"><%=request.getRequestURL()%></td>
  </tr>
  <tr>
    <td class="standardheader">Scheme</td>
    <td class="standard"><%=request.getScheme()%></td>
  </tr>
  <tr>
    <td class="standardheader">Server Name</td>
    <td class="standard"><%=request.getServerName()%></td>
  </tr>
  <tr>
    <td class="standardheader">Server Port</td>
    <td class="standard"><%=request.getServerPort()%></td>
  </tr>
  
</table>

<h3>System Details</h3>
<table class="standard">
  <tr>
    <td class="standardheader">OpenNMS Version:</td>
    <td class="standard"><%=Vault.getProperty("version.display")%></td>
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

<h3>Options for Reporting This Problem</h3>
<p>
There are two options for reporting this problem outside your own organization.
</p>

<h5>OpenNMS Bug Tracker</h5>
<p>
If you have an account on the <a href="http://bugzilla.opennms.org/">OpenNMS bug tracker</a>,
please consider reporting this problem. Bug reports help us make OpenNMS better, and are
often the only way we become aware of problems. Please do search the tracker first to check
that others have not already reported the problem that you have encountered.
</p>

<h5>OpenNMS Commercial Support</h5>
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

<div id="plainTextErrorDetails" style="display: none;">
<h3>Plain Text Error Details</h3>
<div class="boxWrapper">

<textarea id="plainTextArea" style="width: 100%; height: 300px;">
Please take a few moments to include a description of what you were doing when you encountered this problem. Without knowing the context of the error, it's often difficult for the person looking at the problem to narrow the range of possible causes. Bug reports that do not include any information on the context in which the problem occurred will receive a lower priority and may even be closed as invalid. 

<%= errorDetails %>

</textarea>

</div>

</div>

<script type="text/javascript">
var reportArea = document.getElementById("plainTextArea");
</script>


<jsp:include page="/includes/footer.jsp" flush="false" />
