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
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2005 Oct 01: Refactor relative date code. -- DJ Gregor
// 2003 Feb 28: Corrected day/week/month/year reports on some browsers.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 28: Added day/week/month/year reports.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
		org.opennms.web.performance.*,
		org.opennms.web.graph.*,
		org.opennms.core.resource.Vault,
		java.util.*,
		java.io.*,
		org.opennms.web.element.NetworkElementFactory
	"
%>

<%!
    protected PerformanceModel model = null;
    
    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel(Vault.getHomeDir());
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the PerformanceModel", t);
        }

        m_periods = new TimePeriod[] {
            new TimePeriod("lastday", "Last Day", Calendar.DATE, -1),
            new TimePeriod("lastweek", "Last Week", Calendar.DATE, -7),
            new TimePeriod("lastmonth", "Last Month", Calendar.DATE, -31),
            new TimePeriod("lastyear", "Last Year", Calendar.DATE, -366)
	};
    }
%>

<%
    String[] requiredParameters = new String[] { "report", "node" };

    // required parameter reports
    String reports[] = request.getParameterValues( "reports" );
    if(reports == null) {
        throw new MissingParameterException("report", requiredParameters);
    }
        
    // required parameter node
    String nodeIdString = request.getParameter( "node" );
    if (nodeIdString == null) {
        throw new MissingParameterException("node", requiredParameters);
    }
    int nodeId = Integer.parseInt(nodeIdString);
    
    // optional parameter intf
    String intf = request.getParameter( "intf" );

    // see if the start and end time were explicitly set as params    
    String start = request.getParameter( "start" );
    String end   = request.getParameter( "end" );

    String relativeTime = request.getParameter("relativetime");
        
    if ((start == null || end == null) && relativeTime != null) {
	TimePeriod period = m_periods[0]; // default to the first one
	for (int i = 0; i < m_periods.length; i++) {
	    if (relativeTime.equals(m_periods[i].getId())) {
		period = m_periods[i];
		break;
	    }
	}
        Calendar cal = new GregorianCalendar();
        end = Long.toString(cal.getTime().getTime());
        cal.add(period.getOffsetField(), period.getOffsetAmount());
        start = Long.toString(cal.getTime().getTime());        
    }
    
    if( start == null || end == null ) {
        String startMonth = request.getParameter( "startMonth" );
        String startDate  = request.getParameter( "startDate" );
        String startYear  = request.getParameter( "startYear" );
        String startHour  = request.getParameter( "startHour" );

        String endMonth = request.getParameter( "endMonth" );
        String endDate  = request.getParameter( "endDate" );
        String endYear  = request.getParameter( "endYear" );
        String endHour  = request.getParameter( "endHour" );

        if( startMonth == null || startDate == null || startYear == null || startHour == null ||
            endMonth == null   || endDate == null   || endYear == null   || endHour == null )
        {
            throw new MissingParameterException( "startMonth", new String[] { "startMonth", "startDate", "startYear", "startHour", "endMonth", "endDate", "endYear", "endHour" } );
        }
        else
        {
            Calendar startCal = Calendar.getInstance();
            startCal.set( Calendar.MONTH, Integer.parseInt( startMonth ));
            startCal.set( Calendar.DATE, Integer.parseInt( startDate ));
            startCal.set( Calendar.YEAR, Integer.parseInt( startYear ));
            startCal.set( Calendar.HOUR_OF_DAY, Integer.parseInt( startHour ));
            startCal.set( Calendar.MINUTE, 0 );
            startCal.set( Calendar.SECOND, 0 );
            startCal.set( Calendar.MILLISECOND, 0 );

            Calendar endCal = Calendar.getInstance();
            endCal.set( Calendar.MONTH, Integer.parseInt( endMonth ));
            endCal.set( Calendar.DATE, Integer.parseInt( endDate ));
            endCal.set( Calendar.YEAR, Integer.parseInt( endYear ));
            endCal.set( Calendar.HOUR_OF_DAY, Integer.parseInt( endHour ));
            endCal.set( Calendar.MINUTE, 0 );
            endCal.set( Calendar.SECOND, 0 );
            endCal.set( Calendar.MILLISECOND, 0 );

            start = Long.toString( startCal.getTime().getTime() );
            end   = Long.toString( endCal.getTime().getTime() );
        }
    }

    //gather information for displaying around the image
    Date startDate = new Date( Long.parseLong( start ));
    Date endDate   = new Date( Long.parseLong( end ));
    
    //convert the report names to graph objects
    PrefabGraph[] graphs = new PrefabGraph[reports.length];

    for( int i=0; i < reports.length; i++ ) {
        graphs[i] = (PrefabGraph)this.model.getQuery(reports[i]);
        
        if(graphs[i] == null) {
            throw new IllegalArgumentException("Unknown report name: " + reports[i]);
        }
    }

    //sort the graphs by their order in the properties file
    //(PrefabGraph implements the Comparable interface)
    Arrays.sort(graphs);    
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Performance Results" />
  <jsp:param name="headTitle" value="Results" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='performance/index.jsp'>Performance</a>" />
  <jsp:param name="breadcrumb" value="Results" />
