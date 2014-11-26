<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page
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
