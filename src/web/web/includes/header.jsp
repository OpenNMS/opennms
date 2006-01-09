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
		java.util.Date,
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
        } catch (Throwable t) {
	    // notice status will be unknown if the factory can't be initialized
	}
    }
%>

<%
    Date now = new Date(); 
    pageContext.setAttribute("date", dateFormatter.format(now));
    pageContext.setAttribute("time", timeFormatter.format(now));

    String noticeStatus;
    try {
        noticeStatus = NotifdConfigFactory.getInstance().getPrettyStatus();
    } catch (Throwable t) {
        noticeStatus = "<font color=\"ff0000\">Unknown</font>";
    }
    pageContext.setAttribute("noticeStatus", noticeStatus);
%>


<!-- Header -->



<%--

<!-- Start of new box stuff -->
<div class="rbroundbox">
 <div class="rbtop">
  <div></div>
 </div>

 <div class="rbcontent">
  <!-- <p> -->
--%>

<div id="header">


  <div id="headertop">
   <span id="headerlogo">
    <a href="index.jsp"><img src="images/logo.png" hspace="0" vspace="0" border="0" alt="OpenNMS Web Console Home"></a>
   </span><!-- /headerlogo -->
  
   <span id="headertitle">
    <c:out value="${param.title}"/>
   </span><!-- /headertitle -->
  
   <span id="headerinfo">
    <div id="outer">
     <div id="middle">
      <div id="inner">
          [<c:out value="${pageContext.request.remoteUser}"/>]<br>
    
          Notices <c:out value="${noticeStatus}" escapeXml="false"/><br/>
          <div id="headerdate">
            <c:out value="${date}"/><br/>
            <c:out value="${time}"/>
          </div><!-- /headerdate -->
      </div><!-- /inner -->
     </div><!-- /middle -->
    </div><!-- /outer -->
   </span><!-- /headerinfo -->
  
   <div class="spacer">
    &nbsp;
   </div><!-- /spacer -->
  
  </div><!-- /headertop -->



<span id="headernavbar">
          <span id="headernavbarleft">
            <a href="index.jsp">Home</a> 
	    <c:forEach var="breadcrumb" items="${paramValues.breadcrumb}">
              &gt; <c:out value="${breadcrumb}" escapeXml="false"/>
	    </c:forEach>
	  </span>

          <span id="headernavbarright">
            <jsp:include page="/includes/navbar.jsp" flush="false"/>
          </span>
</span>


</div>


<%--
  <!-- End of new header -->
  <!-- </p> -->
 </div><!-- /rbcontent -->

 <div class="rbbot">
  <div></div>
 </div>
</div><!-- /rbroundbox -->

--%>
