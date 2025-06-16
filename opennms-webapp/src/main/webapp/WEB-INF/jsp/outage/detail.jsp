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
	import="java.util.*,
		org.opennms.web.outage.*,
		java.text.DateFormat,
		org.opennms.core.utils.WebSecurityUtils
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>

<%!
    public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
%>

<%
	Outage outage = (Outage)request.getAttribute("outage");

    if( outage == null ) {
        throw new org.opennms.web.outage.OutageIdNotFoundException( "An outage with this ID was not found.", WebSecurityUtils.sanitizeString((String)request.getAttribute("id")) );
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Detail")
          .headTitle("Outages")
          .breadcrumb("Outages", "outage/list")
          .breadcrumb("Outage " + outage.getId())
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Outage <%=outage.getId()%></span>
  </div>

  <table class="table table-sm severity">
        <tr class="d-flex">
          <th class="col-2">Node</th>
          <td class="col-2">
            <% if( outage.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=outage.getNodeId()%>"><%=WebSecurityUtils.sanitizeString(outage.getNodeLabel())%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <th class="col-2">Lost&nbsp;Service&nbsp;Time</th>
          <td class="col-2"><onms:datetime date="<%=outage.getLostServiceTime()%>" /></td>
          
          <th class="col-2">Lost&nbsp;Service&nbsp;Event</th>
          <td class="col-2"><a href="event/detail.jsp?id=<%=outage.getLostServiceEventId()%>"><%=outage.getLostServiceEventId()%></a></td>
          
        </tr>
        <tr class="d-flex">
          <th class="col-2">Interface</th>
          <td class="col-2">
            <% if( outage.getIpAddress() != null ) { %>
              <% if( outage.getNodeId() > 0 ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(outage.getNodeId())%>"/>
                  <c:param name="intf" value="<%=outage.getIpAddress()%>"/>
                </c:url>
                <a href="${interfaceLink}"><%=outage.getIpAddress()%></a>
              <% } else { %>
                <%=outage.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <th class="col-2">Regained&nbsp;Service&nbsp;Time</th>
          <td class="col-2">
            <% Date regainTime = outage.getRegainedServiceTime(); %>
            
            <% if(regainTime != null) { %>
              <onms:datetime date="<%=regainTime%>" />
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>

          <th class="col-2">Regained&nbsp;Service&nbsp;Event</th>
          <td class="col-2">
            <% Integer regainedEventId = outage.getRegainedServiceEventId(); %>
            <% if(regainedEventId != null && regainedEventId > 0) { %>
              <a href="event/detail.jsp?id=<%=regainedEventId%>">
                <%=regainedEventId%>
              </a>
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>
        </tr>
        <tr class="d-flex">
          <th class="col-2">Service</th>
          <td class="col-2">
            <% if( outage.getServiceName() != null ) { %>
              <% if( outage.getIpAddress() != null && outage.getNodeId() > 0 ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(outage.getNodeId())%>"/>
                  <c:param name="intf" value="<%=outage.getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(outage.getServiceId())%>"/>
                </c:url>
                <a href="${serviceLink}"><c:out value="<%=outage.getServiceName()%>"/></a>
              <% } else { %>
                <c:out value="<%=outage.getServiceName()%>"/>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <th class="col-2">Event Source Location</th>
          <td class="col-2">
            <% if( outage.getEventLocation() != null ) { %>
            <%=outage.getEventLocation()%>
            <% } else { %>
            &nbsp;
            <% } %>
          </td>
          <th class="col-2">Node Location</th>
          <td class="col-2">
            <% if( outage.getLocation() != null ) { %>
            <%=outage.getLocation()%>
            <% } else { %>
              &nbsp;&nbsp;
            <% } %>
          </td>
        </tr>
        <tr class="d-flex">
          <th class="col-2">Polling Perspective</th>
          <td class="col-2"><%= OutageUtil.getPerspectiveLabel(outage.getPerspectiveLocation()) %></td>
          <th class="col-2">&nbsp;</th>
          <td class="col-2">&nbsp;</td>
          <th class="col-2">&nbsp;</th>
          <td class="col-2">&nbsp;</td>
        </tr>
      </table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
