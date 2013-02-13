<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="true"
	import="
	java.util.*,
	org.opennms.netmgt.config.*,
	org.opennms.netmgt.config.poller.*,
	org.opennms.web.element.*,
	org.opennms.netmgt.model.OnmsNode,
	org.opennms.netmgt.EventConstants,
	org.opennms.netmgt.xml.event.Event,
	org.opennms.web.api.Util,
	java.net.*,
	java.io.*,
	org.opennms.netmgt.utils.*
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

		event.setTime(EventConstants.formatToString(new java.util.Date()));
		try {
			Util.createEventProxy().send(event);
		} catch (Throwable e) {
			throw new ServletException("Could not send event " + event.getUei(), e);
		}
	}%>
<%
	NotifdConfigFactory.init(); //Must do this early on - if it fails, then just throw the exception to the web gui
	PollOutagesConfigFactory.init(); // Only init - do *not* reload
	PollOutagesConfigFactory pollFactory = PollOutagesConfigFactory.getInstance();
	String deleteName = request.getParameter("deleteOutage");
	if (deleteName != null) {
		pollFactory.getWriteLock().lock();
		try {
			pollFactory.removeOutage(deleteName);
			//Remove from all the package configurations as well
			org.opennms.netmgt.config.threshd.Package[] thresholdingPackages = ThreshdConfigFactory.getInstance().getConfiguration().getPackage();
			for (int i = 0; i < thresholdingPackages.length; i++) {
				org.opennms.netmgt.config.threshd.Package thisPackage = thresholdingPackages[i];
				thisPackage.removeOutageCalendar(deleteName); //Will quietly do nothing if outage doesn't exist
			}
	
			org.opennms.netmgt.config.poller.Package[] pollingPackages = PollerConfigFactory.getInstance().getConfiguration().getPackage();
			for (int i = 0; i < pollingPackages.length; i++) {
				org.opennms.netmgt.config.poller.Package thisPackage = pollingPackages[i];
				thisPackage.removeOutageCalendar(deleteName); //Will quietly do nothing if outage doesn't exist
			}
	
			for (Iterator iter = CollectdConfigFactory.getInstance().getCollectdConfig().getPackages().iterator(); iter.hasNext();) {
				org.opennms.netmgt.config.collectd.Package thisPackage = ((CollectdPackage) iter.next()).getPackage();
				thisPackage.removeOutageCalendar(deleteName); //Will quietly do nothing if outage doesn't exist
			}
	
			NotifdConfigFactory.getInstance().getConfiguration().removeOutageCalendar(deleteName);
	
			pollFactory.saveCurrent();
			NotifdConfigFactory.getInstance().saveCurrent();
			ThreshdConfigFactory.getInstance().saveCurrent();
			CollectdConfigFactory.getInstance().saveCurrent();
			PollerConfigFactory.getInstance().save();
			sendOutagesChangedEvent();
		} finally {
			pollFactory.getWriteLock().unlock();
		}
	}
%>


<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Manage Scheduled Outages" />
	<jsp:param name="headTitle" value="Scheduled Outages" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb"
		value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Scheduled Outages" />
