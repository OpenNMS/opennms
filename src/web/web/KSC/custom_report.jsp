<%@page language="java" contentType="text/html" session="true" import="java.util.*,java.io.*,org.opennms.web.performance.*,org.opennms.web.graph.*,org.opennms.web.element.NetworkElementFactory,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init2.jsp" %> 
<%@ include file="include_rrd.jsp" %> 

<%
    // Get Form Variables
    Report report = this.reportFactory.getWorkingReport();
    int report_index = this.reportFactory.getWorkingReportIndex();      
%>


<html>
<head>
  <title>Performance | Reports | KSC </title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<%-- A script to Save the file --%>
<script language="Javascript" type="text/javascript">
    function saveReport()
    {
        document.customize_form.action.value = "Save"; 
        document.customize_form.submit();
    }
 
    function addNewGraph()
    {
        document.customize_form.action.value = "AddGraph"; 
        document.customize_form.submit();
    }
 
    function modifyGraph(graph_index)
    {
        document.customize_form.action.value = "ModGraph"; 
        document.customize_form.graph_index.value = graph_index; 
        document.customize_form.submit();
    }
 
    function deleteGraph(graph_index)
    {
        document.customize_form.action.value = "DelGraph";
        document.customize_form.graph_index.value = graph_index; 
        document.customize_form.submit();
    }
 
    function cancelReport()
    {
        var fer_sure = confirm("Do you really want to cancel configuration changes?");
        if (fer_sure==true) {
            window.location="KSC/index.jsp";
        }
    }
    
</script>


<% String breadcrumb1 = "<a href='report/index.jsp'>Reports</a>"; %>
<% String breadcrumb2 = "KSC Reports"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<!-- Body -->
<h3 align="center">Customized Report Configuration</h3>

    <form name="customize_form" method="get" action="KSC/form_proc_report.jsp">
        <input type=hidden name="action" value="none">
        <input type=hidden name="graph_index" value="-1">

    <table width="100" align="center">
        <tr align = "center">
            <td> 
                <table align="center">
                    <tr>
                        <td>
                            Title: 
                        </td>
                        <td>
                            <input type="text" name="report_title" value="<%=report.getTitle()%>" size="80" maxlength="80">
                        </td>
                    </tr>
                </table>
            </td> 
        </tr>
        <tr>
            <td>
 

            <table width="100%" border="2">
                <% int graph_count = report.getGraphCount();
                   for (int i=0; i< graph_count; i++) { 
                       Graph current_graph = report.getGraph(i); 
                       int nodeId = Integer.parseInt(current_graph.getNodeId());
                       String intf = current_graph.getInterfaceId();
                       PrefabGraph display_graph = (PrefabGraph) this.model.getQuery(current_graph.getGraphtype());
                       
                       // encode the RRD filenames based on the graph's required data sources 
                       String[] rrds = this.getRRDNames(nodeId, intf, display_graph);  
                       String rrdParm = this.encodeRRDNamesAsParmString(rrds); 
                       
                       // handle external values, if any 
                       String externalValuesParm = this.encodeExternalValuesAsParmString(nodeId, intf, display_graph); 
                %>
            
                    <tr>
                        <td>
                            <input type="button" value="Modify" onclick="modifyGraph(<%=i%>)"><br>
                            <input type="button" value="Delete" onclick="deleteGraph(<%=i%>)">
                        </td>
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
                                Calendar begin_time = Calendar.getInstance();
                                Calendar end_time = Calendar.getInstance();
                                this.reportFactory.getBeginEndTime(current_graph.getTimespan(), begin_time, end_time); 
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
                <input type="button" value="Add New Graph" onclick="addNewGraph()" alt="Add a new graph to the Report"><br>
            </td> 
        </tr>
        <tr>
            <td>
                <table align="center">
                     <tr>
                         <td>
                             <input type=checkbox name="show_timespan"  <% if (report.getShow_timespan_button()) {%> checked <%}%> >
                         </td>
                         <td>
                             Show Timespan Button (allows global manipulation of report timespan)
                         </td>
                     </tr>
                     <tr>
                         <td>
                             <input type=checkbox name="show_graphtype"  <% if (report.getShow_graphtype_button()) {%> checked <%}%> >
                         </td>
                         <td>
                             Show Graphtype Button (allows global manipulation of report prefabricated graph type)
                         </td>
                     </tr>
                </table> 
            </td> 
        </tr>
        <tr>
            <td> 
                <table align="center">
                    <tr>
                        <td>
                            <input type="button" value="Save" onclick="saveReport()" alt="Save the Report to File"><br>
                        </td>
                        <td>
                            <input type="button" value="Cancel" onclick="cancelReport()" alt="Cancel the report configuration"><br>
                        </td>
                    </tr>
                </table>
            </td> 
        </tr>

    </table>

    </form>

<br>
<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="KSC Reports" />
</jsp:include>

</body>

</html>


