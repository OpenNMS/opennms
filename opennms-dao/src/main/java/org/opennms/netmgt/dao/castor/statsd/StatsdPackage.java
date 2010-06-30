/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor.statsd;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a package that selects nodes and lists reports to
 * run on them.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see PackageReport
 * @version $Id: $
 */
public class StatsdPackage {
    private String m_name;
    private String m_filter;
    private List<PackageReport> m_reports = new ArrayList<PackageReport>();
    
    /**
     * <p>getFilter</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilter() {
        return m_filter;
    }
    /**
     * <p>setFilter</p>
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void setFilter(String filter) {
        m_filter = filter;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PackageReport> getReports() {
        return m_reports;
    }
    /**
     * <p>setReports</p>
     *
     * @param reports a {@link java.util.List} object.
     */
    public void setReports(List<PackageReport> reports) {
        m_reports = reports;
    }
    /**
     * <p>addReport</p>
     *
     * @param report a {@link org.opennms.netmgt.dao.castor.statsd.PackageReport} object.
     */
    public void addReport(PackageReport report) {
        m_reports.add(report);
    }
}
