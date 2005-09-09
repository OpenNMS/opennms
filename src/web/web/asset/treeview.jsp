<%@page language="java" contentType="text/html" session="true"%>
<%!
    final int SUCCESS_CODE = 200;
%>
<% 
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>
<html>
<head>
<META HTTP-EQUIV="Expires" CONTENT="0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<title>OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
<script Language="javascript">

function selectFolder()
{
	return;
}

function selectLeaf(title, code) {

        document.location.href="<%=org.opennms.web.Util.calculateUrlBase( request )%>asset/detail.jsp?node=" + code;

}

</script>
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0"> 
<% String breadcrumb1 = "<a href='asset/index.jsp' >Asset</a>"; %> 
<% String breadcrumb2 = "Tree View"; %> 
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node Windows Accounts" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
 
 
 
</jsp:include> 
<br> 
<!-- Body --> 
<table width="100%" border="0" cellspacing="0" cellpadding="2" > 
  <tr> 
    <td>&nbsp;</td> 
    <td <table width="100%" border="0" cellspacing="0" cellpadding="2" > 
        <tr> 
          <td<p><strong>Asset Tree View</strong></p> 
</td> 
        </tr> 
      </table>
      <br>
    </td> 
    <td>&nbsp;<br> 
&nbsp;<br></td> 
  </tr> 
  <tr> 
    <td>&nbsp;</td> 
    <td <jsp:useBean id="treeview" class="org.opennms.web.asset.AssetLocationTreeView" scope="page"> </jsp:useBean> 
      <jsp:setProperty name="treeview" property="dtdPath" value="<%=application.getRealPath("asset/treeview/")%>"/> 
      <jsp:setProperty name="treeview" property="fileXslt" value="<%=application.getRealPath("asset/treeview/treeview.xslt")%>"/> 

      <jsp:setProperty name="treeview" property="mappingImageFile" value="<%=application.getRealPath("asset/treeview/image-mapping.xml")%>"/> 
      <jsp:setProperty name="treeview" property="imagePath" value="<%=application.getRealPath("asset/treeview/Icons")%>"/> 
      <jsp:setProperty name="treeview" property="rootImage" value="root.gif"/> 
      <jsp:setProperty name="treeview" property="rootTitle" value="Buildings"/> 

      <jsp:setProperty name="treeview" property="expanded" value="<%=true%>"/> 
      <%=treeview.getHtmlTreeView()%> 
    </td> 
    <td>&nbsp;</td> 
  </tr> 
</table> 
<br> 
<jsp:include page="/includes/footer.jsp" flush="false"/> 
</body>
</html>
