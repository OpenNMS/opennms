<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.*,org.opennms.netmgt.config.common.*,org.opennms.netmgt.config.poller.*,org.opennms.web.element.*,org.opennms.netmgt.EventConstants,org.opennms.netmgt.xml.event.Event,java.net.*,java.io.*,org.opennms.netmgt.utils.*" %>
<%!
public void sendOutagesChangedEvent() throws ServletException {
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
                EventProxy eventProxy = new TcpEventProxy();
                eventProxy.send(event);
        } catch (Exception e) {
                throw new ServletException("Could not send event " + event.getUei(), e);
        }
}

%>
<%
PollOutagesConfigFactory.init(); //Force a reload (includes init if necessary)
PollOutagesConfigFactory pollFactory=PollOutagesConfigFactory.getInstance();
String deleteName=request.getParameter("deleteOutage");
if(deleteName!=null) {
	pollFactory.removeOutage(deleteName);	
	//Remove from all the package configurations as well
	org.opennms.netmgt.config.threshd.Package[] thresholdingPackages=ThreshdConfigFactory.getInstance().getConfiguration().getPackage();
	for(int i=0; i<thresholdingPackages.length; i++) {
		org.opennms.netmgt.config.threshd.Package thisPackage=thresholdingPackages[i];
		thisPackage.removeOutageCalendar(deleteName); //Will quietly do nothing if outage doesn't exist
	}

	org.opennms.netmgt.config.poller.Package[] pollingPackages=PollerConfigFactory.getInstance().getConfiguration().getPackage();
	for(int i=0; i<pollingPackages.length; i++) {
		org.opennms.netmgt.config.poller.Package thisPackage=pollingPackages[i];
                thisPackage.removeOutageCalendar(deleteName); //Will quietly do nothing if outage doesn't exist
        }


	org.opennms.netmgt.config.collectd.Package[] collectionPackages=CollectdConfigFactory.getInstance().getConfiguration().getPackage();
	for(int i=0; i<collectionPackages.length; i++) {
		org.opennms.netmgt.config.collectd.Package thisPackage=collectionPackages[i];
                thisPackage.removeOutageCalendar(deleteName); //Will quietly do nothing if outage doesn't exist
        }

	NotifdConfigFactory.getInstance().getConfiguration().removeOutageCalendar(deleteName);	
	
	pollFactory.saveCurrent();
	NotifdConfigFactory.getInstance().saveCurrent();
	ThreshdConfigFactory.getInstance().saveCurrent();
	CollectdConfigFactory.getInstance().saveCurrent();
	PollerConfigFactory.getInstance().saveCurrent();
	sendOutagesChangedEvent();
}
%>
<html>
<title>Scheduled Outage Administration</title>
<base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
<body>
<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Manage Scheduled Outages"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage Scheduled Outages" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>


<table id="outages" border="1" cellpadding="5">
<tr><th colspan="4">&nbsp;</th><th colspan="4">Affects...</th></tr>
<tr><th>Name</th><th>Type</th><th>Nodes/Interfaces</th><th>Times</th><th>Notifications</th><th>Polling</th><th>Thresholds</th><th>Data collection</th></tr>

<%
HashMap shortDayNames=new HashMap();
shortDayNames.put("sunday","Sun");
shortDayNames.put("monday","Mon");
shortDayNames.put("tuesday","Tue");
shortDayNames.put("wednesday","Wed");
shortDayNames.put("thursday","Thu");
shortDayNames.put("friday","Fri");
shortDayNames.put("saturday","Sat");

String outageOnImageUrl="images/greentick.gif";
String outageOffImageUrl="images/redcross.gif";

Outage[] outages=pollFactory.getOutages();

Collection notificationOutages=NotifdConfigFactory.getInstance().getConfiguration().getOutageCalendarCollection();

PollerConfigFactory.init(); //Force init

List pollingOutages=new ArrayList();
org.opennms.netmgt.config.poller.Package[] pollingPackages=PollerConfigFactory.getInstance().getConfiguration().getPackage();
for(int i=0; i<pollingPackages.length; i++) {	
	pollingOutages.addAll(pollingPackages[i].getOutageCalendarCollection());
}


