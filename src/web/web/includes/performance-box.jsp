<%--
  This page is included by other JSPs to create a box containing an
  entry to the performance reporting system.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.performance.*,org.opennms.web.Util" %>

<%!
    public PerformanceModel model = null;

    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel( org.opennms.core.resource.Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
        }
    }
%>

<%
    PerformanceModel.QueryableNode[] nodes = this.model.getQueryableNodes();
%>
      
<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" bgcolor="#cccccc">
  <tr>
    <td bgcolor="#999999" ><b><a href="performance/index.jsp">Performance</a></b></td>
  </tr>

<%  if( nodes != null && nodes.length > 0 ) { %>
  <tr> 
    <td>
      <form method="get" action="performance/addIntfFromNode" >
        <input type="hidden" name="endUrl" value="performance/addReportsToUrl" />
        <input type="hidden" name="relativetime" value="lastday" />

        <table width="100%" border="0" cellspacing="0" cellpadding="1">
          <tr>
            <td>
              <font size="-1">Choose a node to query</font>
            </td>
          </tr>
          <tr>
            <td>
              <select name="node" size="1">
                <% for( int i=0; i < nodes.length; i++ ) { %>
                   <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
                <% } %>
              </select>
            </td>
          </tr>
          <tr>
            <td>
              <input type="submit" value="Execute Query" />
            </td>
          </tr>
        </table>
      </form>
    </td>
  </tr>
<% } else { %>
  <tr>
    <td>
      No performance data has been gathered yet
    </td>
  </tr>        
<% }  %>
</table>
