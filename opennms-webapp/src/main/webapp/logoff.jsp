<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Log out" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<div class="row">
  <div class="col-md-4">
    <h2>You have been logged out.</h2>
    <p>You may <a href="login.jsp"><strong>log in</strong></a> again.</p>
  </div>
</div>

<hr />

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
