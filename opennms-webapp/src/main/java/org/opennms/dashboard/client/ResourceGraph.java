/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Image;

/**
 * <p>ResourceGraph class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ResourceGraph extends Image {
    /**
     * <p>Constructor for ResourceGraph.</p>
     */
    public ResourceGraph() {
        super();
    }

    /**
     * <p>displayNoGraph</p>
     */
    public void displayNoGraph() {
        setUrl("images/rrd/error.png");
    }
    
    /**
     * <p>setGraph</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param reportName a {@link java.lang.String} object.
     * @param start a {@link java.lang.String} object.
     * @param end a {@link java.lang.String} object.
     */
    public void setGraph(String resourceId, String reportName, String start, String end) {
        setUrl(buildGraphUrl(resourceId, reportName, start, end));
    }
    
    /**
     * <p>prefetchGraph</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param reportName a {@link java.lang.String} object.
     * @param start a {@link java.lang.String} object.
     * @param end a {@link java.lang.String} object.
     */
    public void prefetchGraph(String resourceId, String reportName, String start, String end) {
        Image.prefetch(buildGraphUrl(resourceId, reportName, start, end));
    }

    private String buildGraphUrl(String resourceId, String report, String start, String end) {
        return "graph/graph.png?resourceId=" + URL.encodeQueryString(resourceId) + "&report=" + URL.encodeQueryString(report) + "&start=" + start + "&end=" + end;
    }
}
