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
	import="java.io.File,
		org.opennms.web.authenticate.Authentication,
		org.opennms.core.resource.Vault"
%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<!-- End of Content -->
</div>

<c:choose>
<c:when test="${param.quiet == 'true'}">
<!-- Not displaying footer -->
</c:when>

<c:otherwise>
<!-- Footer -->

<div id="prefooter"></div>

<div id="footer">        
  <jsp:include page="/includes/navbar.jsp" flush="false"/>
</div>

<div id="copyright">
OpenNMS <a href="help/about.jsp">Copyright</a> &copy; 2002-2005 <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a> OpenNMS&reg; is a registered trademark of <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
</div>

</c:otherwise>
</c:choose>

</body>
</html>
