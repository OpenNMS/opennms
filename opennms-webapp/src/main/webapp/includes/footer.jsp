<%--

  Modifications:

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

    <div id="footer" style="">
      <p>
        OpenNMS <a href="help/about.jsp">Copyright</a> &copy; 2002-2008
	    <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a>
	    OpenNMS&reg; is a registered trademark of
        <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
	  </p>
    </div>
  </c:otherwise>
</c:choose>

<%-- The </body> and </html> tags are unmatched in this file (the matching
     tags are in the header), so we hide them in JSP code fragments so the
     Eclipse HTML validator doesn't complain.  See bug #1728. --%>
<%= "</body>" %>
<%= "</html>" %>
