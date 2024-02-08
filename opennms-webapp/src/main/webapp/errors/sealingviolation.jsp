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
        import="org.opennms.web.utils.ExceptionUtils"
%>

<%
    SecurityException e = ExceptionUtils.getRootCause(exception, SecurityException.class);
    if( !e.getMessage().equals( "sealing violation" )) {
        throw new ServletException( "security exception", e );
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Incorrect Jar Files")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>Incorrect Jar Files</h1>

<p>
  Some of the Java Archive files (jar files) in the Tomcat install
  are out of date.  Please replace them by going to this      
  <a href="http://faq.opennms.org/fom-serve/cache/55.html">OpenNMS FAQ
  entry</a> and following the directions there.  Otherwise, your OpenNMS
  Web system will not work correctly, and you will get undefined results.
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
