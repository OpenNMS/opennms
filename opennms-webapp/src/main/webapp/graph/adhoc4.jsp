<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"	contentType="text/html"	session="true"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils, org.opennms.web.servlet.MissingParameterException, org.opennms.web.api.Util"%>
<%@page import="java.util.*"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>

<%!
    public final static String[] REQUIRED_PARAMS =
	new String[] {
		"resourceId",
		"title",
		"style",
		"ds",
		"startMonth",
		"startDate",
		"startYear",
		"startHour",
		"startMonth",
		"startDate",
		"startYear",
		"startHour"
	};
%>

<%
    ResourceId resourceId = ResourceId.fromString(request.getParameter("resourceId"));

    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    
    ResourceService resourceService = webAppContext.getBean("resourceService", ResourceService.class);
    
    resourceService.promoteGraphAttributesForResource(resourceId);
    

    String title = request.getParameter( "title" );
    String style = request.getParameter( "style" );
    String ds = request.getParameter( "ds" );

    String startMonth = request.getParameter( "startMonth" );
    String startDate  = request.getParameter( "startDate" );
    String startYear  = request.getParameter( "startYear" );
    String startHour  = request.getParameter( "startHour" );

    String endMonth = request.getParameter( "endMonth" );
    String endDate  = request.getParameter( "endDate" );
    String endYear  = request.getParameter( "endYear" );
    String endHour  = request.getParameter( "endHour" );

    
    if( resourceId == null ) {
        throw new MissingParameterException( "resourceId", REQUIRED_PARAMS );
    }
    if( title == null ) {
        throw new MissingParameterException( "title", REQUIRED_PARAMS );
    }
    if( style == null ) {
        throw new MissingParameterException( "style", REQUIRED_PARAMS );
    }
    if( ds == null ) {
        throw new MissingParameterException( "ds", REQUIRED_PARAMS );
    }
    if( startMonth == null ) {
        throw new MissingParameterException( "startMonth", REQUIRED_PARAMS );
    }
    if( startDate == null ) {
        throw new MissingParameterException( "startDate", REQUIRED_PARAMS );
    }
    if( startYear == null ) {
        throw new MissingParameterException( "startYear", REQUIRED_PARAMS );
    }
    if( startHour == null ) {
        throw new MissingParameterException( "startHour", REQUIRED_PARAMS );
    }
    if( endMonth == null ) {
        throw new MissingParameterException( "endMonth", REQUIRED_PARAMS );
    }
    if( endDate == null ) {
        throw new MissingParameterException( "endDate", REQUIRED_PARAMS );
    }
    if( endYear == null ) {
        throw new MissingParameterException( "endYear", REQUIRED_PARAMS );
    }
    if( endHour == null ) {
        throw new MissingParameterException( "endHour", REQUIRED_PARAMS );
    }

    Calendar startCal = Calendar.getInstance();
    startCal.set( Calendar.MONTH, WebSecurityUtils.safeParseInt( startMonth ));
    startCal.set( Calendar.DATE, WebSecurityUtils.safeParseInt( startDate ));
    startCal.set( Calendar.YEAR, WebSecurityUtils.safeParseInt( startYear ));
    startCal.set( Calendar.HOUR_OF_DAY, WebSecurityUtils.safeParseInt( startHour ));
    startCal.set( Calendar.MINUTE, 0 );
    startCal.set( Calendar.SECOND, 0 );
    startCal.set( Calendar.MILLISECOND, 0 );

    Calendar endCal = Calendar.getInstance();
    endCal.set( Calendar.MONTH, WebSecurityUtils.safeParseInt( endMonth ));
    endCal.set( Calendar.DATE, WebSecurityUtils.safeParseInt( endDate ));
    endCal.set( Calendar.YEAR, WebSecurityUtils.safeParseInt( endYear ));
    endCal.set( Calendar.HOUR_OF_DAY, WebSecurityUtils.safeParseInt( endHour ));
    endCal.set( Calendar.MINUTE, 0 );
    endCal.set( Calendar.SECOND, 0 );
    endCal.set( Calendar.MILLISECOND, 0 );

    String start = Long.toString( startCal.getTime().getTime() );
    String end   = Long.toString( endCal.getTime().getTime() );

    //gather information for displaying around the image
    String startPretty = new Date( WebSecurityUtils.safeParseLong( start )).toString();
    String endPretty   = new Date( WebSecurityUtils.safeParseLong( end )).toString();
    
    
    String[] ignores = new String[] { "startMonth", "startYear", "startDate", "startHour","endMonth", "endYear", "endDate", "endHour" };
    Map<String,Object> additions = new HashMap<String,Object>();
    additions.put( "start", start );
    additions.put( "end", end );
    additions.put( "adhoc", "true" );
     
    String queryString = Util.makeQueryString( request, additions, ignores );
%>

<%@page import="org.opennms.web.svclayer.api.ResourceService"%>
<%@ page import="org.opennms.netmgt.model.ResourceId" %>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Custom")
          .headTitle("Resource Graphs")
          .headTitle("Reports")
          .breadcrumb("Reports", "report/index.jsp")
          .breadcrumb("Resource Graphs", "graph/index.jsp")
          .breadcrumb("Custom")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div align="center">
  <img src="graph/graph.png?<%=queryString%>" />

  <br/>

  <strong>From:</strong> <%=startPretty%> <br/>
  <strong>To:</strong> <%=endPretty%>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
