<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of outages.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.outage.*" %>

<%! 
    public static final int ROW_COUNT = 12;
    OutageModel model = new OutageModel();    
%>

<%
    OutageSummary[] summaries = this.model.getCurrentOutageSummaries();
%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  <tr> 
    <td BGCOLOR="#999999"><a href="outage/current.jsp"><b>Nodes with Outages</b></a></td>
  </tr>

<% for( int i=0; i < ROW_COUNT; i++ ) { %>
  <% if( i < summaries.length ) { %>
    <% OutageSummary summary = summaries[i];
       String nodeLabel = summary.getNodeLabel();
       int nodeId = summary.getNodeId();
    %>
    <tr>
      <td align="left"><a href="element/node.jsp?node=<%=nodeId%>"><nobr><%=nodeLabel%></nobr></a></td>
    </tr>
  <% } else { %>
    <tr><td>&nbsp;</td></tr>
  <% } %>
<% } %>

<% if( summaries.length > ROW_COUNT ) { %>
  <tr>
    <td>
      <a HREF="outage/index.jsp"><%=summaries.length - ROW_COUNT%> more</a>
    </td>
  </tr>
<% } %>
</table>      
