<%@page language="java" contentType="text/html" session="true" import="java.util.*,java.io.*,org.opennms.web.Util,org.opennms.web.performance.*,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>


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
    String nodeId = request.getParameter("node");
    int node_intval;
    if (nodeId.equals("null") || (nodeId == null)) {
        node_intval = -1;
    } 
    else {
        node_intval = Integer.valueOf(nodeId).intValue();
    } 

%>

<%-- Start the HTML Page Definition here --%>
<html>
<head>
  <title>Performance | Reports | KSC </title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<%-- A script for validating Node ID Selection Form before submittal --%>
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

  function submitNodeForm()
  {
      if (validateNode())
      {
          document.choose_node.submit();
      }
  }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("report/index.jsp") + "'>Reports</a>"; %>
<% String breadcrumb2 = "KSC Reports"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<h3 align="center">Customized Report - Graph Definition</h3>

<table width="100" align="center">
  <tr>
    <td>
      <h3>Step 1: Select Node to Report On</h3>
      <form method="get" name="choose_node" action="KSC/custom_graph2.jsp" >
          <input type="hidden" name="intf" value="<%=request.getParameter("intf")%>" >
          <table> 
              <tr>
                  <td>
                      <p>
                          <select name="node" size="10">
                              <% for( int i=0; i < nodes.length; i++ ) { %>
                                  <% if (node_intval == nodes[i].nodeId) { %>
                                      <option value="<%=nodes[i].nodeId%>" SELECTED><%=nodes[i].nodeLabel%></option>
                                  <% } else { %>
                                      <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
                                  <% } %>
                              <% } %>
                          </select>
                      </p>
                  </td>
              </tr>
              <tr>
                  <td>
                      <p> <input type="button" value="Submit" onclick="submitNodeForm()" alt="Select Node and Proceed to Step 2"/> </p>
                  </td>
              </tr>
          </table> 
      </form>
    </td>
  </tr>
</table>

<br>
<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="KSC Reports" />
</jsp:include>

</body>
</html>

