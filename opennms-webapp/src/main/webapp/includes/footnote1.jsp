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
<%-- 
  This page is included by certain other JSPs to create a footnote
  for resource types that are not interfaceSnmp, but get processed
  by code that normally deals with interfaceSnmp resources.
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
  <c:when test="${param.quiet == 'true'}">
    <!-- Not displaying footnote1 -->
  </c:when>

  <c:otherwise>
    <!-- Footnote1 -->
    <div class="row">
      <div class="col-md-12">
        <p><strong>(*) Denotes an interface that no longer exists in the database, or a resource that is not an interface.</strong></p>
      </div>
    </div>
  </c:otherwise>
</c:choose>
