<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.Util,org.opennms.web.performance.*" %>

<%!
    public PerformanceModel model = null;

    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel( org.opennms.web.ServletInitializer.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
        }
    }
%>

<%
    PerformanceModel.QueryableNode[] nodes = this.model.getQueryableNodes();
%>

<html>
<head>
  <title>Performance | Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
  function validateNode()
  {
      var isChecked = false
      for( i = 0; i < document.choose_node.node.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_node.node[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the node that you would like to report on.");
      }
      return isChecked;
  }

  function validateNodeAdhoc()
  {
      var isChecked = false
      for( i = 0; i < document.choose_node_adhoc.node.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_node_adhoc.node[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the node that you would like to report on.");
      }
      return isChecked;
  }

  function submitForm()
  {
      if (validateNode())
      {
          document.choose_node.submit();
      }
  }

  function submitFormAdhoc()
  {
      if (validateNodeAdhoc())
      {
          document.choose_node_adhoc.submit();
      }
  }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='report/index.jsp'>Reports</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Performance"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Performance" />
  <jsp:param name="location" value="performance" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">
      <h3>Standard Performance Reports</h3>

      <form method="get" name="choose_node" action="performance/addIntfFromNode" >
        <p>Choose a node to generate a standard performance report on.</p>
        <p>
          <input type="hidden" name="endUrl" value="performance/choosereportanddate.jsp" />
          <select name="node" size="10">
            <% for( int i=0; i < nodes.length; i++ ) { %>
                <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
            <% } %>
          </select>
        </p>
        <p>
          <input type="button" value="Start" onclick="submitForm()"/>
        </p>
      </form>

      <h3>Custom Performance Reports</h3>

      <form method="get" name="choose_node_adhoc" action="performance/adhoc.jsp" >
        <p>Choose a node to generate a custom performance report on.</p>
        <p>
          <select name="node" size="10">
            <% for( int i=0; i < nodes.length; i++ ) { %>
              <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
            <% } %>
          </select>
        </p>
        <p><input type="button" value="Start" onclick="submitFormAdhoc()"/></p>
      </form>
    </td>

    <td>&nbsp;</td>

    <td valign="top" width="60%">
      <h3>Network Performance Data</h3>

      <p>The <strong>Standard Performance Reports</strong> provide a stock way to
        easily visualize the critical SNMP data collected from managed nodes throughout
        your network.
      <p>

      <p><strong>Custom Performance Reports</strong> can be used to produce a single
        graph that contains the data of your choice from a single interface or node.
        You can select the timeframe, line colors, line styles, and title of the graph
        and you can bookmark the results.
      </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="performance" />
</jsp:include>

</body>
</html>
