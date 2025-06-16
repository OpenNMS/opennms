<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
%>
<%@page import="java.util.*" %>
<%@page import="org.opennms.netmgt.config.*" %>
<%@page import="org.opennms.netmgt.config.users.*" %>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
	UserManager userFactory;
  	Map<String,User> users = null;
	
	try
    	{
		UserFactory.init();
		userFactory = UserFactory.getInstance();
      		users = userFactory.getUsers();
	}
	catch(Throwable e)
	{
		throw new ServletException("User:list " + e.getMessage());
	}
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("List")
          .headTitle("Users")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups", "admin/userGroupView/index.jsp")
          .breadcrumb("User List")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

    function addNewUser()
    {
        document.allUsers.action="admin/userGroupView/users/newUser.jsp?action=new";
        document.allUsers.submit();
        
    }
    
    function detailUser(userID)
    {
        document.allUsers.action="admin/userGroupView/users/userDetail.jsp?userID=" + userID;
        document.allUsers.submit();
    }
    
    function deleteUser(userID)
    {
        document.allUsers.action="admin/userGroupView/users/deleteUser";
        document.allUsers.userID.value=userID;
        document.allUsers.submit();
    }
    
    function modifyUser(userID)
    {
        document.allUsers.action="admin/userGroupView/users/modifyUser";
        document.allUsers.userID.value=userID;
        document.allUsers.submit();
    }
    
    function renameUser(userID)
    {
        document.allUsers.userID.value=userID;
        var newID = prompt("Enter new name for user.", userID);

        if (newID != null && newID != "") {
          if (/.*[&<>"`']+.*/.test(newID)) {
            alert("The user ID must not contain any HTML markup.");
            return;
          }

          var element =  document.getElementById('users(' + _.escape(newID) + ').doModify');
          if (typeof(element) != 'undefined' && element != null) {
            alert("A user with this ID already exist.");
            return;
          }

          document.allUsers.newID.value = newID;
          document.allUsers.action="admin/userGroupView/users/renameUser";
          document.allUsers.submit();
        }
    }
    
</script>


<form method="post" name="allUsers">
<input type="hidden" name="redirect"/>
<input type="hidden" name="userID"/>
<input type="hidden" name="newID"/>
<input type="hidden" name="password"/>
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

<p>
  Click on the <i>User ID</i> link to view detailed information about a
  user.
</p>

<p>
  <a id="doNewUser" href="javascript:addNewUser()">
    <i class="fa fa-plus-circle fa-2x"></i> Add new user
  </a>
</p>

   <div class="card">
     <table class="table table-sm table-bordered">
        <thead>
          <tr>
          <th width="5%">Delete</th>
          <th width="5%">Modify</th>
          <th width="5%">Rename</th>
          <th width="5%">User ID</th>
          <th width="15%">Full Name</th>
          <th width="15%">Email</th>
          <th width="15%">Pager Email</th>
          <th width="15%">XMPP Address</th>
          </tr>
        </thead>
        <tbody>
        <% 
           int row = 0;
           for (User curUser : users.values()) {
	      String userid = curUser.getUserId();
	      String email = userFactory.getEmail(userid);
	      String pagerEmail = userFactory.getPagerEmail(userid);
	      String xmppAddress = userFactory.getXMPPAddress(userid);
	      String numericService = userFactory.getNumericPage(userid);
	      String textService = userFactory.getTextPage(userid);
	      String numericPin = userFactory.getNumericPin(userid);
	      String textPin = userFactory.getTextPin(userid);
	      String sanitizedUserId = WebSecurityUtils.sanitizeString(curUser.getUserId());
         %>
         <tr id="user-<%= userid %>">
          <% if (!curUser.getUserId().equals("admin") && !curUser.getUserId().equals("rtc")) { %>
          <td rowspan="2" class="text-center"> 
            <a id="<%= "users("+sanitizedUserId+").doDelete" %>" href="javascript:deleteUser('<%=sanitizedUserId%>')" onclick="return confirm('Are you sure you want to delete the user <%=sanitizedUserId%>?')"><i class="fa fa-trash-o fa-2x"></i></a>
          </td>
          <% } else { %>
          <td rowspan="2" class="text-center">
            <i class="fa fa-trash-o fa-2x" onclick="alert('Sorry, the admin user cannot be deleted.')"></i>
          </td>
          <% } %>
          <td rowspan="2" class="text-center">
            <a id="<%= "users("+sanitizedUserId+").doModify" %>" href="javascript:modifyUser('<%=sanitizedUserId%>')"><i class="fa fa-edit fa-2x"></i></a>
          </td>
          <td rowspan="2" class="text-center">
            <% if ( !curUser.getUserId().equals("admin")) { %>
                <button id="<%= "users("+sanitizedUserId+").doRename" %>" class="btn btn-secondary"  name="rename" onclick="renameUser('<%=sanitizedUserId%>')">Rename</button>
              <% } else { %>
                <button id="<%= "users("+sanitizedUserId+").doRename" %>" class="btn btn-secondary"  name="rename" onclick="alert('Sorry, the admin user cannot be renamed.')">Rename</button>
              <% } %>
          </td>
          <td>
            <a id="<%= "users("+sanitizedUserId+").doDetails" %>" href="javascript:detailUser('<%=sanitizedUserId%>')"><%=sanitizedUserId%></a>
          </td>
          <td>
           <div id="<%= "users("+sanitizedUserId+").fullName" %>" ng-non-bindable>
		    <%= (curUser.getFullName().orElse("")) %>
	      </div>
          </td>
          <td>
            <div id="<%= "users("+sanitizedUserId+").email" %>" ng-non-bindable>
            <%= ((email == null || email.equals("")) ? "&nbsp;" : email) %>
            </div>
          </td>
          <td>
           <div id="<%= "users("+sanitizedUserId+").pagerEmail" %>" ng-non-bindable>
            <%= ((pagerEmail == null || pagerEmail.equals("")) ? "&nbsp;" : pagerEmail) %>
            </div>
          </td>
          <td>
           <div id="<%= "users("+sanitizedUserId+").xmppAddress" %>" ng-non-bindable>
            <%= ((xmppAddress == null || xmppAddress.equals("")) ? "&nbsp;" : xmppAddress) %>
           </div>
          </td>
          </tr>
          <tr>
            <td colspan="5">
             <div id="<%= "users("+sanitizedUserId+").userComments" %>" ng-non-bindable>
             <%= WebSecurityUtils.sanitizeString(curUser.getUserComments().orElse("No Comments")) %>
	        </div>
            </td>
          </tr>
         <% row++;
            } %>
       </tbody>
     </table>
  </div> <!-- panel -->
</form>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="underscore-js" />
</jsp:include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
