<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.Util,org.opennms.netmgt.config.*,org.opennms.netmgt.config.destinationPaths.*" %>

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

<html>
<head>
  <title>Choose Commands | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

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

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/notification/index.jsp'>Configure Notifications</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>"); %>
<% String breadcrumb4 = java.net.URLEncoder.encode("Choose Commands"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Commands" />
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
    <h3>Choose the commands to use for each user and group. More than one command can be choosen for each.</h3>
    <form METHOD="POST" NAME="commands" ACTION="admin/notification/destinationWizard" >
      <%=Util.makeHiddenTags(request)%>
      <input type="hidden" name="sourcePage" value="chooseCommands.jsp"/>
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top" align="left">
            <%=buildCommands(newPath, Integer.parseInt(request.getParameter("targetIndex")))%>
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
    public String buildCommands(Path path, int index)
      throws ServletException
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table width=\"100%\" cellspacing=\"2\" cellpadding=\"2\" border=\"0\">");
        
        Target targets[] = null;
        
        try {
          targets = DestinationPathFactory.getInstance().getTargetList(index, path);
        
        for (int i = 0; i < targets.length; i++)
        {
              //don't let user pick commands for email addresses
              if (targets[i].getName().indexOf("@")==-1)
              {
            buffer.append("<tr><td>").append(targets[i].getName()).append("</td>");
            buffer.append("<td>").append(buildCommandSelect(path, index, targets[i].getName())).append("</td>");
            buffer.append("</tr>");
              }
        }
        } catch (Exception e)
        {
            throw new ServletException("couldn't get list of targets for path " + path.getName(), e);
        }
        
        buffer.append("</table>");
        return buffer.toString();
    }
    
    public String buildCommandSelect(Path path, int index, String name)
      throws ServletException
    {
        StringBuffer buffer = new StringBuffer("<select multiple size=\"3\" NAME=\"" + name + "Commands\">");
        
        TreeMap commands = null;
        Collection selectedOptions = null;
        
        try {
          selectedOptions = DestinationPathFactory.getInstance().getTargetCommands(path, index, name);
          commands = new TreeMap(NotificationCommandFactory.getInstance().getCommands());
        
        if (selectedOptions==null || selectedOptions.size()==0)
        {
            selectedOptions = new ArrayList();
            selectedOptions.add("email");
        }
        
        Iterator i = commands.keySet().iterator();
        while(i.hasNext())
        {
            String curCommand = (String)i.next();
            if (selectedOptions.contains(curCommand))
            {
                buffer.append("<option selected VALUE=\"" + curCommand + "\">").append(curCommand).append("</option>");
            }
            else
            {
                buffer.append("<option VALUE=\"" + curCommand + "\">").append(curCommand).append("</option>");
            }
        }
        } catch (Exception e)
        {
            throw new ServletException("couldn't get list of commands for path/target " + path.getName()+"/"+name, e);
        }
        
        buffer.append("</select>");
        
        return buffer.toString();
    }
%>
