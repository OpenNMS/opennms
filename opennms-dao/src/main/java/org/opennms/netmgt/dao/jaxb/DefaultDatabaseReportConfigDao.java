/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.features.reporting.model.LocalReports;
import org.opennms.features.reporting.model.Report;
import org.opennms.netmgt.dao.api.DatabaseReportConfigDao;

public class DefaultDatabaseReportConfigDao extends AbstractJaxbConfigDao<LocalReports, List<Report>>
        implements DatabaseReportConfigDao {

    /**
     * <p>Constructor for DefaultDatabaseReportConfigDao.</p>
     */
    public DefaultDatabaseReportConfigDao() {
        super(LocalReports.class, "Database Report Configuration");
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> translateConfig(LocalReports reports) {
        return Collections.unmodifiableList(reports.getReportList());
    }

    /** {@inheritDoc} */
    @Override
    public String getReportService(String name) {
        
        Report report = getReport(name);
        
        if(report != null){
            return report.getReportService();
        } else {
        return "";
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDisplayName(String name) {
        
        Report report = getReport(name);
        
        if(report != null){
            return report.getDisplayName();
        } else {
        	return "";
        }
        
    }

    private Report getReport(String name) {
        
        for(Report report : getContainer().getObject()) {
            if (name.equals(report.getId())) {
                return report;
            }
        }
        
        return null;
        
    }

    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Report> getReports() {
        
        return getContainer().getObject();
    
    }
    
    /**
     * <p>getOnlineReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Report> getOnlineReports() {
        
        List<Report> onlineReports = new ArrayList<>();
        for(Report report : getContainer().getObject()) {
            if (report.isOnline()) {
                onlineReports.add(report);
            }
        }
        
        return onlineReports;
        
    }
    
}
