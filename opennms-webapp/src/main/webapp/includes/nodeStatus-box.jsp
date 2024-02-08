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
  This page is included by other JSPs to create a box containing an
  abbreviated status of the node.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="
          org.opennms.web.alarm.*,
          org.opennms.web.alarm.AcknowledgeType,
          org.opennms.web.alarm.filter.AlarmCriteria,
          org.opennms.web.alarm.filter.NodeFilter,
          org.opennms.web.filter.Filter,
          org.opennms.web.servlet.MissingParameterException,
          org.opennms.core.spring.BeanUtils,
          org.opennms.core.utils.WebSecurityUtils,
          org.opennms.netmgt.model.OnmsAlarm,
          org.opennms.netmgt.model.OnmsSeverity,
          org.opennms.netmgt.dao.api.AlarmRepository
        "
%>

<%
    int nodeId = -1;
    int maxSeverity = 3;
    int ackCount    = 0;
    int unackCount  = 0;
    OnmsAlarm[] alarms = new OnmsAlarm[0];

    String nodeIdStr = request.getParameter("nodeId");

    if (nodeIdStr == null) {
        throw new MissingParameterException("node");
    } else {
        nodeId = WebSecurityUtils.safeParseInt(nodeIdStr);
        NodeFilter filter = new NodeFilter(nodeId, getServletContext());
        AlarmCriteria criteria = new AlarmCriteria(new Filter[] { filter }, SortStyle.ID, AcknowledgeType.BOTH, AlarmCriteria.NO_LIMIT, AlarmCriteria.NO_OFFSET);
        AlarmRepository repository = BeanUtils.getBean("daoContext", "alarmRepository", AlarmRepository.class);
        if (repository != null) {
            alarms = repository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        }
    }

    boolean nodeDown = false;
    int intfDown = 0;
    int servDown = 0;
    for (OnmsAlarm alarm : alarms) {
        if (alarm.getSeverity().getId() <= 3) {
            continue;
        }
        if (alarm.getUei().contains("nodeDown")) {
            nodeDown = true;
        }
        if (alarm.getUei().contains("interfaceDown")) {
            intfDown++;
        }
        if (alarm.getUei().contains("nodeLostService")) {
            servDown++;
        }
        if (alarm.isAcknowledged()) {
            ackCount++;
        } else {
            unackCount++;
        }
        if (alarm.getSeverity().getId() > maxSeverity && alarm.getAckTime() == null) {
            maxSeverity = alarm.getSeverity().getId();
        }
    }
    String status  = OnmsSeverity.get(maxSeverity).getLabel();
    String message = "Node has " + (maxSeverity == 3 ? "no" : status.toLowerCase()) + " problems.";
    if (nodeDown) {
        message = "Node is currently down.";
    } else if (intfDown > 0 && servDown == 0) {
        message = "Node has " + intfDown + " " + (intfDown > 1 ? "interfaces" : "interface") + " down.";
    } else if (servDown > 0 && intfDown == 0) {
        message = "Node has " + servDown + " " + (servDown > 1 ? "services" : "service") + " down.";
    } else if (servDown > 0 && intfDown > 0) {
        message = "Node has " + intfDown + " " + (intfDown > 1 ? "interfaces" : "interface") + " and " + servDown + " " + (servDown > 1 ? "services" : "service") + " down.";
    }
    String details = "";
    if (maxSeverity > 3) {
        String unackText = "<a href='alarm/list.htm?filter=node%3d" + nodeId + "&amp;acktype=unack'><b>"
            + unackCount + "</b> unacknowledged</a>";
        String ackText = "<a href='alarm/list.htm?filter=node%3d" + nodeId + "&amp;acktype=ack'><b>"
            + ackCount + "</b> acknowledged</a>";
        details = " There are " + unackText + " problems, and " + ackText + " problems.";
    }
%>

<table class="table table-sm severity">
  <tr class="severity-<%=status%>">
    <td align="left" class="bright">
      <b><%=message%></b><%=details%>
    </td>
  </tr>
</table>

