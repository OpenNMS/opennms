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
    String itemCountString = request.getParameter("itemCount");
    if( itemCountString == null ) {
        throw new MissingParameterException("itemCount", new String[] {"itemCount", "baseurl"});
    }
    
    //required parameter baseurl    
    String baseUrl = request.getParameter("baseurl");
    if( baseUrl == null ) {
        throw new MissingParameterException("baseurl", new String[] {"itemCount", "baseurl"});
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
    long itemCount = WebSecurityUtils.safeParseLong(itemCountString);
    
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
    
    if ( baseUrl.indexOf(limitName) < 0) {
        baseUrl = baseUrl + "&amp;" + limitName + "=" + limit;
    }

    //calculate the start and end numbers of the results that we are showing
    long startResult = (multiple==0) ? 1 : multiple*limit+1;
    long endResult = startResult + itemCount - 1;

%>

 <% if (limit > 0 ) { %> 
  <div class="text-center my-2">
  <strong>Results <%=startResult%>-<%=endResult%>,</strong>
  <jsp:include page="/includes/listSize.jsp" flush="false">
    <jsp:param name="limitSize" value="<%=limit%>" />
  </jsp:include>
  </div>
 <% } else { %>
  <div class="text-center">
  <strong>All Results,</strong>
  <jsp:include page="/includes/listSize.jsp" flush="false">
    <jsp:param name="limitSize" value="<%=limit%>" />
  </jsp:include>
  </div>
 <% } %>

  <% if( itemCount >= limit || multiple > 0 ) { %>
  <nav class="btn-toolbar" role="toolbar">
      <div class="form-group ml-auto mr-auto">
        <a class="btn btn-sm btn-secondary <%=multiple > 0 ? "" : "disabled"%>" role="button" href="<%=baseUrl%>&amp;<%=multipleName%>=0">First</a></a>
        <a class="btn btn-sm btn-secondary <%=multiple > 0 ? "" : "disabled"%>" role="button" href="<%=baseUrl%>&amp;<%=multipleName%>=<%=multiple-1%>">Previous</a></a>
        <a class="btn btn-sm btn-secondary <%=itemCount >= limit ? "" : "disabled"%>" role="button" href="<%=baseUrl%>&amp;<%=multipleName%>=<%=multiple+1%>">Next</a></a>
      </div>
  </nav>
  <% } else { %>
   <br/>
  <% } %>
