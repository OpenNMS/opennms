<%@page language="java" contentType="text/html" session="true" import="java.util.*, org.opennms.web.inventory.*, java.sql.Timestamp,org.opennms.web.element.*"%>

<html>
<head>
  <title>Inventory Query Result | OpenNMS Web Console</title>
  <base HREF=<%=org.opennms.web.Util.calculateUrlBase(request)%>>
  <link rel=stylesheet type=text/css href=includes/styles.css>
  <link href="../includes/styles.css" rel="stylesheet" type="text/css">
</head>

<body marginwidth=0 marginheight=0 LEFTMARGIN=0 RIGHTMARGIN=0 TOPMARGIN=0>
       
<%
  String breadcrumb1 = "<a href='conf/index.jsp' >Inventory</a>";
  String breadcrumb2="List";
%>
<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Inventory Query Result"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>"/>
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>"/>
</jsp:include>
<br>

<%
   Map constraints = new HashMap();
   Map parmDescr = new HashMap();
   Map parmDefVal = new HashMap();
   
   parmDescr.put("nodename","Node name contains");
   parmDescr.put("iplike","IP like");
   parmDescr.put("name","Inventory category");
   parmDescr.put("status","Inventory status");
   parmDescr.put("relativetime","Since");

   parmDefVal.put("nodename","");
   parmDefVal.put("iplike","*.*.*.*");
   parmDefVal.put("name","0");
   parmDefVal.put("status","Y");
   parmDefVal.put("relativetime","0");


   String nodeIdString = request.getParameter( "node" );

   InventoryFactory invFactory = new InventoryFactory();
   Inventory[] inventories = null ;

   String nameParm = "";
   nameParm = request.getParameter( "nodename" );
   if(nameParm!=null && !nameParm.equals("")){
		constraints.put("nodename", nameParm);
   }
   String ipLikeParm	= "";
   ipLikeParm = request.getParameter( "iplike" );
   if(ipLikeParm !=null && !ipLikeParm.equals("") && !ipLikeParm.equals("*.*.*.*")){
		constraints.put("iplike", ipLikeParm);
   }
     
   String name = "";
   name = request.getParameter( "name" );
   if(name!=null && !name.equals("0")){
		constraints.put("name", name );
   }
   String status = "";
   status = request.getParameter( "status" );
   String tmpStatus=null;
   if(status!=null && !status.equals("Y")){
		if(status.equals("A"))
			tmpStatus="Active";
		else if(status.equals("D"))
			tmpStatus="Deleted (Node)";
		else if(status.equals("N"))
			tmpStatus="Not Active";
		constraints.put("status", tmpStatus);
   }
   int sort = InventoryComparator.NODE_LABEL_SORT;
   if(request.getParameter( "sort" )!=null){
   	sort = Integer.parseInt(request.getParameter( "sort" ));
   }
   int lastPollTimeInt = 0;
   int nodeId = -1;
   if( nodeIdString != null ) {
     nodeId = Integer.parseInt( nodeIdString );
     inventories = invFactory.getInventoryOnNode(nodeId);
     InventoryComparator invComp = new InventoryComparator();
     invComp.setSort(sort);
     Arrays.sort(inventories, invComp);
     String nodeName = NetworkElementFactory.getNodeLabel(nodeId);
     %>
    <h2 align="center">Inventories of node: <%=nodeName%></h2>
</table>
<br>
<%
    String pageSizeString = request.getParameter( "pagesize" );
    int defaultPageSize = 10;
    int pageSize = (pageSizeString == null) ? defaultPageSize : Integer.parseInt( pageSizeString );
    String offsetString = request.getParameter( "offset" );	
    int offset = (offsetString == null) ? 0 : Integer.parseInt( offsetString );
    int n = inventories.length;
    if(n >0)
    {
	int npage = n / pageSize;
	if (n%pageSize != 0) npage++;
	int currentpage = 1 + offset / pageSize;
	if (offset%pageSize != 0) 
		currentpage++;
%>

 <table width="100%" cellspacing="0" cellpadding="2" border="0" align="center"><tr><td>&nbsp;</td><td>
<table width="100%" cellspacing="0" cellpadding="4" border="1"  align="center">
  <tr align="center" bgcolor="#999999">
    <td style="font-weight: bold; color: black;text-align: center;">Node</td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.LAST_POLL_TIME_SORT%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="Sort by last poll time (decr.)">Last Poll Time</a></td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.CREATE_TIME_SORT%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="Sort by create time (decr.)">Create Time</a></td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.CATEGORY_SORT%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="Sort by inventory category (case insensitive)">Inventory Category</a></td>
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.STATUS_SORT%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="Sort by status">Status</a></td>
  </tr>

<%
String pathFile = "";

for(int t = offset; t < ((n <(offset + pageSize))? n : offset + pageSize); t++)
{
%>
  <tr bgcolor="<%=(t%2 == 0) ? "white" : "#cccccc"%>"> 
	<%
	String nodeLabel = inventories[t].getNodeLabel();
	pathFile = inventories[t].getPathToFile();
	Timestamp lastPollTime = inventories[t].getLastPollTime();
	String currStatus = "Active";
	if(inventories[t].getStatus().equals("N"))
		currStatus="Not Active";
	else if(inventories[t].getStatus().equals("D")) 
		currStatus="Deleted (Node)";
	String inventName = inventories[t].getName();
	%>
    <td><a href="<%=request.getContextPath()%>/conf/showinventory.jsp?file=<%=pathFile%>&category=<%=inventName%>&lastpolltime=<%=lastPollTime%>&nodelabel=<%=nodeLabel%>"> <%=nodeLabel%></a></td>
    <td><%=lastPollTime%></td>
    <td><%=inventories[t].getCreateTime()%></td>
    <td><%=inventName%></td>
    <td align="center"><%=currStatus%></td>
  </tr>
    <%
}
%>
<tr> 
          <td colspan="5" align="center" bgcolor="#999999">
<%
		/*int npage = n / pageSize;
		if (n%pageSize != 0) npage++;
		int currentpage = 1 + offset / pageSize;
		if (offset%pageSize != 0) currentpage++;*/
		if (currentpage > 1)
		{
%> 
            <a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=sort%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage-2)%>">&lt;&lt;&nbsp;Prev&nbsp;</a> 
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
&nbsp;<a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=sort%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(i-1)%>"><%=i%></a> 
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
            <a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=sort%>&node=<%=nodeIdString%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage)%>">&nbsp;Next&nbsp;&gt;&gt;</a> 
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
	out.println("<h3>&nbsp;Inventories not Found.</h3>");
	}


} else {
		lastPollTimeInt  = Integer.parseInt(request.getParameter( "relativetime" ));
		String lpTime=null;
		if(lastPollTimeInt !=0){
			switch(lastPollTimeInt){
				case 0: lpTime="Any";
				  	 break;
				case 1: lpTime="Last hour";
				  	 break;
				case 2: lpTime="Last 4 hours";
				  	 break;
				case 3: lpTime="Last 8 hours";
				  	 break;
				case 4: lpTime="Last 12 hours";
				  	 break;
				case 5: lpTime="Last day";
				  	 break;
				case 6: lpTime="Last week";
				  	 break;
				case 7: lpTime="Last month";
				   	break;
			}
			constraints.put("relativetime", lpTime);
		}
 	 	inventories = invFactory.getInventories(nameParm,ipLikeParm,name,status,lastPollTimeInt);
     		InventoryComparator invComp = new InventoryComparator();
     		invComp.setSort(sort);
     		Arrays.sort(inventories, invComp);
    %>
    
    <table>
    <tr>
        <td>&nbsp;</td>
    	<td>Current search constraints:</td>
    </tr>
    <%
        
    if(constraints.size()>0)
     {
     Iterator iter = constraints.keySet().iterator();
     int count = 0;
     while(iter.hasNext()){
         %>
         
         <tr>
            <td>&nbsp;</td>
    	    <td>&nbsp;
         <%
	 count++;
	 String key = (String) iter.next();
	 String val = (String) constraints.get(key);
	 out.println(count+". "+ parmDescr.get(key) + ": " + val);
       if(key.equals("nodename")){
	 %>
	 <a href="conf/inventorylist.jsp?sort=<%=sort%>&nodename=<%=parmDefVal.get("nodename")%>&iplike=<%=ipLikeParm%>&status=<%=status%>&relativetime=<%=lastPollTimeInt%>&name=<%=name%>"> [Remove]</a>
  	 <%}
       if(key.equals("iplike")){
	 %>
	 <a href="conf/inventorylist.jsp?sort=<%=sort%>&nodename=<%=nameParm%>&iplike=<%=parmDefVal.get("iplike")%>&status=<%=status%>&relativetime=<%=lastPollTimeInt%>&name=<%=name%>"> [Remove]</a>
  	 <%}
       if(key.equals("status")){
	 %>
	 <a href="conf/inventorylist.jsp?sort=<%=sort%>&nodename=<%=nameParm%>&iplike=<%=ipLikeParm%>&status=<%=parmDefVal.get("status")%>&relativetime=<%=lastPollTimeInt%>&name=<%=name%>"> [Remove]</a>
  	 <%}
       if(key.equals("relativetime")){
	 %>
	 <a href="conf/inventorylist.jsp?sort=<%=sort%>&nodename=<%=nameParm%>&iplike=<%=ipLikeParm%>&status=<%=status%>&relativetime=<%=parmDefVal.get("relativetime")%>&name=<%=name%>"> [Remove]</a>
  	 <%}
       if(key.equals("name")){
	 %>
	 <a href="conf/inventorylist.jsp?sort=<%=sort%>&nodename=<%=nameParm%>&iplike=<%=ipLikeParm%>&status=<%=status%>&relativetime=<%=lastPollTimeInt%>&name=<%=parmDefVal.get("name")%>"> [Remove]</a>
  	 <%
  	 }
  	 %>
  	    </td>
  	 </tr>
       <%
      }

     }else{ %>
    	    <tr>
    	       <td>&nbsp;</td>
	       <td>&nbsp;
    	           No search constraints defined
    	       </td>
    	    </td>
	    <%}
   
%>
</table>
<br>
<%
    String pageSizeString = request.getParameter( "pagesize" );
    int defaultPageSize = 10;
    int pageSize = (pageSizeString == null) ? defaultPageSize : Integer.parseInt( pageSizeString );
    String offsetString = request.getParameter( "offset" );	
    int offset = (offsetString == null) ? 0 : Integer.parseInt( offsetString );
    int n = inventories.length;
if(n >0)
{

%>

 <table width="100%" cellspacing="0" cellpadding="2" border="0" align="center"><tr><td>&nbsp;</td><td>
<table width="100%" cellspacing="0" cellpadding="4" border="1" align="center">
  <tr align="center" bgcolor="#999999">
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.NODE_LABEL_SORT%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="sort by node label (case insensitive)">Node</a></td> 
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.LAST_POLL_TIME_SORT%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="sort by last poll time (decr.)">Last Poll Time</a></td> 
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.CREATE_TIME_SORT%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="sort by create time (decr.)">Create Time</a></td> 
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.CATEGORY_SORT%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="sort by inventory category">Inventory Category</a></td> 
    <td style="font-weight: bold; color: black;text-align: center;"><a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=InventoryComparator.STATUS_SORT%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=offset%>" title="sort by status">Status</a></td> 
  </tr>

<%
String pathFile = "";

for(int t = offset; t < ((n <(offset + pageSize))? n : offset + pageSize); t++)
{
%>
  <tr bgcolor="<%=(t%2 == 0) ? "white" : "#cccccc"%>"> 
	<%
	String nodeLabel = inventories[t].getNodeLabel();
	pathFile = inventories[t].getPathToFile();
	Timestamp lastPollTime = inventories[t].getLastPollTime();
	String currStatus = "Active";
	if(inventories[t].getStatus().equals("N"))
		currStatus="Not Active";
	else if(inventories[t].getStatus().equals("D")) 
		currStatus="Deleted (Node)";
	String inventName = inventories[t].getName();
	%>
    <td><a href="<%=request.getContextPath()%>/conf/showinventory.jsp?file=<%=pathFile%>&category=<%=inventName%>&lastpolltime=<%=lastPollTime%>&nodelabel=<%=nodeLabel%>"> <%=nodeLabel%></a></td>
    <td><%=lastPollTime%></td>
    <td><%=inventories[t].getCreateTime()%></td>
    <td><%=inventName%></td>
    <td align="center"><%=currStatus%></td>
  </tr>
    <%
}
%>
<tr> 
          <td colspan="5" align="center" bgcolor="#999999">
<%
		int npage = n / pageSize;
		if (n%pageSize != 0) npage++;
		int currentpage = 1 + offset / pageSize;
		if (offset%pageSize != 0) currentpage++;
		if (currentpage > 1)
		{
%> 
            <a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=sort%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage-2)%>">&lt;&lt;&nbsp;Prev&nbsp;</a> 
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
&nbsp;<a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=sort%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(i-1)%>"><%=i%></a> 
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
            <a href="<%=request.getContextPath()%>/conf/inventorylist.jsp?sort=<%=sort%>&relativetime=<%=lastPollTimeInt%>&status=<%=status%>&name=<%=name%>&iplike=<%=ipLikeParm%>&nodename=<%=nameParm%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage)%>">&nbsp;Next&nbsp;&gt;&gt;</a> 
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
	out.println("<h3>&nbsp;Inventories not Found.</h3>");
	}
}
%>

  <br>
  &nbsp; 

<br><br>
<jsp:include page="/includes/footer.jsp" flush="false">
</jsp:include>
</body>
</html>