</jsp:include>

<div id="graph-results">
  <h3>
    Node: <a href="element/node.jsp?node=<%=nodeId%>"><%=NetworkElementFactory.getNodeLabel(nodeId)%></a><br/>
    <% if(intf != null ) { %>
      Interface: <%=this.model.getHumanReadableNameForIfLabel(nodeId, intf)%>
    <% } %>
  </h3>

  <% printRelativeTimeForm(out, relativeTime, nodeId, intf, reports); %>

  <h3>Interface Performance Data</h3>
  <strong>From</strong> <%=startDate%> <br/>
  <strong>To</strong> <%=endDate%> <br/>

  <% if(graphs.length > 0) { %>
    <% for(int i=0; i < graphs.length; i++ ) { %>
      <%-- encode the RRD filenames based on the graph's required data sources --%>
      <% String[] rrds = this.getRRDNames(nodeId, intf, graphs[i]); %> 
      <% String rrdParm = this.encodeRRDNamesAsParmString(rrds); %>
                          
      <%-- handle external values, if any --%>
      <% String externalValuesParm = this.encodeExternalValuesAsParmString(nodeId, intf, graphs[i]); %>

      <a href="/opennms/performance/zoom.jsp?intf=<%=intf%>&node=<%=nodeId%>&reports=<%=graphs[i].getName()%>&start=<%=start%>&end=<%=end%>"><img src="snmp/performance/graph.png?props=<%=nodeId%>/strings.properties&report=<%=graphs[i].getName()%>&start=<%=start%>&end=<%=end%>&<%=rrdParm%>&<%=externalValuesParm%>intf=<%=intf%>&node=<%=nodeId%>"/></a>
      <br/>
    <% } %>
  <% } else { %>
    No SNMP performance data has been gathered at this level
  <% } %>

  <% printRelativeTimeForm(out, relativeTime, nodeId, intf, reports); %>

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
            buffer.append(nodeId);            
            buffer.append(File.separator);
            
            if(intf != null && PerformanceModel.INTERFACE_GRAPH_TYPE.equals(graph.getType())) {             
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

    public class TimePeriod {
    	   private String m_id = null;
	   private String m_name = null;
	   private int m_offsetField = Calendar.DATE;
	   private int m_offsetAmount = -1;

	   public TimePeriod() {
	   }

	   public TimePeriod(String id, String name, int offsetField,
	   	  	     int offsetAmount) {
	   	  m_id = id;
		  m_name = name;
		  m_offsetField = offsetField;
		  m_offsetAmount = offsetAmount;
	   }

	   public String getId() {
	   	  return m_id;
	   }

	   public void setId(String id) {
	   	  m_id = id;
	   }

	   public String getName() {
	   	  return m_name;
	   }

	   public void setName(String name) {
	   	  m_name = name;
	   }

	   public int getOffsetField() {
	   	  return m_offsetField;
	   }

	   public void setOffsetField(int offsetField) {
	   	  m_offsetField = offsetField;
	   }

	   public int getOffsetAmount() {
	   	  return m_offsetAmount;
	   }

	   public void setOffsetAmount(int offsetAmount) {
	   	  m_offsetAmount = offsetAmount;
	   }
    }

    private TimePeriod[] m_periods;

    private void printRelativeTimeForm(JspWriter out, String relativetime,
    	 		       int nodeId, String intf, String[] reports)
			       throws IOException {
   	if (relativetime == null) {
            relativetime = "unknown";
        }

        String reportList = "";
	for (int i = 0; i < reports.length; i++) {
	    reportList = reportList + "&reports=" + reports[i];
	}

	out.println("    <div align=\"center\">");
	out.println("      <form name=\"reltimeform\">");
	out.println("        <table class=\"periods\">");
	out.println("	       <tbody>");

	out.println("	         <tr>");
	for (int i = 0; i < m_periods.length; i++) {
	    out.println("	           <td>" + m_periods[i].getName() +
	    		"</td>");
	}
	out.println("	         </tr>");

	out.println("	         <tr>");
	for (int i = 0; i < m_periods.length; i++) {
	    out.println("	           <td>" +
	    	        "<input type=\"radio\" name=\"rtstatus\"" +
			(relativetime.equals(m_periods[i].getId()) ?
			    " checked" : "") +
			" onclick=\"top.location = " +
			"'/opennms/performance/results.jsp?" +
			"relativetime=" + m_periods[i].getId() +
			"&intf=" + intf +
			"&node=" + nodeId +
			reportList + "'\"/></td>");
	}
	out.println("	         </tr>");
	out.println("	       </tbody>");
	out.println("        </table>");
	out.println("      </form>");
	out.println("    </div>");
    }

%>
