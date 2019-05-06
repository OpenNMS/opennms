<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

    final String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
    final String interfaceBreadCrumb = "<a href='element/interface.jsp?node=" + nodeId  + "&intf=" + ipAddr + "'>Interface</a>";
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Metadata" />
    <jsp:param name="headTitle" value="<%= ipAddr %>" />
    <jsp:param name="headTitle" value="Meta-Data" />
    <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
    <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
    <jsp:param name="breadcrumb" value="<%= interfaceBreadCrumb %>" />
    <jsp:param name="breadcrumb" value="Meta-Data" />
</jsp:include>

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
                            <th width="30%"><%= WebSecurityUtils.sanitizeString(entry2.getKey()) %></th>
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
