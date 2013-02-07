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

<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*,
        org.opennms.netmgt.config.*,
        org.opennms.netmgt.config.poller.*,
        org.opennms.core.utils.WebSecurityUtils,
        org.opennms.core.resource.Vault,
        org.opennms.web.element.*,
        org.opennms.web.pathOutage.*,
        org.opennms.netmgt.model.OnmsNode,
        org.opennms.netmgt.EventConstants,
        org.opennms.netmgt.xml.event.Event,
        org.opennms.core.utils.*,
        org.opennms.netmgt.utils.*,
        org.opennms.web.api.Util,
        org.exolab.castor.xml.ValidationException,
        java.net.*,
        java.io.*,
        java.sql.*,
        java.text.NumberFormat,
        java.text.SimpleDateFormat
        "
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!//A singleton instance of a "Match-any" interface, which can be used for generic tests/removals etc.
	private static org.opennms.netmgt.config.poller.Interface matchAnyInterface;
	{
		matchAnyInterface = new org.opennms.netmgt.config.poller.Interface();
		matchAnyInterface.setAddress("match-any");
	}

	private static void addNode(Outage theOutage, int newNodeId) throws ValidationException {
		try {
			org.opennms.netmgt.config.poller.Node newNode = new org.opennms.netmgt.config.poller.Node();
			newNode.setId(newNodeId);
			if (!theOutage.getNodeCollection().contains(newNode)) {
				newNode.validate();
				theOutage.addNode(newNode);
				theOutage.removeInterface(matchAnyInterface); //Just arbitrarily try and remove it.  If it's not there, this will do nothing
			}
		} catch (NumberFormatException e) {
			//Just ignore it - we can't add the node, why should we care?
		} catch (IndexOutOfBoundsException ioob) {
			// same here
		}
	}
	
	private static void addInterface(Outage theOutage, org.opennms.netmgt.config.poller.Interface newInterface) throws ValidationException {
		if (!theOutage.getInterfaceCollection().contains(newInterface)) {
			newInterface.validate();
			theOutage.addInterface(newInterface);
			try {
				theOutage.removeInterface(matchAnyInterface); //Just arbitrarily try and remove it.  If it's not there, this will do nothing
			} catch (IndexOutOfBoundsException ioob) {
				// ok if it fails
			}
		}
	}

	private static String getNumberSelectField(String name, int start, int end, int selected, int padding) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(padding);
		nf.setGroupingUsed(false);
		StringBuffer sb = new StringBuffer();
		sb.append("<select name=\"").append(name).append("\">");
		for (int i = start; i <= end; i++) {
			sb.append("<option value=\"").append(nf.format(i)).append("\"");
			if (i == selected) {
				sb.append(" selected");
			}
			sb.append(">").append(nf.format(i)).append("</option>");
		}
		sb.append("</select>");

		return sb.toString();
	}
	
	private static String getMonthSelectField(String name, int month) {
		StringBuffer sb = new StringBuffer();
		sb.append("<select name=\"").append(name).append("\">");
		SimpleDateFormat shortDF = new SimpleDateFormat("MMM", Locale.US);
		SimpleDateFormat longDF  = new SimpleDateFormat("MMMM");
		for (int mon = 0; mon < 12; mon++) {
			java.util.Date tempDate = new GregorianCalendar(0, mon, 1).getTime();
			sb.append("<option value=\"").append(shortDF.format(tempDate)).append("\" ");
			sb.append((month==(mon+1))?"selected":"");
			sb.append(">").append(longDF.format(tempDate)).append("</option>");
		}
		sb.append("</select>");
		
		return sb.toString();
	}
	
	private static final String GET_DEPENDENCY_NODES_BY_NODEID="select po.nodeid from pathoutage po left join ipinterface intf on po.criticalpathip=intf.ipaddr where intf.nodeid=?";

    private static final String GET_NODES_IN_PATH = "SELECT DISTINCT pathoutage.nodeid FROM pathoutage, ipinterface WHERE pathoutage.criticalpathip=? AND pathoutage.nodeid=ipinterface.nodeid AND ipinterface.ismanaged!='D' ORDER BY nodeid";

    private static Set<Integer> getAllDependencyNodesByCriticalPath(String criticalpathip) throws SQLException {
	    Set<Integer> allPathNodes = getDependencyNodesByCriticalPath(criticalpathip);
	    Set<Integer> currentNodes=allPathNodes;
	    while (currentNodes.size() > 0) {
	        Set<Integer> nextIterationNodes=new TreeSet<Integer>();
	        for (Integer pathnodeid : currentNodes ) {
	            nextIterationNodes.addAll(getDependencyNodesByNodeid(pathnodeid));
	        }
	        allPathNodes.addAll(nextIterationNodes);
	        currentNodes=nextIterationNodes;
	    }
	    return allPathNodes;        
    }
    

    private static Set<Integer> getDependencyNodesByCriticalPath(String criticalpathip) throws SQLException {
	    Connection conn = Vault.getDbConnection();
	    Set<Integer> pathNodes = new TreeSet<Integer>();
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_NODES_IN_PATH);
            stmt.setString(1, criticalpathip);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pathNodes.add(rs.getInt(1));
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
	    return pathNodes;
        
    }
    
	private static Set<Integer> getAllDependencyNodesByNodeid(int nodeid) throws SQLException {
	    Set<Integer> allPathNodes = getDependencyNodesByNodeid(nodeid);
	    Set<Integer> currentNodes=allPathNodes;
	    while (currentNodes.size() > 0) {
	        Set<Integer> nextIterationNodes=new TreeSet<Integer>();
	        for (Integer pathnodeid : currentNodes ) {
	            nextIterationNodes.addAll(getDependencyNodesByNodeid(pathnodeid));
	        }
	        allPathNodes.addAll(nextIterationNodes);
	        currentNodes=nextIterationNodes;
	    }
	    return allPathNodes;
	}
	
	private static Set<Integer> getDependencyNodesByNodeid(int nodeid) throws SQLException {
	    Connection conn = Vault.getDbConnection();
	    Set<Integer> pathNodes = new TreeSet<Integer>();
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_DEPENDENCY_NODES_BY_NODEID);
            stmt.setInt(1, nodeid);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pathNodes.add(rs.getInt(1));
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
	    
	    return pathNodes;
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
		} catch (Throwable e) {
			throw new ServletException("Could not send event " + event.getUei(), e);
		}
	}
	
