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
<%@page language="java" contentType="text/html" session="true"
    import="org.opennms.core.utils.WebSecurityUtils,
            org.opennms.web.utils.Bootstrap"
%>

<%
    String userID = request.getParameter("userID");
    if (userID == null || userID.trim().isEmpty()) {
        throw new ServletException("userID parameter required");
    }
    String htmlUserID = WebSecurityUtils.sanitizeString(userID);
    // For JavaScript context: escape backslash, quotes, and control chars
    String jsUserID = userID.replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("<", "\\x3c")
                            .replace(">", "\\x3e");
%>

<% Bootstrap.with(pageContext)
          .headTitle("API Tokens for " + htmlUserID)
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users", "admin/userGroupView/users/list.jsp")
          .breadcrumb("User Detail", "admin/userGroupView/users/userDetail.jsp?userID=" + java.net.URLEncoder.encode(userID, "UTF-8"))
          .breadcrumb("API Tokens")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-8">
    <div class="card">
      <div class="card-header">
        <span>API Tokens for user: <%= htmlUserID %></span>
      </div>
      <div class="card-body">
        <div id="token-created-alert" class="alert alert-success" style="display:none;">
          <strong>Token created!</strong> Copy this token now. It will not be shown again.
          <div class="input-group mt-2">
            <input type="text" id="new-token-value" class="form-control" readonly>
            <button class="btn btn-outline-secondary" type="button" onclick="copyToken()">Copy</button>
            <button class="btn btn-outline-danger" type="button" onclick="dismissToken()">Dismiss</button>
          </div>
        </div>

        <div id="token-error-alert" class="alert alert-danger" style="display:none;"></div>

        <table class="table table-sm table-striped" id="tokens-table">
          <thead>
            <tr>
              <th>Description</th>
              <th>Created</th>
              <th>Expires</th>
              <th>Last Used</th>
              <th></th>
            </tr>
          </thead>
          <tbody id="tokens-tbody">
          </tbody>
        </table>
        <p id="no-tokens-msg" style="display:none;">No API tokens found.</p>

        <hr>
        <h5>Generate New Token</h5>
        <form id="create-token-form" onsubmit="return createToken();">
          <div class="form-group mb-2">
            <label for="token-description">Description</label>
            <input type="text" class="form-control" id="token-description" placeholder="e.g., grafana integration" maxlength="256">
          </div>
          <div class="form-group mb-2">
            <label for="token-expiry">Expires in</label>
            <select class="form-control" id="token-expiry">
              <option value="30">30 days</option>
              <option value="90">90 days</option>
              <option value="180">180 days</option>
              <option value="365" selected>1 year</option>
            </select>
          </div>
          <button type="submit" class="btn btn-primary">Generate Token</button>
          <button type="button" class="btn btn-danger float-right" onclick="revokeAll()">Revoke All Tokens</button>
        </form>
      </div>
    </div>
  </div>
</div>

<script type="text/javascript">
var targetUser = '<%= jsUserID %>';
var restUrl = '<%= request.getContextPath() %>/api/v2/apiTokens';

function loadTokens() {
    fetch(restUrl + '?username=' + encodeURIComponent(targetUser), {credentials: 'same-origin'})
        .then(function(r) { return r.json(); })
        .then(function(tokens) {
            var tbody = document.getElementById('tokens-tbody');
            tbody.innerHTML = '';
            if (tokens.length === 0) {
                document.getElementById('no-tokens-msg').style.display = '';
                document.getElementById('tokens-table').style.display = 'none';
            } else {
                document.getElementById('no-tokens-msg').style.display = 'none';
                document.getElementById('tokens-table').style.display = '';
                tokens.forEach(function(t) {
                    var row = '<tr>' +
                        '<td>' + escapeHtml(t.description || '') + '</td>' +
                        '<td>' + formatDate(t.createdAt) + '</td>' +
                        '<td>' + formatDate(t.expiresAt) + '</td>' +
                        '<td>' + (t.lastUsedAt ? formatDate(t.lastUsedAt) : 'never') + '</td>' +
                        '<td><button class="btn btn-sm btn-danger" onclick="revokeToken(' + t.id + ')">Revoke</button></td>' +
                        '</tr>';
                    tbody.innerHTML += row;
                });
            }
        });
}

function createToken() {
    var desc = document.getElementById('token-description').value;
    var days = parseInt(document.getElementById('token-expiry').value);
    fetch(restUrl + '?username=' + encodeURIComponent(targetUser), {
        method: 'POST',
        credentials: 'same-origin',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({description: desc, expiresInDays: days})
    }).then(function(r) {
        if (!r.ok) return r.text().then(function(t) { throw new Error(t); });
        return r.json();
    }).then(function(data) {
        document.getElementById('new-token-value').value = data.token;
        document.getElementById('token-created-alert').style.display = '';
        document.getElementById('token-error-alert').style.display = 'none';
        document.getElementById('token-description').value = '';
        loadTokens();
    }).catch(function(e) {
        document.getElementById('token-error-alert').textContent = e.message;
        document.getElementById('token-error-alert').style.display = '';
    });
    return false;
}

function revokeToken(id) {
    if (!confirm('Revoke this token?')) return;
    fetch(restUrl + '/' + id, {method: 'DELETE', credentials: 'same-origin'})
        .then(function() { loadTokens(); });
}

function revokeAll() {
    if (!confirm('Revoke ALL tokens for ' + targetUser + '? This cannot be undone.')) return;
    fetch(restUrl + '?username=' + encodeURIComponent(targetUser),
        {method: 'DELETE', credentials: 'same-origin'})
        .then(function() { loadTokens(); });
}

function copyToken() {
    var input = document.getElementById('new-token-value');
    input.select();
    document.execCommand('copy');
}

function dismissToken() {
    document.getElementById('new-token-value').value = '';
    document.getElementById('token-created-alert').style.display = 'none';
}

function formatDate(d) {
    if (!d) return '';
    return new Date(d).toLocaleString();
}

function escapeHtml(s) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(s));
    return div.innerHTML;
}

loadTokens();
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
