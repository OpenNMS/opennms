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
          .headTitle("Notifications")
          .breadcrumb("Notifications")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Notification queries</span>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-4 col-xs-6">
            <ul class="list-unstyled">
              <li><a href="notification/browse?acktype=unack&filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">Your outstanding notices</a></li>
              <li><a href="notification/browse?acktype=unack">All outstanding notices</a></li>
              <li><a href="notification/browse?acktype=ack">All acknowledged notices</a></li>
            </ul>
          </div>

          <div class="col-md-8 col-xs-6">
           <%-- search by user --%>
           <div class="row">
             <div class="col-md-12">
               <form role="form" class="form pull-right" method="post" action="notification/browse">
                 <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                 <div class="form-group">
                   <label for="byuser_user">User</label>
                   <div class="input-group">
                    <input type="text" class="form-control" id="byuser_user" name="user"/>
                    <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary" id="btn_search_by_user"><i class="fa fa-search"></i></button>
                    </div>
                   </div>
                 </div>
               </form>
             </div> <!-- column -->
           </div> <!-- row -->

           <%-- search by notice --%>
           <div class="row top-buffer">
             <div class="col-md-12">
               <form role="form" class="form pull-right" method="get" action="notification/detail.jsp" >
                 <div class="form-group">
                   <label for="bynotice_notice">Notice</label>
                   <div class="input-group">
                    <input type="text" class="form-control" id="bynotice_notice" name="notice"/>
                    <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary" id="btn_search_by_notice"><i class="fa fa-search"></i></button>
                    </div>
                   </div>
                 </div>
               </form>
             </div> <!-- column -->
           </div> <!-- row -->
          </div> <!-- column -->
       </div> <!-- row -->
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="row">
      <div class="col-md-12">
        <div class="card">
          <div class="card-header">
            <span>Outstanding and Acknowledged Notices</span>
          </div>
          <div class="card-body">
            <p>When important events are detected by OpenNMS, users may 
              receive a <em>notice</em>, a descriptive message sent automatically
              to a pager, an email address, or both. In order to
              receive notices, the user must have their notification information 
              configured in their user profile (see your Administrator for assistance), 
              notices must be <em>on</em> (see the upper right corner of this window), 
              and an important event must be received.
            </p>

            <p>From this panel, you may: <strong>Check your outstanding notices</strong>, 
              which displays all unacknowledged notices sent to your user ID;
              <strong>View all outstanding notices</strong>, which displays all unacknowledged 
              notices for all users; or <strong>View all acknowledged notices</strong>, 
              which provides a summary of all notices sent and acknowledged for all users.
            </p>

            <p>You may also search for notices associated with a specific user ID 
              by entering that user ID in the <strong>Check notices for user</strong>
              text box. And finally, you can jump immediately to a page with details
              specific to a given notice identifier by entering that numeric 
              identifier in the <strong>Get notice detail</strong> text box. 
              Note that this is particularly useful if you are using a numeric 
              paging service and receive the numeric notice identifier as part of the page.
            </p>
          </div> <!-- card-body -->
        </div> <!-- panel -->
      </div> <!-- column -->
    </div> <!-- row -->

    <div class="row">
      <div class="col-md-12">
        <div class="card">
          <div class="card-header">
            <span>Notification Escalation</span>
          </div>
          <div class="card-body">
            <p>Once a notice is sent, it is considered <em>outstanding</em> until 
                someone <em>acknowledge</em>s receipt of the notice via the OpenNMS
                Notification interface.&nbsp; If the event that 
                triggered the notice was related to managed network devices or systems, 
                the <strong>Network/Systems</strong> group will be notified, one by one, with a
                notice sent to the next member on the list only after 15 minutes has 
                elapsed since the last message was sent. This progression through the
                list, or <em>escalation</em>, can be stopped at any time by acknowledging the
                notice.  Note that this is <strong>not</strong> the same as acknowledging 
                the event which triggered the notice. If all members of the group 
                have been notified and the notice has not been acknowledged, the 
                notice will be escalated to the <strong>Management</strong> group, 
                where all members of that group will be notified at once with no 
                15 minute escalation interval.
            </p>
          </div> <!-- card-body -->
        </div> <!-- panel -->
      </div> <!-- column -->
    </div> <!-- row -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
