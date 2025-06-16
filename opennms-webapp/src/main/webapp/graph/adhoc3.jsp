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
<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,org.opennms.web.api.Util"
%>

<%  
    Calendar now = Calendar.getInstance();

    Calendar yesterday = Calendar.getInstance();
    yesterday.add( Calendar.DATE, -1 );
%>

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

<form method="get" action="graph/adhoc4.jsp">
  <%=Util.makeHiddenTags( request )%>

  <h3>Step 3: Choose the Title for the Graph</h3>
 
  Title: <input type="text" name="title" value="Graph Title" />

  <h3>Step 4: Choose the Time Span of the Graph</h3>

  Query from date<br/>

  <select name="startMonth">
    <% for( int i = 0; i < 12; i++ ) { %>
      <option value="<%=i%>" <% if( yesterday.get( Calendar.MONTH ) == i ) out.print("selected=\"selected\" ");%>><%=MONTHS[i]%></option>
    <% } %>
  </select>

  <input type="text" name="startDate" size="4" maxlength="2" value="<%=yesterday.get( Calendar.DATE )%>" />
  <input type="text" name="startYear" size="6" maxlength="4" value="<%=yesterday.get( Calendar.YEAR )%>" />

  <select name="startHour">
    <% int yesterdayHour = yesterday.get( Calendar.HOUR_OF_DAY ); %>
    <% for( int i = 1; i < 25; i++ ) { %>
      <option value="<%=i%>" <% if( yesterdayHour == i ) out.print( "selected " ); %>>
        <%=(i<13) ? i : i-12%>&nbsp;<%=(i<13) ? "AM" : "PM"%>
      </option>
    <% } %>
  </select>

  <br/>

  Query to date<br/>

  <select name="endMonth">
    <% for( int i = 0; i < 12; i++ ) { %>
      <option value="<%=i%>" <% if( now.get( Calendar.MONTH ) == i ) out.print("selected=\"selected\" ");%>><%=MONTHS[i]%></option>
    <% } %>
  </select>

  <input type="text" name="endDate" size="4" maxlength="2" value="<%=now.get( Calendar.DATE )%>" />
  <input type="text" name="endYear" size="6" maxlength="4" value="<%=now.get( Calendar.YEAR )%>" />

  <select name="endHour">
    <% int nowHour = now.get( Calendar.HOUR_OF_DAY ); %>
    <% for( int i = 1; i < 25; i++ ) { %>
      <option value="<%=i%>" <% if( nowHour == i ) out.print( "selected " ); %>>
        <%=(i<13) ? i : i-12%>&nbsp;<%=(i<13) ? "AM" : "PM"%>
      </option>
    <% } %>
  </select>

  <br/>
  <br/>

  <input type="submit" value="Next"/>
  <input type="reset" />
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    //note these run from 0-11, this is because of java.util.Calendar!
    public static final String[] MONTHS = new String[] {
       "January",
       "February",
       "March",
       "April",
       "May",
       "June",
       "July",
       "August",
       "September",
       "October",
       "November",
       "December"
    };
%>
