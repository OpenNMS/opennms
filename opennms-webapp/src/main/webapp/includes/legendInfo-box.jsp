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
          .flags("quiet")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