%>
<%
	NotifdConfigFactory.init(); //Must do this early on - if it fails, then just throw the exception to the web gui

	// @i18n
	final HashMap<String, String> shortDayNames = new HashMap<String, String>();
	shortDayNames.put("sunday", "Sunday");
	shortDayNames.put("monday", "Monday");
	shortDayNames.put("tuesday", "Tuesday");
	shortDayNames.put("wednesday", "Wednesday");
	shortDayNames.put("thursday", "Thursday");
	shortDayNames.put("friday", "Friday");
	shortDayNames.put("saturday", "Saturday");
	shortDayNames.put("1", "1st");
	shortDayNames.put("2", "2nd");
	shortDayNames.put("3", "3rd");
	shortDayNames.put("4", "4th");
	shortDayNames.put("5", "5th");
	shortDayNames.put("6", "6th");
	shortDayNames.put("7", "7th");
	shortDayNames.put("8", "8th");
	shortDayNames.put("9", "9th");
	shortDayNames.put("10", "10th");
	shortDayNames.put("11", "11th");
	shortDayNames.put("12", "12th");
	shortDayNames.put("13", "13th");
	shortDayNames.put("14", "14th");
	shortDayNames.put("15", "15th");
	shortDayNames.put("16", "16th");
	shortDayNames.put("17", "17th");
	shortDayNames.put("18", "18th");
	shortDayNames.put("19", "19th");
	shortDayNames.put("20", "20th");
	shortDayNames.put("21", "21st");
	shortDayNames.put("22", "22nd");
	shortDayNames.put("23", "23rd");
	shortDayNames.put("24", "24th");
	shortDayNames.put("25", "25th");
	shortDayNames.put("26", "26th");
	shortDayNames.put("27", "27th");
	shortDayNames.put("28", "28th");
	shortDayNames.put("29", "29th");
	shortDayNames.put("30", "30th");
	shortDayNames.put("31", "31st");

	GregorianCalendar today = new GregorianCalendar();
	final int date = today.get(Calendar.DATE);
	final int month = today.get(Calendar.MONTH) + 1;
	final int year = today.get(Calendar.YEAR);

	PollOutagesConfigFactory.init(); //Only init - do *not* reload
	PollOutagesConfigFactory pollFactory = PollOutagesConfigFactory.getInstance();
	Outage theOutage;

	if ("Cancel".equals(request.getParameter("cancelButton"))) {
		response.sendRedirect("index.jsp");
		return;
	}

	String nameParam = request.getParameter("name");
	if (nameParam != null) {
		//first time in - name is passed as a param.  Find the outage, copy it, and shove it in the session
		//Also keep a copy of the name, for later saving (replacing the original with the edited copy)
		Outage tempOutage = pollFactory.getOutage(nameParam);
		CharArrayWriter writer = new CharArrayWriter();
		tempOutage.marshal(writer);
		theOutage = (Outage) Outage.unmarshal(new CharArrayReader(writer.toCharArray()));
		request.getSession().setAttribute("opennms.editoutage", theOutage);
		request.getSession().setAttribute("opennms.editoutage.origname", nameParam);
	} else if ("true".equals(request.getParameter("addNew"))) {
		theOutage = new Outage();
		String nodes[] = request.getParameterValues("nodeID");
		String interfaces[] = request.getParameterValues("ipAddr");


		//Nuke whitespace - it messes with all sorts of things
		theOutage.setName(request.getParameter("newName").trim());
		
		request.getSession().setAttribute("opennms.editoutage", theOutage);
		request.getSession().removeAttribute("opennms.editoutage.origname");
		if (nodes != null) {
			for(int i = 0 ; i < nodes.length; i++ ) {
				int node = WebSecurityUtils.safeParseInt(nodes[i]);
				addNode(theOutage, node);
			}
		}
		if (interfaces != null) {
			for(int i = 0 ; i < interfaces.length; i++ ) {
				org.opennms.netmgt.config.poller.Interface newInterface = new org.opennms.netmgt.config.poller.Interface();
				// hope this has builtin safeParseStuff
				newInterface.setAddress(interfaces[i]);
				addInterface(theOutage, newInterface);
			}
		}
	} else {
		//Neither starting the edit, nor adding a new outage.  
		theOutage = (Outage) request.getSession().getAttribute("opennms.editoutage");
		if (theOutage == null) {
			//No name, and no outage in the session.  Give up
%>
<html>
<body>
<p>
Could not find an outage to edit because no outage name parameter was specified nor is any outage stored in the session. Cannot edit!
</p>
</body>
</html>
<%
	return;

		}
	}
	//Load the initial set of enabled outages from the external configurations
	// This will be overridden by a formSubmission to use the form values, but is necessary for the initial load of the page
	//It is more efficient to piggy back on this initial setup (creating the hashmaps) than doing it separately
	Set<String> enabledOutages = new HashSet<String>();

	// ******* Notification outages config *********
	Collection<String> notificationOutages = NotifdConfigFactory.getInstance().getConfiguration().getOutageCalendarCollection();
	if (notificationOutages.contains(theOutage.getName())) {
		enabledOutages.add("notifications");
	}

	// ******* Threshd outages config *********
	ThreshdConfigFactory.init();
	Map<org.opennms.netmgt.config.threshd.Package, List<String>> thresholdOutages = new HashMap<org.opennms.netmgt.config.threshd.Package, List<String>>();
	for (org.opennms.netmgt.config.threshd.Package thisPackage : ThreshdConfigFactory.getInstance().getConfiguration().getPackage()) {
		thresholdOutages.put(thisPackage, thisPackage.getOutageCalendarCollection());
		if (thisPackage.getOutageCalendarCollection().contains(theOutage.getName())) {
			enabledOutages.add("threshold-" + thisPackage.getName());
		}
	}

	// ******* Polling outages config *********
	PollerConfigFactory.init();
	Map<org.opennms.netmgt.config.poller.Package, List<String>> pollingOutages = new HashMap<org.opennms.netmgt.config.poller.Package, List<String>>();
	for (org.opennms.netmgt.config.poller.Package thisPackage : PollerConfigFactory.getInstance().getConfiguration().getPackage()) {
		pollingOutages.put(thisPackage, thisPackage.getOutageCalendarCollection());
		if (thisPackage.getOutageCalendarCollection().contains(theOutage.getName())) {
			enabledOutages.add("polling-" + thisPackage.getName());
		}
	}

	// ******* Collectd outages config *********
	CollectdConfigFactory.init();
	Map<org.opennms.netmgt.config.collectd.Package, List<String>> collectionOutages = new HashMap<org.opennms.netmgt.config.collectd.Package, List<String>>();
	for (CollectdPackage pkg : CollectdConfigFactory.getInstance().getCollectdConfig().getPackages()) {
		org.opennms.netmgt.config.collectd.Package thisPackage = pkg.getPackage();
		collectionOutages.put(thisPackage, thisPackage.getOutageCalendarCollection());
		if (thisPackage.getOutageCalendarCollection().contains(theOutage.getName())) {
			enabledOutages.add("collect-" + thisPackage.getName());
		}
	}

	if (request.getParameter("deleteOutageType") != null) {
		theOutage.setType(null);
		theOutage.removeAllTime();
	} else {
	    if (request.getParameter("outageType") != null) {
			theOutage.setType(request.getParameter("outageType"));
	    }
	}
	
	String isFormSubmission = request.getParameter("formSubmission");
	if ("true".equals(isFormSubmission)) {

		pollFactory.getWriteLock().lock();
		
		try {

			//Process the form submission - yeah, this should be a servlet, but this is a quick and dirty hack for now
			//It can be tidied up later -- of course, it's been what, almost 3 years?  "later" means 2.0 + rewrite  ;)
			//First, process any changes to the editable inputs
	
			//Now handle any buttons that were clicked.  There should be only one
			//If there is more than one, we use the first and ignore the rest.
			if (request.getParameter("saveButton") != null) {
				//Save was clicked - save 
	
				//Process the notifications status.  NB: we keep an in-memory copy initially, and only save when the save button is clicked
				if ("on".equals(request.getParameter("notifications"))) {
					//Want to turn it on.
					enabledOutages.add("notifications");
				} else {
					//Want to turn off (missing, or set to something other than "on")
					enabledOutages.remove("notifications");
				}
	
				for (org.opennms.netmgt.config.poller.Package thisKey : pollingOutages.keySet()) {
					String name = "polling-" + thisKey.getName();
					System.out.println("Checking " + name);
					if ("on".equals(request.getParameter(name))) {
						System.out.println(" is on - adding to enabledOutages");
						enabledOutages.add(name);
					} else {
						enabledOutages.remove(name);
					}
				}
	
				for (org.opennms.netmgt.config.threshd.Package thisKey : thresholdOutages.keySet()) {
					String name = "threshold-" + thisKey.getName();
					System.out.println("Checking " + name);
					if ("on".equals(request.getParameter(name))) {
						enabledOutages.add(name);
					} else {
						enabledOutages.remove(name);
					}
				}
	
				for (org.opennms.netmgt.config.collectd.Package thisKey : collectionOutages.keySet()) {
					String name = "collect-" + thisKey.getName();
					System.out.println("Checking " + name);
					if ("on".equals(request.getParameter(name))) {
						enabledOutages.add(name);
					} else {
						enabledOutages.remove(name);
					}
				}
	
				//Check if the outage is a new one, or an edited old one
				String origname = (String) request.getSession().getAttribute("opennms.editoutage.origname");
				if (origname == null) {
					//A new outage - just plonk it in place
					pollFactory.addOutage(theOutage);
				} else {
					//An edited outage - replace the old one
					pollFactory.replaceOutage(pollFactory.getOutage(origname), theOutage);
				}
				//Push the enabledOutages into the actual configuration of the various packages
				//Don't do until after we've successfully put the outage into the polloutages configuration (for coherency)
				if (enabledOutages.contains("notifications")) {
					if (!notificationOutages.contains(theOutage.getName())) {
						NotifdConfigFactory.getInstance().getConfiguration().addOutageCalendar(theOutage.getName());
					}
				} else {
					if (notificationOutages.contains(theOutage.getName())) {
						NotifdConfigFactory.getInstance().getConfiguration().removeOutageCalendar(theOutage.getName());
					}
				}
	
				for (org.opennms.netmgt.config.poller.Package thisKey : pollingOutages.keySet()) {
					Collection<String> pollingPackage = pollingOutages.get(thisKey);
					String name = "polling-" + thisKey.getName();
					if (enabledOutages.contains(name)) {
						if (!pollingPackage.contains(theOutage.getName())) {
							thisKey.addOutageCalendar(theOutage.getName());
						}
					} else {
						if (pollingPackage.contains(theOutage.getName())) {
							thisKey.removeOutageCalendar(theOutage.getName());
						}
					}
				}
	
				for (org.opennms.netmgt.config.threshd.Package thisKey : thresholdOutages.keySet()) {
					Collection<String> thresholdPackage = thresholdOutages.get(thisKey);
					String name = "threshold-" + thisKey.getName();
					if (enabledOutages.contains(name)) {
						if (!thresholdPackage.contains(theOutage.getName())) {
							thisKey.addOutageCalendar(theOutage.getName());
						}
					} else {
						if (thresholdPackage.contains(theOutage.getName())) {
							thisKey.removeOutageCalendar(theOutage.getName());
						}
					}
				}
	
				for (org.opennms.netmgt.config.collectd.Package thisKey : collectionOutages.keySet()) {
					Collection<String> collectPackage = collectionOutages.get(thisKey);
					String name = "collect-" + thisKey.getName();
					if (enabledOutages.contains(name)) {
						if (!collectPackage.contains(theOutage.getName())) {
							thisKey.addOutageCalendar(theOutage.getName());
						}
					} else {
						if (collectPackage.contains(theOutage.getName())) {
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
				// RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/sched-outages/index.jsp");
				response.sendRedirect("index.jsp");
				// dispatcher.forward(request, response);
				return;
			} else if (request.getParameter("addNodeButton") != null) {
				String newNode = request.getParameter("newNode");
				if (newNode == null || "".equals(newNode.trim())) {
					// No node was specified
				} else {
					int newNodeId = WebSecurityUtils.safeParseInt(newNode);
					addNode(theOutage, newNodeId);
					if (request.getParameter("addPathOutageNodeRadio") != null) {
						for (Integer pathOutageNodeid: getAllDependencyNodesByNodeid(newNodeId)) {
						    addNode(theOutage,pathOutageNodeid.intValue());
						}
					}

				}
			} else if (request.getParameter("addInterfaceButton") != null) {
				String newIface = request.getParameter("newInterface");
				if (newIface == null || "".equals(newIface.trim())) {
					// No interface was specified
				} else {
					org.opennms.netmgt.config.poller.Interface newInterface = new org.opennms.netmgt.config.poller.Interface();
					newInterface.setAddress(newIface);
					addInterface(theOutage, newInterface);
					if (request.getParameter("addPathOutageInterfaceRadio") != null) {
						for (Integer pathOutageNodeid: getAllDependencyNodesByCriticalPath(newIface)) {
						    addNode(theOutage,pathOutageNodeid.intValue());
						}
					}
				}
			} else if (request.getParameter("matchAny") != null) {
				//To turn on matchAny, all normal nodes and interfaces are removed
				theOutage.removeAllInterface();
				theOutage.removeAllNode();
				theOutage.addInterface(matchAnyInterface);
			} else if (request.getParameter("addOutage") != null && theOutage.getType() != null) {
				if (theOutage.getType().equalsIgnoreCase("specific")) {
					org.opennms.netmgt.config.poller.Time newTime = new org.opennms.netmgt.config.poller.Time();
	
					StringBuffer timeBuffer = new StringBuffer(17);
					timeBuffer.append(request.getParameter("chooseStartDay"));
					timeBuffer.append("-");
					timeBuffer.append(request.getParameter("chooseStartMonth"));
					timeBuffer.append("-");
					timeBuffer.append(request.getParameter("chooseStartYear"));
					timeBuffer.append(" ");
					timeBuffer.append(request.getParameter("chooseStartHour"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseStartMinute"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseStartSecond"));
					newTime.setBegins(timeBuffer.toString());
	
					timeBuffer = new StringBuffer(17);
					timeBuffer.append(request.getParameter("chooseFinishDay"));
					timeBuffer.append("-");
					timeBuffer.append(request.getParameter("chooseFinishMonth"));
					timeBuffer.append("-");
					timeBuffer.append(request.getParameter("chooseFinishYear"));
					timeBuffer.append(" ");
					timeBuffer.append(request.getParameter("chooseFinishHour"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseFinishMinute"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseFinishSecond"));
					newTime.setEnds(timeBuffer.toString());
	
					theOutage.addTime(newTime);
				} else {
					org.opennms.netmgt.config.poller.Time newTime = new org.opennms.netmgt.config.poller.Time();
	
					if (theOutage.getType().equalsIgnoreCase("monthly")) {
						newTime.setDay(request.getParameter("chooseDayOfMonth"));
					} else if (theOutage.getType().equalsIgnoreCase("weekly")) {
						newTime.setDay(request.getParameter("chooseDayOfWeek"));
					}
	
					StringBuffer timeBuffer = new StringBuffer(8);
					timeBuffer.append(request.getParameter("chooseStartHour"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseStartMinute"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseStartSecond"));
					newTime.setBegins(timeBuffer.toString());
					
					timeBuffer = new StringBuffer(8);
					timeBuffer.append(request.getParameter("chooseFinishHour"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseFinishMinute"));
					timeBuffer.append(":");
					timeBuffer.append(request.getParameter("chooseFinishSecond"));
					newTime.setEnds(timeBuffer.toString());
					
					theOutage.addTime(newTime);
				}
			} else {
				//Look for deleteNode or deleteInterface or deleteTime prefix
				List<String> paramList = Collections.list(request.getParameterNames());
				for (String paramName : paramList) {
					if (paramName.startsWith("deleteNode")) {
						String indexStr = paramName.substring("deleteNode".length(), paramName.indexOf("."));
						try {
							int index = WebSecurityUtils.safeParseInt(indexStr);
							theOutage.removeNodeAt(index);
						} catch (NumberFormatException e) {
							//Ignore - nothing we can do
							continue;
						} catch (IndexOutOfBoundsException ioob) {
							//Ignore - it's already removed
							continue;
						}
						break;
					} else if (paramName.startsWith("deleteInterface")) {
						String indexStr = paramName.substring("deleteInterface".length(), paramName.indexOf("."));
						try {
							int index = WebSecurityUtils.safeParseInt(indexStr);
							theOutage.removeInterfaceAt(index);
						} catch (NumberFormatException e) {
							//Ignore - nothing we can do
							continue;
						} catch (IndexOutOfBoundsException ioob) {
							//Ignore - it's already removed
							continue;
						}
						break;
					} else if (paramName.startsWith("deleteTime")) {
						String indexStr = paramName.substring("deleteTime".length(), paramName.indexOf("."));
						try {
							int index = WebSecurityUtils.safeParseInt(indexStr);
							theOutage.removeTime(theOutage.getTime(index));
						} catch (NumberFormatException e) {
							//Ignore - nothing we can do
							continue;
						}
						break;
					}
				}
			}

		} finally {
			pollFactory.getWriteLock().unlock();
		}

	} //end if form submission
	boolean hasMatchAny = theOutage.getInterfaceCollection().contains(matchAnyInterface);
%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Edit Outage" />
	<jsp:param name="headTitle" value="Edit" />
	<jsp:param name="headTitle" value="Scheduled Outages" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb"
		value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
		value="<a href='admin/sched-outages/index.jsp'>Scheduled Outages</a>" />
	<jsp:param name="breadcrumb" value="Edit" />
</jsp:include>

<link type="text/css" href="js/jquery/themes/base/jquery.ui.all.css" rel="stylesheet" />
<script type="text/javascript" src="js/jquery/jquery.js"></script>
<script type="text/javascript" src="js/jquery/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="js/jquery/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="js/jquery/ui/jquery.ui.button.js"></script>
<script type="text/javascript" src="js/jquery/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="js/jquery/ui/jquery.ui.autocomplete.js"></script>
<style type="text/css">
	/* TODO shouldn't be necessary */
	.ui-button { margin-left: -1px; }
	.ui-button-icon-only .ui-button-text { padding: 0em; } 
	.ui-autocomplete-input { margin: 0; padding: 0.12em 0 0.12em 0.20em; }
</style>

<style type="text/css">
TD {
	font-size: 0.8em;
}

UL
{
	margin-top: 0px;
}

P
{
	font-size: 0.8em;
	margin: 0px;
}

LABEL 
{
	font-weight: bold;
	font-size: small;
}
</style>

<script type="text/javascript">
var enabledIds = new Array();
var disabledIds = new Array();

function updateOutageTypeDisplay(selectElement) {
	if (selectElement == null) {
		selectElement = document.getElementById("outageTypeSelector");
	}

	var isSpecific=selectElement.options[selectElement.selectedIndex].value=="specific";
	var isDaily=selectElement.options[selectElement.selectedIndex].value=="daily";
	var isWeekly=selectElement.options[selectElement.selectedIndex].value=="weekly";
	var isMonthly=selectElement.options[selectElement.selectedIndex].value=="monthly";
	
	if (isDaily) {
		enabledIds = new Array("chooseStartSpan", "chooseStartTime", "chooseFinishSpan", "chooseFinishTime");
		disabledIds = new Array("chooseStartDate", "chooseFinishDate", "chooseDay", "chooseDayOfMonth", "chooseDayOfWeek");
	} else if (isWeekly) {
		enabledIds = new Array("chooseStartSpan", "chooseStartTime", "chooseFinishSpan", "chooseFinishTime", "chooseDay", "chooseDayOfWeek");
		disabledIds = new Array("chooseStartDate", "chooseFinishDate", "chooseDayOfMonth");
	} else if (isMonthly) {
		enabledIds = new Array("chooseStartSpan", "chooseStartTime", "chooseFinishSpan", "chooseFinishTime", "chooseDay", "chooseDayOfMonth");
		disabledIds = new Array("chooseStartDate", "chooseFinishDate", "chooseDayOfWeek");
	} else {
		// isSpecific, or nothing's selected
		enabledIds = new Array("chooseStartSpan", "chooseStartDate", "chooseStartTime", "chooseFinishSpan", "chooseFinishDate", "chooseFinishTime");
		disabledIds = new Array("chooseDay", "chooseDayOfMonth", "chooseDayOfWeek");
	}

	for (value in enabledIds) {
		document.getElementById(enabledIds[value]).style.display="inline";
	}
	for (value in disabledIds) {
		document.getElementById(disabledIds[value]).style.display="none";
	}
}

	// Invoke a jQuery function
	(function($) {
		// Create the 'combobox' widget which can widgetize a <select> tag
		$.widget("ui.combobox", {
			_create: function() {
				var self = this;
				// Hide the existing tag
				var select = this.element.hide();
				// Add an autocomplete text field
				var input = $("<input name=\"" + self.options.name + "\">")
					.insertAfter(select)
					.autocomplete({
						source: self.options.jsonUrl,
						delay: 1000,
						change: function(event, ui) {
							if (!ui.item) {
								// remove invalid value, as it didn't match anything
								$(this).val("");
								return false;
							}
							select.val(ui.item.id);
							self._trigger("selected", event, {
								item: select.find("[value='" + ui.item.id + "']")
							});
							
						},
						blur: function(event, ui) {
							if (this("widget").is(":visible")) {
								this("close");
								return;
							}
						},
						minLength: 0
					})
					.addClass("ui-widget ui-widget-content ui-corner-left");

				// Add a dropdown arrow button that will expand the entire list
				$("<button type=\"button\">&nbsp;</button>")
					.attr("tabIndex", -1)
					.attr("title", "Show All Items")
					.insertAfter(input)
					.button({
						icons: {
							primary: "ui-icon-triangle-1-s"
						},
						text: false
					})
					.removeClass("ui-corner-all")
					.addClass("ui-corner-right ui-button-icon")
					.click(function() {
						// close if already visible
						if (input.autocomplete("widget").is(":visible")) {
							input.autocomplete("close");
							return;
						}
						// pass empty string as value to search for, displaying all results
						input.autocomplete("search", "");
						input.focus();
					});
			}
		});

	})(jQuery);

	// Apply the combobox widget to the #newNodeSelect and #newInterfaceSelect elements
	$(function() {
		$("#newNodeSelect").combobox({name:"newNode", jsonUrl: "admin/sched-outages/jsonNodes.jsp"});
		$("#newInterfaceSelect").combobox({name: "newInterface", jsonUrl: "admin/sched-outages/jsonIpInterfaces.jsp"});
	});

</script>

<%
	Enumeration<String> enumList = request.getParameterNames();
	while (enumList.hasMoreElements()) {
		String paramName = enumList.nextElement();
%>
<!--	<%=paramName%>=<%=request.getParameter(paramName)%><br/>  -->
<%
	}
%>

<h2>Editing Outage: <%= theOutage.getName() %></h2>

		<label>Nodes and Interfaces:</label>
			<table class="normal" border="0">
				<tr>
					<th valign="top">Node Labels</th>
					<th valign="top">Interfaces</th>
				</tr>
				<tr>
					<td valign="top">
						<form id="addNode" action="admin/sched-outages/editoutage.jsp" method="post">
							<input type="hidden" name="formSubmission" value="true" />
							<p style="font-weight: bold; margin-bottom: 2px;">Search (max 200 results):</p>
							<div class="ui-widget">
								<select id="newNodeSelect" name="newNodeSelect" style="display: none"></select>
								<input type="radio"  value="addPathOutageDependency" name="addPathOutageNodeRadio"/> Add with path outage dependency
								<input type="submit" value="Add" name="addNodeButton"/>
							</div>
						</form>
						<p style="font-weight: bold; margin: 10px 0px 2px 0px;">Current selection:</p>
						<% {
						if (hasMatchAny) {
							%>
							<p><i>All nodes</i></p>
							<%
						} else { 
							org.opennms.netmgt.config.poller.Node[] outageNodes = theOutage.getNode();

							if (outageNodes.length > 0) {
								%>
								<form id="deleteNodes" action="admin/sched-outages/editoutage.jsp" method="post">
								<input type="hidden" name="formSubmission" value="true" />
								<%
								for (int i = 0; i < outageNodes.length; i++) {
									org.opennms.netmgt.config.poller.Node node = outageNodes[i];
									int nodeId = node.getId();
									out.println("<input type=\"image\" src=\"images/redcross.gif\" name=\"deleteNode" + i + "\" />");
									OnmsNode thisNode = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
									if (thisNode != null) {
										out.println(thisNode.getLabel());
									} else {
										out.println("Node " + nodeId + " is null");
									}
									out.println("<br/>");
								}
								%>
								</form>
								<%
							} else { %>
								<p><i>No specific nodes selected</i></p>
							<% }
						}
						} %>
					</td>
					<td valign="top">
						<form id="addInterface" action="admin/sched-outages/editoutage.jsp" method="post">
							<input type="hidden" name="formSubmission" value="true" />
							<p style="font-weight: bold; margin-bottom: 2px;">Search (max 200 results):</p>
							<div class="ui-widget">
								<select id="newInterfaceSelect" name="newInterfaceSelect" style="display: none"></select>
								<input type="radio"  value="addPathOutageDependency" name="addPathOutageInterfaceRadio"/> Add with path outage dependency
								<input type="submit" value="Add" name="addInterfaceButton"/>
							</div>
						</form>
						<p style="font-weight: bold; margin: 10px 0px 2px 0px;">Current selection:</p>
						<%
						{
						if (hasMatchAny) { %>
							<p><i>All interfaces</i></p>
						<% } else {
							org.opennms.netmgt.config.poller.Interface[] outageInterfaces = theOutage.getInterface();
							if (outageInterfaces.length > 0) { %>
								<form id="deleteInterfaces" action="admin/sched-outages/editoutage.jsp" method="post">
									<input type="hidden" name="formSubmission" value="true" />
									<% for (int i = 0; i < outageInterfaces.length; i++) {
										org.opennms.netmgt.config.poller.Interface iface = outageInterfaces[i];
										String addr = iface.getAddress();
										org.opennms.web.element.Interface[] interfaces = NetworkElementFactory.getInstance(getServletContext()).getInterfacesWithIpAddress(addr);
										if (interfaces.length > 0) {
											for ( org.opennms.web.element.Interface thisInterface : interfaces ) {
												String caption = thisInterface.getIpAddress();
												String thisName = thisInterface.getHostname();
												// If the hostname is different, append it
												if(thisName != null && !thisName.equals(caption)) {
													caption += " (" + thisName + ")";
												}
												// If the interface is unmanaged, append a note
												if (!thisInterface.isManaged()) {
													caption += " (Unmanaged)";
												}
												%>
												<input type="image" src="images/redcross.gif" name="deleteInterface<%=String.valueOf(i)%>" />
												<c:out value="<%=caption%>"/>
												<br/>
											<% }
										} else { %>
											<i>Could not find <c:out value="<%=addr%>"/> in the database</i><br/>
										<% }
									} %>
								</form>
							<% } else { %>
								<p><i>No specific interfaces selected</i></p>
							<% }
							}
						} %>
					</td>
				</tr>
				<% if (!hasMatchAny) { %>
				<tr>
					<td valign="top">
						<script type="text/javascript">
							function verifyAddAll() {
								return confirm("Are you sure you want to add all nodes and interfaces? This will erase the current lists of specific nodes and interfaces.");
							}
						</script>
						<form onsubmit="return verifyAddAll();" id="matchAnyForm" action="admin/sched-outages/editoutage.jsp" method="post">
							<input type="hidden" name="formSubmission" value="true" />
							<input type="submit" name="matchAny" value="Select all nodes and interfaces" />
						</form>
					</td>
				</tr>
				<% } %>
				<% if (!hasMatchAny && theOutage.getInterfaceCount() == 0 && theOutage.getNodeCount() == 0) { %>
					<tr>
						<td colspan="2"><span style="color: #ff0000">You must select at least one node or interface for this scheduled outage.</span></td>
					</tr>
				<% } %>
			</table>
		<form id="editOutage" action="admin/sched-outages/editoutage.jsp" method="post">
		<input type="hidden" name="formSubmission" value="true" />
		<label>Outage Type:</label>
			<table class="normal">
				<tr>
					<td>
						<% if (theOutage.getType() != null) { %>
							<input type="image" src="images/modify.gif" name="deleteOutageType" value="true" /> <%= theOutage.getType() %>
						<% } %>
						<span style="<%= theOutage.getType() == null? "" : "display: none" %>">
							<select id="outageTypeSelector" name="outageType" onChange="updateOutageTypeDisplay(this);">
								<% String outageType = theOutage.getType(); if (outageType == null) { outageType = ""; } %>
								<option value="specific" <%= outageType.equalsIgnoreCase("specific")? "selected=\"selected\"":"" %>>Specific</option>
								<option value="daily"    <%= outageType.equalsIgnoreCase("daily")?    "selected=\"selected\"":"" %>>Daily</option>
								<option value="weekly"   <%= outageType.equalsIgnoreCase("weekly")?   "selected=\"selected\"":"" %>>Weekly</option>
								<option value="monthly"  <%= outageType.equalsIgnoreCase("monthly")?  "selected=\"selected\"":"" %>>Monthly</option>
							</select>
							<input type="submit" value="Set" name="setOutageType" />
						</span>
					</td>
				</tr>
			</table>
		<label>Time:</label>
			<table class="normal">
				<%
				org.opennms.netmgt.config.poller.Time[] outageTimes = theOutage.getTime();
					for (int i = 0; i < outageTimes.length; i++) {
						org.opennms.netmgt.config.poller.Time thisTime = outageTimes[i];
				%>
				<tr>
					<td> <input type="image" src="images/redcross.gif" name="deleteTime<%=i%>" /> </td>
					<td>
						<%
							StringBuffer outputBuffer = new StringBuffer();
							if (thisTime.getDay() != null) {
								if (thisTime.getDay().contains("day")) {
									// weekly
									outputBuffer.append("Every&nbsp;").append(shortDayNames.get(thisTime.getDay())).append(",&nbsp;");
								} else {
									// monthly
									outputBuffer.append("The&nbsp;").append(shortDayNames.get(thisTime.getDay())).append("&nbsp;of&nbsp;Each&nbsp;Month,&nbsp;");
								}
							} else {
								if (thisTime.getBegins().contains("-")) {
									// specific
									outputBuffer.append("One-Time,&nbsp;");
								} else {
									// daily
									outputBuffer.append("Daily,&nbsp;");
								}
							}
							outputBuffer.append("From&nbsp;");
							outputBuffer.append(thisTime.getBegins()).append("&nbsp;Through&nbsp;");
							outputBuffer.append(thisTime.getEnds());
							out.println(outputBuffer.toString());
						%>
					</td>
				</tr>
				<%
					}
				%>
			</table>
			<table class="normal" border="0">
				<tr id="chooseDay" style="display: none">
					<td>
						<span id="chooseDayOfMonth" style="display: none">
							Day:
							<select name="chooseDayOfMonth">
								<% for (int i = 1; i < 32; i++) { %>
								<option value="<%= i %>"><%= shortDayNames.get(Integer.toString(i)) %></option>
								<% } %>
							</select>
						</span>
						<span id="chooseDayOfWeek" style="display: none">
							Day of the week:
							<select name="chooseDayOfWeek">
								<option value="sunday">Sunday</option>
								<option value="monday">Monday</option>
								<option value="tuesday">Tuesday</option>
								<option value="wednesday">Wednesday</option>
								<option value="thursday">Thursday</option>
								<option value="friday">Friday</option>
								<option value="saturday">Saturday</option>
							</select>
						</span>
					</td>
				</tr>
				<tr id="chooseStartSpan" style="display: none">
					<td>
						<span id="chooseStartDate" style="display: none">
							<%= getNumberSelectField("chooseStartDay", 1, 31, date, 2) %>
							<%= getMonthSelectField("chooseStartMonth", month) %>
							<%= getNumberSelectField("chooseStartYear", (year - 1), (year + 4), year, 4) %>
						</span>
						<span id="chooseStartTime" style="display: none">
							<%=
								getNumberSelectField("chooseStartHour", 0, 23, 0, 2)
							%>:<%=
								getNumberSelectField("chooseStartMinute", 0, 59, 0, 2)
							%>:<%=
								getNumberSelectField("chooseStartSecond", 0, 59, 0, 2)
							%>
						</span>
					</td>
				</tr>
				<tr id="chooseFinishSpan" style="display: none">
					<td>
						<span id="chooseFinishDate" style="display: none">
							<%= getNumberSelectField("chooseFinishDay", 1, 31, date, 2) %>
							<%= getMonthSelectField("chooseFinishMonth", month) %>
							<%= getNumberSelectField("chooseFinishYear", (year - 1), (year + 4), year, 4) %>
						</span>
						<span id="chooseFinishTime" style="display: none">
							<%=
								getNumberSelectField("chooseFinishHour", 0, 23, 23, 2)
							%>:<%=
								getNumberSelectField("chooseFinishMinute", 0, 59, 59, 2)
							%>:<%=
								getNumberSelectField("chooseFinishSecond", 0, 59, 59, 2)
							%>
						</span>
					</td>
				</tr>
				<tr>
					<td>
						<input type="submit" value="Add Outage" name="addOutage" />
					</td>
				</tr>
				<% if (theOutage.getTimeCount() == 0) { %>
					<tr>
						<td><span style="color: #ff0000">You must have at least one time span defined.</span></td>
					</tr>
				<% } %>
			</table>
		<label>Applies To:</label>
			<ul class="treeview">
				<li>
					<p>Notifications:</p>
					<ul>
						<li><input type="checkbox" <%=(enabledOutages.contains("notifications"))?"checked=\"checked\"":""%> name="notifications" id="notifications"/> <label for="notifications">All Notifications</label> </li>
					</ul>
				</li>
				<li>
					<p>Status Polling:</p>
					<ul>
						<% for (org.opennms.netmgt.config.poller.Package thisKey : pollingOutages.keySet()) {
								String name = "polling-" + thisKey.getName();
								%>
								<li><input type="checkbox" name="<%=name%>" <%=enabledOutages.contains(name)?"checked=\"checked\"":""%> id="<%=name%>"/> <label for="<%=name%>"><%= thisKey.getName() %></label> </li>
						<% } %>
					</ul>
				</li>
				<li>
					<p>Threshold Checking:</p>
					<ul>
						<% for (org.opennms.netmgt.config.threshd.Package thisKey : thresholdOutages.keySet()) {
								String name = "threshold-" + thisKey.getName();
								%>
								<li><input type="checkbox" name="<%=name%>" <%=enabledOutages.contains(name)?"checked=\"checked\"":""%> id="<%=name%>"/> <label for="<%=name%>"><%= thisKey.getName() %></label> </li>
						<% } %>
					</ul>
				</li>
				<li>
					<p>Data Collection:</p>
					<ul>
						<% for (org.opennms.netmgt.config.collectd.Package thisKey : collectionOutages.keySet()) {
								String name = "collect-" + thisKey.getName();
								%>
								<li><input type="checkbox" name="<%=name%>" <%=enabledOutages.contains(name)?"checked=\"checked\"":""%> id="<%=name%>"/> <label for="<%=name%>"><%= thisKey.getName() %></label> </li>
						<% } %>
					</ul>
				</li>
			</ul>
			<%
				if (theOutage != null
						&& theOutage.getTimeCount() > 0
						&& theOutage.getType() != null
						&& (hasMatchAny || (theOutage.getInterfaceCount() > 0) || (theOutage.getNodeCount() > 0))
						) {
			%><input type="submit" value="Save" name="saveButton" /><% } %>
			<input type="submit" value="Cancel" name="cancelButton" />
</form>

<script type="text/javascript">
updateOutageTypeDisplay(null);
</script>

<jsp:include page="/includes/footer.jsp" flush="true" />
