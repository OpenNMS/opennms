<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.common.*,
		org.opennms.netmgt.config.poller.*,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.netmgt.EventConstants,
		org.opennms.netmgt.xml.event.Event,
		org.opennms.netmgt.utils.*,
		org.opennms.web.Util,
		java.net.*,
		java.io.*
	"
%>

<%!
//A singleton instance of a "Match-any" interface, which can be used for generic tests/removals etc.
private static org.opennms.netmgt.config.poller.Interface matchAnyInterface;
{
	matchAnyInterface=new org.opennms.netmgt.config.poller.Interface();
	matchAnyInterface.setAddress("match-any");
}
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
		Util.createEventProxy().send(event);
	} catch (Exception e) {
		throw new ServletException("Could not send event " + event.getUei(), e);
	}
}

%>
<%
NotifdConfigFactory.init(); //Must do this early on - if it fails, then just throw the exception to the web gui
HashMap shortDayNames=new HashMap();
shortDayNames.put("sunday","Sun");
shortDayNames.put("monday","Mon");
shortDayNames.put("tuesday","Tue");
shortDayNames.put("wednesday","Wed");
shortDayNames.put("thursday","Thu");
shortDayNames.put("friday","Fri");
shortDayNames.put("saturday","Sat");
shortDayNames.put("1","1st");
shortDayNames.put("2","2nd");
shortDayNames.put("3","3rd");
shortDayNames.put("4","4th");
shortDayNames.put("5","5th");
shortDayNames.put("6","6th");
shortDayNames.put("7","7th");
shortDayNames.put("8","8th");
shortDayNames.put("9","9th");
shortDayNames.put("10","10th");
shortDayNames.put("11","11th");
shortDayNames.put("12","12th");
shortDayNames.put("13","13th");
shortDayNames.put("14","14th");
shortDayNames.put("15","15th");
shortDayNames.put("16","16th");
shortDayNames.put("17","17th");
shortDayNames.put("18","18th");
shortDayNames.put("19","19th");
shortDayNames.put("20","20th");
shortDayNames.put("21","21st");
shortDayNames.put("22","22nd");
shortDayNames.put("23","23rd");
shortDayNames.put("24","24th");
shortDayNames.put("25","25th");
shortDayNames.put("26","26th");
shortDayNames.put("27","27th");
shortDayNames.put("28","28th");
shortDayNames.put("29","29th");
shortDayNames.put("30","30th");
shortDayNames.put("31","31st");

   PollOutagesConfigFactory.init(); //Only init - do *not* reload
   PollOutagesConfigFactory pollFactory=PollOutagesConfigFactory.getInstance();
   Outage theOutage;
   String nameParam=request.getParameter("name");
	if(nameParam!=null) {
		//first time in - name is passed as a param.  Find the outage, copy it, and shove it in the session
		//Also keep a copy of the name, for later saving (replacing the original with the edited copy)
   		Outage tempOutage=pollFactory.getOutage(nameParam);
		CharArrayWriter writer=new CharArrayWriter();
		tempOutage.marshal(writer);
		theOutage=(Outage)Outage.unmarshal(new CharArrayReader(writer.toCharArray()));
		request.getSession().setAttribute("opennms.editoutage",theOutage);
		request.getSession().setAttribute("opennms.editoutage.origname", nameParam);
	} else if("true".equals(request.getParameter("addNew"))) {
		theOutage=new Outage();
		//Nuke whitespace - it messes with all sorts of things
		theOutage.setName(request.getParameter("newName").trim());
		theOutage.setType("specific");
		request.getSession().setAttribute("opennms.editoutage",theOutage);
		request.getSession().removeAttribute("opennms.editoutage.origname");
	} else {
		//Neither starting the edit, nor adding a new outage.  
		theOutage=(Outage)request.getSession().getAttribute("opennms.editoutage");
		if(theOutage==null) {
			//No name, and no outage in the session.  Give up
			%>
            <html><body> 
			No outage name parameter, nor outage stored in the session.  Cannot edit!<BR/>
			</body></html>
			<%
			return;
			
		}
	}
   //Load the initial set of enabled outages from the external configuration
   // This will be overridden by a formSubmission to use the form values, but is necessary for the initial load of the page
   //It is more efficient to piggy back on this initial setup setp (creating the hashmaps) than doing it separately
   Set enabledOutages=new HashSet();
   
