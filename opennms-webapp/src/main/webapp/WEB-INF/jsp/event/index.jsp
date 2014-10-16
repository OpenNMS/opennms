<%@ page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@ page import="org.opennms.web.tags.filters.EventFilterCallback" %>
<%@ page import="org.opennms.web.tags.filters.FilterCallback" %>
<%@ page import="org.opennms.netmgt.model.OnmsFilterFavorite" %>
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
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Events" />
  <jsp:param name="headTitle" value="Events" />
  <jsp:param name="location" value="event" />  
  <jsp:param name="breadcrumb" value="Events" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/tooltip.js'></script>" />
</jsp:include>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="../../taglib.tld" prefix="onms" %>

  <div class="TwoColLeft">
      <h3>Event Queries</h3>
      <div class="boxWrapper">
        <%--<jsp:include page="/includes/event-querypanel.jsp" flush="false" />--%>
        <form action="event/detail.jsp" method="get">
            <p align="right">Event ID:
                <input type="text" name="id" />
                <input type="submit" value="Get details"/></p>
        </form>

        <ul class="plain">
            <li><a href="event/list" title="View all outstanding events">All events</a></li>
            <li><a href="event/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
        </ul>
    </div>
    <br/>
    <h3>Event Filter Favorites</h3>
    <onms:alert/>
    <div class="boxWrapper">
        <c:choose>
            <c:when test="${!empty favorites}">
                <!-- Filters -->
                <ul class="plain">
                    <c:forEach var="eachFavorite" items="${favorites}">
                        <li><img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('${eachFavorite.id}')" onMouseOut="hideTT()"/>
                            <a href="event/list?favoriteId=${eachFavorite.id}&${eachFavorite.filter}" title='show events for this favorite'>${eachFavorite.name}</a> [ <a href="event/deleteFavorite?favoriteId=${eachFavorite.id}&redirect=/event/index" title='delete favorite'>X</a> ]
                        </li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <p>No favorites available.</p>
            </c:otherwise>
        </c:choose>
      </div>
  </div>

<!-- Tooltips for filters -->
<c:forEach var="eachFavorite" items="${favorites}">
    <%
        OnmsFilterFavorite current = (OnmsFilterFavorite) pageContext.getAttribute("eachFavorite");
        FilterCallback callback = (EventFilterCallback) request.getAttribute("callback");

        NormalizedQueryParameters params = new NormalizedQueryParameters();
        params.setFilters(callback.parse(current.getFilter()));

        pageContext.setAttribute("parms", params);
    %>
    <div class="tooltip" style="" id="${eachFavorite.id}">
        <p><b>Filter: </b><br/>
            <onms:filters
                    context="/event/index"
                    favorite="${eachFavorite}"
                    parameters="${parms}"
                    showRemoveLink="false"
                    showAcknowledgeFilter="false"
                    callback="${callback}" />
        </p>
    </div>
</c:forEach>

  <div class="TwoColRight">
      <h3>Outstanding and acknowledged events</h3>
		<div class="boxWrapper">
      <p>Events can be <em>acknowledged</em>, or removed from the view of other users, by
        selecting the event in the <em>Ack</em> check box and clicking the <em>Acknowledge
        Selected Events</em> at the bottom of the page.  Acknowledging an event gives
        users the ability to take personal responsibility for addressing a network
        or systems-related issue.  Any event that has not been acknowledged is
        active in all users' browsers and is considered <em>outstanding</em>.
      </p>
            
      <p>If an event has been acknowledged in error, you can select the 
        <em>View all acknowledged events</em> link, find the event, and <em>unacknowledge</em> it,
        making it available again to all users' views.
      </p>
        
      <p>If you have a specific event identifier for which you want a detailed event
        description, type the identifier into the <em>Get details for Event ID</em> box and
        hit <b>[Enter]</b>.  You will then go to the appropriate details page.
      </p>
		</div>
  </div>
	<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