ThreshdConfigFactory.init();
List thresholdingOutages=new ArrayList();
org.opennms.netmgt.config.threshd.Package[] thresholdingPackages=ThreshdConfigFactory.getInstance().getConfiguration().getPackage();
for(int i=0; i<thresholdingPackages.length; i++) {
        thresholdingOutages.addAll(thresholdingPackages[i].getOutageCalendarCollection());
}

CollectdConfigFactory.init();
List collectionOutages=new ArrayList();
org.opennms.netmgt.config.collectd.Package[] collectionPackages=CollectdConfigFactory.getInstance().getConfiguration().getPackage();
for(int i=0; i<collectionPackages.length; i++) {
        collectionOutages.addAll(collectionPackages[i].getOutageCalendarCollection());
}

for(int i=0; i<outages.length; i++) {
	Outage thisOutage=outages[i];
	String outageName=thisOutage.getName();
%>
<tr valign="top">
        <td>
		<%=outageName%>
	</td>
	<td>
		<%=pollFactory.getOutageType(outageName)%>
	</td>
	<td>
	<% 
	org.opennms.netmgt.config.poller.Node[] nodeList=pollFactory.getNodeIds(outageName);
	for(int j=0; j<nodeList.length; j++) { 
		org.opennms.web.element.Node elementNode=NetworkElementFactory.getNode(nodeList[j].getId());
		%>
		<%=elementNode.getLabel()%><BR>
	<% } 
	org.opennms.netmgt.config.poller.Interface[] interfaceList=pollFactory.getInterfaces(outageName);
	for(int j=0; j<interfaceList.length; j++) { 
		StringBuffer display;
		String rawAddress=interfaceList[j].getAddress();
		if("match-any".equals(rawAddress)) {
			display=new StringBuffer("All nodes/interfaces");
		} else {
			display=new StringBuffer();
			org.opennms.web.element.Interface[] interfaces=NetworkElementFactory.getInterfacesWithIpAddress(rawAddress);
			for(int k=0; k<interfaces.length; k++) {
				org.opennms.web.element.Interface thisInterface=interfaces[k];
				if(thisInterface.isManaged()) {
					display.append(thisInterface.getHostname()+"<BR>");
				}
			}
		}
		%><%=display%><% 
	 } %>
	</td>
	<td noWrap>
	<%
	Time[] outageTimes=pollFactory.getOutageTimes(outageName);
	for(int j=0; j<outageTimes.length; j++) {
		Time thisOutageTime=outageTimes[j];
                String rawDay=thisOutageTime.getDay();
                String day = rawDay;
                if ("weekly".equals(pollFactory.getOutageType(outageName)))
                        day=(rawDay==null)?"":(String)shortDayNames.get(rawDay);
                if ("specific".equals(pollFactory.getOutageType(outageName)))
                        day= "";
		%><%=day%> <%=thisOutageTime.getBegins()%> -<%="specific".equals(pollFactory.getOutageType(outageName))?"<BR>":""%> <%=thisOutageTime.getEnds()%><BR><%
	}
	%>
	</td>
        <td align="center"><img src="<%=(notificationOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
        <td align="center"><img src="<%=(pollingOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
        <td align="center"><img src="<%=(thresholdingOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
        <td align="center"><img src="<%=(collectionOutages.contains(outageName))?outageOnImageUrl:outageOffImageUrl%>"></td>
	<td><a id="<%=outageName%>.edit" href="admin/sched-outages/editoutage.jsp?name=<%=java.net.URLEncoder.encode(outageName)%>">Edit</a></td>
	<td><a id="<%=outageName%>.delete" href="admin/sched-outages/index.jsp?deleteOutage=<%=java.net.URLEncoder.encode(outageName)%>" onClick="if(!confirm('Are you sure you wish to delete this outage?')) {return false;}">Delete</a></td>
</tr>

<%	
} //end for outages 
%>
</table>
<form action="admin/sched-outages/editoutage.jsp" method="POST">
<input type="text" value="New Name" size="40" name="newName" />
<input type="hidden" name="addNew" value="true"/>
<input type="submit" name="newOutage" value="Add new outage"/>
</form>
<br/>
<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="admin" />
</jsp:include>

</body>
</html>
