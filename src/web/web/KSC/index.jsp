<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.Util,org.opennms.web.performance.*,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init2.jsp" %>

<%
    PerformanceModel.QueryableNode[] nodes = this.model.getQueryableNodes();
%>

<%
    int r_count=0;
    ReportsList report_configuration = this.reportFactory.getConfiguration();  
    Report[] report_array = null;
    try {
         if (report_configuration == null){
            throw new ServletException ( "Couldn't retrieve KSC Report File configuration");
         }
         else {
            r_count = report_configuration.getReportCount(); 
            report_array = report_configuration.getReport();
         } 
    }
    catch( Exception e ) {
        throw new ServletException ( "Couldn't retrieve reports from KSC_PerformanceReportFactory.", e );
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
      for( i = 0; i < document.choose_node.report.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_node.report[i].selected)
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


<%-- A script for validating Custom Report Form before submittal --%>
<script language="Javascript" type="text/javascript" >
  function validateReport()
  {
      var isChecked = false
      for( i = 0; i < document.choose_report.report.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_report.report[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("No reports selected.  Please click on a report title to make a report selection.");
      }
      return isChecked;
  }

  function submitReportForm()
  {
      // Create New Action (Don't need to validate select list if adding new report) 
      if (document.choose_report.report_action[2].checked == true) {
          document.choose_report.submit();
      }
      else {
          if (validateReport()) {
              // Delete Action
              if (document.choose_report.report_action[4].checked == true) {
                  var fer_sure=confirm("Are you sure you wish to delete this report?")
                  if (fer_sure==true) {
                      document.choose_report.submit();
                  }
              }
              else {
                  // View, Customize, or CreateFrom Action 
                  document.choose_report.submit();
              }
          }
      }
  }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='report/index.jsp'>Reports</a>"; %>
<% String breadcrumb2 = "KSC and Node Reports"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports and Node Reports" />
  <jsp:param name="location" value="KSC and Node Reports" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<h3 align="center">Key SNMP Customized Peformance Reports and Node Reports</h3>

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  
  <tr>
    <td valign="top">
      <h3>KSC Performance Reports</h3>
      <p>Choose the custom report title to view or modify from the list below. There are <%=r_count%> custom reports to select from.</p>
      <form method="get" name="choose_report" action="KSC/form_proc_main.jsp">
         <table>
            <tr>
                <td>
                    <p> <select name="report" size="10">
                            <% for( int i=0; i < r_count; i++ ) { %>
                                <option value="<%=i%>"> <%=report_array[i].getTitle()%>  </option>
                            <% } %>
                        </select>
                    </p>
                </td>
                <td>
                    <p> 
                        <input type="radio" name="report_action" value="View" CHECKED>View <br>
                        <input type="radio" name="report_action" value="Customize">Customize <br>
                        <input type="radio" name="report_action" value="Create">Create New<br>
                        <input type="radio" name="report_action" value="CreateFrom">Create New From Existing<br>
                        <input type="radio" name="report_action" value="Delete">Delete<br>
                    </p>
                    <p> <input type="button" value="Submit" onclick="submitReportForm()" alt="Initiates Action for Custom Report"/> </p>
                </td>
            </tr>
        </table>
      </form>
    
    </td>
  </tr>
  
  <tr>
    <td>
      <h3>Node Reports</h3>
      <p>Select Node for desired performance report</p>
      <form method="get" name="choose_node" action="KSC/custom_view.jsp" >
          <input type="hidden" name="type" value="node">
          <table> 
              <tr>
                  <td>
                      <p>
                          <select name="report" size="10">
                              <% for( int i=0; i < nodes.length; i++ ) { %>
                                  <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
                              <% } %>
                          </select>
                      </p>
                  </td>
                  <td>
                      <p> <input type="button" value="Submit" onclick="submitNodeForm()" alt="Initiates Generation of Node Report"/> </p>
                  </td>
              </tr>
          </table> 
      </form>
    </td>
  </tr>

</table>
<br>
<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="KSC and Node Reports" />
</jsp:include>

</body>
</html>

