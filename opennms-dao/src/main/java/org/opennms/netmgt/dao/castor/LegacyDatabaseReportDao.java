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
package org.opennms.netmgt.dao.castor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.databaseReports.ReportParm;
import org.opennms.netmgt.config.databaseReports.legacy.LegacyDatabaseReports;
import org.opennms.netmgt.config.databaseReports.legacy.Report;
import org.opennms.netmgt.dao.DatabaseReportDao;

public class LegacyDatabaseReportDao extends AbstractCastorConfigDao<LegacyDatabaseReports, List<Report>>
        implements DatabaseReportDao {
    
    public LegacyDatabaseReportDao() {
        super(LegacyDatabaseReports.class, "Legacy Database Report Configuration");
    }

    
    
    @Override
    public List<Report> translateConfig(LegacyDatabaseReports castorConfig) {
        return Collections.unmodifiableList(castorConfig.getReportCollection());
    }



    public List<ReportParm> getParmsByName(String reportName) {
        
        for (Report report : getContainer().getObject()) {
            if (reportName.equals(report.getName())) {
                return report.getReportParmCollection();
            }
        }
        
        // Return an empty list if the report does not exist.
        
        return new ArrayList<ReportParm>();
        
    }



    public List<String> getNames() {
        List<String> reports = new ArrayList<String>();
        for (Report report : getContainer().getObject()) {
            reports.add(report.getName());
        }   
        return reports;
    }

}
