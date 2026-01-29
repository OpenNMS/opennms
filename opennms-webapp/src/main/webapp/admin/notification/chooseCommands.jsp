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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Choose Commands")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications", "admin/notification/index.jsp")
          .breadcrumb("Destination Paths", "admin/notification/destinationPaths.jsp")
          .breadcrumb("Choose Commands")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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

<div class="card">
  <div class="card-header">
    <span>Choose the commands to use for each user and group. More than one
    command can be chosen for each (except for email addresses). Also
    choose the desired behavior for automatic notification on "UP" events.</span>
  </div>
  <div class="card-body">
    <div class="row">
      <div class="col-md-4">
        <%=buildCommands(newPath, WebSecurityUtils.safeParseInt(request.getParameter("targetIndex")))%>
      </div>
    </div>
  </div> <!-- card-body -->
  <div class="card-footer">
      <input type="reset" class="btn btn-secondary"/>
      <a class="btn btn-secondary" href="javascript:next()">Next Step <i class="fas fa-arrow-right"></i></a>
  </div>
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    public String buildCommands(Path path, int index)
      throws ServletException
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table class=\"table table-sm table-borderless\">");
        
        Target targets[] = null;
        
        try {
          targets = DestinationPathFactory.getInstance().getTargetList(index, path);
        
        for (int i = 0; i < targets.length; i++)
        {
            buffer.append("<tr><td>").append(targets[i].getName()).append("</td>");
            // don't let user pick commands for email addresses
            if (!(targets[i].getCommands().size() == 1 && "email".equals(targets[i].getCommands().get(0))))
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
        StringBuffer buffer = new StringBuffer("<select class=\"form-control custom-select\" multiple size=\"3\" NAME=\"" + name + "Commands\">");
        
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
          StringBuffer buffer = new StringBuffer("<select class=\"form-control custom-select\" size=\"3\" NAME=\"" + name  + "AutoNotify\">");
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
