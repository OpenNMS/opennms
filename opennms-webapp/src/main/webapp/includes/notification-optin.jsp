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
<%--
    Opt-in control for browser notifications. Status text and button visibility are filled in
    by the notifications asset that bootstrap-footer.jsp loads.

    It expects that a <base> tag has been set in the including page.
--%>

<%@page language="java" contentType="text/html" session="true" %>

<div class="card">
  <div class="card-header">
    <span>Browser Notifications</span>
  </div>
  <div class="card-body">
    <p class="mb-2">
      Notices sent to you through a destination path that uses the <code>browser</code> command can also be shown
      as desktop notifications. This is granted per browser rather than per account, so it needs to be enabled
      in each browser you sign in from.
    </p>
    <div id="onms-notification-optin" class="d-none">
      <p class="onms-notification-optin-status mb-2"></p>
      <button type="button" class="onms-notification-optin-button btn btn-secondary d-none">
        Enable browser notifications
      </button>
    </div>
  </div> <!-- card-body -->
</div> <!-- panel -->