// ******* Notification outages config *********
   Collection notificationOutages=NotifdConfigFactory.getInstance().getConfiguration().getOutageCalendarCollection();
   if(notificationOutages.contains(theOutage.getName())) {
	enabledOutages.add("notifications");
   }
   

// ******* Threshd outages config *********
   ThreshdConfigFactory.init();
   Map thresholdOutages=new HashMap();
   org.opennms.netmgt.config.threshd.Package[] thresholdingPackages=ThreshdConfigFactory.getInstance().getConfiguration().getPackage();
   for(int i=0; i<thresholdingPackages.length; i++) {
	org.opennms.netmgt.config.threshd.Package thisPackage=thresholdingPackages[i];
	thresholdOutages.put(thisPackage, thisPackage.getOutageCalendarCollection());
	if(thisPackage.getOutageCalendarCollection().contains(theOutage.getName())) {
		enabledOutages.add("threshold-"+thisPackage.getName());
	}	
   }

// ******* Polling outages config *********
   PollerConfigFactory.init();
   Map pollingOutages=new HashMap();
   org.opennms.netmgt.config.poller.Package[] pollingPackages=PollerConfigFactory.getInstance().getConfiguration().getPackage();
   for(int i=0; i<pollingPackages.length; i++) {
	org.opennms.netmgt.config.poller.Package thisPackage=pollingPackages[i];
	pollingOutages.put(thisPackage, thisPackage.getOutageCalendarCollection());
	if(thisPackage.getOutageCalendarCollection().contains(theOutage.getName())) {
		enabledOutages.add("polling-"+thisPackage.getName());
	}	
   }

