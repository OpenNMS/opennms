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
		org.opennms.netmgt.config.destinationPaths.*,
        org.opennms.netmgt.config.notificationCommands.Command
	"
%>

<%!
    public void init() throws ServletException {
        try {
            UserFactory.init();
            GroupFactory.init();
            DestinationPathFactory.init();
            NotificationCommandFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    HttpSession user = request.getSession(true);
    Path newPath = (Path)user.getAttribute("newPath");
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Choose Commands" />
  <jsp:param name="headTitle" value="Choose Commands" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>" />
  <jsp:param name="breadcrumb" value="Choose Commands" />
</jsp:include>

<script type="text/javascript" >

    function next() 
    {
        var missingCommands=false;
        for (i=0; i<document.commands.length; i++)
        {
            if (document.commands.elements[i].type=="select-multiple" && 
                document.commands.elements[i].selectedIndex==-1)
            {
                missingCommands=true;
            }
        }
        
        if (missingCommands)
        {
            alert("Please choose at least command for each user and group.");
        }
        else
        {
            document.commands.submit();
        }
    }

</script>

<h2><%=(newPath.getName()!=null ? "Editing path: " + newPath.getName() + "<br/>" : "")%></h2>

<form method="post" name="commands"
      action="admin/notification/destinationWizard">
  <%=Util.makeHiddenTags(request)%>
  <input type="hidden" name="sourcePage" value="chooseCommands.jsp"/>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Choose the commands to use for each user and group. More than one
    command can be chosen for each (except for email addresses). Also
    choose the desired behavior for automatic notification on "UP" events.</h3>
  </div>
  <div class="panel-body">
    <div class="row">
      <div class="col-md-4">
        <%=buildCommands(newPath, WebSecurityUtils.safeParseInt(request.getParameter("targetIndex")))%>
      </div>
    </div>
    <input type="reset" class="btn btn-default"/>
  </div> <!-- panel-body -->
  <div class="panel-footer">
    <a href="javascript:next()">Next &#155;&#155;&#155;</a>
  </div>
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    public String buildCommands(Path path, int index)
      throws ServletException
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table class=\"table table-condensed table-borderless\">");
        
        Target targets[] = null;
        
        try {
          targets = DestinationPathFactory.getInstance().getTargetList(index, path);
        
        for (int i = 0; i < targets.length; i++)
        {
            buffer.append("<tr><td>").append(targets[i].getName()).append("</td>");
            // don't let user pick commands for email addresses
            if (targets[i].getName().indexOf("@")==-1)
            {
                buffer.append("<td>").append(buildCommandSelect(path, index, targets[i].getName())).append("</td>");
            }
            else
            {
                buffer.append("<td>").append("email adddress").append("</td>");
            }
            buffer.append("<td>").append(buildAutoNotifySelect(targets[i].getName(), targets[i].getAutoNotify().orElse(null))).append("<td>");
            buffer.append("</tr>");
        }
        } catch (Throwable e)
        {
            throw new ServletException("couldn't get list of targets for path " + path.getName(), e);
        }
        
        buffer.append("</table>");
        return buffer.toString();
    }
    
    public String buildCommandSelect(Path path, int index, String name)
      throws ServletException
    {
        StringBuffer buffer = new StringBuffer("<select class=\"form-control\" multiple size=\"3\" NAME=\"" + name + "Commands\">");
        
        TreeMap<String, Command> commands = null;
        Collection<String> selectedOptions = null;
        
        try {
          selectedOptions = DestinationPathFactory.getInstance().getTargetCommands(path, index, name);
          commands = new TreeMap<String, Command>(NotificationCommandFactory.getInstance().getCommands());
        
        if (selectedOptions==null || selectedOptions.size()==0)
        {
            selectedOptions = new ArrayList<>();
            selectedOptions.add("javaEmail");
        }

        for(String curCommand : commands.keySet()) {
            if (selectedOptions.contains(curCommand))
            {
                buffer.append("<option selected VALUE=\"" + curCommand + "\">").append(curCommand).append("</option>");
            }
            else
            {
                buffer.append("<option VALUE=\"" + curCommand + "\">").append(curCommand).append("</option>");
            }
        }
        } catch (Throwable e)
        {
            throw new ServletException("couldn't get list of commands for path/target " + path.getName()+"/"+name, e);
        }
        
        buffer.append("</select>");
        
        return buffer.toString();
    }

    public String buildAutoNotifySelect(String name, String currValue)
    {
          String values[] = {"off", "auto", "on"};
          StringBuffer buffer = new StringBuffer("<select class=\"form-control\" size=\"3\" NAME=\"" + name  + "AutoNotify\">");
          String defaultOption = "on";
 
          if(currValue == null || currValue.equals("")) {
              currValue = defaultOption;
          }
          for (int i = 0; i < values.length; i++)
          {
             if (values[i].equalsIgnoreCase(currValue))
             {
                 buffer.append("<option selected VALUE=\"" + values[i] + "\">").append(values[i]).append("</option>");
             }
             else
             {
                  buffer.append("<option VALUE=\"" + values[i] + "\">").append(values[i]).append("</option>");
             }
          }
          buffer.append("</select>");
          
          return buffer.toString();
    }
%>
