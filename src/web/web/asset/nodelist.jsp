<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.asset.*" %>

<%!
    AssetModel model;

    public void init() throws ServletException {
        this.model = new AssetModel();
    }
%>

<%
    String column = request.getParameter( "column" );
    String search = request.getParameter( "searchvalue" );

    if( column == null ) {
        throw new org.opennms.web.MissingParameterException( "column", new String[] {"column","searchvalue"} );
    }

    if( search == null ) {
        throw new org.opennms.web.MissingParameterException( "searchvalue", new String[] {"column","searchvalue"} );
    }

    AssetModel.MatchingAsset[] assets = model.searchAssets( column, search );

    String pageSizeString = request.getParameter( "pagesize" );
    int defaultPageSize = 10;
    int pageSize = (pageSizeString == null) ? defaultPageSize : Integer.parseInt( pageSizeString );
    String offsetString = request.getParameter( "offset" );	
    int offset = (offsetString == null) ? 0 : Integer.parseInt( offsetString );
%>

<html>
<head>
  <title>Asset List | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='asset/index.jsp'>Assets</a>"; %>
<% String breadcrumb2 = "Asset List"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Asset List" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <h3>Assets</h3>
    <td> &nbsp; </td>
  </tr>

  <tr>
    <td> &nbsp; </td>

  <% if( assets.length > 0 ) { %>
    <td valign="top">
      <table width="100%" cellspacing="2" cellpadding="2" border="1">
        <tr bgcolor="#999999">
          <td width="10%" align="center"><b>Asset</b></td>
          <td align="center"><b>Matching Text</b></td>
          <td align="center"><b>Category</b></td>
          <td align="center"><b>Asset Number</b></td>
          <td align="center"><b>Department</b></td>
          <td align="center"><b>Building</b></td>
          <td align="center"><b>Room ID</b></td>
          <td align="center"><b>Vendor</b></td>
        </tr>

      <% //for( int i=0; i < assets.length; i++ ) { 
        int n = assets.length;
	  for(int t = offset; t < ((n <(offset + pageSize))? n : offset + pageSize); t++){
	%>
        <tr  bgcolor="<%=(t%2 == 0) ? "white" : "#cccccc"%>">
          <td><a href="asset/detail.jsp?node=<%=assets[t].nodeId%>"><%=assets[t].nodeLabel%></a></td>
          <td><%=assets[t].matchingValue%>&nbsp;</td>
<%
			Asset asset = this.model.getAsset(assets[t].nodeId);
%>
          <td><%=asset.getCategory()%>&nbsp;</td>
          <td><%=asset.getAssetNumber()%>&nbsp;</td>
          <td><%=asset.getDepartment()%>&nbsp;</td>
          <td><%=asset.getBuilding()%>&nbsp;</td>
          <td><%=asset.getRoom()%>&nbsp;</td>
          <td><%=asset.getVendor()%>&nbsp;</td>
        </tr>
      <% } %>
<tr> 
          <td colspan="8" align="center" bgcolor="#999999">
<%
		int npage = n / pageSize;
		if (n%pageSize != 0) npage++;
		int currentpage = 1 + offset / pageSize;
		if (offset%pageSize != 0) currentpage++;
		if (currentpage > 1)
		{
%> 
            <a href="<%=request.getContextPath()%>/asset/nodelist.jsp?column=<%=column%>&searchvalue=<%=search%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage-2)%>">&lt;&lt;&nbsp;Prev&nbsp;</a> 
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
&nbsp;<a href="<%=request.getContextPath()%>/asset/nodelist.jsp?column=<%=column%>&searchvalue=<%=search%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(i-1)%>"><%=i%></a> 
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
            <a href="<%=request.getContextPath()%>/asset/nodelist.jsp?column=<%=column%>&searchvalue=<%=search%>&pagesize=<%=pageSize%>&offset=<%=pageSize*(currentpage)%>">&nbsp;Next&nbsp;&gt;&gt;</a> 
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
   </td>
  <% } else { %>
    <td>
      None found.
    </td>    
  <% } %>
    
    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
