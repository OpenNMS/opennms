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
<%@page language="java" contentType="text/html" session="false" import="org.opennms.core.utils.WebSecurityUtils,org.opennms.web.servlet.MissingParameterException" %>

<%!
    protected static final String DEFAULT_LIMIT_PARAM_NAME    = "limit";
    protected static final String DEFAULT_MULTIPLE_PARAM_NAME = "multiple";

    protected static final int DEFAULT_LIMIT    = 25;
    protected static final int DEFAULT_MULTIPLE = 0;

    protected static final int LOWER_OFFSET = 5;
    protected static final int UPPER_OFFSET = 4;    
%>

<%
    //required parameter count
    String countString = request.getParameter("count");
    if( countString == null ) {
        throw new MissingParameterException("count", new String[] {"count", "baseurl"});
    }
    
    //required parameter baseurl    
    String baseUrl = request.getParameter("baseurl");
    if( baseUrl == null ) {
        throw new MissingParameterException("baseurl", new String[] {"count", "baseurl"});
    }

    //optional parameter limit    
    String limitString = request.getParameter("limit");

    //optional parameter multiple    
    String multipleString = request.getParameter("multiple");

    //optional parameter, limitname
    String limitName = request.getParameter("limitname");
    if(limitName == null) {
        limitName = DEFAULT_LIMIT_PARAM_NAME;
    } else {
        limitName = WebSecurityUtils.sanitizeString(limitName);
    }

    //optional parameter, multiplename
    String multipleName = request.getParameter("multiplename");
    if(multipleName == null) {
        multipleName = DEFAULT_MULTIPLE_PARAM_NAME;
    } else {
        multipleName = WebSecurityUtils.sanitizeString(multipleName);
    }

    //get the count    
    long count = WebSecurityUtils.safeParseLong(countString);
    
    //get the limit, use the default if not set in the request
    int limit    = (limitString != null) ? WebSecurityUtils.safeParseInt(limitString) : DEFAULT_LIMIT;
    if (limit < 1) {
    	limit = DEFAULT_LIMIT;
    }

    // get the multiple, use the default if not set in the request
    int multiple = (multipleString != null) ? Math.max(DEFAULT_MULTIPLE, WebSecurityUtils.safeParseInt(multipleString)) : DEFAULT_MULTIPLE;

    //format the base url to accept limit and multiple parameters
    if( baseUrl.indexOf("?") < 0 ) {
        //does not contain a "?", so append one
        baseUrl = baseUrl + "?";
    }    

    //calculate the start and end numbers of the results that we are showing
    long startResult = (multiple==0) ? 1 : multiple*limit;
    long endResult = (multiple+1)*limit;    
    endResult = (endResult < count) ? endResult : count;

    //this is the total number of pages, each showing <limit> number of results,
    //that it would take to display the entire result set with <count> results
    int highestPossibleIndex = (int)Math.ceil(count/(float)limit)-1; 
        
    //calculate the start and end number of the page indices that we are showing
    int startIndex = multiple-LOWER_OFFSET; 
    startIndex = (startIndex < 0) ? 0 : startIndex;
    
    int endIndex = multiple+UPPER_OFFSET;
    endIndex = (endIndex > highestPossibleIndex) ? highestPossibleIndex : endIndex;    
%>
<div class="my-2">
 <% if (limit > 0 ) { %> 
  <strong>Results <%=startResult%>-<%=endResult%> of <%=count%>,</strong>
 <% } else { %>
  <strong>All Results,</strong>
 <% } %>

 <jsp:include page="/includes/listSize.jsp" flush="false">
  <jsp:param name="limitSize" value="<%=limit%>" />
 </jsp:include>
</div>
<% if( count > limit ) { %>
  <nav>
    <ul class="pagination pagination-sm">
    <li class="page-item <%=multiple > 0 ? "" : "disabled"%>"><a class="page-link" href="<%=baseUrl%>&amp;<%=multipleName%>=0&amp;<%=limitName%>=<%=limit%>">First</a></li>
    <li class="page-item <%=multiple > 0 ? "" : "disabled"%>"><a class="page-link" href="<%=baseUrl%>&amp;<%=multipleName%>=<%=multiple-1%>&amp;<%=limitName%>=<%=limit%>">Previous</a></li>

    <% for( int i=startIndex; i <= endIndex; i++ ) { %>
      <% if( multiple == i ) { %>
         <li class="page-item active"><a class="page-link"><%=i+1%> <span class="sr-only">(current)</span></a></li>
      <% } else { %>
        <li class="page-item"><a class="page-link" href="<%=baseUrl%>&amp;<%=multipleName%>=<%=i%>&amp;<%=limitName%>=<%=limit%>"><%=i+1%></a></li>
      <% } %>
    <% } %>
      <li class="page-item <%=multiple < highestPossibleIndex ? "" : "disabled"%>"><a class="page-link" href="<%=baseUrl%>&amp;<%=multipleName%>=<%=multiple+1%>&amp;<%=limitName%>=<%=limit%>">Next</a></li>
      <li class="page-item <%=multiple < highestPossibleIndex ? "" : "disabled"%>"><a class="page-link" href="<%=baseUrl%>&amp;<%=multipleName%>=<%=highestPossibleIndex%>&amp;<%=limitName%>=<%=limit%>">Last</a></li>
    </ul>
  </nav>
<% } %>
