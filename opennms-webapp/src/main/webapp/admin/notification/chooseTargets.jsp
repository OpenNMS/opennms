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
	import="java.util.*,
			org.opennms.core.utils.WebSecurityUtils,
            org.opennms.web.api.Util,
            org.opennms.netmgt.config.*,
            org.opennms.netmgt.config.destinationPaths.*"
%>

<%!public void init() throws ServletException {
        try {
            UserFactory.init();
            GroupFactory.init();
            DestinationPathFactory.init();
        } catch (Throwable e) {
            throw new ServletException("Cannot load configuration file", e);
        }
    }%>

<%
            HttpSession user = request.getSession(true);
            Path newPath = (Path) user.getAttribute("newPath");

            Collection<Target> targets = null;

            int index = WebSecurityUtils.safeParseInt(request.getParameter("targetIndex"));
            if (index < 0) {
                targets = newPath.getTargets();
            } else {
                targets = newPath.getEscalates().get(index).getTargets();
            }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Choose Targets")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications", "admin/notification/index.jsp")
          .breadcrumb("Destination Paths", "admin/notification/destinationPaths.jsp")
          .breadcrumb("Choose Targets")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

    function next() 
    {
        if (document.targets.groups.selectedIndex >= 0)
        {
            selectAllEmails();
            document.targets.nextPage.value="groupIntervals.jsp";
            document.targets.submit();
        } 
        else if (document.targets.users.selectedIndex >= 0)
        {
            selectAllEmails();
            document.targets.nextPage.value="chooseCommands.jsp";
            document.targets.submit();
        }
        else if (document.targets.roles.selectedIndex >= 0)
        {
        		selectAllEmails();
            document.targets.nextPage.value="chooseCommands.jsp";
            document.targets.submit();
        }
        else if (document.targets.emails.length>0)
        {
            selectAllEmails();
            // document.targets.nextPage.value="pathOutline.jsp";
            document.targets.nextPage.value="chooseCommands.jsp";
            document.targets.submit();
        }
        else
        {
            alert("Please choose at least one user, group, or email address as a target.");
        }
    }
    
    function selectAllEmails()
    {
        //select all emails to they get sent to the servlet
        for (i=0; i < document.targets.emails.length; i++) 
        {
            document.targets.emails.options[i].selected = true;
        }
    }
    
    function addAddress()
    {
        var address = prompt("Please type in an email address.");
        
        if (address!="")
        {
            if(address.indexOf("@",0)==-1)
            {
                alert("The address '"+address+"' does not contain an '@' symbol and may be confused with a user or group name. Please enter a new email address.");
            }
        else
        {
                document.targets.emails.options[document.targets.emails.length]= new Option(address);
            }
        }
    }
    
    function removeAddress()
    {
        if (document.targets.emails.selectedIndex >=0)
        {
            for ( i=(document.targets.emails.length-1); i>=0; i--) 
            {
                if (document.targets.emails.options[i].selected == true ) 
                {
                    document.targets.emails.options[i] = null;
                }
            }
        }
        else
        {
            alert("To remove an address please select it in the list.");
        }
    }

</script>


<h2><%=(newPath.getName() != null ? "Editing path: "
                            + newPath.getName() + "<br/>" : "")%></h2>

<form method="post" name="targets"
action="admin/notification/destinationWizard" >
<%=Util.makeHiddenTags(request)%>
<input type="hidden" name="sourcePage" value="chooseTargets.jsp"/>
<input type="hidden" name="nextPage"/>

