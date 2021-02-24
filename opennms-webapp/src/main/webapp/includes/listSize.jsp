<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
<% int limitSize = Integer.parseInt(request.getParameter("limitSize")); %>

<script type="text/javascript">
function setParameter(params, name, value) {
    var matcher = new RegExp('^' + name + '=(\\d*)$');

    var matched = false;
    for (var i=0; i < params.length; i++) {
        if (params[i].match(matcher)) {
            params[i] = name + '=' + value;
            matched = true;
            break;
        }
    }

    if (!matched) {
        params.push(name + '=' + value);
    }
}

function UpdateLimitSize(value) {
    if (value == <%= limitSize %>) {
        console.debug('limit unchanged:', value);
        return;
    }

    var params = [];
    var url = window.location.href;
    var parts = url.split('?');
    var query = parts[1];
    if (query) {
        params = query.split('&');
    }

    setParameter(params, 'limit', value);
    setParameter(params, 'multiple', 0);

    window.location.href = parts[0] + '?' + params.join('&');
}
</script>
<strong>
Show:
<select class="limit1"  id="limitSize" onchange="UpdateLimitSize(this.value)">
<%
    if (limitSize == 0) {
        limitSize = 20;
    }
    final int[] options = { 10, 20, 50, 100, 250, 500, 1000 };
    for (final int option : options) {
%>
  <option value="<%= option %>" <% if (limitSize == option) { %>selected<% } %>><%= option %></option>
<%  } %>
</select> Items
</strong>

