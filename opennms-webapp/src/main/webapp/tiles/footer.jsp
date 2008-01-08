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

<%@page language="java" contentType="text/html" session="true"%>


<!-- Footer -->
<!--
<table width="100%" border="0" cellspacing="0" cellpadding="2" id="footer">
  <tr>
    <td class="footer" ALIGN="center">
      <font SIZE="-1" FACE="arial">
-->

<div id="prefooter">
<!-- Can't leave this as <div/>.  Safari doesn't think the div has ended. -->
</div>

<div id="footer">        

  <div id="navbar">
  Thanks for coming to this years DevJam!!!
  </div>
</div>
<div id="copyright">
OpenNMS <a href="help/about.jsp">Copyright</a> &copy; 2002-2008 <a HREF="http://www.opennms.com/">The OpenNMS Group, Inc.</a> OpenNMS&reg; is a registered trademark of <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
</div>
