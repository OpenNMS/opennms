<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.notification.*" %>

<%!
    NotificationModel model = new NotificationModel();
%>

<%
    String username = request.getParameter( "username" );
    
    
    if( username == null ) {
        throw new org.opennms.web.MissingParameterException( "username" );
    }

    //allow all users to acknowlege notices even if they don't own them
    boolean editable = true; //request.getRemoteUser().equals( username );
    Notification[] notices = this.model.getOutstandingNotices( username );   
%>

<html>
<head>
  <title>Notifications Listing | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
    function checkAllCheckboxes() 
    {
       if (verifyUser())
       {
          for( i = 0; i < document.acknowledge_form.notices.length; i++ ) 
          {
              document.acknowledge_form.notices[i].checked = true
          }
          submitForm();
       }
    }
    
    function submitChecked()
    {
        if (verifyUser())
        {
            submitForm();
        }
    }
    
    function submitForm()
    {
        document.acknowledge_form.submit();
    }
    
    function verifyUser()
    {
        if (document.acknowledge_form.notifUser.value != document.acknowledge_form.curUser.value)
        {
            return confirm("You are not the owner of these notifications. Are you sure you want to acknowledge them?");
        }
        return true;
    }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='notification/index.jsp'>Notification</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("List"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notifications Listing" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
      <h3>Notifications for <%=username%> </h3>

<%  if( editable ) { %>
      <form METHOD="post" ACTION="notification/acknowledge.jsp" name="acknowledge_form" >
        <input type="hidden" name="notifUser" value="<%=username%>">
        <input type="hidden" name="curUser" value="<%=request.getRemoteUser()%>">
<%  }                %>
        <table WIDTH="100%" BORDER="1" CELLSPACING="0" CELLPADDING="2" bordercolor="#666666">
          <tr BGCOLOR="#999999">
<% if( editable ) { %>
            <th WIDTH="5%">Ack </th>
<% }                %>
          
            <th WIDTH="5%">Notice</th>
            <th WIDTH="5%">Event ID</th>
            <th WIDTH="25%">Time Sent</th>
            <th>Message</th>
          </tr>

<%      for( int i = 0; i < notices.length; i++ ) {  %>
          <tr <% if( i%2 == 0 ) out.print( "BGCOLOR=\"#cccccc\""); %> valign="top">

<!--all users can acknowlege any notice -->          
<%         if( editable ) {  %>            
            <td>
              <input TYPE="checkbox" name="notices" VALUE="<%=notices[i].getId()%>" />
            </td>
<%          } %>
            <td><a HREF="notification/detail.jsp?notice=<%=notices[i].getId()%>"><%=notices[i].getId()%></a></td>
            <td><a href="event/detail.jsp?id=<%=notices[i].getEventId()%>"><%=notices[i].getEventId()%></a></td>
            <td><%=notices[i].getTimeSent()%></td>            
            <td><%=notices[i].getTextMessage()%></td>
          </tr>
<%      } %>

        </table>
        <br>

<%      if( editable ) {  %>
        <input TYPE="button" VALUE="Acknowledge" onClick="submitChecked()"/>
        <input TYPE="button" VALUE="Acknowledge All" onClick="checkAllCheckboxes()"/>
        <input TYPE="reset" />

        <p><font SIZE="-1">       
          Check the boxes next to the notices you are acknowledging and then click the
          <em>Acknowledge</em> button.  
          <br>
          Or to acknowledge all the listed notices, click the
          <em>Acknowledge All</em> button.
          <br>
          Or click the <em>Reset</em> button to clear all selections.
        </font></p>

      </form>
<%     } %>
    </td>

    <td>&nbsp;</td>

  </tr>
</table>
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
