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
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.web.servlet.MissingParameterException
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    String nodeIdString = request.getParameter( "node" );
    String ipAddr = request.getParameter( "intf" );
    String serviceIdString = request.getParameter( "service" );

    if( nodeIdString == null ) {
        throw new MissingParameterException( "node", new String[] { "node", "intf", "service" } );
    }

    if( ipAddr == null ) {
        throw new MissingParameterException( "intf", new String[] { "node", "intf", "service" } );
    }

    if( serviceIdString == null ) {
        throw new MissingParameterException( "service", new String[] { "node", "intf", "service" } );
    }

    int nodeId = -1;
    int serviceId = -1;

    try {
        nodeId = WebSecurityUtils.safeParseInt( nodeIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type for node ID, should be integer", e );
    }

    try {
        serviceId = WebSecurityUtils.safeParseInt( serviceIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type for service ID, should be integer", e );
    }

    String serviceName = NetworkElementFactory.getInstance(getServletContext()).getServiceNameFromId( serviceId );

    String headTitle = serviceName + " Service on " + ipAddr;
%>

<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>
<c:url var="interfaceLink" value="element/interface.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
  <c:param name="intf" value="<%=ipAddr%>"/>
</c:url>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="headTitle" value="<%= headTitle %>" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='${nodeLink}'>Node</a>" />
  <jsp:param name="breadcrumb" value="<a href='${interfaceLink}'>Interface</a>" />
  <jsp:param name="breadcrumb" value="Service Deleted" />
</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Finished Deleting <c:out value="<%=serviceName%>"/> Service on <c:out value="<%=ipAddr%>"/></h3>
  </div>
  <div class="panel-body">
    <p>
      OpenNMS should not need to be restarted, but it may take a moment for
      the Categories to be updated.
    </p>
  </div>
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
