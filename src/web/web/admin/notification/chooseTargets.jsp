<!--

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

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.Util,org.opennms.netmgt.config.*,org.opennms.netmgt.config.destinationPaths.*" %>

<%!
    public void init() throws ServletException {
        try {
            UserFactory.init();
            GroupFactory.init();
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    HttpSession user = request.getSession(true);
    Path newPath = (Path)user.getAttribute("newPath");
    
    String[] targetLinks = null;
    Collection targets = null;
    
    int index = Integer.parseInt(request.getParameter("targetIndex"));
    if (index < 0)
    {
        targets = newPath.getTargetCollection();
    }
    else
    {
        targets = newPath.getEscalate()[index].getTargetCollection();
    }
%>

<html>
<head>
  <title>Choose Targets | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

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
        else if (document.targets.emails.length>0)
        {
            selectAllEmails();
            document.targets.nextPage.value="pathOutline.jsp";
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

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/notification/index.jsp'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>"; %>
<% String breadcrumb4 = "Choose Targets"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Targets" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
    <h2><%=(newPath.getName()!=null ? "Editing path: " + newPath.getName() + "<br>" : "")%></h2>
    <h3>Choose the users and groups to send the notice to.</h3>
    <form METHOD="POST" NAME="targets" ACTION="admin/notification/destinationWizard" >
      <%=Util.makeHiddenTags(request)%>
      <input type="hidden" name="sourcePage" value="chooseTargets.jsp"/>
      <input type="hidden" name="nextPage"/>
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top"><h4>Send to Selected Users:</h4></td>
          <td>&nbsp;</td>
          <td valign="top"><h4>Send to Selected Groups:</h4></td>
          <td>&nbsp;</td>
          <td valign="top"><h4>Send to Email Addresses:</h4></td>
        </tr>
        <tr>
          <td valign="top">Highlight each user that needs to receive the notice.</td>
          <td>&nbsp;</td>
          <td valign="top">Highlight each group that needs to receive the notice. Each user in the group
              will receive the notice.</td>
          <td>&nbsp;</td>
          <td valign="top">Add any email addresses you want the notice to be sent to.</td>
        </tr>
        <tr>
          <td width="25%" valign="top" align="left">
            <select WIDTH="200" STYLE="width: 200px" NAME="users" SIZE="10" multiple>
             <% Map users = getUsers(targets);
                Iterator iterator = users.keySet().iterator();
                while(iterator.hasNext()) 
                { 
                  String key = (String)iterator.next();
                  if ( ((Boolean)users.get(key)).booleanValue() )  {  %>
                    <option selected VALUE=<%=key%>><%=key%></option>
            <%    } else { %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%    }
               } %>
            </select>
          </td>
          <td>&nbsp;</td>
          <td width="25%" valign="top" align="left">
            <select WIDTH="200" STYLE="width: 200px" NAME="groups" SIZE="10" multiple>
             <% Map groups = getGroups(targets);
                iterator = groups.keySet().iterator();
                while(iterator.hasNext()) 
                { 
                  String key = (String)iterator.next();
                  if ( ((Boolean)groups.get(key)).booleanValue() ) {  %>
                    <option selected VALUE=<%=key%>><%=key%></option>
            <%    } else { %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%    }
               } %>
            </select>
           </td>
           <td>&nbsp;</td>
           <td width="25%" valign="top" align="left">
            <input type="button" value="Add Address" onclick="javascript:addAddress()"/>
            <br>&nbsp;<br>
            <select  WIDTH="200" STYLE="width: 200px" NAME="emails" SIZE="7" multiple>
             <% Map emails = getEmails(targets);
                iterator = emails.keySet().iterator();
                while(iterator.hasNext())
                { 
                  String key = (String)iterator.next();
                %>
                    <option VALUE=<%=key%>><%=key%></option>
            <%  } %>
            </select>
            <br>
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
           <a HREF="javascript:next()">Next &#155;&#155;&#155;</a>
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
    public Map getUsers(Collection targets)
      throws ServletException
    {
        Map allUsers = null;
        
        try {
	  allUsers = new TreeMap(new Comparator() {
		public int compare(Object o1, Object o2) {
			if(o1 instanceof String && o2 instanceof String) {
				return ((String)o1).compareToIgnoreCase((String)o2);
			} 
			throw new RuntimeException("Non string comparision for a string comparator");	
		}
 
	  });
          allUsers.putAll(UserFactory.getInstance().getUsers());
        Collection targetNames = getTargetNames(targets);
        
        Iterator i = allUsers.keySet().iterator();
        while(i.hasNext())
        {
            String key = (String)i.next();
            if (targetNames.contains(key))
            {
                allUsers.put(key, new Boolean(true));
            }
            else
            {
                allUsers.put(key, new Boolean(false));
            }
        }
        } catch (Exception e)
        { 
            throw new ServletException("could not get list of all users.", e);
        }
        
        return allUsers;
    }
    
    public Map getGroups(Collection targets)
      throws ServletException
    {
        Map allGroups = null;
        
        try {
          allGroups = new TreeMap(GroupFactory.getInstance().getGroups());
        Collection targetNames = getTargetNames(targets);
        
        Iterator i = allGroups.keySet().iterator();
        while(i.hasNext())
        {
            String key = (String)i.next();
            if (targetNames.contains(key))
            {
                allGroups.put(key, new Boolean(true));
            }
            else
            {
                allGroups.put(key, new Boolean(false));
            }
           }
        } catch (Exception e)
        { 
            throw new ServletException("could not get list of all groups.", e);
        }
        
        return allGroups;
    }
    
    public Map getEmails(Collection targets)
      throws ServletException
    {
        Map emails = new TreeMap();
        
        try {
          Collection targetNames = getTargetNames(targets);
          
          Iterator i = targetNames.iterator();
          while(i.hasNext())
          {
              String key = (String)i.next();
              if (key.indexOf("@") > -1)
              {
                  emails.put(key, key);
              }
           }
        } catch (Exception e)
        { 
            throw new ServletException("could not get list of email targets.", e);
        }
        
        return emails;
    }
    
    public Collection getTargetNames(Collection targets)
    {
        Collection targetNames = new ArrayList();
        
        Iterator i = targets.iterator();
        while(i.hasNext())
          targetNames.add( ((Target)i.next()).getName() );
        
        return targetNames;
    }
%>
