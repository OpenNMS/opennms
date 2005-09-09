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
<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr>
    <td class="footer" ALIGN="center">
      <font SIZE="-1" FACE="arial">
        <%-- Node List --%>
        <%  if( "element".equals( location ) ) { %>
              List Nodes&nbsp;|&nbsp;
        <%  } else { %>
              <a href="element/nodelist.jsp">List Nodes</a>&nbsp;|&nbsp;
        <%  } %>

        <%-- Elements Search --%>
        <%  if( "element".equals( location ) ) { %>
              Search&nbsp;|&nbsp;
        <%  } else { %>
              <a href="element/index.jsp">Search</a>&nbsp;|&nbsp;
        <%  } %>    

        <%-- Outages --%>
        <%  if( "outages".equals( location ) ) { %>
              Outages&nbsp;|&nbsp;
        <%  } else { %>
              <a href="outage/index.jsp">Outages</a>&nbsp;|&nbsp;
        <%  } %>

        <%-- Events --%>
        <%  if( "event".equals( location ) ) { %>
              Events&nbsp;|&nbsp;
        <%  } else { %>
              <a href="event/index.jsp">Events</a>&nbsp;|&nbsp;
        <%  } %>

        <%-- Notification --%>                               
        <%  if( "notification".equals( location ) ) { %>
              Notification&nbsp;|&nbsp;
        <%  } else { %>
              <a href="notification/index.jsp">Notification</a>&nbsp;|&nbsp;
        <%  } %>

        <%-- Assets --%>                               
        <%  if( "asset".equals( location ) ) { %>
              Assets&nbsp;|&nbsp;
        <%  } else { %>
              <a href="asset/index.jsp">Assets</a>&nbsp;|&nbsp;
        <%  } %>

<%--        
        <%-- Security -- % >                               
        <%  if( "security".equals( location ) ) { %>
              Security&nbsp;|&nbsp;
        <%  } else { %>
              <a href="security.jsp">Security</a>&nbsp;|&nbsp;
        <%  } %>
--%>

        <%-- Report --%>                               
        <%  if( "report".equals( location ) ) { %>
              Reports&nbsp;|&nbsp;
        <%  } else { %>
              <a href="report/index.jsp">Reports</a>&nbsp;|&nbsp;
        <%  } %>

        <%  if( "inventory".equals( location ) ) { %>
              Inventory&nbsp;|&nbsp;
        <%  } else { %>
              <a href="conf/index.jsp">Inventory</a>&nbsp;|&nbsp;
        <%  } %>

<% if( file.exists() ) { %>
        <%-- Map --%>                               
        <%  if( "map".equals( location ) ) { %>
              Map&nbsp;|&nbsp;
        <%  } else { %>
              <a href="map/index.jsp">Map</a>&nbsp;|&nbsp;
        <%  } %>
<% } %>

<% if( request.isUserInRole( Authentication.ADMIN_ROLE )) { %>
        <%-- Admin --%>                               
        <%  if( "admin".equals( location ) ) { %>
              Admin&nbsp;|&nbsp;
        <%  } else { %>
              <a href="admin/index.jsp">Admin</a>&nbsp;|&nbsp;
        <%  } %>
<% } %>

        <%-- Help --%>                               
        <%  if( "help".equals( location ) ) { %>
              Help
        <%  } else { %>
              <a href="help/index.jsp">Help</a>
        <%  } %>
      </font>
    </td>
  </tr>
  <tr> 
    <td align="center" >
      <font  SIZE="-1">OpenNMS <a href="help/about.jsp">Copyright</a> &copy; 2002-2005 <a HREF="http://www.opennms.com/">The OpenNMS Group, Inc.</a> OpenNMS&reg; is a registered trademark of <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a> </font>
    </td>
  </tr>
</table>
