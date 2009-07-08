/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.dashboard.client;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ResourceGraph extends Image {
    public ResourceGraph() {
        super();
    }

    public void displayNoGraph() {
        setUrl("images/rrd/error.png");
    }
    
    public void setGraph(String resourceId, String reportName, String start, String end) {
        setUrl(buildGraphUrl(resourceId, reportName, start, end));
    }
    
    public void prefetchGraph(String resourceId, String reportName, String start, String end) {
        Image.prefetch(buildGraphUrl(resourceId, reportName, start, end));
    }

    private String buildGraphUrl(String resourceId, String report, String start, String end) {
        return "graph/graph.png?resourceId=" + URL.encodeComponent(resourceId) + "&report=" + URL.encodeComponent(report) + "&start=" + start + "&end=" + end;
    }
}