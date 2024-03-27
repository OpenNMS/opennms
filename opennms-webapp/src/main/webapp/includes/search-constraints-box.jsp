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
<%@page language="java"	contentType="text/html"	session="true" %>

<%@page import="org.opennms.web.api.Util"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@page import="org.opennms.web.servlet.XssRequestWrapper"%>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.outage.OutageQueryParms"%>
<%@page import="org.opennms.web.outage.OutageType"%>
<%@page import="org.opennms.web.outage.OutageUtil"%>

<%
	XssRequestWrapper req = new XssRequestWrapper(request);

    //required attribute parms
    OutageQueryParms parms = (OutageQueryParms)req.getAttribute( "parms" );

    if( parms == null ) {
        throw new ServletException( "Missing the outage parms request attribute." );
    }

    int length = parms.filters.size();
%>

<!-- acknowledged/outstanding row -->

<form action="outage/list.htm" method="get" name="outage_search_constraints_box_outtype_form">
  <%=Util.makeHiddenTags(req, new String[] {"outtype"})%>
  <input type="hidden" name="outtype"/>
</form>

<div class="btn-group">
  <button 
    type="button" 
    class="btn btn-secondary <%=(parms.outageType == OutageType.CURRENT) ? "active" : ""%>"
    onclick="document.outage_search_constraints_box_outtype_form.outtype.value = '<%=OutageType.CURRENT.getShortName() %>'; document.outage_search_constraints_box_outtype_form.submit();"
  >
    Current
  </button>
  <button 
    type="button" 
    class="btn btn-secondary <%=(parms.outageType == OutageType.RESOLVED) ? "active" : ""%>"
    onclick="document.outage_search_constraints_box_outtype_form.outtype.value = '<%=OutageType.RESOLVED.getShortName() %>'; document.outage_search_constraints_box_outtype_form.submit();"
  >
    Resolved
  </button>
  <button 
    type="button" 
    class="btn btn-secondary <%=(parms.outageType == OutageType.BOTH) ? "active" : ""%>"
    onclick="document.outage_search_constraints_box_outtype_form.outtype.value = '<%=OutageType.BOTH.getShortName() %>'; document.outage_search_constraints_box_outtype_form.submit();"
  >
    Both Current &amp; Resolved
  </button>
</div>

<br/>

<% if( length > 0 ) { %>
  <br/>
  <strong>Search constraints: 
      <% for(int i=0; i < length; i++) { %>
        <% Filter filter = (Filter)parms.filters.get(i); %> 
        &nbsp; <span class="label label-default"><%=filter.getTextDescriptionAsSanitizedHtml()%></span> <a href="<%=OutageUtil.makeLink(req, parms, filter, false)%>"> <i class="fa fa-minus-square-o"></i></a>
      <% } %>
  </strong>
  <br/>
<% } %>
