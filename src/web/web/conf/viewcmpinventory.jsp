<%@page language="java" contentType="text/html" session="true" import="java.util.*, java.sql.Timestamp, java.io.*,org.opennms.web.inventory.*"%>
<%!
	String[] statusInventoryItem = { "equal", "added", "removed", "changed", "empty" };
	
	String printRowNumber(int base, int n)
	{
		String rv = "";
		for(int i = 1; i <= n; i++)
			rv += (base + i) + "<br>";
		return rv;
	}
%>
<html>
<head>
<script type="text/javascript">
function setSize(id1, id2)
{
	var preferred_width = (window.screen.width - 150) / 2;
	document.getElementById(id1).style.width= preferred_width + "px";
	document.getElementById(id2).style.width= preferred_width + "px";	
}
function scrolldiv(source, dest)
{
	document.getElementById(dest).scrollTop=document.getElementById(source).scrollTop;
	document.getElementById(dest).scrollLeft=document.getElementById(source).scrollLeft;
}
function scrollerGoHome(id1, id2)
{
	document.getElementById(id1).scrollTop=document.getElementById(id2).scrollTop=document.getElementById(id1).scrollLeft=document.getElementById(id2).scrollLeft=0;
}
</script>
<style type="text/css">
<!--
pre {
	margin: 0px 0px 0px 0px;
	padding: 0px 0px 0px 0px;	
}
.number {
	background-color:#b0d0b0;
	width: 1px;
}
.compare {
	padding: 0px;
	margin: 0px;
	border: 2px inset;
	overflow: scroll;
	width: 450px;
	height: 380px;
	text-align: left;
}
.equal1 {
	background-color:#FFFFFF;
}
.added1 {
	background-color:#5994ec;
}
.removed1 {
	background-color:#c9dfc9;
}
.changed1 {
	background-color:#f382f2;
}
.empty1 {
	background-color:#f382f2;
}

.equal2 {
	background-color:#FFFFFF;
}
.added2 {
	background-color:#f6d655;
}
.removed2 {
	background-color:#c9dfc9;
}
.changed2 {
	background-color:#f382f2;
}
.empty2 {
	background-color:#f382f2;
}

-->
</style>
<title>Inventory Comparison Result</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel=stylesheet type=text/css href=includes/styles.css>
<link href="../includes/styles.css" rel="stylesheet" type="text/css">
</head>
<body marginwidth=0 marginheight=0 LEFTMARGIN=0 RIGHTMARGIN=0 TOPMARGIN=0 onLoad="setSize('sx', 'dx');scrollerGoHome('sx', 'dx');"> 
<%


  String breadcrumb1 = "<a href='conf/index.jsp' >Inventory</a>";
  String breadcrumb2="Comparison Result";
%> 
<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Inventory Comparison Result"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>"/>
 
</jsp:include> 
<br> 
<%
String invCat = request.getParameter("invcat");
String firstPath = request.getParameter("firstinvpath");
String firstLabel = request.getParameter("firstlabel");
String firstLpTime = request.getParameter("firstlptime");

