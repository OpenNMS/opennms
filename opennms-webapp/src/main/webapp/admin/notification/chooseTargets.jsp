<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
			org.opennms.web.WebSecurityUtils,
            org.opennms.web.Util,
            org.opennms.netmgt.config.*,
            org.opennms.netmgt.config.destinationPaths.*"
%>

<%!public void init() throws ServletException {
        try {
            UserFactory.init();
            GroupFactory.init();
            DestinationPathFactory.init();
        } catch (Exception e) {
            throw new ServletException("Cannot load configuration file", e);
        }
    }%>

<%
            HttpSession user = request.getSession(true);
            Path newPath = (Path) user.getAttribute("newPath");

            Collection targets = null;

            int index = WebSecurityUtils.safeParseInt(request.getParameter("targetIndex"));
            if (index < 0) {
                targets = newPath.getTargetCollection();
            } else {
                targets = newPath.getEscalate()[index].getTargetCollection();
            }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
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

<h3>Choose the users and groups to send the notice to.</h3>

<form method="post" name="targets"
action="admin/notification/destinationWizard" >
<%=Util.makeHiddenTags(request)%>
<input type="hidden" name="sourcePage" value="chooseTargets.jsp"/>
<input type="hidden" name="nextPage"/>

<table cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top"><h4>Send to Selected Users:</h4></td>
          <td>&nbsp;</td>
          <td valign="top"><h4>Send to Selected Groups:</h4></td>
          <td>&nbsp;</td>
          <td valign="top"><h4>Send to Selected Roles:</h4></td>
          <td>&nbsp;</td>
          <td valign="top"><h4>Send to Email Addresses:</h4></td>
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
            <select WIDTH="200" STYLE="width: 200px" NAME="users" SIZE="10" multiple>
             <%
                         Map users = getUsers(targets);
                         Iterator iterator = users.keySet().iterator();
                         while (iterator.hasNext()) {
                             String key = (String) iterator.next();
                             if (((Boolean) users.get(key)).booleanValue()) {
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
            <select WIDTH="200" STYLE="width: 200px" NAME="groups" SIZE="10" multiple>
             <%
                         Map groups = getGroups(targets);
                         iterator = groups.keySet().iterator();
                         while (iterator.hasNext()) {
                             String key = (String) iterator.next();
                             if (((Boolean) groups.get(key)).booleanValue()) {
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
            <select WIDTH="200" STYLE="width: 200px" NAME="roles" SIZE="10" multiple>
             <%
                         Map roles = getRoles(targets);
                         iterator = roles.keySet().iterator();
                         while (iterator.hasNext()) {
                             String key = (String) iterator.next();
                             if (((Boolean) roles.get(key)).booleanValue()) {
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
            <input type="button" value="Add Address" onclick="javascript:addAddress()"/>
            <br/>&nbsp;<br/>
            <select  WIDTH="200" STYLE="width: 200px" NAME="emails" SIZE="7" multiple>
             <%
                         Map emails = getEmails(targets);
                         iterator = emails.keySet().iterator();
                         while (iterator.hasNext()) {
                             String key = (String) iterator.next();
             %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%
            }
            %>
            </select>
            <br/>
            <input type="button" value="Remove Address" onclick="javascript:removeAddress()"/>
            </td>
            
        </tr>
        <tr>
          <td colspan="2">
            <input type="reset"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a href="javascript:next()">Next &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
public Map getUsers(Collection targets) throws ServletException {
        Map<String, Boolean> allUsers = null;

        try {
            allUsers = new TreeMap<String, Boolean>(new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((String)o1).compareToIgnoreCase((String)o2);
                }

            });
            
            Collection targetNames = getTargetNames(targets);
            for (String key : UserFactory.getInstance().getUserNames()) {
                allUsers.put(key, targetNames.contains(key));
            }

        } catch (Exception e) {
            throw new ServletException("could not get list of all users.", e);
        }

        return allUsers;
    }

    public Map getGroups(Collection targets) throws ServletException {
        try {
            Collection targetNames = getTargetNames(targets);

            Map<String, Boolean> allGroups = new TreeMap<String, Boolean>();
            for(String key : GroupFactory.getInstance().getGroupNames()) {
                allGroups.put(key, targetNames.contains(key));
            }
            return allGroups;
            
        } catch (Exception e) {
            throw new ServletException("could not get list of all groups.", e);
        }
    }

    public Map getRoles(Collection targets) throws ServletException {
        try {
            Map<String, Boolean> rolesMap = new TreeMap<String, Boolean>();

            Collection targetNames = getTargetNames(targets);

            for(String key : GroupFactory.getInstance().getRoleNames()) {
                rolesMap.put(key, targetNames.contains(key));
            }

            return rolesMap;
        } catch (Exception e) {
            throw new ServletException("could not get list of all groups.", e);
        }
    }

    public Map getEmails(Collection targets) throws ServletException {
        Map<String, String> emails = new TreeMap<String, String>();

        try {
            Collection targetNames = getTargetNames(targets);

            Iterator i = targetNames.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                if (key.indexOf("@") > -1) {
                    emails.put(key, key);
                }
            }
        } catch (Exception e) {
            throw new ServletException("could not get list of email targets.",
                    e);
        }

        return emails;
    }

    public Collection<String> getTargetNames(Collection targets) {
        Collection<String> targetNames = new ArrayList<String>();

        Iterator i = targets.iterator();
        while (i.hasNext()) {
            targetNames.add(((Target) i.next()).getName());
        }
        return targetNames;
    }%>
