<%@page language="java" contentType="text/html" session="true" import="java.util.*, java.sql.Timestamp, java.io.*,org.opennms.web.inventory.*"%>
<html>
<head>
	<title>Node Inventory Detail</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />

  <link rel=stylesheet type=text/css href=includes/styles.css>
  <link href="../includes/styles.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
.view {
	padding: 0px;
	margin: 0px;
	border: 2px inset;
	overflow: scroll;
	width: 600px;
	height: 380px;
	text-align: left;
}
-->
</style>
</head>

<body marginwidth=0 marginheight=0 LEFTMARGIN=0 RIGHTMARGIN=0 TOPMARGIN=0>

<%


  String breadcrumb1 = "<a href='conf/index.jsp' >Inventory</a>";
  String breadcrumb2="Detail";
%>
<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Node Inventory Detail"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>"/>
</jsp:include>
<br>
<%
String file = request.getParameter("file");
File f = new File(file);
if(!f.exists()){
	%>
	&nbsp;<strong>File <%=file%> not exists or its name/path is changed.</strong>
	<br>
	<%
}
else
{/*begin else file not exists*/
String inventCategory = request.getParameter("category");
String nodeLabel = request.getParameter("nodelabel");
String lastPollTime = request.getParameter("lastpolltime");
%>

<br>
<table width="100%" heigh="100px" cellspacing="0" cellpadding="2" border="0" align="center">
	<tr>
	<td>&nbsp;</td>
	<td align="center">
	<b>
	<%=inventCategory%> inventory for node <%=nodeLabel%> (<%=lastPollTime%>)
	<b>
	</td>
	<td>&nbsp;</td>
	</tr>
<tr>
  <td width="15%">&nbsp;</td>
  <td width="70%" align="center">

<%

InventoryVisualization invVisual = new InventoryVisualization(inventCategory, file);
Map parameters = new HashMap();
String visualType = invVisual.getVisualType();
if(visualType.equals("tree")) {
	parameters.put("fileDtd", application.getRealPath("conf/visualization/treeview.dtd"));
	parameters.put("fileXslt", application.getRealPath("conf/visualization/treeview.xslt"));
}
if(visualType.equals("text")){
	parameters.put("xslt-file", application.getRealPath("conf/visualization/textview.xslt"));
%>
	<div width="100%" class="view"><%}
String outStr=null;
try{
	outStr = invVisual.getVisualization(parameters) ;
}catch(Exception e){
	out.write(e.toString());
}
%><%=outStr%>
<%
	if(visualType.equals("text")){
		%></div>
<%	
	}
%>
  </td>
  <td width="15%">&nbsp;</td>
</tr>

    <tr>
    <td>&nbsp;</td>
    <td  align="center">  
      <input type="button" value="Compare" onclick="location.href='<%=org.opennms.web.Util.calculateUrlBase( request )%>conf/compareinventory.jsp?file=<%=file%>&name=<%=inventCategory%>&firstinvpath=<%=file%>&nodelabel=<%=nodeLabel%>&lptime=<%=lastPollTime%>';"/>
    </td>
    <td>&nbsp;</td> 
    </tr>


</table>
<%
}
/*
end else file not exists
*/
%>
<br>
<jsp:include page="/includes/footer.jsp" flush="false"/>

</body>
</html>
