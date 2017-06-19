<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Choose Targets" />
  <jsp:param name="headTitle" value="Choose Targets" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>" />
  <jsp:param name="breadcrumb" value="Choose Targets" />
</jsp:include>

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

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Choose the users and groups to send the notice to.</h3>
  </div>
  <table class="table table-condensed table-borderless">
        <tr>
          <td valign="top"><h3>Send to Selected Users:</h3></td>
          <td>&nbsp;</td>
          <td valign="top"><h3>Send to Selected Groups:</h3></td>
          <td>&nbsp;</td>
          <td valign="top"><h3>Send to Selected Roles:</h3></td>
          <td>&nbsp;</td>
          <td valign="top"><h3>Send to Email Addresses:</h3></td>
        </tr>
        <tr>
          <td valign="top">Highlight each user that needs to receive the notice.</td>
          <td>&nbsp;</td>
          <td valign="top">Highlight each group that needs to receive the notice. Each user in the group
              will receive the notice.</td>
          <td>&nbsp;</td>
          <td valign="top">Highlight each role that needs to receive the notice. The users scheduled for the time that the notification comes in
              will receive the notice.</td>
          <td>&nbsp;</td>
          <td valign="top">Add any email addresses you want the notice to be sent to.</td>
        </tr>
        <tr>
          <td width="25%" valign="top" align="left">
            <select class="form-control" name="users" size="10" multiple>
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
          <td>&nbsp;</td>
          <td width="25%" valign="top" align="left">
            <select class="form-control" name="groups" size="10" multiple>
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
           <td>&nbsp;</td>
          <td width="25%" valign="top" align="left">
            <select class="form-control" name="roles" size="10" multiple>
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
           <td>&nbsp;</td>
           <td width="25%" valign="top" align="left">
            <input type="button" class="btn btn-default" value="Add Address" onclick="javascript:addAddress()"/>
            <br/>&nbsp;<br/>
            <select class="form-control" name="emails" size="7" multiple>
             <%
                 for (String key : getEmails(targets).keySet()) {
             %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%
            }
            %>
            </select>
            <br/>
            <input type="button" class="btn btn-default" value="Remove Address" onclick="javascript:removeAddress()"/>
            </td>
            
        </tr>
        <tr>
          <td colspan="2">
            <input type="reset" class="btn btn-default"/>
          </td>
        </tr>
  </table>
  <div class="panel-footer">
    <a href="javascript:next()">Next &#155;&#155;&#155;</a>
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
            for (String key : getTargetNames(targets)) {
                if (key.indexOf("@") > -1) {
                    emails.put(key, key);
                }
            }
        } catch (Throwable e) {
            throw new ServletException("could not get list of email targets.",
                    e);
        }

        return emails;
    }

    public Collection<String> getTargetNames(Collection<Target> targets) {
        Collection<String> targetNames = new ArrayList<String>();
        for (Target target : targets) {
            targetNames.add(target.getName());
        }
        return targetNames;
    }%>
