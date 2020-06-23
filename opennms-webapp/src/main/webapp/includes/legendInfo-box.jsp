<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Additional Legend Information" />
  <jsp:param name="quiet" value="true" />
</jsp:include>

<p>
Each status cell is an intersection of a Location and Application
</p>
<p>
An Application is defined by a subset of the set of IP based services created in OpenNMS
</p>
<p>
A Location is an arbitrary entity defined through configuration by the OpenNMS user
</p>
<p>
Each Location presents Availability as the best percentage possible based on the history of status<br/>
of services monitored from <b>all</b> remote pollers in that Location since midnight of the current day.<br/>
If there were 2 services being monitored by 2 remote pollers and each 1 service down, uniquely, then<br/>
 the availability would still be 100%.
</p>
<p>
Each Location presents Status as the worst known status of all remote pollers in a Started state.
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
