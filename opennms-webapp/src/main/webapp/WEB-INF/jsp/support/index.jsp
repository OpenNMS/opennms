<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//      http://www.opennms.com/

--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page language="java"
	contentType="text/html"
session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Support" />
  <jsp:param name="headTitle" value="Get Support" />
  <jsp:param name="location" value="help" />
  <jsp:param name="breadcrumb" value="Support" />
</jsp:include>

<style type="text/css">
.textwrapper {
    overflow: hidden;
    display: block;
	width: 100%;
	border: 1px solid black;
	padding: 1px;
	padding-top: 2px;
	padding-bottom: 2px;
}

</style>

<div class="TwoColLeft">
    <c:choose>

      <c:when test="${!results.needsLogin}">
        <!-- we have a login session, show support details -->

        <h3>
          Commercial Support
          <form method="post" action="support/index.htm" id="signout">
            <input type="hidden" name="operation" value="logout" />
          </form>
        </h3>
        <div class="boxWrapper">

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

        <form method="post" action="support/index.htm">
          <table class="top" style="width:100%">
            <tr>
              <td>Username:</td>
              <td colspan="2">
                <c:out value="${results.username}" />
                <input type="button" value="Sign Out" onClick="document.forms['signout'].submit();" />
              </td>
            </tr>
            <tr>
              <td>Queue:</td>
              <td colspan="2">
                <c:out value="${results.queue}" />
              </td>
            </tr>
            <tr>
              <td>Subject:</td>
              <td colspan="2">
                <input class="textwrapper" type="text" name="subject" value="${sessionScope.errorReportSubject}" />
              </td>
            </tr>
            <tr>
              <td>Text:</td>
              <td colspan="2">
                <textarea class="textwrapper" name="text" rows="15" />${sessionScope.errorReportDetails}</textarea>
              </td>
            </tr>
            <tr>
              <td></td>
              <td style="text-align: left">
                <input type="checkbox" name="include-report" id="include-report" checked="checked" value="true" />
                <label for="include-report">Include Basic System Report</label>
              </td>
              <td style="text-align: right">
                <input type="reset" value="Clear" />
                <input type="submit" value="Create Ticket" />
              </td>
            </tr>
          </table>
          <input type="hidden" name="operation" value="createTicket" />
        </form>
        
        <hr />

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
      </c:when>

      <c:otherwise>
        <!-- no account session found, ask for login -->
        
        <h3>Commercial Support</h3>
        <div class="boxWrapper">

        <p>
          Enter your OpenNMS Group commercial support login to open a support ticket or view your open
          issues.
        </p>
        <p>
          If you do not have a commercial support agreement, see
          <a href="http://www.opennms.com/support/">the OpenNMS.com support page</a> for more details.
        </p>
        <form method="post" action="support/index.htm">
          <table class="top">
            <tr>
              <td>Username:</td>
              <td colspan="2">
                <input type="text" name="username" width="30" />
              </td>
            </tr>
            <tr>
              <td>Password:</td>
              <td colspan="2">
                <input type="password" name="password" width="30" />
              </td>
            </tr>
            <tr>
              <td colspan="2"></td>
              <td style="text-align: right">
                <input type="reset" value="Clear" />
                <input type="submit" value="Log In" />
              </td>
            </tr>
          </table>
          <input type="hidden" name="operation" value="login" />
        </form>
      </c:otherwise>

    </c:choose>
  </div>
</div>
<div class="TwoColRight">
  <h3>About</h3>
  <div class="boxWrapper">
    <ul class="plain">
      <li><a href="support/about.jsp">About the OpenNMS Web Console</a></li>
      <li><a href="http://www.opennms.org/documentation/ReleaseNotesStable.html#whats-new">Release Notes</a></li>
      <li><a href="http://www.opennms.org/wiki/">Online Documentation</a></li>
    </ul>
  </div>
  <hr />
  <h3>Other Support Options</h3>
  <div class="boxWrapper">
    <ul class="plain">
      <li><a href="admin/support/systemReport.htm">Generate a System Report</a></li>
      <li><a href="http://issues.opennms.org/">Open a Bug or Enhancement Request</a></li>
      <li><a href="irc://irc.freenode.net/%23opennms">Chat with Developers on IRC</a></li>
    </ul>
  </div>
</div>
<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
