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
// Created: March 9th, 2010 jonathan@opennms.org
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

import org.opennms.netmgt.config.reporting.jasperReports.JasperReports;
import org.opennms.netmgt.config.reporting.jasperReports.Report;
import org.opennms.netmgt.dao.JasperReportConfigDao;

/**
 * <p>DefaultJasperReportConfigDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultJasperReportConfigDao extends
        AbstractCastorConfigDao<JasperReports, List<Report>> implements
        JasperReportConfigDao {

    /**
     * <p>Constructor for DefaultJasperReportConfigDao.</p>
     */
    public DefaultJasperReportConfigDao() {
        super(JasperReports.class, "JasperReports configuration");
    }

    /** {@inheritDoc} */
    public String getEngine(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getEngine();
        }
        return null;
    }

    /** {@inheritDoc} */
    public String getTemplateLocation(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getTemplate();
        }
        return null;
    }

    private Report getReport(String id) {
        for (Report report : getContainer().getObject()) {
            if (id.equals(report.getId())) {
                return report;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> translateConfig(JasperReports castorConfig) {
        return Collections.unmodifiableList(castorConfig.getReportCollection());
    }
    
}
