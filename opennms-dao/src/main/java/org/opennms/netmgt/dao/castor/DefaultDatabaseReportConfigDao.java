//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: October 5th, 2009 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao.castor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.databaseReports.DatabaseReports;
import org.opennms.netmgt.config.databaseReports.Report;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;

public class DefaultDatabaseReportConfigDao extends AbstractCastorConfigDao<DatabaseReports, List<Report>>
        implements DatabaseReportConfigDao {
    
    public DefaultDatabaseReportConfigDao() {
        super(DatabaseReports.class, "Database Report Configuration");
    }

    @Override
    public List<Report> translateConfig(DatabaseReports castorConfig) {
        return Collections.unmodifiableList(castorConfig.getReportCollection());
    }
    
    
    public String getReportService(String name) {
        
        Report report = getReport(name);
        
        if(report != null){
            return report.getReportService();
        } else {
        return new String();
        }
        
    }
    
    public String getDisplayName(String name) {
        
        Report report = getReport(name);
        
        if(report != null){
            return report.getDisplayName();
        } else {
        return new String();
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

    public List<Report> getReports() {
        
        return getContainer().getObject();
    
    }
    
    public List<Report> getOnlineReports() {
        
        List<Report> onlineReports = new ArrayList<Report>();
        
        for(Report report : getContainer().getObject()) {
            if (report.isOnline()) {
                onlineReports.add(report);
            }
        }
        
        return onlineReports;
        
    }
    
}