</jsp:include>
<style type="text/css">
div.nodeintbox {
  white-space: nowrap;
  max-height: 196px;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>

<table id="outages" border="1" cellpadding="5">
	<tr>
		<th colspan="4">&nbsp;</th>
		<th colspan="4">Affects...</th>
		<th colspan="2"></th>
	</tr>
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

		pollFactory.getReadLock().lock();

		try {
			Outage[] outages = pollFactory.getOutages();
	
			Collection<String> notificationOutages = NotifdConfigFactory.getInstance().getConfiguration().getOutageCalendarCollection();
	
			PollerConfigFactory.init(); //Force init
	
			List<String> pollingOutages = new ArrayList<String>();
			org.opennms.netmgt.config.poller.Package[] pollingPackages = PollerConfigFactory.getInstance().getConfiguration().getPackage();
			for (int i = 0; i < pollingPackages.length; i++) {
				pollingOutages.addAll(pollingPackages[i].getOutageCalendarCollection());
			}
	
			ThreshdConfigFactory.init();
			List<String> thresholdingOutages = new ArrayList<String>();
			org.opennms.netmgt.config.threshd.Package[] thresholdingPackages = ThreshdConfigFactory.getInstance().getConfiguration().getPackage();
			for (int i = 0; i < thresholdingPackages.length; i++) {
				thresholdingOutages.addAll(thresholdingPackages[i].getOutageCalendarCollection());
			}
	
			CollectdConfigFactory.init();
			List<String> collectionOutages = new ArrayList<String>();
	
			for (Iterator iter = CollectdConfigFactory.getInstance().getCollectdConfig().getPackages().iterator(); iter.hasNext();) {
				org.opennms.netmgt.config.collectd.Package thisPackage = ((CollectdPackage) iter.next()).getPackage();
				collectionOutages.addAll(thisPackage.getOutageCalendarCollection());
			}
	
			for (int i = 0; i < outages.length; i++) {
				Outage thisOutage = outages[i];
				String outageName = thisOutage.getName();
	%>
	<tr valign="top">
		<td><%=outageName%></td>
		<td><%=pollFactory.getOutageType(outageName)%></td>
		<td><div class="nodeintbox">
		<%
			org.opennms.netmgt.config.poller.Node[] nodeList = pollFactory.getNodeIds(outageName);
				for (int j = 0; j < nodeList.length; j++) {
					OnmsNode elementNode = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeList[j].getId());
		%> <%=elementNode == null || elementNode.getType().charAt(0) == 'D' ? "Node: Node ID " + nodeList[j].getId() + " Not Found" : "Node: " + elementNode.getLabel()%><br/>
		<%
			}
				org.opennms.netmgt.config.poller.Interface[] interfaceList = pollFactory.getInterfaces(outageName);
				for (int j = 0; j < interfaceList.length; j++) {
					StringBuffer display;
					String rawAddress = interfaceList[j].getAddress();
					if ("match-any".equals(rawAddress)) {
						display = new StringBuffer("All nodes/interfaces");
					} else {
						display = new StringBuffer();
						List<Integer> nodeids = NetworkElementFactory.getInstance(getServletContext()).getNodeIdsWithIpLike(rawAddress);
						//org.opennms.web.element.Interface[] interfaces = NetworkElementFactory.getInstance(getServletContext()).getInterfacesWithIpAddress(rawAddress);
						if (nodeids.size() == 0) {
							display.append("Intfc: " + rawAddress + " Not Found<br/>");
						}
						for (Integer nodeid: nodeids) {
							org.opennms.web.element.Interface thisInterface = NetworkElementFactory.getInstance(getServletContext()).getInterface(nodeid,rawAddress);
							if (thisInterface.isManagedChar()=='D') {
								display.append("Intfc: " + thisInterface.getIpAddress() + " Not Found<br/>");
							} else {
								if (thisInterface.getHostname() != null && !thisInterface.getHostname().equals(thisInterface.getIpAddress())) {
									display.append("Intfc: " + thisInterface.getIpAddress() + " " + thisInterface.getHostname());
								} else {
									display.append("Intfc: " + thisInterface.getIpAddress());
								}
								if (thisInterface.isManaged()) {
									display.append("<br/>");
								} else {
									display.append(" (unmanaged)<br/>");
								}
							}
						}
					}
		%><%=display%>
		<%
			}
		%></div>
		</td>
		<td><div class="nodeintbox">
		<%
			org.opennms.netmgt.config.poller.Time[] outageTimes = pollFactory.getOutageTimes(outageName);
				for (int j = 0; j < outageTimes.length; j++) {
					org.opennms.netmgt.config.poller.Time thisOutageTime = outageTimes[j];
					String rawDay = thisOutageTime.getDay();
					String day = rawDay;
					if ("daily".equals(pollFactory.getOutageType(outageName)))
						day = "";
					if ("weekly".equals(pollFactory.getOutageType(outageName)))
						day = (rawDay == null) ? "" : (String) shortDayNames.get(rawDay);
					if ("specific".equals(pollFactory.getOutageType(outageName)))
						day = "";
		%><%=day%> <%=thisOutageTime.getBegins()%> -<%="specific".equals(pollFactory.getOutageType(outageName)) ? "<br/>" : ""%>
		<%=thisOutageTime.getEnds()%><br/>
		<%
			}
		%></div>
		</td>
		<td align="center"><img
			src="<%=(notificationOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td align="center"><img
			src="<%=(pollingOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td align="center"><img
			src="<%=(thresholdingOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td align="center"><img
			src="<%=(collectionOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
		<td><a id="<%=outageName%>.edit"
			href="admin/sched-outages/editoutage.jsp?name=<%=java.net.URLEncoder.encode(outageName, "UTF-8")%>">Edit</a></td>
		<td><a id="<%=outageName%>.delete"
			href="admin/sched-outages/index.jsp?deleteOutage=<%=java.net.URLEncoder.encode(outageName, "UTF-8")%>"
			onClick="if(!confirm('Are you sure you wish to delete this outage?')) {return false;}">Delete</a></td>
	</tr>

	<%
			} //end for outages
		} finally {
			pollFactory.getReadLock().unlock();
		}
	%>
</table>
<form action="admin/sched-outages/editoutage.jsp" method="post" >
  <input type="text" value="New Name" size="40" name="newName" /> 
  <input type="hidden" name="addNew" value="true" /> 
  <input type="submit" name="newOutage" value="Add new outage" />
</form>

<jsp:include page="/includes/footer.jsp" flush="true" />
