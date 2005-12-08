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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.text.DateFormat,
		java.io.File,
		java.util.LinkedList,
		java.util.Iterator,
		org.opennms.web.authenticate.Authentication,
		org.opennms.netmgt.config.NotifdConfigFactory
		"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

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
    File mapEnableFile = new File("@install.etc.dir@/map.enable");

%>

<!-- Header -->



<!-- Start of new box stuff -->
<div class="rbroundbox">
 <div class="rbtop">
  <div></div>
 </div>

 <div class="rbcontent">
  <!-- <p> -->

<div id="header">


  <div id="headertop">
   <span id="headerlogo">
    <a href="index.jsp"><img src="images/logo.png" hspace="0" vspace="0" border="0" alt="OpenNMS Web Console Home"></a>
   </span><!-- /headerlogo -->
  
   <span id="headertitle">
    <%=title%>
   </span><!-- /headertitle -->
  
   <span id="headerinfo">
    <div id="outer">
     <div id="middle">
      <div id="inner">
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
          </div><!-- /headerdate -->
      </div><!-- /inner -->
     </div><!-- /middle -->
    </div><!-- /outer -->
   </span><!-- /headerinfo -->
  
   <div class="spacer">
    &nbsp;
   </div><!-- /spacer -->
  
  </div><!-- /headertop -->

 <%!
	public class NavBarEntry {
		private String m_locationMatch;
		private String m_URL;
		private String m_name;

		public NavBarEntry(String locationMatch, String URL, String name) {
			m_locationMatch = locationMatch;
			m_URL = URL;
			m_name = name;
		}

		public String getLocationMatch() {
			return m_locationMatch;
		}

		public String getURL() {
			return m_URL;
		}

		public String getName() {
			return m_name;
		}

		public boolean isMatchingLocation(String locationMatch) {
			return m_locationMatch.equals(locationMatch);
		}
	}
 %>

 <%

	LinkedList headerNavBar = new LinkedList();
	headerNavBar.add(new NavBarEntry("nodelist", "element/nodelist.jsp", "Node List"));
	headerNavBar.add(new NavBarEntry("element", "element/index.jsp", "Search"));
	headerNavBar.add(new NavBarEntry("outages", "outage/index.jsp", "Outages"));
	headerNavBar.add(new NavBarEntry("event", "event/index.jsp", "Events"));
	headerNavBar.add(new NavBarEntry("alarm", "alarm/index.jsp", "Alarms"));
	headerNavBar.add(new NavBarEntry("notification", "notification/index.jsp", "Notification"));
	headerNavBar.add(new NavBarEntry("asset", "asset/index.jsp", "Assets"));
	//headerNavBar.add(new NavBarEntry("security", "security.jsp", "Security"));
	headerNavBar.add(new NavBarEntry("report", "report/index.jsp", "Reports"));
	if (mapEnableFile.exists()) {
	  headerNavBar.add(new NavBarEntry("map", "map/index.jsp", "Map"));
	} 
	if(request.isUserInRole(Authentication.ADMIN_ROLE)) {
	  headerNavBar.add(new NavBarEntry("admin", "admin/index.jsp", "Admin"));
	}
	headerNavBar.add(new NavBarEntry("help", "help/index.jsp", "Help"));

	request.setAttribute("headerNavBar", headerNavBar);
	request.setAttribute("location", location);

	request.setAttribute("breadcrumbs", breadcrumbs);
 %>


<span id="headernavbar">
          <span id="headernavbarleft">
            <a href="index.jsp">Home</a> 
	    <c:forEach var="breadcrumb" items="${breadcrumbs}">
              &gt; <c:out value="${breadcrumb}" escapeXml="false"/>
	    </c:forEach>
	  </span>

          <span id="headernavbarright">

   	  <div id="navbar">
	  <ul>
	    <c:forEach var="headerNavEntry" items="${headerNavBar}">
	      <c:choose>
	        <c:when test="${headerNavEntry.name == 'Help'}">
	          <li class="last">
	        </c:when>
		<c:otherwise>
	          <li>
		</c:otherwise>
	      </c:choose>

	      <c:choose>
	        <c:when test="${location == headerNavEntry.locationMatch}">
	          <c:out value="${headerNavEntry.name}"/>
	        </c:when>
		<c:otherwise>
	          <a href="<c:out value="${headerNavEntry.URL}"/>"><c:out value="${headerNavEntry.name}"/></a>
		</c:otherwise>
	      </c:choose>
              </li>
	      </c:forEach>
	    </ul>
	  </div>
</span>
</span>


</div>


  <!-- End of new header -->
  <!-- </p> -->
 </div><!-- /rbcontent -->

 <div class="rbbot">
  <div></div>
 </div>
</div><!-- /rbroundbox -->

