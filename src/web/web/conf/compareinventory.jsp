<%@page language="java" contentType="text/html" session="true" import="java.util.*, org.opennms.web.inventory.*, java.sql.Timestamp,org.opennms.web.element.*"%>

<html>
<head>
  <title>Inventory Comparison | OpenNMS Web Console</title>
  <base HREF=<%=org.opennms.web.Util.calculateUrlBase(request)%>>
  <link rel=stylesheet type=text/css href=includes/styles.css>
  <link href="../includes/styles.css" rel="stylesheet" type="text/css">
</head>

<body marginwidth=0 marginheight=0 LEFTMARGIN=0 RIGHTMARGIN=0 TOPMARGIN=0>
       
<%
  String breadcrumb1 = "<a href='conf/index.jsp' >Inventory</a>";
  String breadcrumb2="Compare";
%>
<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Inventory Comparison"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>"/>
</jsp:include>
<br>

<%
      String firstInvPath = request.getParameter( "firstinvpath" );
	String firstNodeLabel = request.getParameter( "nodelabel" );
      String firstLpTime = request.getParameter("lptime");
	String name = request.getParameter( "name" );
	String pathToFile = request.getParameter ("file");

   	int sort = InventoryComparator.NODE_LABEL_SORT;
   	if(request.getParameter( "sort" )!=null){
   		sort = Integer.parseInt(request.getParameter( "sort" ));
   	}      
	InventoryFactory invFactory = new InventoryFactory();
	Inventory[] inventories = null ;
 	inventories = invFactory.getSimilarInventories(pathToFile , name);
	InventoryComparator invComp = new InventoryComparator();
      invComp.setSort(sort);
      Arrays.sort(inventories, invComp);


    // code for paging
    String pageSizeString = request.getParameter( "pagesize" );
    int defaultPageSize = 10;
    int pageSize = (pageSizeString == null) ? defaultPageSize : Integer.parseInt( pageSizeString );
    String offsetString = request.getParameter( "offset" );	
    int offset = (offsetString == null) ? 0 : Integer.parseInt( offsetString );

    int n = inventories.length;
    int npage = n / pageSize;
    if (n%pageSize != 0) 
		npage++;
    int currentpage = 1 + offset / pageSize;
    if (offset%pageSize != 0)
		currentpage++;
    %>
<%
if(n >0)
{

%>    
    <table width="100%">
    <tr>

    	<td align="center"><h3>Select an inventory for the comparison</h3><b>with <%=name%> inventory of node <%=firstNodeLabel%> (<%=firstLpTime%>)</b></td>

    </tr>
    </table>
<br>


 <table width="100%" cellspacing="0" cellpadding="2" border="0" align="center"><tr><td>&nbsp;</td><td>
<table width="80%" cellspacing="0" cellpadding="4" border="1" bordercolor="black" align="center">
  <tr align="center" bgcolor="#999999">
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=offset%>&file=<%=pathToFile%>" title="Sort by node label (case insensitive)">Node</a></td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=offset%>&file=<%=pathToFile%>" title="Sort by last poll time (decr.)">Last Poll Time</a></td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=offset%>&file=<%=pathToFile%>" title="Sort by create time (decr.)">Create Time</a></td>
    <td style="font-weight: bold; color: black;text-align: center;">Inventory Category</td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=offset%>&file=<%=pathToFile%>" title="Sort by status">Status</a></td>
    <td style="font-weight: bold; color: black;text-align: center;">Action</td>
  </tr>

<%
String pathFile = "";

for(int t = offset; t < ((n <(offset + pageSize))? n : offset + pageSize); t++)
{
%>
  <tr bordercolor="#000000" bgcolor="<%=(t%2 == 0) ? "white" : "#cccccc"%>"> 
	<%
	int nodeid = inventories[t].getNodeID();
	pathFile = inventories[t].getPathToFile();
      String nodeLabel = NetworkElementFactory.getNodeLabel(nodeid);
	Timestamp lastPollTime = inventories[t].getLastPollTime();
	String currStatus = "Active";
	if(inventories[t].getStatus().equals("N"))
		currStatus="Not Active";
	else if(inventories[t].getStatus().equals("D")) 
		currStatus="Deleted (Node)";
	String inventName = inventories[t].getName();
	%>
    <td><%=nodeLabel%></td>
    <td><%=lastPollTime%></td>
    <td><%=inventories[t].getCreateTime()%></td>
    <td><%=inventName%></td>
    <td align="center"><%=currStatus%></td>
    <td><input type="button" value="Compare" onclick="location.href='<%=org.opennms.web.Util.calculateUrlBase( request )%>conf/viewcmpinventory.jsp?firstinvpath=<%=firstInvPath%>&firstlabel=<%=firstNodeLabel%>&firstlptime=<%=firstLpTime%>&invcat=<%=name%>&secondinvpath=<%=pathFile%>&secondlabel=<%=nodeLabel%>&secondlptime=<%=lastPollTime%>';"/>
</td>
  </tr>
    <%
}
%>
<tr> 
          <td colspan="6" align="center">
<%

		if (currentpage > 1)
		{
%> 
            <a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage-2)%>&file=<%=pathToFile%>">&lt;&lt;&nbsp;Prev&nbsp;</a> 
<%
		}
		else
		{
%> 
		&lt;&lt;&nbsp;Prev&nbsp; 
<%
		}
		for (int i = 1; i <= npage; i++)
		{
			if (i != currentpage) {
%> 
&nbsp;<a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(i-1)%>&file=<%=pathToFile%>"><%=i%></a> 
            <%
			}
			else
			{
%> 
&nbsp;<%=i%> 
            <%		
			}
		}
		if (currentpage < npage)
		{
		%> 
            <a href="<%=request.getContextPath()%>/conf/compareinventory.jsp?sort=<%=sort%>&name=<%=name%>&firstinvpath=<%=firstInvPath%>&nodelabel=<%=firstNodeLabel%>&lptime=<%=firstLpTime%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage)%>&file=<%=pathToFile%>">&nbsp;Next&nbsp;&gt;&gt;</a> 
            <%
		}
		else
		{
		%> 
		&nbsp;Next&nbsp;&gt;&gt; 
            <%
		}
%>
</tr>
</table>
</td><td>&nbsp;</td></tr>
</table><br>
<%
}
else
	{
	out.println("<br>&nbsp;&nbsp;<strong>Inventories for comparison not found.</strong><br>");
	}

%>

  <br>
  &nbsp; 

<br><br>
<jsp:include page="/includes/footer.jsp" flush="false"/>
</body>
</html>