// ******* Collectd outages config *********
   CollectdConfigFactory.init();
   Map collectionOutages=new HashMap();
   Collection collectionPackages =CollectdConfigFactory.getInstance().getCollectdConfig().getPackages();
   for(Iterator it = collectionPackages.iterator(); it.hasNext();) {
			CollectdPackage pkg = (CollectdPackage)it.next();
			org.opennms.netmgt.config.collectd.Package thisPackage=pkg.getPackage();
			collectionOutages.put(thisPackage, thisPackage.getOutageCalendarCollection());
			if(thisPackage.getOutageCalendarCollection().contains(theOutage.getName())) {
					enabledOutages.add("collect-"+thisPackage.getName());
			}	
   }
   
   
   


   String isFormSubmission=request.getParameter("formSubmission");
   if("true".equals(isFormSubmission)) {


	//Process the form submission - yeah, this should be a servlet, but this is a quick and dirty hack for now
	//It can be tidied up later
	//First, process any changes to the editable inputs
	theOutage.setType(request.getParameter("outageType"));
	
	
	//Process the notifications status.  NB: we keep an in-memory copy initially, and only save when the save button is clicked
	if("on".equals(request.getParameter("notifications"))) {
		//Want to turn it on.
		enabledOutages.add("notifications");
	} else {
		//Want to turn off (missing, or set to something other than "on")
		enabledOutages.remove("notifications");
	}
	Iterator keys=pollingOutages.keySet().iterator();
	while(keys.hasNext()) {
		org.opennms.netmgt.config.poller.Package thisKey=(org.opennms.netmgt.config.poller.Package)keys.next();
		String name="polling-"+thisKey.getName();
		System.out.println("Checking "+name);
		if("on".equals(request.getParameter(name))) {
			System.out.println(" is on - adding to enabledOutages");
			enabledOutages.add(name);
		} else {
			enabledOutages.remove(name);
		}
	}	
	keys=thresholdOutages.keySet().iterator();
        while(keys.hasNext()) {
                org.opennms.netmgt.config.threshd.Package thisKey=(org.opennms.netmgt.config.threshd.Package)keys.next();
		String name="threshold-"+thisKey.getName();
		System.out.println("Checking "+name);
                if("on".equals(request.getParameter(name))) {
			enabledOutages.add(name);
                } else {
			enabledOutages.remove(name);
                }
        }

	keys=collectionOutages.keySet().iterator();
        while(keys.hasNext()) {
                org.opennms.netmgt.config.collectd.Package thisKey=(org.opennms.netmgt.config.collectd.Package)keys.next();
		String name="collect-"+thisKey.getName();
		System.out.println("Checking "+name);
                if("on".equals(request.getParameter(name))) {
			enabledOutages.add(name);
                } else {
			enabledOutages.remove(name);
                }
        }


	//Now handle any buttons that were clicked.  There should be only one
	//If there is more than one, we use the first and ignore the rest.
	if(request.getParameter("saveButton")!=null) {
		//Save was clicked - save 
	
		//Check if the outage is a new one, or an edited old one
		String origname=(String)request.getSession().getAttribute("opennms.editoutage.origname");
		if(origname==null) {
			//A new outage - just plonk it in place
			pollFactory.addOutage(theOutage);
		} else {
			//An edited outage - replace the old one
			pollFactory.replaceOutage(pollFactory.getOutage(origname), theOutage);
		}
		//Push the enabledOutages into the actual configuration of the various packages
		//Don't do until after we've successfully put the outage into the polloutages configuration (for coherency)
		if(enabledOutages.contains("notifications")) {
			if(!notificationOutages.contains(theOutage.getName())) {
				NotifdConfigFactory.getInstance().getConfiguration().addOutageCalendar(theOutage.getName());
			}
		} else {
			if(notificationOutages.contains(theOutage.getName())) {
				NotifdConfigFactory.getInstance().getConfiguration().removeOutageCalendar(theOutage.getName());
			} 
		}	
		keys=pollingOutages.keySet().iterator();
		while(keys.hasNext()) {
			org.opennms.netmgt.config.poller.Package thisKey=(org.opennms.netmgt.config.poller.Package)keys.next();
			Collection pollingPackage=(Collection)pollingOutages.get(thisKey);
			String name="polling-"+thisKey.getName();
			if(enabledOutages.contains(name)) {
				if(!pollingPackage.contains(theOutage.getName())) {
					thisKey.addOutageCalendar(theOutage.getName());
				}
			} else {
				if(pollingPackage.contains(theOutage.getName())) {
					thisKey.removeOutageCalendar(theOutage.getName());
				}
			}
		}

		keys=thresholdOutages.keySet().iterator();
	        while(keys.hasNext()) {
	                org.opennms.netmgt.config.threshd.Package thisKey=(org.opennms.netmgt.config.threshd.Package)keys.next();
	                Collection thresholdPackage=(Collection)thresholdOutages.get(thisKey);
			String name="threshold-"+thisKey.getName();
			if(enabledOutages.contains(name)) {
	                        if(!thresholdPackage.contains(theOutage.getName())) {
	                                thisKey.addOutageCalendar(theOutage.getName());
	                        }
	                } else {
	                        if(thresholdPackage.contains(theOutage.getName())) {
	                                thisKey.removeOutageCalendar(theOutage.getName());
	                        }
	                }
	        }
	
		keys=collectionOutages.keySet().iterator();
	        while(keys.hasNext()) {
	                org.opennms.netmgt.config.collectd.Package thisKey=(org.opennms.netmgt.config.collectd.Package)keys.next();
	                Collection collectPackage=(Collection)collectionOutages.get(thisKey);
			String name="collect-"+thisKey.getName();
			if(enabledOutages.contains(name)) {
        	                if(!collectPackage.contains(theOutage.getName())) {
	                                thisKey.addOutageCalendar(theOutage.getName());
	                        }
	                } else {
	                        if(collectPackage.contains(theOutage.getName())) {
	                                thisKey.removeOutageCalendar(theOutage.getName());
	                        }
	                }
	        }
		//Save to disk	
		pollFactory.saveCurrent();	
		NotifdConfigFactory.getInstance().saveCurrent();
		ThreshdConfigFactory.getInstance().saveCurrent();
		CollectdConfigFactory.getInstance().saveCurrent();
		PollerConfigFactory.getInstance().save();
		sendOutagesChangedEvent();

		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/sched-outages/index.jsp");
		dispatcher.forward( request, response );
	} else if (request.getParameter("addNodeButton")!=null) {
		try {
			int newNodeId=WebSecurityUtils.safeParseInt(request.getParameter("newNode"));
			org.opennms.netmgt.config.poller.Node newNode=new org.opennms.netmgt.config.poller.Node();
			newNode.setId(newNodeId);
			if(!theOutage.getNodeCollection().contains(newNode)) {
				theOutage.addNode(newNode);
				theOutage.removeInterface(matchAnyInterface); //Just arbitrarily try and remove it.  If it's not there, this will do nothing
			}
		} catch (NumberFormatException e) {
			//Just ignore it - we can't add the node, why should we care?
		}
	} else if (request.getParameter("addInterfaceButton")!=null) {
		org.opennms.netmgt.config.poller.Interface newInterface=new org.opennms.netmgt.config.poller.Interface();
		newInterface.setAddress(request.getParameter("newInterface"));
		if(!theOutage.getInterfaceCollection().contains(newInterface)) {
			theOutage.addInterface(newInterface);
			theOutage.removeInterface(matchAnyInterface); //Just arbitrarily try and remove it.  If it's not there, this will do nothing
		}
	} else if (request.getParameter("matchAny") != null) {
		//To turn on matchAny, all normal nodes and interfaces are removed
		theOutage.removeAllInterface();
		theOutage.removeAllNode();
		theOutage.addInterface(matchAnyInterface);
	} else if (request.getParameter("addSpecificTime")!=null) {
		Time newTime=new Time();
		StringBuffer beginsTime=new StringBuffer(17);
		beginsTime.append(request.getParameter("startNewDate"));
		beginsTime.append("-");
		beginsTime.append(request.getParameter("startNewMonth"));
		beginsTime.append("-");
		beginsTime.append(request.getParameter("startNewYear"));
		beginsTime.append(" ");
		beginsTime.append(request.getParameter("startNewSpecificHour"));
		beginsTime.append(":");
		beginsTime.append(request.getParameter("startNewSpecificMinute"));
		beginsTime.append(":");
		beginsTime.append(request.getParameter("startNewSpecificSecond"));
		newTime.setBegins(beginsTime.toString());
		StringBuffer endsTime=new StringBuffer(17);
		endsTime.append(request.getParameter("endNewDate"));
		endsTime.append("-");
		endsTime.append(request.getParameter("endNewMonth"));
		endsTime.append("-");
		endsTime.append(request.getParameter("endNewYear"));
		endsTime.append(" ");
		endsTime.append(request.getParameter("endNewSpecificHour"));
		endsTime.append(":");
		endsTime.append(request.getParameter("endNewSpecificMinute"));
		endsTime.append(":");
		endsTime.append(request.getParameter("endNewSpecificSecond"));
		newTime.setEnds(endsTime.toString());
		theOutage.addTime(newTime);
	} else if (request.getParameter("addDayTime")!=null) {
		Time newTime=new Time();
		String dayValue="1"; //Default to something vaguely acceptable 
		if("monthly".compareToIgnoreCase(theOutage.getType())==0) {
			dayValue=request.getParameter("startNewDayNum");	
		} else if ("weekly".compareToIgnoreCase(theOutage.getType())==0) {
			dayValue=request.getParameter("startNewDayTxt");
		}
		newTime.setDay(dayValue);
		StringBuffer beginsTime=new StringBuffer(8);
		beginsTime.append(request.getParameter("startNewHour"));
                beginsTime.append(":");
                beginsTime.append(request.getParameter("startNewMinute"));
                beginsTime.append(":");
                beginsTime.append(request.getParameter("startNewSecond"));
                newTime.setBegins(beginsTime.toString());

		StringBuffer endsTime=new StringBuffer(8);
                endsTime.append(request.getParameter("endNewHour"));
                endsTime.append(":");
                endsTime.append(request.getParameter("endNewMinute"));
                endsTime.append(":");
                endsTime.append(request.getParameter("endNewSecond"));
                newTime.setEnds(endsTime.toString());
	
		theOutage.addTime(newTime);	
	} else {
		//Look for deleteNode or deleteInterface or deleteTime prefix
		Enumeration paramEnum=request.getParameterNames();
		boolean found=false;
		while(paramEnum.hasMoreElements() && !found) {
			String paramName=(String)paramEnum.nextElement();
			if(paramName.startsWith("deleteNode")) {
				found=true;
				String indexStr=paramName.substring("deleteNode".length(), paramName.indexOf("."));
				try {
					int index=WebSecurityUtils.safeParseInt(indexStr);
					theOutage.removeNode(theOutage.getNode(index));		
				} catch (NumberFormatException e) {
					
					//Ignore - nothing we can do
				}
			} else if (paramName.startsWith("deleteInterface")) {
				found=true;
				String indexStr=paramName.substring("deleteInterface".length(), paramName.indexOf("."));
				try {
					int index=WebSecurityUtils.safeParseInt(indexStr);
					theOutage.removeInterface(theOutage.getInterface(index));		
				} catch (NumberFormatException e) {
					//Ignore - nothing we can do
				}
			} else if (paramName.startsWith("deleteTime")) {
				found=true;
				String indexStr=paramName.substring("deleteTime".length(), paramName.indexOf("."));
				try {
					int index=WebSecurityUtils.safeParseInt(indexStr);
					theOutage.removeTime(theOutage.getTime(index));
				} catch (NumberFormatException e) {
					//Ignore - nothing we can do
				}
			}
		}
	}

   } //end if form submission
