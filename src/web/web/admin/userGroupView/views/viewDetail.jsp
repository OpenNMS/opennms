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

<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,java.text.*,org.opennms.netmgt.config.views.*"%>
<%
	View view = null;
  	String viewName = request.getParameter("viewName");
	try
  	{
		ViewFactory.init();
		ViewFactory viewFactory = ViewFactory.getInstance();
      		view = viewFactory.getView(viewName);
  	}
	catch (Exception e)
  	{
      		throw new ServletException("Could not find view " + viewName + " in view factory.", e);
  	}
%>
<html>
<head>
<title>View Detail | User Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/userGroupView/index.jsp'>Users, Groups, and Views</a>"; %>
<% String breadcrumb3 = "<a href='admin/userGroupView/views/list.jsp'>Views List</a>"; %>
<% String breadcrumb4 = "View Detail"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="View Detail" />
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
        <td colspan="3">
          <h2>Details for View: 
		<% if(view != null){ %>
		<%= view.getName() %></h2>
		<% } %>
          <table width="100%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td width="10%" valign="top"> 
                <b>Title:</b>
              </td>
              <td width="90%" valign="top">
		<% if(view != null){ String title = view.getTitle(); %>
                <%= (title != null || title.equals(""))? "":title %>
		<% } %>
              </td>
            </tr>
            <tr>
              <td width="10%" valign="top"> 
                <b>Comments:</b>
              </td>
              <td width="90%" valign="top">
		<% if(view != null){ String comment = view.getComment(); %>
                <%= (comment != null || comment.equals(""))? "":comment %>
		<% } %>
              </td>
            </tr>
            <tr>
              <td width="10%" valign="top"> 
                <b>Common Rule:</b>
              </td>
              <td width="90%" valign="top">
		<% if(view != null){ Common common = view.getCommon(); %>
                <%= (common != null || common.getRule().equals(""))? "":common.getRule() %>
                <% } %> 
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td align="left" valign="top">
          <b>User Members:</b>
          <table width="20%" border="1" cellspacing="0" cellpadding="2" >
            <%  Membership membership= view.getMembership();
		if(membership != null)
		{
	                Enumeration enummember = membership.enumerateMember();
        	        List users = new ArrayList();
                	while(enummember != null && enummember.hasMoreElements())
	                {
        	                Member member = (Member)enummember.nextElement();
                	        if(member.getType().equals("user"))
                        	        users.add(member.getContent());
	                }  
			if(users != null){
	               for (int i = 0; i < users.size(); i++) { %>
               <tr>
                 <td>
                   <%=(String)users.get(i)%>
                 </td>
               </tr>
               <% }}} %>
          </table>
          
        </td>
        <td align="left" valign="top">
          <b>Group Members:</b>
          <table width="20%" border="1" cellspacing="0" cellpadding="2" >
            <%  membership= view.getMembership();
		if(membership != null)
		{
			Enumeration enummember = membership.enumerateMember();
			List groups = new ArrayList();
			while(enummember != null && enummember.hasMoreElements())
			{
				Member member = (Member)enummember.nextElement();
				if(member.getType().equals("group"))
					groups.add(member.getContent());
			}
			if(groups != null){
		       for (int i = 0; i < groups.size(); i++) { %>
               <tr>
                 <td>
                   <%=(String)groups.get(i)%>
                 </td>
               </tr>
              <% }}} %>
          </table>
        </td>
        <td width="100%">
          &nbsp;
        </td>
        
      </tr>
      <tr>
        <td colspan="3">
          <table width="100%" border="0" cellspacing="0" cellpadding="2" >
            <tr>
              <td>
                <b>Categories:</b>
                <table width="100%" border="1" cellspacing="0" cellpadding="2" >
                  <tr bgcolor="#999999">
                    <td width="5%"><b>Label</b></td>
                    <td width="5%"><b>Normal</b></td>
                    <td width="5%"><b>Warning</b></td>
                    <td width="85%"><b>Rule</b></td>
                  </tr>
                  <% Categories categories = view.getCategories();
		     Collection catcoll = categories.getCategoryCollection();
                     Iterator iter = catcoll.iterator();
		     while (iter.hasNext()) 
                     { 
                        Category curCategory = (Category)iter.next();
                  %>
                    <tr>
                      <td>
                        <%= (curCategory == null)? "" : curCategory.getLabel() %>
                      </td>
                      <td>
                        <%= curCategory.getNormal()%>
                      </td>
                      <td>
                        <%= curCategory.getWarning()%>
                      </td>
                      <td>
                        <%=  (curCategory == null)? "" : curCategory.getRule()%>
                      </td>
                    <tr>
                  <% } %>
                </table>
              </td>
            </tr>
          </table>
        </td>
        
      </tr>
    </table>
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>
 
<br>

<jsp:include page="/includes/footer.jsp" flush="false">
</jsp:include>
</body>
</html>
