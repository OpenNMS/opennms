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
	import="
	java.util.*,
	org.opennms.core.spring.BeanUtils,
	org.opennms.core.utils.WebSecurityUtils,
	org.opennms.netmgt.config.*,
	org.opennms.netmgt.config.dao.outages.api.WriteablePollOutagesDao,
	org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao,
	org.opennms.netmgt.config.collectd.Package,
	org.opennms.netmgt.config.poller.*,
	org.opennms.netmgt.config.poller.outages.*,
	org.opennms.web.element.*,
	org.opennms.netmgt.model.OnmsNode,
	org.opennms.netmgt.model.OnmsNode.NodeType,org.opennms.netmgt.events.api.EventConstants,
	org.opennms.netmgt.xml.event.Event,
	org.opennms.web.api.Util,
	java.net.*
"%>

<%!public void sendOutagesChangedEvent() throws ServletException {
		Event event = new Event();
		event.setSource("Web UI");
		event.setUei(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);
		try {
			event.setHost(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException uhE) {
			event.setHost("unresolved.host");
		}

		event.setTime(new java.util.Date());
		try {
			Util.createEventProxy().send(event);
		} catch (Throwable e) {
			throw new ServletException("Could not send event " + event.getUei(), e);
		}
	}%>
<%
	WriteableThreshdDao threshdDao = BeanUtils.getBean("thresholdingContext", "threshdDao", WriteableThreshdDao.class);
	WriteablePollOutagesDao pollOutagesDao = BeanUtils.getBean("pollerConfigContext", "pollOutagesDao",
			WriteablePollOutagesDao.class);

	NotifdConfigFactory.init(); //Must do this early on - if it fails, then just throw the exception to the web gui
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Scheduled Outages")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Scheduled Outages")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
	<div class="card-header">
		<h4 class="pull-left">Scheduled Outages</h4>
    <form role="form" class="form-inline pull-right" action="admin/sched-outages/editoutage.jsp" method="post" >
	  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <input type="hidden" name="addNew" value="true" />
		<div class="input-group">
			<input type="text" class="form-control" value="New Name" size="40" name="newName" />
			<div class="input-group-append">
				<button type="submit" class="btn btn-secondary" name="newOutage"><i class="fa fa-plus"></i></button>
			</div>
		</div>
    </form>
  </div> <!-- card-header -->
<div class="card-body">

<script type="text/javascript">
	function DeleteAction(name) {
		if (!confirm('Are you sure you wish to delete this outage?')) {
			return false;
		}

		var xhttp = new XMLHttpRequest();
		xhttp.onload = function() {
			location.reload();
		}
		xhttp.open("DELETE", "/opennms/rest/sched-outages/" + encodeURIComponent(name), true);
		xhttp.setRequestHeader("Content-type", "application/json");
		xhttp.send(null);
	}
</script>

