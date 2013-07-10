/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.reporting.jasperReports.JasperReports;
import org.opennms.netmgt.config.reporting.jasperReports.Report;
import org.opennms.netmgt.dao.api.JasperReportConfigDao;

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
    @Override
    public String getEngine(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getEngine();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
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
