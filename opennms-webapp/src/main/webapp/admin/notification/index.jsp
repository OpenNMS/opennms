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
<%@page language="java"
	contentType="text/html"
	session="true"
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Configure Notifications</span>
      </div>
      <div class="card-body">
        <p>
          <a href="admin/notification/noticeWizard/eventNotices.htm">Configure Event Notifications</a>
        </p>
        <p>
          <a href="admin/notification/destinationPaths.jsp">Configure Destination Paths</a>
        </p>
        <p>
          <a href="admin/notification/noticeWizard/buildPathOutage.jsp?newRule=IPADDR+IPLIKE+*.*.*.*&showNodes=on">Configure Path Outages</a>
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Event Notifications</span>
      </div>
      <div class="card-body">
        <p>
          Each event can be configured to send a notification whenever that event is
          triggered. This wizard will walk you through the steps needed for
          configuring an event to send a notification.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->

    <div class="card">
      <div class="card-header">
        <span>Destination Paths</span>
      </div>
      <div class="card-body">
        <p>
          A destination path describes what users or groups will receive
          notifications, how the notifications will be sent, and who to notify
          if escalation is needed. This wizard will walk you through setting up
          a resuable list of who to contact and how to contact them, 
          which are used in the event configuration.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->

    <div class="card">
      <div class="card-header">
        <span>Path Outages</span>
      </div>
      <div class="card-body">
        <p>
          Configuring a path outage consists of selecting an IP address/service pair
          which defines the critical path to a group of nodes.  When a node down
          condition occurs for a node in the group, the critical path will be tested.
          If it fails to respond, the node down notifications will be suppressed. The
          critical path service is typically ICMP, and at this time ICMP is the only
          critical path service supported.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
