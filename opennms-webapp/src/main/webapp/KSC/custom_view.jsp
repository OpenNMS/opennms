<%--

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
// 2006 Nov 09: Added Read-Only User
// 2006 Oct 04: Added zoom capability.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.opennms.com/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		java.io.*,
		org.opennms.web.*,
		org.opennms.web.performance.*,
		org.opennms.web.graph.PrefabGraph,
		org.opennms.web.element.NetworkElementFactory,
                org.opennms.web.acegisecurity.Authentication,
		org.opennms.netmgt.config.kscReports.*,
		org.opennms.netmgt.config.KSC_PerformanceReportFactory
	"
%>

<%@ include file="/WEB-INF/jspf/KSC/init2.jspf" %> 
<%@ include file="/WEB-INF/jspf/graph-common.jspf"%>
<%@ include file="/WEB-INF/jspf/KSC/nodereport.jspf" %> 
<%@ include file="/WEB-INF/jspf/KSC/domainreport.jspf" %> 

<%
    String[] requiredParameters = new String[] { "report or domain", "type" };

    // Get number of graphs per line
    int report_graphsperline=1;

    // Get Form Variable
    String report_type = request.getParameter("type");
    if (report_type == null) {
        throw new MissingParameterException("type", requiredParameters);
    }

    String r_index = request.getParameter("report");
    String domain = request.getParameter("domain");
    int report_index=0;
    if (r_index != null) {
        report_index = Integer.parseInt(r_index);
    } else if (domain == null) {
        throw new MissingParameterException("report", requiredParameters);
    }
    
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
    } else if (report_type.equals("domain")) {
        report = buildDomainReport(domain);
    } else { 
        ReportsList reports_list = this.reportFactory.getConfiguration();
        report = reports_list.getReport(report_index);
    } 
    if (report == null) {
        throw new ServletException ("Report does not exist");
    }

    // Define the possible graph options 
    Graph graph = null; 
    PrefabGraph[] graph_options = null;
    if (report.getGraphCount() > 0) {
        graph = report.getGraph(0); // get the first graph in the list
	boolean isNode = false;
        boolean includeNodeQueries = false;
        if(report_type.equals("custom")) {
            includeNodeQueries = true;
        }

        if(graph.getDomain() != null && !graph.getDomain().equals("null")) {
            graph_options = this.model.getAllQueries (graph.getDomain(), includeNodeQueries, isNode);
	    this.log("custom_view: Retrieving graph options for domain " + graph.getDomain());
	}
        else {
	    isNode = true;
            graph_options = this.model.getAllQueries (graph.getNodeId(), includeNodeQueries, isNode);
	    this.log("custom_view: Retrieving graph options for node " + graph.getNodeId());
        }

        // Get default graph type from last element of graph_options

        if((report_type.equals("node") || report_type.equals("domain")) && override_graphtype.equals("none")) {
	    override_graphtype = graph_options[graph_options.length - 1].getName();
	    this.log("custom_view: setting default graph type to " + graph_options[graph_options.length - 1].getName());
	}

        if(graph_options.length > 1) {
            Arrays.sort(graph_options, 0, graph_options.length - 2);
	}
    }
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.jsp'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom View" />
</jsp:include>


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

<h3 align="center"><%=report.getTitle()%></h3>
<% if (report_type.equals("node") && this.model.getQueries(r_index).length > 0) { %>
  <h3 align="center"><a href="graph/results.htm?parentResourceType=node&amp;parentResource=<%=report_index%>&amp;resourceType=node&amp;resource=&amp;reports=all">Show Node-level Performance Data</a></h3>
<% } %>

