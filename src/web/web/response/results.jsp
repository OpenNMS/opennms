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
// 2003 Feb 28: Corrected issue with day/week/month/year reports and some browsers.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 01: Added day/week/month/year reports.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 12: Added response time reports to webUI. Based on original
//              performance reports.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.*,
		org.opennms.web.response.*,
		org.opennms.web.graph.*,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.core.resource.Vault,
		java.util.*,
		java.io.*
	"
%>

<%!
    protected ResponseTimeModel model = null;
    
    
    public void init() throws ServletException {
        try {
            this.model = new ResponseTimeModel(Vault.getHomeDir());
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the ResponseTimeModel", t);
        }
    }
%>

<%
    String[] requiredParameters = new String[] {"report", "node", "intf"};

    // required parameter reports
    String reports[] = request.getParameterValues("reports");
    if (reports == null) {
        throw new MissingParameterException("report", requiredParameters);
    }
        
    // required parameter node
    String nodeIdString = request.getParameter("node");
    if (nodeIdString == null) {
        throw new MissingParameterException("node", requiredParameters);
    }
    // XXX no error checking!  Did parseInt fail?  Is it a valid nodeID?
    int nodeId = Integer.parseInt(nodeIdString);
    
    // required parameter intf
    String intf = request.getParameter("intf");
    if (intf == null) {
        throw new MissingParameterException("intf", requiredParameters);
    }

    // see if the start and end time were explicitly set as params    
    String start = request.getParameter("start");
    String end   = request.getParameter("end");
    
    if (start == null || end == null) {
        String relativeTime = request.getParameter("relativetime");
        
        /*
	 * TODO: Only support last 24 hours in this version, need to clean up
         * this code by making a comman date param API, LJK 04/30/2002
	 */
        if(relativeTime != null ) {
            if (relativeTime.equals("lastweek")) {
               java.util.Calendar cal = new java.util.GregorianCalendar();
               end = Long.toString(cal.getTime().getTime());
               cal.add(java.util.Calendar.DATE, -7);
               start = Long.toString(cal.getTime().getTime());
            } else if (relativeTime.equals("lastmonth")) {
               java.util.Calendar cal = new java.util.GregorianCalendar();
               end = Long.toString(cal.getTime().getTime());
               cal.add( java.util.Calendar.DATE, -31 );
               start = Long.toString(cal.getTime().getTime());
            } else if (relativeTime.equals("lastyear")) {
               java.util.Calendar cal = new java.util.GregorianCalendar();
               end = Long.toString(cal.getTime().getTime());
               cal.add(java.util.Calendar.DATE, -366);
               start = Long.toString(cal.getTime().getTime());
            } else {
               java.util.Calendar cal = new java.util.GregorianCalendar();
               end = Long.toString(cal.getTime().getTime());
               cal.add(java.util.Calendar.DATE, -1);
               start = Long.toString(cal.getTime().getTime());
            }
        }
    }
    
    if (start == null || end == null) {
        String startMonth = request.getParameter("startMonth");
        String startDate  = request.getParameter("startDate");
        String startYear  = request.getParameter("startYear");
        String startHour  = request.getParameter("startHour");

        String endMonth = request.getParameter("endMonth");
        String endDate  = request.getParameter("endDate");
        String endYear  = request.getParameter("endYear");
        String endHour  = request.getParameter("endHour");

        if (startMonth == null || startDate == null
	    || startYear == null || startHour == null
            || endMonth == null   || endDate == null
            || endYear == null   || endHour == null) {
            throw new MissingParameterException("startMonth", new String[] { "startMonth", "startDate", "startYear", "startHour", "endMonth", "endDate", "endYear", "endHour" } );
        }

        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.MONTH, Integer.parseInt(startMonth));
        startCal.set(Calendar.DATE, Integer.parseInt(startDate));
        startCal.set(Calendar.YEAR, Integer.parseInt(startYear));
        startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.MONTH, Integer.parseInt(endMonth));
        endCal.set(Calendar.DATE, Integer.parseInt(endDate));
        endCal.set(Calendar.YEAR, Integer.parseInt(endYear));
        endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        start = Long.toString( startCal.getTime().getTime());
        end  = Long.toString( endCal.getTime().getTime());
    }

    // gather information for displaying around the image
    Date startDate = new Date(Long.parseLong(start));
    Date endDate   = new Date(Long.parseLong(end));
    
    // convert the report names to graph objects
    PrefabGraph[] graphs = new PrefabGraph[reports.length];

    for (int i=0; i < reports.length; i++) {
        graphs[i] = (PrefabGraph)this.model.getQuery(reports[i]);
        
        if(graphs[i] == null) {
            throw new IllegalArgumentException("Unknown report name: " + reports[i]);
        }
    }

    /*
     * Sort the graphs by their order in the properties file.
     * Note: PrefabGraph implements the Comparable interface.
     */
    Arrays.sort(graphs);    

    String relativetime = request.getParameter("relativetime");

    if (relativetime == null) {
        relativetime = "unknown";
    }

    String reportList = "";
    for (int i=0; i < reports.length; i++) {
        reportList = reportList + "&reports=" + reports[i];
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Response Time Results" />
  <jsp:param name="headTitle" value="Results" />
  <jsp:param name="headTitle" value="Response Time" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='response/index.jsp'>Response Time</a>" />
  <jsp:param name="breadcrumb" value="Results" />
</jsp:include>

<div align="center">
  <h3>
    Node: <a href="element/node.jsp?node=<%=nodeId%>"><%=NetworkElementFactory.getNodeLabel(nodeId)%></a><br/>
    <% if(intf != null ) { %>
      Interface: <%=this.model.getHumanReadableNameForIfLabel(nodeId, intf)%>
    <% } %>
  </h3>

  <form name="reltimeform">
    <table>
      <tr>
        <td align="center" width="80">Last Day</td>
        <td align="center" width="80">Last Week</td>
        <td align="center" width="80">Last Month</td>
        <td align="center" width="80">Last Year</td>
      </tr>

      <tr>
        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastday") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastday&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>

        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastweek") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastweek&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>

        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastmonth") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastmonth&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>

        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastyear") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastyear&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>
      </tr>
    </table>
  </form>

  <h3>Interface Response Time Data</h3>
  
  <strong>From</strong> <%=startDate%> <br/>
  <strong>To</strong> <%=endDate%><br/>

  <% if(graphs.length > 0) { %>
    <% for(int i=0; i < graphs.length; i++ ) { %>
      <%-- Encode the RRD filenames based on the graph's required data
        -- sources.
        --%>
      <% String[] rrds = this.getRRDNames(nodeId, intf, graphs[i]); %> 
      <% String rrdParm = this.encodeRRDNamesAsParmString(rrds); %>
                        
      <%-- handle external values, if any --%>
      <% String externalValuesParm = this.encodeExternalValuesAsParmString(nodeId, intf, graphs[i]); %>
            
      <img src="response/graph.png?report=<%=graphs[i].getName()%>&start=<%=start%>&end=<%=end%>&<%=rrdParm%>&<%=externalValuesParm%>"/>
      <br/>
    <% } %>
  <% } else { %>
    No response time data has been gathered at this level.
  <% } %>


  <form name="reltimeform">
    <table>
      <tr>
        <td align="center" width="80">Last Day</td>
        <td align="center" width="80">Last Week</td>
        <td align="center" width="80">Last Month</td>
        <td align="center" width="80">Last Year</td>
      </tr>

      <tr>
        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastday") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastday&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>

        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastweek") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastweek&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>

        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastmonth") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastmonth&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>

        <td align="center">
          <input type="radio" name="rtstatus" <%=(relativetime.equals("lastyear") ? "checked" : "")%>
                 onclick="top.location = '/opennms/response/results.jsp?relativetime=lastyear&intf=<%=intf%>&node=<%=nodeId%><%=reportList%>'" ></input>
        </td>
      </tr>
    </table>
  </form>


  <jsp:include page="/includes/bookmark.jsp" flush="false" />
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    /** intf can be null */           
    public String[] getRRDNames(int nodeId, String intf, PrefabGraph graph) {
        if(graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }            
    
        String[] columns = graph.getColumns();
        String[] rrds = new String[columns.length];
         
        for(int i=0; i < columns.length; i++ ) {
            StringBuffer buffer = new StringBuffer();
            // buffer.append(nodeId);            
            // buffer.append(File.separator);
            
            if(intf != null) {             
                buffer.append(intf);
                buffer.append(File.separator);
            }
            
            buffer.append(columns[i]);
            buffer.append(org.opennms.netmgt.utils.RrdFileConstants.RRD_SUFFIX);            

            rrds[i] = buffer.toString();
        }   

        return rrds;             
    }



    public String encodeRRDNamesAsParmString(String[] rrds) {
        if(rrds == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        String parmString = "";
        
        if(rrds.length > 0) {
            StringBuffer buffer = new StringBuffer("rrd=");
            buffer.append(java.net.URLEncoder.encode(rrds[0]));
              
            for(int i=1; i < rrds.length; i++ ) {
                buffer.append("&rrd=");
                buffer.append(java.net.URLEncoder.encode(rrds[i]));
            }
            
            parmString = buffer.toString();              
        }
        
        return parmString;
    }
  
  
    /** currently only know how to handle ifSpeed external value; intf can be null */
    public String encodeExternalValuesAsParmString(int nodeId, String intf, PrefabGraph graph) throws java.sql.SQLException {
        if(graph == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");      
        }
        
        String parmString = "";        
        String[] externalValues = graph.getExternalValues();
        
        if(externalValues != null && externalValues.length > 0) {
            StringBuffer buffer = new StringBuffer();
            
            for(int i=0; i < externalValues.length; i++) {
                if("ifSpeed".equals(externalValues[i])) {
                    String speed = this.getIfSpeed(nodeId, intf);
                    
                    if(speed != null) {
                        buffer.append(externalValues[i]);
                        buffer.append("=");                        
                        buffer.append(speed);   
                        buffer.append("&");                        
                    }
                }
                else {
                    throw new IllegalStateException("Unsupported external value name: " + externalValues[i]);
                }                
            }
            
            parmString = buffer.toString();
        }        
        
        return parmString;
    }
    
    
    public String getIfSpeed(int nodeId, String intf) throws java.sql.SQLException {
        if(intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String speed = null;
        
        try {
            Map intfInfo = org.opennms.netmgt.utils.IfLabel.getInterfaceInfoFromIfLabel(nodeId, intf);

            //if the extended information was found correctly
            if(intfInfo != null) {
                speed = (String)intfInfo.get("snmpifspeed");
            }
        }
        catch (java.sql.SQLException e) {
            this.log("SQLException while trying to fetch extended interface info", e);
        }


        return speed;
    }
%>
