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
	isErrorPage="true"
	import="org.opennms.web.element.*"
%>
<%@page import="org.opennms.core.utils.WebSecurityUtils" %>

<%!
    public ElementNotFoundException findElementNotFoundException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        if (throwable instanceof ElementNotFoundException) {
            return (ElementNotFoundException) throwable;
        }
        return findElementNotFoundException(throwable.getCause());
    }
%>
<%
    final ElementNotFoundException enfe = findElementNotFoundException(exception);
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Element Not Found")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1><%=enfe.getElemType(true)%>  Not Found</h1>

<p>
  The <%=enfe.getElemType()%> is invalid. <%=WebSecurityUtils.sanitizeString(enfe.getMessage())%>
  <br/>
  <% if (enfe.getDetailUri() != null) { %>
  <p>
  To search again by <%=enfe.getElemType()%> ID, enter the ID here:
  </p>
  <form role="form" method="get" action="<%=enfe.getDetailUri()%>" class="form">
    <div class="row">
      <div class="form-group col-md-2">
        <label for="input_text">Get&nbsp;details&nbsp;for&nbsp;<%=enfe.getElemType()%></label>
        <input type="text" class="form-control" id="input_text" name="<%=enfe.getDetailParam()%>"/>
      </div>
    </div>
    <button type="submit" class="btn btn-secondary">Search</button>
  </form>
  <% } %>
  
  <% if (enfe.getBrowseUri() != null) { %>
  <p>
  To find the <%=enfe.getElemType()%> you are looking for, you can
  browse the <a href="<%=enfe.getBrowseUri()%>"><%=enfe.getElemType()%> list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