<table id="outages" class="table table-sm table-striped">
	<tr>
		<th>Name</th>
		<th>Type</th>
		<th>Nodes/Interfaces</th>
		<th>Times</th>
		<th>Notifications</th>
		<th>Polling</th>
		<th>Thresholds</th>
		<th>Data collection</th>
		<th colspan="2"></th>
	</tr>

	<%
	    HashMap<String,String> shortDayNames = new HashMap<String,String>();
				shortDayNames.put("sunday", "Sun");
				shortDayNames.put("monday", "Mon");
				shortDayNames.put("tuesday", "Tue");
				shortDayNames.put("wednesday", "Wed");
				shortDayNames.put("thursday", "Thu");
				shortDayNames.put("friday", "Fri");
				shortDayNames.put("saturday", "Sat");

				String outageOnImageUrl = "images/greentick.gif";
				String outageOffImageUrl = "images/redcross.gif";

				pollOutagesDao.getReadLock().lock();
				try {
					List<Outage> outages = pollOutagesDao.getWriteableConfig().getOutages();
			
					Collection<String> notificationOutages = NotifdConfigFactory.getInstance().getConfiguration().getOutageCalendars();
			
					PollerConfigFactory.init(); //Force init
			
					List<String> pollingOutages = new ArrayList<>();
					for (final org.opennms.netmgt.config.poller.Package pkg : PollerConfigFactory.getInstance().getExtendedConfiguration().getPackages()) {
						pollingOutages.addAll(pkg.getOutageCalendars());
					}

					List<String> thresholdingOutages = new ArrayList<>();
					for (final org.opennms.netmgt.config.threshd.Package thisPackage : threshdDao.getWriteableConfig().getPackages()) {
						thresholdingOutages.addAll(thisPackage.getOutageCalendars());
					}
			
					List<String> collectionOutages = new ArrayList<>();
					CollectdConfigFactory collectdConfig = new CollectdConfigFactory();
					for (Package thisPackage : collectdConfig.getPackages()) {
						collectionOutages.addAll(thisPackage.getOutageCalendars());
					}
			
					for (int i = 0; i < outages.size(); i++) {
						Outage thisOutage = outages.get(i);
						String rowClass   = pollOutagesDao.isCurTimeInOutage(thisOutage) ? "severity-Critical" : "severity-Cleared";
						String outageName = thisOutage.getName();
	%>
	<tr valign="top" class="<%=rowClass%>">
		<td><%=java.net.URLEncoder.encode(outageName, "UTF-8")%></td>
		<td><%=pollOutagesDao.getOutageType(outageName)%></td>
		<td><ul class="list-unstyled">
		<%
		    List<org.opennms.netmgt.config.poller.outages.Node> nodeList = pollOutagesDao.getNodeIds(outageName);
						for (int j = 0; j < nodeList.size(); j++) {
							OnmsNode elementNode = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeList.get(j).getId());
		%> <li><%=elementNode == null || elementNode.getType() == NodeType.DELETED ? "Node: Node ID " + nodeList.get(j).getId() + " Not Found" : "Node: " + WebSecurityUtils.sanitizeString(elementNode.getLabel())%></li>
		<%
		    }
						List<org.opennms.netmgt.config.poller.outages.Interface> interfaceList = pollOutagesDao.getInterfaces(outageName);
						for (int j = 0; j < interfaceList.size(); j++) {
							StringBuffer display;
							String rawAddress = interfaceList.get(j).getAddress();
							if ("match-any".equals(rawAddress)) {
								display = new StringBuffer("All nodes/interfaces");
							} else {
								display = new StringBuffer();
								List<Integer> nodeids = NetworkElementFactory.getInstance(getServletContext()).getNodeIdsWithIpLike(rawAddress);
								//org.opennms.web.element.Interface[] interfaces = NetworkElementFactory.getInstance(getServletContext()).getInterfacesWithIpAddress(rawAddress);
								if (nodeids.size() == 0) {
									display.append("Intfc: " + rawAddress + " Not Found");
								}
								for (Integer nodeid: nodeids) {
									org.opennms.web.element.Interface thisInterface = NetworkElementFactory.getInstance(getServletContext()).getInterface(nodeid,rawAddress);
									if (thisInterface.isManagedChar()=='D') {
										display.append("Intfc: " + thisInterface.getIpAddress() + " Not Found");
									} else {
										if (thisInterface.getHostname() != null && !thisInterface.getHostname().equals(thisInterface.getIpAddress())) {
											display.append("Intfc: " + thisInterface.getIpAddress() + " " + thisInterface.getHostname());
										} else {
											display.append("Intfc: " + thisInterface.getIpAddress());
										}
										if (!thisInterface.isManaged()) {
											display.append(" (unmanaged)");
										}
									}
								}
							}
		%><li><%=display%></li>
		<%
			}
		%></ul>
		</td>
		<td><ul class="list-unstyled">
		<%
		    List<org.opennms.netmgt.config.poller.outages.Time> outageTimes = pollOutagesDao.getOutageTimes(outageName);
						for (int j = 0; j < outageTimes.size(); j++) {
							org.opennms.netmgt.config.poller.outages.Time thisOutageTime = outageTimes.get(j);
							String rawDay = thisOutageTime.getDay().orElse("");
							String day = rawDay;
							if ("daily".equals(pollOutagesDao.getOutageType(outageName)))
								day = "";
							if ("weekly".equals(pollOutagesDao.getOutageType(outageName)))
								day = (rawDay == null) ? "" : (String) shortDayNames.get(rawDay);
							if ("specific".equals(pollOutagesDao.getOutageType(outageName)))
								day = "";
		%><li><%=day%> <%=thisOutageTime.getBegins()%> - <%=thisOutageTime.getEnds()%></li>
		<%
			}
		%></ul>
		</td>
		<td align="center"><img
			src="<%=(notificationOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td align="center"><img
			src="<%=(pollingOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td align="center"><img
			src="<%=(thresholdingOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td align="center"><img
			src="<%=(collectionOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td><a id="<%=java.net.URLEncoder.encode(outageName, "UTF-8")%>.edit"
			href="admin/sched-outages/editoutage.jsp?name=<%=java.net.URLEncoder.encode(outageName, "UTF-8")%>">Edit</a></td>
		<td><a id="<%=java.net.URLEncoder.encode(outageName, "UTF-8")%>.delete"
			   href="javascript:DeleteAction('<%=outageName%>')">Delete</a></td>
	</tr>

	<%
			} //end for outages
		} finally {
			pollOutagesDao.getReadLock().unlock();
		}
	%>
</table>
	</div> <!-- card-body -->
</div> <!-- card -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true" />
