<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%--
  This page is included by other JSPs to create a uniform header. 
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
  
  This include JSP takes two parameters:
    title (required): used in the middle of the header bar
    location (optional): used to "dull out" the item in the menu bar
      that has a link to the location given  (for example, on the
      outage/index.jsp, give the location "outages")
--%>

<%@page language="java" contentType="text/html" session="true" import="java.text.DateFormat,org.opennms.web.authenticate.Authentication,org.opennms.netmgt.config.NotifdConfigFactory"%>

<%!
    static DateFormat dateFormatter = DateFormat.getDateInstance( DateFormat.MEDIUM );
    static DateFormat timeFormatter = DateFormat.getTimeInstance( DateFormat.SHORT ); 
    
    public void init() throws ServletException {
        try {
            NotifdConfigFactory.init();
        }
        catch( Exception e ) {/*notice status will be unknown if the factory can't be initialized*/}
    }
%>

<%
    String title = request.getParameter( "title" );
    String location = request.getParameter( "location" );
    String[] breadcrumbs = request.getParameterValues( "breadcrumb" );

    if( breadcrumbs == null ) {
        breadcrumbs = new String[0];
    }

    java.util.Date now = new java.util.Date(); 
    String date = dateFormatter.format( now );
    String time = timeFormatter.format( now );
%>

<!-- Header -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" background="images/logo-background.gif">
  <tr> 
    <td WIDTH="30%">
      <a href="index.jsp"><img src="images/logo.gif" hspace="0" vspace="0" border="0" alt="OpenNMS Web Console Home"></a>
    </td>
    <td ALIGN="center">
      <b><%=title%></b>
    </td>
    <td width="20%" ALIGN="right" >
      <b>[<%=request.getRemoteUser()%>]</b><br>
      <%String status = "Unknown";
        try
        {
            status = NotifdConfigFactory.getInstance().getPrettyStatus();
        } catch (Exception e) { /*if factory can't be initialized, status is already 'Unknown'*/ }
      %>
      <b>Notices 
      <%if (status.equals("Unknown")) { %>
        <font color="FF0000"><%=status%></font>
      <% } else { %>
        <%=status%>
      <% } %>
      </b><br>
      <font face="arial" size="-1"><b><%=date%><br>
      <%=time%></b></font>
    </td>
  </tr>
  <tr bgcolor="white">
    <td COLSPAN="3" ALIGN="center" >
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td align="left">      
            <font SIZE="-1" FACE="arial">
            <a href="index.jsp">Home</a> 
            <% for( int i = 0; i < breadcrumbs.length; i++ ) { %> &gt; <%=breadcrumbs[i]%> <% } %>
            </font>
          </td>
          <td align="right">
          <font SIZE="-1" FACE="arial">
            <%-- Element Search --%>
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
      </table>
    </td>
  </tr>
</table>
