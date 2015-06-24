/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.ksc.add.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

public final class GraphInfo {
    private String m_report     = null;
    private String m_title      = null;
    private String m_resourceId = null;
    private String m_timespan   = null;

    public GraphInfo(final Element elem) {
        // required
        m_report               = elem.getAttribute("reportName");
        m_resourceId           = elem.getAttribute("resourceId");
        m_timespan             = elem.getAttribute("timespan");
        if (m_report == null) {
            GWT.log("element missing the required report tag!");
        }
        if (m_resourceId == null) {
            GWT.log("element missing the required resourceId tag!");
        }
        if (m_timespan == null) {
            GWT.log("element missing the required timespan tag!");
        }

        // optional
        m_title                = elem.getAttribute("graphTitle");
        
        if ("lastday".equals(m_timespan)) {
            m_timespan = "1_day";
        } else if ("lastweek".equals(m_timespan)) {
            m_timespan = "7_day";
        } else if ("lastmonth".equals(m_timespan)) {
            m_timespan = "1_month";
        } else if ("lastyear".equals(m_timespan)) {
            m_timespan = "1_year";
        } else {
            GWT.log("invalid timespan '" + m_timespan + "', using 7_day");
        }
    }

    public String getReportName() {
        return m_report;
    }
    
    public String getResourceId() {
        return m_resourceId;
    }
    
    public String getTimespan() {
        return m_timespan;
    }

    public String getTitle() {
        return m_title;
    }
}