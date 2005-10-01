<%--

  Modifications:

  2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor

--%>
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

<%@page language="java" contentType="text/html" session="true" import="java.text.DateFormat,java.io.File,org.opennms.web.authenticate.Authentication,org.opennms.netmgt.config.NotifdConfigFactory"%>

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
    File file = new File("@install.etc.dir@/map.enable");

%>

<!-- Header -->

<div id="header">
<div id="headertop">
<!--
<table width="100%" border="0" cellspacing="0" cellpadding="2" id="header" background="images/logo-background.gif">
-->



<!--
  <tr> 
    <td WIDTH="30%">
-->

<span id="headerlogo">
      <a href="index.jsp"><img src="images/logo.png" hspace="0" vspace="0" border="0" alt="OpenNMS Web Console Home"></a>
</span>

<!--
    </td>
    <td ALIGN="center">
      <b><%=title%></b>
    </td>
-->

<span id="headertitle">
      <%=title%>
</span>

<!--
    <td width="20%" ALIGN="right" >
-->

<span id="headerinfo">

<div id="outer">
 <div id="middle">
  <div id="inner">

<!--      <b>[<%=request.getRemoteUser()%>]</b><br> -->
      [<%=request.getRemoteUser()%>]<br>

      <%
        String status;
        try {
            status = NotifdConfigFactory.getInstance().getPrettyStatus();
        } catch (Exception e) {
            status = "<font color=\"ff0000\">Unknown</font>";
	}
      %>
      Notices <%= status %><br/>
      <div id="headerdate">
        <%=date%><br/>
        <%=time%>
      </div>
<!--
    </td>
  </tr>
-->
  </div>
 </div>
</div>
</span>

<div class="spacer">
     &nbsp;
</div>

</div>

<!--
  <tr bgcolor="white">
    <td COLSPAN="3" ALIGN="center" >
      <table width="100%" border="0" cellspacing="0" cellpadding="0" id="sub-header">
        <tr>
          <td align="left">      
-->

<span id="headernavbar">
          <span id="headernavbarleft">
	    <!-- XXX this should go -->
<!--            <font SIZE="-1" FACE="arial"> -->
            <a href="index.jsp">Home</a> 
            <% for( int i = 0; i < breadcrumbs.length; i++ ) { %> &gt; <%=breadcrumbs[i]%> <% } %>
	    <!-- XXX this should go, too -->
<!--            </font> -->
	  </span>

<!--
          </td>
-->

          <span id="headernavbarright">

<!--
          <td align="right">
-->


	  <!-- XXX this should go -->
<!--          <font SIZE="-1" FACE="arial">-->
   	  <div id="navbar">
	  <ul>
	    <li>
            <%-- Node List --%>
            <%  if( "nodelist".equals( location ) ) { %>
                  Node List
            <%  } else { %>
                  <a href="element/nodelist.jsp">Node List</a>
            <%  } %>
	    </li>

	    <li>
            <%-- Element Search --%>
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
	    </ul>
	  </div>
	  <!-- XXX this should go, too -->
          </font>
</span>
</span>

</div>

<!--
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
-->