boolean hasMatchAny=theOutage.getInterfaceCollection().contains(matchAnyInterface);
//for(int i=0; i<theOutage.getInterfaceCount(); i++) {
//	if(theOutage.getInterface(i).getAddress().equals("match-any")) {
//		hasMatchAny=true;
//		break; //out of for loop
//	}
//}
theOutage.getInterfaceCollection().contains("match-any");
%>    

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Edit Outage" />
  <jsp:param name="headTitle" value="Edit" />
  <jsp:param name="headTitle" value="Scheduled Outages" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/sched-outages/index.jsp'>Scheduled Outages</a>" />
  <jsp:param name="breadcrumb" value="Edit" />
</jsp:include>

<style>
TD {
        font-size: 0.8em;
}
</style>

<script>
function outageTypeChanged(selectElement) {
	var isSpecific=selectElement.options(selectElement.selectedIndex).value=="specific";
	var isMonthly=selectElement.options(selectElement.selectedIndex).value=="monthly";
	document.getElementById("newSpecificTimeTR").style.display=((isSpecific)?'':'none');
	document.getElementById("newDayTimeTR").style.display=((isSpecific)?'none':'');
	document.getElementById("startNewDayTxt").style.display=((isMonthly)?'none':'');
	document.getElementById("startNewDayNum").style.display=((isMonthly)?'':'none');
}
</script>


