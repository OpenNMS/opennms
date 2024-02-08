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
        import="
            java.util.*,
            org.opennms.web.api.Authentication,
            org.opennms.web.element.*,
            org.opennms.netmgt.model.OnmsMetaData,
            org.opennms.core.utils.WebSecurityUtils,
            org.opennms.netmgt.model.OnmsIpInterface"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    final int nodeId = WebSecurityUtils.safeParseInt(request.getParameter("node"));
    final String ipAddr = request.getParameter("ipAddr");

    final OnmsIpInterface entity = NetworkElementFactory
            .getInstance(getServletContext())
            .getNode(nodeId).getIpInterfaceByIpAddress(ipAddr);
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle(ipAddr)
          .headTitle("Meta-Data")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "element/node.jsp?node=" + nodeId)
          .breadcrumb("Interface", "element/interface.jsp?node=" + nodeId  + "&intf=" + ipAddr)
          .breadcrumb("Meta-Data")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h4>Meta-Data for Interface: <strong><%= ipAddr %></strong></h4>

<div class="row">
    <div class="col-md-12">
        <%
            final Map<String, Map<String, String>> metaData = new TreeMap<>();
            
            for(final OnmsMetaData onmsNodeMetaData : entity.getMetaData()) {
                metaData.putIfAbsent(onmsNodeMetaData.getContext(), new TreeMap<String, String>());
                metaData.get(onmsNodeMetaData.getContext()).put(onmsNodeMetaData.getKey(), onmsNodeMetaData.getValue());
            }

            if (metaData.size()>0) {
        %>
        <div class="card-columns mb-3">
        <%
                for(final Map.Entry<String, Map<String, String>> entry1 : metaData.entrySet()) {
        %>
            <div class="card">
                <div class="card-header">
                    Context <strong><%= WebSecurityUtils.sanitizeString(entry1.getKey()) %></strong>
                </div>
                <!-- general info box -->
                <div class="card-body p-0">
                    <table class="table table-sm table-striped mb-0">
                <%
                            for(final Map.Entry<String, String> entry2 : entry1.getValue().entrySet()) {
                                String value = entry2.getValue();

                                if (!request.isUserInRole(Authentication.ROLE_ADMIN) && entry2.getKey().matches(".*([pP]assword|[sS]ecret).*")) {
                                    value = "***";
                                }
                %>
                        <tr>
                            <th style="width:auto;-webkit-hyphens: auto; -moz-hyphens: auto; -ms-hyphens: auto;"><%= WebSecurityUtils.sanitizeString(entry2.getKey()) %></th>
                            <td><%= WebSecurityUtils.sanitizeString(value) %></td>
                        </tr>
                <%
                            }
                %>
                    </table>
                </div>
            </div> <!-- panel -->
        <%
                }
        %>
        </div> <!-- card-columns -->
        <%
            } else {
        %>
        <strong>No Meta-Data available for this interface.</strong><br/><br/>
        <%
            }
        %>
    </div> <!-- col -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
