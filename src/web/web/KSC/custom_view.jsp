<%@page language="java" contentType="text/html" session="true" import="java.util.*,java.io.*,org.opennms.web.*,org.opennms.web.performance.*,org.opennms.web.graph.*,org.opennms.web.element.NetworkElementFactory,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init2.jsp" %> 
<%@ include file="include_rrd.jsp" %> 
<%@ include file="include_nodereport.jsp" %> 

<%
    // Get Form Variable
    String report_type = request.getParameter("type");
    if (report_type == null) {
        throw new MissingParameterException("type", new String[] {"report","type"});
    }

    String r_index = request.getParameter("report");
    if (r_index == null) {
        throw new MissingParameterException("report", new String[] {"report","type"});
    }
    int report_index = Integer.parseInt(r_index);     
    
    String override_timespan = request.getParameter("timespan");
    String override_graphtype = request.getParameter("graphtype");
    if ((override_timespan == null) || (override_timespan.equals("null"))) {
            override_timespan = "none"; 
    }
    if ((override_graphtype == null) || (override_graphtype.equals("null"))) {
        override_graphtype = "none";
    }
   
    // Load report to view 
    Report report = null;
    if (report_type.equals("node")) {
        report = buildNodeReport(report_index);
    } 
    else { 
        ReportsList reports_list = this.reportFactory.getConfiguration();
        report = reports_list.getReport(report_index);
    } 
    if (report == null) {
        throw new ServletException ("Report does not exist");
    }
    
    // Define the possible graph options (based on first graph in list)
    Graph graph = null; 
    PrefabGraph[] graph_options = null;
    if (report.getGraphCount() > 0) {
        graph = report.getGraph(0); // get the first graph in the list
        boolean includeNodeQueries = true;
        graph_options = this.model.getQueries (Integer.parseInt(graph.getNodeId()), graph.getInterfaceId(), includeNodeQueries); 
    }
%>


<html>
<head>
  <title>Performance | Reports | KSC </title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<%-- A script to Save the file --%>
<script language="Javascript" type="text/javascript">
    function customizeReport()
    {
        document.view_form.action.value = "Customize"; 
        document.view_form.submit();
    }
 
    function updateReport()
    {
        document.view_form.action.value = "Update"; 
        document.view_form.submit();
    }
 
    function exitReport()
    {
        document.view_form.action.value = "Exit"; 
        document.view_form.submit();
    }
</script>


