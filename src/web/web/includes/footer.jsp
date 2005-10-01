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

<%@page language="java" contentType="text/html" session="true" import="java.io.File,org.opennms.web.authenticate.Authentication" %>

<%
    String location = (String)request.getParameter( "location" );
    File file = new File("@install.etc.dir@/map.enable");
%>

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

        <li>
        <%-- Node List --%>
        <%  if( "nodelist".equals( location ) ) { %>
              Node List
        <%  } else { %>
              <a href="element/nodelist.jsp">Node List</a>
        <%  } %>
	</li>

        <li>
        <%-- Elements Search --%>
        <%  if( "element".equals( location ) ) { %>
              Search
        <%  } else { %>
              <a href="element/index.jsp">Search</a>
        <%  } %>    
	</li>

        <li>
        <%-- Outages --%>
        <%  if( "outages".equals( location ) ) { %>
              Outages
        <%  } else { %>
              <a href="outage/index.jsp">Outages</a>
        <%  } %>
	</li>

        <li>
        <%-- Events --%>
        <%  if( "event".equals( location ) ) { %>
              Events
        <%  } else { %>
              <a href="event/index.jsp">Events</a>
        <%  } %>
	</li>

        <li>
        <%-- Alarms --%>
        <%  if( "alarm".equals( location ) ) { %>
              Alarms
        <%  } else { %>
              <a href="alarm/index.jsp">Alarms</a>
        <%  } %>
	</li>

        <li>
        <%-- Notification --%>                               
        <%  if( "notification".equals( location ) ) { %>
              Notification
        <%  } else { %>
              <a href="notification/index.jsp">Notification</a>
        <%  } %>
	</li>

        <li>
        <%-- Assets --%>                               
        <%  if( "asset".equals( location ) ) { %>
              Assets
        <%  } else { %>
              <a href="asset/index.jsp">Assets</a>
        <%  } %>
	</li>

<%--        
        <li>
        <%-- Security -- % >                               
        <%  if( "security".equals( location ) ) { %>
              Security
        <%  } else { %>
              <a href="security.jsp">Security</a>
        <%  } %>
	</li>
--%>

        <li>
        <%-- Report --%>                               
        <%  if( "report".equals( location ) ) { %>
              Reports
        <%  } else { %>
              <a href="report/index.jsp">Reports</a>
        <%  } %>
	</li>

<% if( file.exists() ) { %>
        <li>
        <%-- Map --%>                               
        <%  if( "map".equals( location ) ) { %>
              Map
        <%  } else { %>
              <a href="map/index.jsp">Map</a>
        <%  } %>
	</li>
<% } %>

<% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %>
        <li>
        <%-- Admin --%>                               
        <%  if( "admin".equals( location ) ) { %>
              Admin
        <%  } else { %>
              <a href="admin/index.jsp">Admin</a>
        <%  } %>
	</li>
<% } %>

        <li class="last">
        <%-- Help --%>                               
        <%  if( "help".equals( location ) ) { %>
              Help
        <%  } else { %>
              <a href="help/index.jsp">Help</a>
        <%  } %>
	</li>
<!--
      </font>
    </td>
  </tr>
-->

 </div>
</div>

<!--
  <tr> 
    <td align="center" >
      <font  SIZE="-1">
-->
<div id="copyright">
OpenNMS <a href="help/about.jsp">Copyright</a> &copy; 2002-2005 <a HREF="http://www.opennms.com/">The OpenNMS Group, Inc.</a> OpenNMS&reg; is a registered trademark of <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
</div>

<!--
      </font>
    </td>
  </tr>
</table>
-->
