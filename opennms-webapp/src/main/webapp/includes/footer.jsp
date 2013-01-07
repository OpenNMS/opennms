<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%--

  Modifications:

  2013 Jan 04: Happy new year - jeffg@opennms.org
  2011 Jan 01: Happy new year - jeffg@opennms.org
  2010 Feb 09: Happy new year - jeffg@opennms.org
  2009 Jan 14: Happy new year, copyright update. - jeffg@opennms.org
  2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor

--%>
<%-- 
  This page is included by other JSPs to create a uniform footer. 
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
  
  This include JSP takes one parameter:
    location (optional): used to "dull out" the item in the menu bar
      that has a link to the location given  (for example, on the
      outage/index.jsp, give the location "outages")
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.io.File"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

  <!-- End of Content -->
  <div class="spacer"><!-- --></div>
<%-- This </div> tag is unmatched in this file (its matching tag is in the
     header), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%= "</div>" %><!-- id="content" -->

<c:choose>
  <c:when test="${param.quiet == 'true'}">
    <!-- Not displaying footer -->
  </c:when>

  <c:otherwise>
    <!-- Footer -->

    <div id="prefooter"></div>

    <div id="footer">
      <p>
        OpenNMS <a href="support/about.jsp">Copyright</a> &copy; 2002-2013
	    <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a>
	    OpenNMS&reg; is a registered trademark of
        <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
	  </p>
    </div>
  </c:otherwise>
</c:choose>

<%
  File extraIncludes = new File(request.getSession().getServletContext().getRealPath("includes") + File.separator + "custom-footer");
  if (extraIncludes.exists()) {
	  for (File file : extraIncludes.listFiles()) {
		  if (file.isFile()) {
			  pageContext.setAttribute("file", "custom-footer/" + file.getName());
%>
<jsp:include page="${file}" />
<%
		  }
	  }
  }
%>
<%-- The </body> and </html> tags are unmatched in this file (the matching
     tags are in the header), so we hide them in JSP code fragments so the
     Eclipse HTML validator doesn't complain.  See bug #1728. --%>
<%= "</body>" %>
<%= "</html>" %>