<%
Enumeration enumList=request.getParameterNames();
while(enumList.hasMoreElements()) {
	String paramName=(String)enumList.nextElement();
	%>
<!--	<%=paramName%>=<%=request.getParameter(paramName)%><BR>  -->
	<%
}
 %>
<form id="editForm" action="admin/sched-outages/editoutage.jsp" method="post">
<input type="hidden" name="formSubmission" value="<%=true%>"/>
<table border="0">
<tr><td><b>Name:</b></td>
<td><%=theOutage.getName()%></td>
<td></td>
</tr><tr>
<td><b>Type:</b> </td><td><select name="outageType" onChange="outageTypeChanged(this);">
	<option value="specific" <%=("specific".compareToIgnoreCase(theOutage.getType())==0)?"selected":""%>>Specific</option>
	<option value="weekly" <%=("weekly".compareToIgnoreCase(theOutage.getType())==0)?"selected":""%>>Weekly</option>
	<option value="monthly" <%=("monthly".compareToIgnoreCase(theOutage.getType())==0)?"selected":""%>>Monthly</option>
</select></td>
<td colspan="2"></td>
</tr>
<tr><td colspan=6><hr></td></tr>
<tr><td colspan=3 valign="top"><b>Nodes:</b><BR>
<select name="newNode">
	<% 
	org.opennms.web.element.Node[] allNodes=NetworkElementFactory.getAllNodes();
	for(int j=0; j<allNodes.length; j++) {%>
		<option value="<%=allNodes[j].getNodeId()%>"><%=allNodes[j].getLabel()%></option>
        <%}%>
