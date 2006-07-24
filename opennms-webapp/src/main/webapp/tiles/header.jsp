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

<%@page language="java" contentType="text/html" session="true" %>
<%@taglib prefix="tiles" uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>


<!-- Header -->

<div id="header">
<div id="headertop">
<span id="headerlogo">
      <a href="index.jsp"><img src="images/logo.png" hspace="0" vspace="0" border="0" alt="Home"></a>
</span>

<span id="headertitle">
      <tiles:getAsString name="title"/>
</span>

<span id="headerinfo">

<div id="outer">
 <div id="middle">
  <div id="inner">

      OGP Member<br/>
      Welcome<br/>
      <div id="headerdate">
	      <jsp:useBean id="now" class="java.util.Date" />
		  <fmt:formatDate value="${now}" dateStyle="long"/>
        <br/>
		  <fmt:formatDate value="${now}" timeStyle="long"/>
      </div>
  </div>
 </div>
</div>
</span>

<div class="spacer">
     &nbsp;
</div>

</div>


<span id="headernavbar">
	<span id="headernavbarleft">
            <a href="index.jsp">Home</a> 
	</span>

    <span id="headernavbarright">
    		You finally made it!
	</span>
</span>

</div>
