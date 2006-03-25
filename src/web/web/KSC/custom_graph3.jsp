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
		org.opennms.web.performance.*,
		org.opennms.web.*,
		org.opennms.web.graph.PrefabGraph,
		java.io.File,
		org.opennms.netmgt.config.kscReports.*,
		org.opennms.netmgt.config.KSC_PerformanceReportFactory,
		org.opennms.web.element.NetworkElementFactory
	"
%>

<%@ include file="/WEB-INF/jspf/KSC/init2.jspf" %>
<%@ include file="/WEB-INF/jspf/graph-common.jspf"%>

<%
    String[] requiredParameters = new String[] {"node or domain", "intf"};

    // optional parameter node
    String nodeIdString = request.getParameter("node");

    //optional parameter domain
    String domain = request.getParameter("domain");

    //optional parameter gtype
    String gtype = request.getParameter("graphtype");

    if((nodeIdString == null) && ( domain == null )) {
        throw new MissingParameterException("node", requiredParameters);
    }

    // required parameter intf, a value of "" means to discard the intf
    String intf = request.getParameter("intf");
    if (intf == null) {
        throw new MissingParameterException("intf", requiredParameters);
    }
    
    int nodeId = 0;

    String nodeLabel = "";     

    PrefabGraph[] graph_options = null;

    if( nodeIdString != null && !nodeIdString.equals("null")) {
        boolean includeNodeQueries = false;
        nodeId = Integer.parseInt(nodeIdString);
        nodeLabel = NetworkElementFactory.getNodeLabel(nodeId);
	if(intf.equals("")) {
            graph_options = this.model.getQueries(nodeId);
        } else {
            graph_options = this.model.getQueries(nodeId, intf, includeNodeQueries);
        }
    } else {
        graph_options = this.model.getQueriesForDomain(domain, intf);
        nodeIdString = "null";
    }
    Report report = this.reportFactory.getWorkingReport(); 
    org.opennms.netmgt.config.kscReports.Graph sample_graph = this.reportFactory.getWorkingGraph(); 
    if (sample_graph == null) {
        throw new IllegalArgumentException("Invalid working graph argument -- null pointer. Possibly missing prefab report in snmp-graph.properties?");
    }
    int graph_index = this.reportFactory.getWorkingGraphIndex(); 

    // Set the node and interface values in the working graph (in case they changed)
    sample_graph.setNodeId(nodeIdString);
    sample_graph.setDomain(domain);
    sample_graph.setInterfaceId(intf);

    PrefabGraph display_graph = null;
    if(graph_options.length > 0) {
        if(gtype == null) {
            display_graph = (PrefabGraph) this.model.getQuery(graph_options[0].getName());
        } else {
            display_graph = (PrefabGraph) this.model.getQuery(sample_graph.getGraphtype());
        }
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance " />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.jsp'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Graph" />
</jsp:include>

<script language="Javascript" type="text/javascript">
 
    function saveGraph()
    {
        document.customize_graph.action.value="Save";
        document.customize_graph.submit();
    }
        
    function updateGraph()
    {
        document.customize_graph.action.value="Update";
        document.customize_graph.submit();
    }
   
    function cancelGraph()
    {
        var fer_sure = confirm("Do you really want to cancel graph configuration changes?");
        if (fer_sure==true) {
            document.customize_graph.action.value="Cancel";
            document.customize_graph.submit();
        }
    }
  
</script>


<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
    <form name="customize_graph" method="get" action="KSC/form_proc_graph.jsp" >
      <input type="hidden" name="action" value="none" />

      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
            <td colspan="2">
                <% if(graph_options.length > 0) { %>
                    <h3 align="center">Customized Report Graph Definition</h3> 
                    <h3 align="center">Choose Graph Type & Timespan</h3> 
                <% } else { %>
                    <h3 align="left">No graph options available. Check that the
                    correct data is being collected and that appropriate reports
                    are defined.</h3>
                <% } %>
            </td>
        </tr>

        <tr>
             <td>

                <table width="100%" border="2">
                    <tr>
                        <td>
                            <h3 align="center">Sample Graph Text</h3> 
                        </td>
                        <td>
                            <h3 align="center">Sample Graph Image</h3> 
                        </td>
                    </tr>
		    <% String[] rrds; %>
                    <%-- encode the RRD filenames based on the graph's required data sources --%>
                    <% if(nodeId > 0) { %>
                        <% rrds = this.getRRDNames(nodeId, intf, display_graph); %> 
                    <% } else { %>
                        <% rrds = this.getRRDNames(domain, intf, display_graph); %> 
                    <% } %>
                    <% String rrdParm = this.encodeRRDNamesAsParmString(rrds); %>

                    <% String externalValuesParm = ""; %>
                        
                    <%-- handle external values, if any --%>
                    <% if(nodeId > 0) { %>
                        <% this.log("custom graph3: encoding external values for node " + nodeId); %>
                        <% externalValuesParm = this.encodeExternalValuesAsParmString(nodeId, intf, display_graph); %>
                    <% } %>

                    <tr>
                        <td align="right">
                            Title: &nbsp; <input type="text" name="title" value="<%=sample_graph.getTitle()%>" size="40" maxlength="40"/> <br>
                            <h3>
                            <%if(nodeId > 0) {%>
                                Node: <a href="element/node.jsp?node=<%=nodeId%>">
                                <%=NetworkElementFactory.getNodeLabel(nodeId)%></a><br>
                                <% if(intf != null && !intf.equals("")) { %>
                                    Interface: <%=this.model.getHumanReadableNameForIfLabel(nodeId, intf)%>
                                <% } %>
                            <%} else {%>
                                Domain: <%=domain%><br>
                                Interface: <a href="element/nodelist.jsp?listInterfaces&ifAlias=<%=intf%>"><%=intf%></a><br/>
                            <%}%>
                            </h3>

                            <%-- gather start/stop time information --%>
                            <%  
                                Calendar begin_time = Calendar.getInstance();
                                Calendar end_time = Calendar.getInstance();
                                this.reportFactory.getBeginEndTime(sample_graph.getTimespan(), begin_time, end_time); 
                                String start = Long.toString( begin_time.getTime().getTime() );
                                String startPretty = new Date( Long.parseLong( start )).toString();
                                String end = Long.toString( end_time.getTime().getTime() );
                                String endPretty = new Date( Long.parseLong( end )).toString();
                             %>

                            <b>From</b> <%=startPretty%> <br>
                            <b>To</b> <%=endPretty%>
                        </td>
              
                        <td align="left">
                            <img src="graph/graph.png?type=performance&props=<%=nodeId%>/strings.properties&report=<%=display_graph.getName()%>&start=<%=start%>&end=<%=end%>&<%=rrdParm%>&<%=externalValuesParm%>"/>
                        </td>
                    </tr>
                </table>

          </td>
        </tr>

        <tr>
            <td>
                <table align="center">
                    <!-- Select Timespan Input -->  
                    <tr>
                        <td>
                            Graph Timespan
                        </td>
                        <td>
                            <SELECT name="timespan">
                            <% for (int i=0; i < this.reportFactory.timespan_options.length; i++) { %>
                                <% if (this.reportFactory.timespan_options[i].equals(sample_graph.getTimespan())) { %>
                                      <OPTION SELECTED> <%=this.reportFactory.timespan_options[i]%> 
                                <% } else { %>                  
                                      <OPTION> <%=this.reportFactory.timespan_options[i]%> 
                                <% } %>                  
                            <% } %>                  
                            </SELECT>  
                            (This selects the relative start and stop times for the report) 
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <!-- Select Graphtype Input -->  
                            Graph Type  
                        </td>
                        <td>
                            <SELECT name="graphtype">
                            <% for (int i=0; i < graph_options.length; i++) { %>
                                <% if (graph_options[i].getName().equals(sample_graph.getGraphtype())) { %>
                                        <OPTION SELECTED> <%=graph_options[i].getName() %> 
                                <% } else { %>                  
                                        <OPTION> <%=graph_options[i].getName() %>                   
                                <% } %>                  
                            <% } %>                  
                            </SELECT>   
                            (This selects the prefabricated graph type definition to use) 
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <!-- Select Graph Index -->  
                            Graph Index  
                        </td>
                        <td>
                            <SELECT name="graphindex">
                            <%  int max_graphs = report.getGraphCount();
                                if (graph_index == -1)  
                                    graph_index = max_graphs++;
                                for (int i=0; i < max_graphs; i++) { 
                                    if (i == graph_index) { %>
                                        <OPTION SELECTED> <%=i+1%> 
                                <% } else { %>                  
                                        <OPTION> <%=i+1%>                   
                                <% } %>                  
                            <% } %>                  
                            </SELECT>   
                            (This selects the desired position in the report for the graph to be inserted) 
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
                            <input type="button" value="Cancel" onclick="cancelGraph()" alt="Cancel the graph configuration"/>
                        </td>
                        <td>
                            <input type="button" value="Refresh Sample View" onclick="updateGraph()" alt="Update changes to sample graph"/>
                        </td>
                        <td>
                            <input type="button" value="Save" onclick="saveGraph()" alt="Save the graph configuration"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
      </table>
    </form>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>
                                         
<jsp:include page="/includes/footer.jsp" flush="false" />
