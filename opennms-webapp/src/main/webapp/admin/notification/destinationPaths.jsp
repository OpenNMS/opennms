<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
	import="java.util.*,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.destinationPaths.*
	"
%>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Destination Paths" />
  <jsp:param name="headTitle" value="Destination Paths" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Destination Paths" />
</jsp:include>

<script type="text/javascript" >

    function editPath() 
    {
        if (document.path.paths.selectedIndex==-1)
        {
            alert("Please select a path to edit.");
        }
        else
        {
            document.path.userAction.value="edit";
            document.path.submit();
        }
    }
    
    function newPath()
    {
        document.path.userAction.value="new";
        return true;
    }
    
    function deletePath()
    {
        if (document.path.paths.selectedIndex==-1)
        {
            alert("Please select a path to delete.");
        }
        else
        {
            message = "Are you sure you want to delete the path " + document.path.paths.options[document.path.paths.selectedIndex].value + "?";
            if (confirm(message))
            {
                document.path.userAction.value="delete";
                document.path.submit();
            }
        }
    }
    
</script>


<h2>Destination Paths</h2>

<form method="post" name="path" action="admin/notification/destinationWizard" onsubmit="return newPath();">
  <input type="hidden" name="userAction" value=""/>
  <input type="hidden" name="sourcePage" value="destinationPaths.jsp"/>

  <h3>Create a new Destination Path or edit an existing path.</h3>

  <input type="submit" value="New Path"/>

  <h4>Existing Paths</h4>
 
  <select NAME="paths" SIZE="10">
    <% Map<String, Path> pathsMap = new TreeMap<String, Path>(DestinationPathFactory.getInstance().getPaths());
       for (String key : pathsMap.keySet()) {
    %>
         <option VALUE=<%=key%>><%=key%></option>
    <% } %>
  </select>

  <br/>

  <input type="button" value="Edit" onclick="editPath()"/>
  <input type="button" value="Delete" onclick="deletePath()"/>
</form>
    
<jsp:include page="/includes/footer.jsp" flush="false" />
