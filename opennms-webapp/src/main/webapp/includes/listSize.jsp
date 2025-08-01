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

