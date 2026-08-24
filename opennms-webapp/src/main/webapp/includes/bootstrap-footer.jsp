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

<%-- Close the #content-body wrapper opened in bootstrap.jsp, so the footer is a
     sibling of it rather than inside it — that is what lets #content-body take
     the slack and leave the footer at the bottom. Guarded by the same
     'superQuiet' test as the #content close below, since that branch emits
     neither div. Closed here, ahead of the footer markup, rather than with
     #content at the end of this file. --%>
<c:choose>
    <c:when test="${param.superQuiet == 'true'}">
        <%-- nothing to do --%>
    </c:when>
    <c:otherwise>
        <%= "</div>" %><!-- id="content-body" -->
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${param.quiet == 'true'}">
        <!-- Not displaying footer -->
    </c:when>

    <c:otherwise>
        <!-- Footer -->

        <footer id="footer" class="card-footer">
            <p>
                OpenNMS <a href="about/index.jsp">Copyright</a> &copy; 1999-2026
                <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a>
                OpenNMS&reg; is a registered trademark of
                <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
                <%
                    if (req.getUserPrincipal() != null) {
                        out.print(" - Version: " + Vault.getProperty("version.display"));
                    }
                %>
            </p>
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

<%--
  Whatever an install drops in includes/custom-footer lands here, between the
  footer and the close of #content — which is a flex column (see #content in
  opennms-theme.scss). Give it a plain block of its own to live in, for the same
  reason #content-body exists: as direct flex items, inline-level markup would be
  stretched and anything sized by auto margins would collapse. Inside this div it
  lays out normally, and #content keeps a hand-countable set of items.
--%>
<%
    File extraIncludes = new File(request.getSession().getServletContext().getRealPath("includes") + File.separator + "custom-footer");
    if (extraIncludes.exists()) {
%>
<div id="custom-footer">
<%
        for (File file : extraIncludes.listFiles()) {
            if (file.isFile()) {
                pageContext.setAttribute("file", "custom-footer/" + file.getName());
%>
<jsp:include page="${file}"/>
<%
            }
        }
%>
</div>
<%
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
