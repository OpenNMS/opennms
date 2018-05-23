/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import java.util.Collections;
import java.util.List;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.opennms.netmgt.config.reporting.OpennmsReports;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.netmgt.config.reporting.Report;
import org.opennms.netmgt.config.reporting.StringParm;
import org.opennms.netmgt.dao.api.OnmsReportConfigDao;

public class DefaultOnmsReportConfigDao extends AbstractJaxbConfigDao<OpennmsReports, List<Report>>
implements OnmsReportConfigDao {
    
    /**
     * <p>Constructor for DefaultOnmsReportConfigDao.</p>
     */
    public DefaultOnmsReportConfigDao() {
        super(OpennmsReports.class, "OpenNMS Report Configuration");
    }

    /** {@inheritDoc} */
    @Override
    public String getHtmlStylesheetLocation(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getHtmlTemplate().orElse(null);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getPdfStylesheetLocation(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getPdfTemplate().orElse(null);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getSvgStylesheetLocation(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getSvgTemplate().orElse(null);
        }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getLogo(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getLogo();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getType();
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
    public Parameters getParameters(String id) {
        final Report report = getReport(id);
        if (report != null) {
            return report.getParameters().orElse(null);
        }
        
        return null;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public List<DateParm> getDateParms(String id) {
        final Report report = getReport(id);
        if (report != null && report.getParameters().isPresent()) {
            return report.getParameters().get().getDateParms();
        }
        
        return null;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public List<StringParm> getStringParms(String id) {
        final Report report = getReport(id);
        if (report != null && report.getParameters().isPresent()) {
            return report.getParameters().get().getStringParms();
        }
        
        return null;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public List<IntParm> getIntParms(String id) {
        final Report report = getReport(id);
        if (report != null && report.getParameters().isPresent()) {
            return report.getParameters().get().getIntParms();
        }
        
        return null;
        
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> translateConfig(OpennmsReports config) {
        return Collections.unmodifiableList(config.getReports());
    }
    
}
