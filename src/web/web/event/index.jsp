<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Events | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Events"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Events" />
  <jsp:param name="location" value="event" />  
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">     
      <h3>Event Queries</h3>
      
      <jsp:include page="/event/querypanel.jsp" flush="false" />

      <p><a href="event/list" title="View all outstanding events">View all events</a></p>
      <p><a href="event/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></p>

      <p>      
        <table width="50%" border="0" cellpadding="2" cellspacing="0" >
          <tr>
            <td colspan="2">Get&nbsp;details&nbsp;for&nbsp;Event&nbsp;ID:</td>
          </tr>
          <tr>
            <form action="event/detail.jsp" method="GET">          
              <td><input type="TEXT" NAME="id" /></td>
              <td><input type="submit" value="Search"/></td>                
            </form>
          </tr>                    
        </table>
      </p>
      
    </td>

    <td>&nbsp;</td>

    <td valign="top" width="60%">
      <h3>Outstanding and acknowledged events</h3>

      <p>Events can be <em>acknowledged</em>, or removed from the view of other users, by
        selecting the event in the <em>Ack</em> check box and clicking the <em>Acknowledge
        Selected Events</em> at the bottom of the page.  Acknowledging an event gives
        users the ability to personally take responsibility for addressing a network
        or systems-related issue.  Any event that has not been acknowledged is
        active in all users' browsers and is considered <em>outstanding</em>.
      </p>
            
      <p>If an event has been acknowledged in error, you can select the appropriate
        <em>View all acknowledged events</em> link, find the event, and <em>unacknowledge</em> it,
        making it available again to all users' views.
      </p>
        
      <p>If you have a specific event identifier for which you want a detailed event
        description, type the identifier into the <em>Get details for Event ID</em> box and
        hit <b>[Enter]</b>.  You will then go to the appropriate details page.
      </p>
    </td>

     <td> &nbsp; </td>
  </tr>
</table>                                    
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="event" />
</jsp:include>

</body>
</html>
