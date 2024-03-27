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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page language="java"
        contentType="text/html"
        session="true"
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Get Support")
          .breadcrumb("Support")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
    <div class="col-md-4">
        <!-- no account session found, ask for login -->
        <div class="card">
            <div class="card-header">
                <span>Commercial Support</span>
            </div>
            <div class="card-body">
                <table class="table">
                    <tr>
                        <td style="border-top: none;"><a href="https://support.opennms.com" target="_blank" class="btn btn-secondary" role="button" style="width: 100%">OpenNMS Support Portal</a></td>
                        <td style="border-top: none;">Login to the OpenNMS Support Portal to create a support ticket. Please attach a basic system report to help the support engineer who works your ticket diagnose the problem more quickly.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <div class="col-md-4">
        <jsp:include page="/includes/support-system-diagnostics.jsp" flush="false"/>
    </div>

    <div class="col-md-4">
        <jsp:include page="/includes/help-contact.jsp" flush="false"/>
    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
