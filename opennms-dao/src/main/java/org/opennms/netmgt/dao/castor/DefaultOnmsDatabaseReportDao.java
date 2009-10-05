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
package org.opennms.netmgt.dao.castor;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.databaseReports.opennms.Report;
import org.opennms.netmgt.config.databaseReports.opennms.OpennmsDatabaseReports;
import org.opennms.netmgt.dao.OnmsDatabaseReportDao;

public class DefaultOnmsDatabaseReportDao extends AbstractCastorConfigDao<OpennmsDatabaseReports, List<Report>>
implements OnmsDatabaseReportDao {
    
    public DefaultOnmsDatabaseReportDao() {
        super(OpennmsDatabaseReports.class, "Database Report Configuration");
    }

    public String getHtmlStylesheetLocation(String OnmsReportName) {
        Report report = getReport(OnmsReportName);
        if (report != null) {
            return report.getHtmlTemplate();
        }
        return null;
    }

    public String getPdfStylesheetLocation(String OnmsReportName) {
        Report report = getReport(OnmsReportName);
        if (report != null) {
            return report.getPdfTemplate();
        }
        return null;
    }

    public String getSvgStylesheetLocation(String OnmsReportName) {
        Report report = getReport(OnmsReportName);
        if (report != null) {
            return report.getSvgTemplate();
        }
        return null;
    }

    @Override
    public List<Report> translateConfig(OpennmsDatabaseReports castorConfig) {
        return Collections.unmodifiableList(castorConfig.getReportCollection());
    }

    public String getType(String OnmsReportName) {
        Report report = getReport(OnmsReportName);
        if (report != null) {
            return report.getType();
        }
        return null;
    }
    
    private Report getReport(String OnmsReportName) {
        for (Report report : getContainer().getObject()) {
            if (OnmsReportName.equals(report.getReportName())) {
                return report;
            }
        }
        
        return null;
        
    }

}