</select><input type="submit" value="Add" name="addNodeButton" />
<table border=1>
<% 
org.opennms.netmgt.config.poller.Node[] nodeList=theOutage.getNode();
for(int i=0; i<nodeList.length; i++) {
	int nodeId=nodeList[i].getId();
	org.opennms.web.element.Node thisNode=NetworkElementFactory.getNode(nodeId);
	%><tr><%
	if(thisNode!=null) { %>
		<td><%=thisNode.getLabel()%></td>
	<% } else { %>
		<td>Can't find node with id:<%=nodeId%></td>
	<% } %>
	<td><input type="image" src="images/redcross.gif" name="deleteNode<%=i%>"/></td>
	</tr>
<% } %>

</table>
</td><td valign="top">
<b>Interfaces:</b><BR>
<select name="newInterface">
        <%
	org.opennms.web.element.Interface[] allInterfaces=NetworkElementFactory.getAllInterfaces(false);
	Arrays.sort(allInterfaces, new Comparator() {
		public int compare(Object o1, Object o2) {
			org.opennms.web.element.Interface i1=(org.opennms.web.element.Interface)o1;
			org.opennms.web.element.Interface i2=(org.opennms.web.element.Interface)o2;
			String h1=i1.getHostname();
			String h2=i2.getHostname();
			if(h1==null) {
				if(h2==null) {
					return 0; //two nulls - the same
				} else {
					return -1; //null is less than something
				}
			} else if(h2==null) {
				//h1 is not null, by definition
				return 1; //h2 is null, h1 is not, so return a positive number
			}
			return h1.compareTo(h2);
		}
	});
	for(int j=0; j<allInterfaces.length; j++) {
		org.opennms.web.element.Interface thisInterface=allInterfaces[j];
		String ipaddress=thisInterface.getIpAddress();
		if(!"0.0.0.0".equals(ipaddress) && thisInterface.isManaged()) {%>
	                <option value="<%=ipaddress%>"><%=thisInterface.getHostname()%></option>
		<%}
        }%>
</select><input type="submit" value="Add" name="addInterfaceButton">

<% 
if(!hasMatchAny) { %>
<table border=1>
<%
org.opennms.netmgt.config.poller.Interface[] interfaceList=theOutage.getInterface();
for(int i=0; i<interfaceList.length; i++) { 
	org.opennms.web.element.Interface[] interfaces=NetworkElementFactory.getInterfacesWithIpAddress(interfaceList[i].getAddress());
	for(int j=0; j<interfaces.length; j++) {
		org.opennms.web.element.Interface thisInterface=interfaces[j]; 
		if(thisInterface.isManaged()) {%>
		<tr>
			<td><%=thisInterface.getHostname()%></td>
			<td><input type="image" src="images/redcross.gif" name="deleteInterface<%=i%>"/></td>
		</tr>
<%		}
	}

} %>
</table>
<% } //end if has match any %>
</td></tr>
<tr><td colspan="4"><b>OR</b></td></tr>
<tr><td colspan="4"><%
	if(hasMatchAny) {
	%> <b>Applies to ALL nodes/interfaces</b><%
	} else {
	%><input type="submit" name="matchAny" value="All nodes/interfaces"/><%
	}%>
</td></tr>
<tr><td colspan="4"><hr></td></tr>
<tr><td colspan="4"><b>Times:</b><BR></td></tr>

<tr id="newDayTimeTR" style="display:<%=("specific".compareToIgnoreCase(theOutage.getType())==0)?"none":"''"%>">
<td valign="top">Add time</td>
<td>
<select style="display:<%=("weekly".compareToIgnoreCase(theOutage.getType())==0)?"''":"none"%>" id="startNewDayTxt" name="startNewDayTxt">
	<option value="sunday">Sun</option>
	<option value="monday">Mon</option>
	<option value="tuesday">Tue</option>
	<option value="wednesday">Wed</option>
	<option value="thursday">Thu</option>
	<option value="friday">Fri</option>
	<option value="saturday">Sat</option>
    </select>
<select name="startNewDayNum" id="startNewDayNum"  style="display:<%=("monthly".compareToIgnoreCase(theOutage.getType())==0)?"''":"none"%>">
	<% for(int i=1; i<32; i++) { %>
                <option value="<%=i%>"><%=i%></option>
	<% } %>
     </select>