<div class="card">
  <div class="card-header">
    <span>Choose the users and groups to send the notice to.</span>
  </div>
  <table class="table table-sm table-borderless">
        <tr>
          <td><h3>Send to Selected Users:</h3></td>
          <td><h3>Send to Selected Groups:</h3></td>
          <td><h3>Send to Selected Roles:</h3></td>
          <td><h3>Send to Email Addresses:</h3></td>
        </tr>
        <tr>
          <td>Highlight each user that needs to receive the notice.</td>
          <td>Highlight each group that needs to receive the notice. Each user in the group
              will receive the notice.</td>
          <td>Highlight each role that needs to receive the notice. The users scheduled for the time that the notification comes in
              will receive the notice.</td>
          <td>Add any email addresses you want the notice to be sent to.</td>
        </tr>
        <tr>
          <td width="25%">
            <select class="form-control custom-select" name="users" multiple>
             <%
                         for (Map.Entry<String,Boolean> entry : getUsers(targets).entrySet()) {
                             String key = entry.getKey();
                             if (entry.getValue().booleanValue()) {
             %>
                    <option selected VALUE=<%=key%>><%=key%></option>
            <%
            } else {
            %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%
                        }
                        }
            %>
            </select>
          </td>
          <td width="25%">
            <select class="form-control custom-select" name="groups" multiple>
             <%
                         for (Map.Entry<String,Boolean> entry : getGroups(targets).entrySet()) {
                             String key = entry.getKey();
                             if (entry.getValue().booleanValue()) {
             %>
                    <option selected VALUE=<%=key%>><%=key%></option>
            <%
            } else {
            %>
                    <option VALUE="<%=key%>"><%=key%></option>
            <%
                        }
                        }
            %>
            </select>
           </td>
          <td width="25%">
            <select class="form-control custom-select" name="roles" multiple>
             <%
                     for (Map.Entry<String,Boolean> entry : getRoles(targets).entrySet()) {
                         String key = entry.getKey();
                         if (entry.getValue().booleanValue()) {
             %>
                    <option selected VALUE=<%=key%>><%=key%></option>
            <%
            } else {
            %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%
                        }
                        }
            %>
            </select>
           </td>
           <td width="25%">
            <select class="form-control custom-select mb-3" name="emails" multiple>
             <%
                 for (String key : getEmails(targets).keySet()) {
             %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%
            }
            %>
            </select>
            <input type="button" class="btn btn-secondary" value="Add Address" onclick="javascript:addAddress()"/>
            <input type="button" class="btn btn-secondary" value="Remove Selected Addresses" onclick="javascript:removeAddress()"/>
            </td>
            
        </tr>
  </table>
  <div class="card-footer">
      <input type="reset" class="btn btn-secondary"/>
      <a class="btn btn-secondary" href="javascript:next()">Next Step <i class="fa fa-arrow-right"></i></a>
  </div>
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
public Map<String,Boolean> getUsers(Collection<Target> targets) throws ServletException {
        Map<String, Boolean> allUsers = null;

        try {
            allUsers = new TreeMap<String, Boolean>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }

            });
            
            Collection<String> targetNames = getTargetNames(targets);
            for (String key : UserFactory.getInstance().getUserNames()) {
                allUsers.put(key, targetNames.contains(key));
            }

        } catch (Throwable e) {
            throw new ServletException("could not get list of all users.", e);
        }

        return allUsers;
    }

    public Map<String,Boolean> getGroups(Collection<Target> targets) throws ServletException {
        try {
            Collection<String> targetNames = getTargetNames(targets);

            Map<String, Boolean> allGroups = new TreeMap<String, Boolean>();
            for(String key : GroupFactory.getInstance().getGroupNames()) {
                allGroups.put(key, targetNames.contains(key));
            }
            return allGroups;
            
        } catch (Throwable e) {
            throw new ServletException("could not get list of all groups.", e);
        }
    }

    public Map<String,Boolean> getRoles(Collection<Target> targets) throws ServletException {
        try {
            Map<String, Boolean> rolesMap = new TreeMap<String, Boolean>();

            Collection<String> targetNames = getTargetNames(targets);

            for(String key : GroupFactory.getInstance().getRoleNames()) {
                rolesMap.put(key, targetNames.contains(key));
            }

            return rolesMap;
        } catch (Throwable e) {
            throw new ServletException("could not get list of all groups.", e);
        }
    }

    public Map<String,String> getEmails(Collection<Target> targets) throws ServletException {
        Map<String, String> emails = new TreeMap<String, String>();

        try {
            for (Target target : targets) {
                if (target.getCommands().size() == 1 && "email".equals(target.getCommands().get(0))) {
                    emails.put(target.getName(), target.getName());
                }
            }
        } catch (Throwable e) {
            throw new ServletException("could not get list of email targets.",
                    e);
        }

        return emails;
    }

    public Collection<String> getTargetNames(Collection<Target> targets) {
        Collection<String> targetNames = new ArrayList<>();
        for (Target target : targets) {
            if (target.getCommands().size() == 1 && "email".equals(target.getCommands().get(0))) {
                continue;
            }
            targetNames.add(target.getName());
        }
        return targetNames;
    }%>