<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("report/index.jsp") + "'>Reports</a>"; %>
<% String breadcrumb2 = java.net.URLEncoder.encode("KSC and Node Reports"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports and Node Reports" />
  <jsp:param name="location" value="KSC and Node Reports" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<!-- Body -->
<h3 align="center"><%=report.getTitle()%></h3>

<% int graph_count = report.getGraphCount(); %>
<% if (graph_count <= 0) { %>
    <h3 align="center">No graphs defined for this report.</h3>
<% } else { %>

    <form name="view_form" method="get" action="KSC/form_proc_view.jsp">
        <input type=hidden name="type" value="<%=report_type%>" >
        <input type=hidden name="action" value="none">
        <input type=hidden name="report" value="<%=r_index%>">

    <table width="100%" align="center">

        <tr>
            <td>
            <table width="100%" >
                <% for (int i=0; i< graph_count; i++) { 
                       Graph current_graph = report.getGraph(i); 
                       int nodeId = Integer.parseInt(current_graph.getNodeId());
                       String intf = current_graph.getInterfaceId();
                       String display_graphtype = null;
                       if (override_graphtype.equals("none")) {
                           display_graphtype = current_graph.getGraphtype();
                       } 
                       else { 
                           display_graphtype = override_graphtype;
                       } 
                       PrefabGraph display_graph = (PrefabGraph) this.model.getQuery(display_graphtype);
                       
                       // encode the RRD filenames based on the graph's required data sources 
                       String[] rrds = this.getRRDNames(nodeId, intf, display_graph);  
                       String rrdParm = this.encodeRRDNamesAsParmString(rrds); 
                       
                       // handle external values, if any 
                       String externalValuesParm = this.encodeExternalValuesAsParmString(nodeId, intf, display_graph); 
                %>
            
                    <tr>
                        <td align="right">
                            <h3> <%=current_graph.getTitle()%> <br>
                                Node: <a href="element/node.jsp?node=<%=nodeId%>">
                                <%=NetworkElementFactory.getNodeLabel(nodeId)%></a><br>
                                <% if(intf != null ) { %>
                                    Interface: <%=this.model.getHumanReadableNameForIfLabel(nodeId, intf)%>
                                <% } %>
                            </h3>

                            <%-- gather start/stop time information --%>
                            <%
                                String display_timespan = null;
                                if (override_timespan.equals("none")) {
                                    display_timespan = current_graph.getTimespan();
                                } 
                                else { 
                                    display_timespan = override_timespan;
                                } 
                                Calendar begin_time = Calendar.getInstance();
                                Calendar end_time = Calendar.getInstance();
                                this.reportFactory.getBeginEndTime(display_timespan, begin_time, end_time); 
                                String start = Long.toString( begin_time.getTime().getTime() );
                                String startPretty = new Date( Long.parseLong( start )).toString();
                                String end = Long.toString( end_time.getTime().getTime() );
                                String endPretty = new Date( Long.parseLong( end )).toString();
                             %>

                            <b>From</b> <%=startPretty%> <br>
                            <b>To</b> <%=endPretty%>
                        </td>
              
                        <td align="left">
                            <img src="snmp/performance/graph.png?report=<%=display_graph.getName()%>&start=<%=start%>&end=<%=end%>&<%=rrdParm%>&<%=externalValuesParm%>"/>
                        </td>
                    </tr>
                <% }  //end for loop %> 
            </table>  
            </td> 
        </tr>


        <tr>
            <td>
                <table align="center">
                    <!-- Select Timespan Input --> 
                    <% if (report.getShow_timespan_button()) { %> 
                        <tr>
                            <td>
                                Override Graph Timespan
                            </td>
                            <td>
                                <SELECT name="timespan">
                                    <% if (override_timespan.equals("none")) { %>
                                         <OPTION SELECTED>none 
                                    <% } else { %>                  
                                         <OPTION>none 
                                    <% } %>                  
                                    <% for (int i=0; i < this.reportFactory.timespan_options.length; i++) { %>
                                        <% if (this.reportFactory.timespan_options[i].equals(override_timespan)) { %>
                                              <OPTION SELECTED> <%=this.reportFactory.timespan_options[i]%> 
                                        <% } else { %>                  
                                              <OPTION> <%=this.reportFactory.timespan_options[i]%> 
                                        <% } %>                  
                                    <% } %>                  
                                </SELECT>  
                                (Press update button to reflect option changes to ALL graphs) 
                            </td>
                        </tr>
                    <% } %>

                    <% if (report.getShow_graphtype_button()) { %> 
                        <tr>
                            <td>
                                <!-- Select Graphtype Input -->  
                                Override Graph Type  
                            </td>
                            <td>
                                <SELECT name="graphtype">
                                    <% if (override_graphtype.equals("none")) { %>
                                         <OPTION SELECTED>none 
                                    <% } else { %>                  
                                         <OPTION>none 
                                    <% } %>                  
                                    <% for (int i=0; i < graph_options.length; i++) { %>
                                        <% if (graph_options[i].getName().equals(override_graphtype)) { %>
                                            <OPTION SELECTED> <%=graph_options[i].getName() %> 
                                        <% } else { %>                  
                                            <OPTION> <%=graph_options[i].getName() %>                   
                                        <% } %>                  
                                    <% } %>                  
                                </SELECT>   
                                (Press update button to reflect option changes to ALL graphs) 
                            </td>
                        </tr>
                    <% } %>
                </table>

            </td> 
        </tr>
        <tr>
            <td> 
                <table align="center">
                    <tr>
                        <td>
                            <input type="button" value="Exit Report Viewer" onclick="exitReport()"><br>
                        </td>
                        <td>
                            <% if (report.getShow_timespan_button() ||  report.getShow_graphtype_button()) { %> 
                                <input type="button" value="Update Report View" onclick="updateReport()"><br>
                            <% } %>
                        </td>
                        <td>
                            <input type="button" value="Customize This Report" onclick="customizeReport()"><br>
                        </td>
                    </tr>
                </table>
            </td> 
        </tr>

    </table>

    </form>

<% } // end if (graph_count <=0) %>

<br>
<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="KSC and Node Reports" />
</jsp:include>

</body>

</html>

