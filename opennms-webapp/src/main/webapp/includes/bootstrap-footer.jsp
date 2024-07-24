<%--
    This file is part of BlueBirdOps(tm).

    BlueBirdOps is Copyright (C) 2025 BlueBirdOps Contributors.

    Portions Copyright (C) 2002-2025 The OpenNMS Group, Inc.

    See the LICENSE.md file distributed with this work for additional
    information regarding copyright ownership.

    BlueBirdOps is free software: you can redistribute it and/or modify it
    under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or (at your
    option) any later version.

    BlueBirdOps is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
    for more details.

    You should have received a copy of the GNU Affero General Public License
    along with BlueBirdOps. If not, see <https://www.gnu.org/licenses/>.
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
        import="java.io.File,
                org.opennms.web.api.Util,
                org.opennms.core.resource.Vault,
                org.opennms.web.api.HtmlInjectHandler,
                org.opennms.web.servlet.XssRequestWrapper"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    XssRequestWrapper req = new XssRequestWrapper(request);
%>

<c:choose>
    <c:when test="${param.quiet == 'true'}">
        <!-- Not displaying footer -->
    </c:when>

    <c:otherwise>
        <!-- Footer -->

        <footer id="footer" class="card-footer">
            <span>
                <a href="about/index.jsp">BlueBirdOps</a>
                <%
                    if (req.getUserPrincipal() != null) {
                        out.print(" v" + Vault.getProperty("version.display"));
                    }
                %>
            </span>
        </footer>

        <% if (req.getUserPrincipal() != null) { %>
            <!-- Browser notifications -->
            <jsp:include page="/assets/load-assets.jsp" flush="false">
                <jsp:param name="asset" value="notifications" />
                <jsp:param name="asset-defer" value="true" />
            </jsp:include>
        <% } %>
    </c:otherwise>
</c:choose>

<%
    File extraIncludes = new File(request.getSession().getServletContext().getRealPath("includes") + File.separator + "custom-footer");
    if (extraIncludes.exists()) {
        for (File file : extraIncludes.listFiles()) {
            if (file.isFile()) {
                pageContext.setAttribute("file", "custom-footer/" + file.getName());
%>
<jsp:include page="${file}"/>
<%
            }
        }
    }
%>

<%-- This </div> tag is unmatched in this file (its matching tag is in the
     header), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<c:choose>
    <c:when test="${param.superQuiet == 'true'}">
        <%-- nothing to do --%>
    </c:when>
    <c:otherwise>
        <%= "</div>" %><!-- id="content" class="container-fluid" -->
    </c:otherwise>
</c:choose>

<%-- Allows services exposed via the OSGi registry to inject HTML content --%>
<%= HtmlInjectHandler.inject(request) %>

<%-- The </body> and </html> tags are unmatched in this file (the matching
     tags are in the header), so we hide them in JSP code fragments so the
     Eclipse HTML validator doesn't complain.  See bug #1728. --%>
<%= "</body>" %>
<%= "</html>" %>