<% int graph_count = report.getGraphCount(); %>
<% if (graph_count <= 0) { %>
    <h3 align="center">No interface graphs defined for this report.</h3>
<% } else { %>

    <form name="view_form" method="get" action="KSC/form_proc_view.jsp">
        <input type=hidden name="type" value="<%=report_type%>" >
        <input type=hidden name="action" value="none">
        <input type=hidden name="domain" value="<%=domain%>">
        <input type=hidden name="report" value="<%=r_index%>">

    <table width="100%" align="center">

        <tr>
            <td>
            <table width="100%" >
                <% for (int i=0; i< graph_count; i++) { 
                       int nodeId = 0;
                       String parentResourceType;
                       String parentResourceTypeLabel;
                       String parentResource;
                       String parentResourceLabel;
                       String parentResourceLink = null;
                       
                       Graph current_graph = report.getGraph(i); 
                       String curr_domain = current_graph.getDomain();
                       String intf = current_graph.getInterfaceId();
                       
                       if(current_graph.getNodeId() != null && !current_graph.getNodeId().equals("null")) {
                           nodeId = Integer.parseInt(current_graph.getNodeId());
                           parentResourceType = "node";
                           parentResourceTypeLabel = "Node";
                           parentResource = Integer.toString(nodeId);
                           parentResourceLink = "element/node.jsp?node=" + nodeId;
                           parentResourceLabel = NetworkElementFactory.getNodeLabel(nodeId);
                       } else {
                           parentResourceType = "domain";
                           parentResourceTypeLabel = "Domain";
                           parentResource = current_graph.getDomain();
                           parentResourceLabel = current_graph.getDomain();
                       }

                       String display_graphtype = null;
                       if (override_graphtype.equals("none")) {
                           display_graphtype = current_graph.getGraphtype();
                       } else { 
                           display_graphtype = override_graphtype;
                       } 
                       PrefabGraph display_graph = (PrefabGraph) this.model.getQuery(display_graphtype);

                       String resourceType;
                       String resourceTypeLabel = null;
                       String resource;
                       String resourceLabel = null;
                       String resourceLink = null;
                       if (intf == null || "".equals(intf) || !display_graph.getType().equals("interface")) {
                           resourceType = "node";
                           resource = "";
                       } else {
                           resourceType = "interface";
                           resourceTypeLabel = "Interface";
                           resource = intf;
                           if ("domain".equals(parentResourceType)) {
                               resourceLabel = intf;
                               resourceLink = "element/nodelist.jsp?listInterfaces&ifAlias=" + intf;
                           } else {
                               resourceLabel = this.model.getHumanReadableNameForIfLabel(nodeId, intf);
                           }
                       }
                       
                %>
            
		    <% if ((i == 0) || (i%report_graphsperline == 0)) { %>
                    <tr>
		    <% } %>
                        <td align="center">
			    <table>
			    <tr><td>
                            <b> <%=current_graph.getTitle()%> <br/></b>
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
			    </td><td>
                                <%= parentResourceTypeLabel %>:
                                <% if (parentResourceLink != null) { %>
                                  <a href="<%= parentResourceLink %>"><%= parentResourceLabel %></a>
                                <% } else { %>
                                  <%= parentResourceLabel %>
                                <% } %>
                                <br/>
                                <% if (resourceTypeLabel != null) { %>
                                    <%= resourceTypeLabel %>:
                                    <% if (resourceLink != null) { %>
                                      <a href="<%= resourceLink %>"><%= resourceLabel %></a>
                                    <% } else { %>
                                      <%= resourceLabel %>
                                    <% } %>
                                    <br/>
                                <% } %>
                                
                                <a href="graph/results.htm?type=performance&amp;parentResourceType=<%= parentResourceType %>&amp;parentResource=<%= parentResource %>&amp;resourceType=<%= resourceType %>&amp;resource=<%= resource %>&amp;reports=all&amp;start=<%=start%>&amp;end=<%=end%>">Detail</a>
			            </td></tr></table>
                                    <br/>
                                    <a href="graph/results.htm?zoom=true&amp;type=performance&amp;parentResourceType=<%= parentResourceType %>&amp;parentResource=<%= parentResource %>&amp;resourceType=<%= resourceType %>&amp;resource=<%= resource %>&amp;reports=<%=display_graph.getName()%>&amp;start=<%=start%>&amp;end=<%=end%>">

                            <img src="graph/graph.png?type=performance&amp;parentResourceType=<%= parentResourceType %>&amp;parentResource=<%= parentResource %>&amp;resourceType=<%= resourceType %>&amp;resource=<%= resource %>&amp;report=<%=display_graph.getName()%>&start=<%=start%>&end=<%=end%>"/>
                            </a>

                        </td>
		    <% if (((i+1)%report_graphsperline == 0) || (i+1) == graph_count){ %>
                    </tr>
		    <% } %>
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
                                    <% for (int i=0; i < graph_options.length - 1; i++) { %>
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
                        <% if( !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
                        <td>
                            <input type="button" value="Customize This Report" onclick="customizeReport()"><br>
                        </td>
                        <% } %>
                    </tr>
                </table>
            </td> 
        </tr>

    </table>

    </form>

<% } // end if (graph_count <=0) %>

<jsp:include page="/includes/footer.jsp" flush="false"/>
