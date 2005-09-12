<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.inventory.*,java.util.*, java.sql.*"%>
<html>
<%
    InventoryFactory invFactory;
    List names;    
    invFactory = new InventoryFactory();
    names = invFactory.getNames();
%>

<head>
  <title>Inventory Management | OpenNMS Web Console</title>
  <base HREF=<%=org.opennms.web.Util.calculateUrlBase(request)%>>
  <link rel=stylesheet type=text/css href=includes/styles.css>
  <link href="../includes/styles.css" rel="stylesheet" type="text/css">
</head>
<body marginwidth=0 marginheight=0 LEFTMARGIN=0 RIGHTMARGIN=0 TOPMARGIN=0 >
<%
  String breadcrumb1="Inventory";
%>
<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Inventory"/>
  <jsp:param name="location" value="inventory" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>"/>
</jsp:include>
 
<br>
 
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>
    <td valign="top">
      <h3>Inventory Queries</h3>

      <p>
            <form action="conf/inventorylist.jsp" method="POST">          
        <table width="50%" border="0" cellpadding="2" cellspacing="0" >
          <tr>
            <td>Node name containing:</td>
            <td>TCP/IP Address like:</td>
          </tr>
          <tr>
              <td><input type="text" name="nodename" /></td>
              <td><input type="text" name="iplike" value="*.*.*.*" /></td>
          </tr>
          
          <tr>
            <td>Status:</td>
            <td>Time:</td>
          </tr>
          <tr>
            <td>
                <select name="status" size="1">
                  <option value="Y" selected>Any</option>
                  <option value="A">Active</option>
                  <option value="N">Not Active</option>
                  <option value="D">Deleted(Node)</option>
                </select>
              </td>
            <td>
                <select name="relativetime" size="1">
                  <option value="0" selected>Any</option>
                  <option value="1">Last hour</option>
                  <option value="2">Last 4 hours</option>
                  <option value="3">Last 8 hours</option>
                  <option value="4">Last 12 hours</option>
                  <option value="5">Last day</option>
                  <option value="6">Last week</option>
                  <option value="7">Last month</option>
                </select>
              </td>
          </tr>

          <tr>
            <td colspan="2">Inventory Category:</td>
          </tr>
          <tr>
              <td>
               <select name="name" size="1">
                    <option value="0">Any</option>
          <%   Iterator inventoryNameIterator = names.iterator();
		   while( inventoryNameIterator.hasNext() ) {
                    String name = (String)inventoryNameIterator.next(); %> 
                    <option value="<%=name%>"><%=name%></option>
                  <% } %>          
                </select>
              </td>
                <td><input type="submit" value="Search"></td>
          </tr>          
        </table></form>
      </p>

      <p>
        <a href="conf/inventorylist.jsp?nodename=&iplike=*.*.*.*&status=Y&relativetime=0&name=0">List all inventories</a>
      </p>

</td>

    <td>&nbsp;</td>
    <td valign="top" width="60%">
      <h3>Inventory</h3>
      <p>The OpenNMS system provides a means for you to easily track and share
            important information about router and switch configuration in your organization. This
            data, when coupled with the information about your network that the
            OpenNMS system obtains during network discovery, can be a powerful tool not
            only for solving problems, but in tracking the current state of
            equipment repairs as well as network or system-related moves, additions,
            or changes.
        </p>
	<p>
            Once you begin adding data to the OpenNMS system's configuration page,
            any node with a map will be displayed on the
            lower half of this page, providing you a one-click mechanism for
            tracking the current physical status of that device.
        </p>
    </td>
    <td>&nbsp;  </td>
  </tr>
</table>


<br><br><br><br><br><br><br>
<jsp:include page="/includes/footer.jsp" flush="false">
	<jsp:param name="location" value="inventory" />
</jsp:include>
</body>
</html>