<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
        session="true"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/opennms-trendline.css" />

<%
    String box1 = System.getProperty("org.opennms.trendbox.box1", "nodes");
    String box2 = System.getProperty("org.opennms.trendbox.box2", "severity");
    String box3 = System.getProperty("org.opennms.trendbox.box3", "outages-total");
    String box4 = System.getProperty("org.opennms.trendbox.box4", "bsm-total");
%>
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Trend</h3>
    </div>
    <div style="display:flex;">
       <div class="alert-box panel-body" style="width:50%;">
           <jsp:include page="/trend/trend.htm" flush="false">
               <jsp:param name="name" value="<%= box1 %>"/>
           </jsp:include>
       </div>
       <div class="alert-box panel-body" style="width:50%;">
          <jsp:include page="/trend/trend.htm" flush="false">
               <jsp:param name="name" value="<%= box2 %>"/>
           </jsp:include>
       </div>
    </div>
    <div style="display:flex;">
       <div class="alert-box panel-body" style="width:50%;">
          <jsp:include page="/trend/trend.htm" flush="false">
               <jsp:param name="name" value="<%= box3 %>"/>
           </jsp:include>
       </div>
       <div class="alert-box panel-body" style="width:50%;">
          <jsp:include page="/trend/trend.htm" flush="false">
             <jsp:param name="name" value="<%= box4 %>"/>
          </jsp:include>
       </div>
    </div>
</div>
