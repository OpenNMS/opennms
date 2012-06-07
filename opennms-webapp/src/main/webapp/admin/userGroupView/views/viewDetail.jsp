<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.config.*,
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.views.*,
		org.opennms.web.servlet.MissingParameterException
	"
%>

<%
	View view = null;
  	String viewName = request.getParameter("viewName");
	if (viewName == null) {
		throw new MissingParameterException("viewName");
	}
	try {
		ViewFactory.init();
		ViewFactory viewFactory = ViewFactory.getInstance();
      		view = viewFactory.getView(viewName);
  	} catch (Throwable e) {
      		throw new ServletException("Could not find view " + viewName + " in view factory.", e);
  	}
	if (view == null) {
      		throw new ServletException("Could not find view " + viewName + " in view factory.");
	}
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="View Detail" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTitle" value="Views" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups, and Views</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/views/list.jsp'>Views List</a>" />
  <jsp:param name="breadcrumb" value="View Detail" />
</jsp:include>

          <h2>Details for View: 
		<% if(view != null){ %>
		<%= view.getName() %>
		<% } %>
	  </h2>
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
          
          <h2>User Members</h2>
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

          <h2>Group Members</h2>
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

                <h2>Categories</h2>
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

<jsp:include page="/includes/footer.jsp" flush="false"/>
