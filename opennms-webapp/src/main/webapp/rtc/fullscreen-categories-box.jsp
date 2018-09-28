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
        import="org.opennms.web.api.Util"
        %>

<%
    final String baseHref = Util.calculateUrlBase(request);
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Style-Type" content="text/css"/>
    <meta http-equiv="Content-Script-Type" content="text/javascript"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="manifest" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="bootstrap" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="opennms-theme" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="font-awesome" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="vendor" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="global" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="jquery-js" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="bootstrap" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="opennms-theme" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>

    <title>RTC Console</title>
</head>
<body>
    <jsp:include page="/includes/categories-box.jsp" flush="false"/>
</body>
</html>