String secondPath = request.getParameter("secondinvpath");
String secondLabel = request.getParameter("secondlabel");
String secondLpTime = request.getParameter("secondlptime");
File file1 = new File(firstPath);
File file2 = new File(secondPath);
if(!file1.exists() || !file2.exists()){
	if(!file1.exists()){
		%>
		  &nbsp;<strong>File <%=file1%> not exists or its name/path is changed</strong>
		  <br>
		<%}
	if(!file2.exists()){
		%>
		  &nbsp;<strong>File <%=file2%> not exists or its name/path is changed</strong>
		  <br>
		<%}

}
else
{/*start else file1 or file2 not exists*/

org.opennms.web.inventory.Comparator cmp = new org.opennms.web.inventory.Comparator();
cmp.setFirstInventory(firstPath);
cmp.setSecondInventory(secondPath);
cmp.compare();
%> 
<table width="100%" border="0"> 
  <tr> 
    <td>&nbsp;</td> 
    <td> <table width="100%" height="100px" border="0"> 
        <tr> 
          <td width="50%" align="center"> <b><%=invCat%> for <%=firstLabel%> (<%=firstLpTime%>)</b> </td> 
          <td width="50%" align="center"> <b><%=invCat%> for <%=secondLabel%> (<%=secondLpTime%>)</b> </td> 
          <%
	String firstInventoryHtml;
	String secondInventoryHtml;
	secondInventoryHtml = firstInventoryHtml = "<table cellpadding='0' cellspacing='0' border='0' width='100%'>";
	
	List firstInventoryList = cmp.getFirstInventoryItemList();
	List secondInventoryList = cmp.getSecondInventoryItemList();

	InventoryItem[] firstInventoryArray = (InventoryItem[])firstInventoryList.toArray(new InventoryItem[firstInventoryList.size()]);
	InventoryItem[] secondInventoryArray = (InventoryItem[])secondInventoryList.toArray(new InventoryItem[secondInventoryList.size()]);

	InventoryItem currentItem;	
	int base = 0, added = 0, removed = 0, changed = 0;
	for (int i = 0, n = firstInventoryArray.length; i < n; i++)
	{
			currentItem = firstInventoryArray[i];	
			if (currentItem.getStatus() == InventoryItem.ADDED_STATUS) added++;
			if (currentItem.getStatus() == InventoryItem.CHANGED_STATUS) changed++;
			firstInventoryHtml += "<tr id='first' class='" + statusInventoryItem[currentItem.getStatus()] + 1 + "'><td style='padding-left: " + (10 * currentItem.getNumColumn()) + "px'><pre>";
			base += currentItem.getNumRows();
			if (currentItem.getStatus() == InventoryItem.REMOVED_STATUS || currentItem.getStatus() == InventoryItem.EMPTY_STATUS)
			{
				//firstInventoryHtml +=currentItem.getNumRows();
				for(int j = 0; j < currentItem.getNumRows(); j++) {
					firstInventoryHtml += "&nbsp;<br>";
				}
			}else{
				firstInventoryHtml += currentItem.getName() + "<div style='margin: 0px 0px 0px 0px;padding: 0px 0px 0px 10px;'>" + ((currentItem.getDataitem() != null) ? currentItem.getDataitem() : "") + "</div>";
			}
			firstInventoryHtml += "</pre></td></tr>";			
	}
	firstInventoryHtml += "</table>";
	
	base = 0;
	for (int i = 0, n = secondInventoryArray.length; i < n; i++)
	{
			currentItem = secondInventoryArray[i];
			if (currentItem.getStatus() == InventoryItem.ADDED_STATUS) removed++;
			secondInventoryHtml += "<tr id='second' class='" + statusInventoryItem[currentItem.getStatus()] + 2 + "'><td style='padding-left: " + (10 * currentItem.getNumColumn()) + "px'><pre>";
			base += currentItem.getNumRows();
			if (currentItem.getStatus() == InventoryItem.REMOVED_STATUS || currentItem.getStatus() == InventoryItem.EMPTY_STATUS)
			{
				//secondInventoryHtml += currentItem.getNumRows();
				for(int j = 0; j < currentItem.getNumRows(); j++) {
					secondInventoryHtml += "&nbsp;<br>";
				}
			}else{
				secondInventoryHtml += currentItem.getName() + "<div style='margin: 0px 0px 0px 0px;padding: 0px 0px 0px 10px;'>" + ((currentItem.getDataitem() != null) ? currentItem.getDataitem() : "") + "</div>";
			}
			secondInventoryHtml += "</pre></td></tr>";			
	}
	secondInventoryHtml += "</table>";	
%> 
        </tr> 
        <tr>
          <td width="50%" align="center"><div class="compare" id="sx" onScroll="scrolldiv('sx','dx')"> <%=firstInventoryHtml%> </div></td> 
          <td width="50%" align="center"><div class="compare" id="dx" onScroll="scrolldiv('dx','sx')"> <%=secondInventoryHtml%> </div></td> 
        </tr>
		<tr>
			<td width="50%" align="center" colspan="2"><table cellpadding="0" cellspacing="0" border="1" width="30%"><tr><td class="added1" align="center"><%=added%>&nbsp;Added</td><td class="changed1" align="center"><%=changed%>&nbsp;Changed</td><td class="added2" align="center"><%=removed%>&nbsp;Removed</td></tr></table></td>			
		</tr>
      </table></td> 
    <td>&nbsp;</td> 
  </tr> 
</table> 
<%
} /*end else file1 or file2 not exists*/
%>
<br> 
<jsp:include page="/includes/footer.jsp" flush="false"/>
</body>
</html>