<BR>
<select name="startNewHour">
	<% for(int i=0; i<24; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
	<% } %>
     </select>:<select name="startNewMinute">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>:<select name="startNewSecond">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select> -&gt;
<BR>
<select name="endNewHour">
	<% for(int i=0; i<24; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
	<% } %>
     </select>:<select name="endNewMinute">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>:<select name="endNewSecond">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>
</td>
<td valign="bottom"><input type="submit" value="Add" name="addDayTime"/></td>
</tr>
<tr id="newSpecificTimeTR" style="display:<%=("specific".compareToIgnoreCase(theOutage.getType())==0)?"''":"none"%>">
<td valign="top">Add time</td>
<td><select name="startNewDate">
        <% 
	   GregorianCalendar today=new GregorianCalendar();
	   int date=today.get(Calendar.DATE);
	   int month=today.get(Calendar.MONTH)+1;
	   int year=today.get(Calendar.YEAR);
	   for(int i=1; i<32; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>" <%=(date==i)?"selected":""%>><%=i%></option>
        <% } %>
      </select>
      <select name="startNewMonth">
	<option value="Jan" <%=(month==1)?"selected":""%>>Jan</option>
	<option value="Feb" <%=(month==2)?"selected":""%>>Feb</option>
	<option value="Mar" <%=(month==3)?"selected":""%>>Mar</option>
	<option value="Apr" <%=(month==4)?"selected":""%>>Apr</option>
	<option value="May" <%=(month==5)?"selected":""%>>May</option>
	<option value="Jun" <%=(month==6)?"selected":""%>>Jun</option>
	<option value="Jul" <%=(month==7)?"selected":""%>>Jul</option>
	<option value="Aug" <%=(month==8)?"selected":""%>>Aug</option>
	<option value="Sep" <%=(month==9)?"selected":""%>>Sep</option>
	<option value="Oct" <%=(month==10)?"selected":""%>>Oct</option>
	<option value="Nov" <%=(month==11)?"selected":""%>>Nov</option>
	<option value="Dec" <%=(month==12)?"selected":""%>>Dec</option>
      </select>
      <select name="startNewYear">
	<option value="2004" <%=(year==2004)?"selected":""%>>2004</option>
	<option value="2005" <%=(year==2005)?"selected":""%>>2005</option>
	<option value="2006" <%=(year==2006)?"selected":""%>>2006</option>
	<option value="2007" <%=(year==2007)?"selected":""%>>2007</option>
	<option value="2008" <%=(year==2008)?"selected":""%>>2008</option>
	<option value="2009" <%=(year==2009)?"selected":""%>>2009</option>
      </select>
      <select name="startNewSpecificHour">
        <% for(int i=0; i<24; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>:<select name="startNewSpecificMinute">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>:<select name="startNewSpecificSecond">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select> -&gt;
<BR>
<select name="endNewDate">
        <% for(int i=1; i<32; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>" <%=(date==i)?"selected":""%>><%=i%></option>
        <% } %>
      </select>
      <select name="endNewMonth">
	<option value="Jan" <%=(month==1)?"selected":""%>>Jan</option>
	<option value="Feb" <%=(month==2)?"selected":""%>>Feb</option>
	<option value="Mar" <%=(month==3)?"selected":""%>>Mar</option>
	<option value="Apr" <%=(month==4)?"selected":""%>>Apr</option>
	<option value="May" <%=(month==5)?"selected":""%>>May</option>
	<option value="Jun" <%=(month==6)?"selected":""%>>Jun</option>
	<option value="Jul" <%=(month==7)?"selected":""%>>Jul</option>
	<option value="Aug" <%=(month==8)?"selected":""%>>Aug</option>
	<option value="Sep" <%=(month==9)?"selected":""%>>Sep</option>
	<option value="Oct" <%=(month==10)?"selected":""%>>Oct</option>
	<option value="Nov" <%=(month==11)?"selected":""%>>Nov</option>
	<option value="Dec" <%=(month==12)?"selected":""%>>Dec</option>
      </select>
      <select name="endNewYear">
	<option value="2004" <%=(year==2004)?"selected":""%>>2004</option>
	<option value="2005" <%=(year==2005)?"selected":""%>>2005</option>
	<option value="2006" <%=(year==2006)?"selected":""%>>2006</option>
	<option value="2007" <%=(year==2007)?"selected":""%>>2007</option>
	<option value="2008" <%=(year==2008)?"selected":""%>>2008</option>
	<option value="2009" <%=(year==2009)?"selected":""%>>2009</option>
      </select>
	
	<select name="endNewSpecificHour">
        <% for(int i=0; i<24; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>:<select name="endNewSpecificMinute">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>:<select name="endNewSpecificSecond">
        <% for(int i=0; i<60; i++) { %>
                <option value="<%=(i<10)?"0":""%><%=i%>"><%=i%></option>
        <% } %>
     </select>
</td>

<td valign="bottom"><input type="submit" value="Add" name="addSpecificTime"/></td>
</tr>
<tr><td colspan="4">
<table border="1">
<%
Time[] outageTimes=theOutage.getTime();
for(int i=0; i<outageTimes.length; i++) { 
	Time thisTime=outageTimes[i];%>
<tr>
	<td><%=(thisTime.getDay()!=null)?shortDayNames.get(thisTime.getDay())+"&nbsp;":""%><%=thisTime.getBegins()%> - <%=thisTime.getEnds()%></td>
	<td><input type="image" src="images/redcross.gif" name="deleteTime<%=i%>"/></td>
</tr>
<% } %>
</table></td></tr>

<tr><td colspan=4><hr></td></tr>
<tr><td colspan="4">
<b>Applies to:</b><br/>
<table border="1">
<tr><td colspan="2">Notifications</td><td><input type="checkbox" <%=(enabledOutages.contains("notifications"))?"checked":""%> name="notifications"></td></tr>
<%
Iterator keys=pollingOutages.keySet().iterator();
boolean doneTitle=false;
while(keys.hasNext()) {
	org.opennms.netmgt.config.poller.Package thisKey=(org.opennms.netmgt.config.poller.Package)keys.next();
	Collection pollingPackage=(Collection)pollingOutages.get(thisKey);
	String name="polling-"+thisKey.getName();
%>
	<tr>
	<td><%=!doneTitle?"Status polling":""%></td>
	<td>
		<%=thisKey.getName()%>
	</td>
	<td>
		<input type="checkbox" name="<%=name%>" <%=enabledOutages.contains(name)?"checked":""%>>
	</td>
	</tr>
<%
	doneTitle=true;
}
keys=thresholdOutages.keySet().iterator();
doneTitle=false;
while(keys.hasNext()) {
	org.opennms.netmgt.config.threshd.Package thisKey=(org.opennms.netmgt.config.threshd.Package)keys.next();
	Collection thresholdPackage=(Collection)thresholdOutages.get(thisKey);
	String name="threshold-"+thisKey.getName();
%>
	<tr>
	<td><%=!doneTitle?"Threshold checking":""%></td>
	<td>
		<%=thisKey.getName()%>
	</td>
	<td>
		<input type="checkbox" name="<%=name%>" <%=enabledOutages.contains(name)?"checked":""%>>
	</td>
	</tr>
<%
	doneTitle=true;
}
keys=collectionOutages.keySet().iterator();
doneTitle=false;
while(keys.hasNext()) {
	org.opennms.netmgt.config.collectd.Package thisKey=(org.opennms.netmgt.config.collectd.Package)keys.next();
	Collection collectPackage=(Collection)collectionOutages.get(thisKey);
	String name="collect-"+thisKey.getName();
%>
	<tr>
	<td><%=!doneTitle?"Data collection":""%></td>
	<td>
		<%=thisKey.getName()%>
	</td>
	<td>
		<input type="checkbox" name="<%=name%>" <%=enabledOutages.contains(name)?"checked":""%>>
	</td>
	</tr>
<%
	doneTitle=true;
}
%>
</table></td></tr>
</table>
<input type="submit" value="Save" name="saveButton" />
</form>
<form id="cancelForm" action="admin/sched-outages/index.jsp" method="post">
<input type="submit" value="Cancel" name="cancelButton" />
</form>

<jsp:include page="/includes/footer.jsp" flush="true"/>
