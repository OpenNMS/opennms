<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,java.text.*,org.opennms.netmgt.config.users.*"%>
<%
	User user = null;
	UserFactory userFactory = UserFactory.getInstance();
  	String userID = request.getParameter("userID");
	try
  	{
		UserFactory.init();
      		user = userFactory.getUser(userID);
  	}
	catch (Exception e)
  	{
      		throw new ServletException("Could not find user " + userID + " in user factory.", e);
  	}
%>
<html>
<head>
<title>User Detail | User Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/userGroupView/index.jsp'>Users and Groups</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("<a href='admin/userGroupView/users/list.jsp'>User List</a>"); %>
<% String breadcrumb4 = java.net.URLEncoder.encode("User Detail"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="User Detail" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>

<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>
    
    <td>
    <table width="100%" border="0" cellspacing="0" cellpadding="2" >
      <tr>
        <td>
          <h2>Details for User: <%=user.getUserId()%></h2>
          <table width="100%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td width="10%" valign="top">
                <b>Full Name:</b>
              </td>
              <td width="90%" valign="top">
                <%=user.getFullName()%>
              </td>
            </tr>
            
            <tr>
              <td width="10%" valign="top"> 
                <b>Comments:</b>
              </td>
              <td width="90%" valign="top">
                <%=user.getUserComments()%>
              </td>
            </tr>
          </table>
        </td>
      </tr>
     <br>
      <tr>
        <td>
          
            <table width="100%" border="0" cellspacing="0" cellpadding="2" >
              
              <tr>
                <td>
                  <table>
                    <tr>
                      <td>
                        <b>Notification Information</b>
                      </td>
                    </tr>
                    <tr>
                      <td width="10%" valign="top">
                        <b>Email:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Pager Email:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getPagerEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Numerical Service:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getNumericPage(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Numerical Pin:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getNumericPin(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Text Service:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getTextPage(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Text Pin:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getTextPin(userID)%>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <tr>
                <td>
                <b>Duty Schedules:</b>
                  
                      <table width="50%" border="1" cellspacing="0" cellpadding="2" >
			<% Collection dutySchedules = user.getDutyScheduleCollection(); %>
                        <%
                                int i =0;
                                Iterator iter = dutySchedules.iterator();
                                while(iter.hasNext())
                                {  
                                        DutySchedule tmp = new DutySchedule((String)iter.next());
                                        Vector curSched = tmp.getAsVector();        
					i++;
                              
                        %>
                        <tr>
                           <% ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");
                             for (int j = 0; j < 7; j++)
                             {
                                Boolean curDay = (Boolean)curSched.get(j);
                          %>
                          <td width="5%">
                           <%= (curDay.booleanValue() ? days.format(j) : "X")%>
                          </td>
                          <% } %>
                          <td width="5%">
                            <%=curSched.get(7)%>
                          </td>
                          <td width="5%">
                            <%=curSched.get(8)%>
                          </td>
                        </tr>
                        <% } %>
                      </table>
                </td>
              </tr>
        
          </table>
      </table>
   </td>
   <td>&nbsp;</td>
   </tr>
 </table>
 
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
</jsp:include>
</body>
</html>
