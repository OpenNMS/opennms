<%--
  This page is included by other JSPs to create a box containing a
  table that provides links for notification queries.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.notification.*" %>

<%!
    protected NotificationModel model = new NotificationModel();
    protected java.text.ChoiceFormat formatter = new java.text.ChoiceFormat( "0#No outstanding notices|1#1 outstanding notice|2#{0} outstanding notices" );
%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" bgcolor="#cccccc">
  <tr> 
    <td bgcolor="#999999" ><b><a href="notification/index.jsp">Notification</a></b></td>
  </tr>
  <tr> 
    <td>
      <table width="100%" border="0" cellspacing="0" cellpadding="1">
        <tr>
          <td>
            <a href="notification/browse?akctype=unack&filter=<%=java.net.URLEncoder.encode("user="+request.getRemoteUser())%>">Check Your Notices</a>
          </td>
        </tr>
        <tr>
          <td>
            <a href="notification/browse?acktype=unack">Check All Open Notices</a>
          </td>
        </tr>
        <tr>
          <td>
            <%
                int count = this.model.getOutstandingNoticeCount();
                String format = this.formatter.format( count );
                out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
             %>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
