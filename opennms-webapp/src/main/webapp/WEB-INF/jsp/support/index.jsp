<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page language="java"
	contentType="text/html"
session="true"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Support" />
  <jsp:param name="headTitle" value="Get Support" />
  <jsp:param name="location" value="help" />
  <jsp:param name="breadcrumb" value="Support" />
</jsp:include>

<script type="text/javascript">
  // Shorthand for 'onload'
  $(function() {
    // Check to see if the URL provided by the opennms-docs package is present on the system.
    // If so, display the links to the offline docs symlinked from /usr/share/doc/opennms-${version}.
    $.ajax({
      url: 'docs/guide-install/index.html',
      method: 'HEAD',
      error: function()
      {
        $('#online-documentation').css("display", "inline-block");
      },
      success: function()
      {
        $('#offline-documentation').css("display", "inline-block");
      }
    });
  });
</script>

<div class="row">
<div class="col-md-6">
    <c:choose>

      <c:when test="${!results.needsLogin}">
        <!-- we have a login session, show support details -->

		<div class="panel panel-default">
		  <div class="panel-heading">
        	<h3 class="panel-title">Commercial Support</h3>
        </div>
        <div class="panel-body">

        <c:if test="${not empty results.message}">
          <div class="something">
            <c:out value="${results.message}" escapeXml="false" />
          </div>
        </c:if>

        <p>To create a support ticket, enter a subject and a description of the
           problem below. Please choose a descriptive subject and indicate whether
           this is a new problem (something that worked before but doesn't now) or
           a &quot;day one&quot; problem.</p>

        <p>You may elect to include a basic system report to help
           the support engineer who works your ticket diagnose the problem more
           quickly.</p>

        <form method="post" action="support/index.htm" id="signout">
	      <input type="hidden" name="operation" value="logout" />
        </form>
        <form role="form" class="form-horizontal" method="post" action="support/index.htm">
          <div class="form-group">
            <div class="col-md-2">
              <label for="sign-out" class="control-label">Username:</label>
	        </div>
            <div class="col-md-2">
              <p class="form-control-static"><c:out value="${results.username}" /></p>
	        </div>
            <div class="col-md-8">
	          <input id="sign-out" class="btn btn-default pull-right" value="Sign Out" onClick="document.forms['signout'].submit();"/>
	        </div>
          </div>
          <div class="form-group">
          	<div class="col-md-2">
              <label class="control-label">Queue:</label>
	        </div>
	        <div class="col-md-2">
              <p class="form-control-static"><c:out value="${results.queue}" /></p>
	        </div>
          </div>
          <div class="form-group">
          	<div class="col-md-12">
              <label for="subject" class="control-label">Subject:</label>
			  <input id="subject" class="form-control" type="text" name="subject" value="${sessionScope.errorReportSubject}" />
			</div>
          </div>
          <div class="form-group">
            <div class="col-md-12">
              <label for="details" class="control-label">Details:</label>
              <textarea id="details" class="form-control" name="text" rows="15" >${sessionScope.errorReportDetails}</textarea>
            </div>
          </div>
          <div class="form-group">
            <div class="col-md-12">
              <div class="checkbox pull-left">
          	    <label>
                  <input type="checkbox" name="include-report" id="include-report" checked="checked" value="true">Include Basic System Report
                </label>
              </div>
          	  <div class="pull-right">
                <input class="btn btn-default" type="reset" value="Clear" />
                <input class="btn btn-default" type="submit" value="Create Ticket" />
              </div>
            </div>
          </div>
          <input type="hidden" name="operation" value="createTicket" />
        </form>

        <p>
        	Your newest tickets are listed below.  For a complete list, log in to the
        	<a href="<c:out value="${results.RTUrl}" />">OpenNMS support portal</a>.
        </p>
        
        <table>
        <c:forEach var="ticket" items="${results.latestTickets}">
          <tr>
            <td><c:out value="${ticket.created}" /></td>
            <td><a href="<c:out value="${results.RTUrl}/Ticket/Display.html?id=${ticket.id}" />" target="_blank"><c:out value="${ticket.subject}" /></a></td>
          </tr>
        </c:forEach>
        </table>
        </div>
        </div>
      </c:when>

      <c:otherwise>
        <!-- no account session found, ask for login -->
        <div class="panel panel-default">
        <div class="panel-heading">
        	<h3 class="panel-title">Commercial Support</h3>
        </div>
        <div class="panel-body">

        <p>
          Enter your OpenNMS Group commercial support login to open a support ticket or view your open
          issues.
        </p>
        <p>
          If you do not have a commercial support agreement, see
          <a href="http://www.opennms.com/support/">the OpenNMS.com support page</a> for more details.
        </p>
        <form role="form" method="post" action="support/index.htm">
			<div class="form-group">
    					<label for="username" class="control-label">Username:</label>
      					<input type="text" name="username" class="form-control" id="username" placeholder="Username">
  			</div>
  			<div class="form-group">
    			<label for="password" class="control-label">Password:</label>
      			<input type="password" name="password" class="form-control" id="password" placeholder="Password">
  			</div>
  			<div class="form-group">
  				<button type="reset" class="btn btn-default">Clear</button>
  				<button type="submit" class="btn btn-default">Log in</button>
  				<input type="hidden" name="operation" value="login" />
  			</div>
        </form>
        </div>
        </div>
      </c:otherwise>

    </c:choose>
</div>

<div class="col-md-6">
  <div class="panel panel-default">
  <div class="panel-heading">
  	<h3 class="panel-title">About</h3>
  </div>
  <div class="panel-body">
    <ul class="list-unstyled">
      <li><a href="support/about.jsp">About the OpenNMS Web Console</a></li>
      <li><a href="http://docs.opennms.org/opennms/releases/latest/releasenotes/releasenotes.html">Release Notes</a></li>
    </ul>
  </div>
  </div>

  <div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Documentation</h3>
  </div>
  <div class="panel-body">
    <span id="online-documentation" style="display:none;">
    <ul class="list-unstyled">
      <li><a href="http://docs.opennms.org/opennms/releases/latest/guide-install/guide-install.html">Installation Guide</a></li>
      <li><a href="http://docs.opennms.org/opennms/releases/latest/guide-user/guide-user.html">Users Guide</a></li>
      <li><a href="http://docs.opennms.org/opennms/releases/latest/guide-admin/guide-admin.html">Administrators Guide</a></li>
      <li><a href="http://docs.opennms.org/opennms/releases/latest/guide-development/guide-development.html">Developers Guide</a></li>
      <li><a href="http://www.opennms.org/wiki/">Online Wiki Documentation</a></li>
    </ul>
    </span>
    <span id="offline-documentation" style="display:none;">
    <ul class="list-unstyled">
      <li><a href="docs/guide-install/index.html">Installation Guide</a></li>
      <li><a href="docs/guide-user/index.html">Users Guide</a></li>
      <li><a href="docs/guide-admin/index.html">Administrators Guide</a></li>
      <li><a href="docs/guide-development/index.html">Developers Guide</a></li>
      <li><a href="http://www.opennms.org/wiki/">Online Wiki Documentation</a></li>
    </ul>
    </span>
  </div>
  </div>

  <div class="panel panel-default">
  <div class="panel-heading">
  	<h3 class="panel-title">Other Support Options</h3>
  </div>
  <div class="panel-body">
    <ul class="list-unstyled">
      <li><a href="admin/support/systemReport.htm">Generate a System Report</a></li>
      <li><a href="http://issues.opennms.org/">Open a Bug or Enhancement Request</a></li>
      <li><a href="irc://irc.freenode.net/%23opennms">Chat with Developers on IRC</a></li>
    </ul>
  </div>
</div>
</div><!-- col -->
</div><!-- row -->
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
