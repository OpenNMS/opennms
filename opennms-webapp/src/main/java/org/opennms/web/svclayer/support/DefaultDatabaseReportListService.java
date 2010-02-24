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
// Created: October 5th, 2009
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
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.databaseReports.Report;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.web.svclayer.DatabaseReportListService;

public class DefaultDatabaseReportListService implements
        DatabaseReportListService {
    
    DatabaseReportConfigDao m_dao;

    public List<DatabaseReportDescription> getAll() {
        
        List <DatabaseReportDescription> allReports = new ArrayList<DatabaseReportDescription>();
        
        for(Report report : m_dao.getReports()) {
            DatabaseReportDescription summary = new DatabaseReportDescription();
            summary.setId(report.getId());
            summary.setDisplayName(report.getDisplayName());
            summary.setDescription(report.getDescription());
            allReports.add(summary);
        }
        
        return allReports;
        
    }

    public List<DatabaseReportDescription> getAllOnline() {

        List <DatabaseReportDescription> onlineReports = new ArrayList<DatabaseReportDescription>();
        
        for(Report report : m_dao.getOnlineReports()) {
            DatabaseReportDescription summary = new DatabaseReportDescription();
            summary.setId(report.getId());
            summary.setDisplayName(report.getDisplayName());
            summary.setDescription(report.getDescription());
            onlineReports.add(summary);
        }
        
        return onlineReports;
    }
   

    public void setDatabaseReportConfigDao(DatabaseReportConfigDao dao) {
        
        m_dao = dao;

    }

}
