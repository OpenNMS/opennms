<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2002 Nov 12: Add response time graphs to webUI.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%><%@page
	language="java"
	contentType="text/plain"
	session="true"
	import="org.opennms.web.rss.*"
%><%!
	private Feed feed;
	private String output = "";
%><%
	String feedName = request.getParameter("feed");
	String feedType = request.getParameter("type");
	if (feedType == null) {
	    feedType = "atom_1.0";
	}
	if (feedName != null) {
	    String className = feedName.toLowerCase();
        className = "org.opennms.web.rss." + Character.toUpperCase(className.charAt(0)) + className.substring(1) + "Feed";
    	
        try {
            feed = (Feed)Class.forName(className).newInstance();
            String urlBase = request.getRequestURL().toString();
            urlBase = urlBase.substring(0, urlBase.lastIndexOf("/") + 1);
    		feed.setUrlBase(urlBase);
    		feed.setFeedType(feedType);
    		feed.setRequest(request);
    		out.println(feed.render());
        } catch (NoClassDefFoundError e) {
            throw new Exception("unable to locate class for " + className);
        }
	}
%>