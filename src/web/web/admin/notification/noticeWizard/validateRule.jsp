<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.admin.notification.noticeWizard.*,org.opennms.web.Util,org.opennms.netmgt.filter.Filter,org.opennms.netmgt.filter.FilterParseException,org.opennms.netmgt.config.*,org.opennms.netmgt.config.notifications.*" %>

<%!
    public void init() throws ServletException {
        try {
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<% HttpSession user = request.getSession(true);
   Notification newNotice = (Notification)user.getAttribute("newNotice");
   String newRule = (String)request.getParameter("newRule");
   String services[] = request.getParameterValues("services");
   if (services==null)
      services = new String[0];
%>

<html>
<head>
  <title>Validate Rule | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
  
  function next()
  {
      document.addresses.userAction.value="next";
      document.addresses.submit();
  }
  
  function rebuild()
  {
      document.addresses.userAction.value="rebuild";
      document.addresses.submit();
  }
  
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/notification/index.jsp'>Configure Notifications</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Validate Rule"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Validate Rule" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h2><%=(newNotice.getName()!=null ? "Editing notice: " + newNotice.getName() + "<br>" : "")%></h2>
      <h3>Check the TCP/IP addresses below to ensure that the rule has given the expected results. If it hasn't click the
          'Rebuild' link below the table. If the results look good continue by clicking the 'Next' link also below the table.</h3>
      <table width="100%" cellspacing="0" cellpadding="0" border="0">
        <tr>
          <td width="10%">Current Rule:
          </td>
          <td align="left"> <%=newRule%>
          </td>
      </table>
      <br>
      <form METHOD="POST" NAME="addresses" ACTION="admin/notification/noticeWizard/notificationWizard">
        <%=Util.makeHiddenTags(request)%>
        <input type="hidden" name="userAction" value=""/>
        <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_VALIDATE%>"/>
        <table width="25%" cellspacing="2" cellpadding="2" border="1">
          <tr bgcolor="#999999">
            <td width="50%">
              <b>Interfaces</b>
            </td>
            <td width="50%">
              <b>Services Associated with the Interfaces</b>
            </td>
          </tr>
          <%=buildInterfaceTable(newRule, services)%>
        </table>
        <table 
        <table width="100%" cellspacing="2" cellpadding="2" border="0">
         <tr> 
          <td>
           <a HREF="javascript:rebuild()">&#060;&#060;&#060; Rebuild</a>&nbsp;&nbsp;&nbsp;
           <a HREF="javascript:next()">Next &#062;&#062;&#062;</a>
          </td>
        </tr>
        </table>
      </form>
      
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%!
  public String buildInterfaceTable(String rule, String[] serviceList)
      throws FilterParseException
  {
          StringBuffer buffer = new StringBuffer();
          Filter filter = new Filter();
          //return filter.getIPServiceMap(rule);
          
          Map interfaces = filter.getIPServiceMap(rule);
          
          Iterator i = interfaces.keySet().iterator();
          while(i.hasNext())
          {
              String key = (String)i.next();
              buffer.append("<tr><td width=\"50%\" valign=\"top\">").append(key).append("</td>");
              buffer.append("<td width=\"50%\">");
              
              if (serviceList.length!=0)
              {
                  Map services = (Map)interfaces.get(key);
                  Iterator j = services.keySet().iterator();
                  while(j.hasNext())
                  {
                      buffer.append((String)j.next()).append("<br>");
                  }
              }
              else
              {
                  buffer.append("All services");
              }
              buffer.append("</td>");
                  
              buffer.append("</tr>");
          }
          
          return buffer.toString();
  }
%>
