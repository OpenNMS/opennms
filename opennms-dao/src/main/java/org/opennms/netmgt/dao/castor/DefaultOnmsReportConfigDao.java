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

import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.netmgt.config.reporting.StringParm;
import org.opennms.netmgt.config.reporting.opennms.OpennmsReports;
import org.opennms.netmgt.config.reporting.opennms.Report;
import org.opennms.netmgt.dao.api.OnmsReportConfigDao;

public class DefaultOnmsReportConfigDao extends AbstractCastorConfigDao<OpennmsReports, List<Report>>
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
            return report.getHtmlTemplate();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getPdfStylesheetLocation(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getPdfTemplate();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getSvgStylesheetLocation(String id) {
        Report report = getReport(id);
        if (report != null) {
            return report.getSvgTemplate();
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
        
        Parameters parameters = null;
        Report report = getReport(id);
        if (report != null) {
            parameters = report.getParameters();
        }
        
        return parameters;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public DateParm[] getDateParms(String id) {
        
        DateParm[] dateParms = null;
        Report report = getReport(id);
        if (report != null) {
            dateParms = report.getParameters().getDateParm();
        }
        
        return dateParms;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public StringParm[] getStringParms(String id) {
        
        StringParm[] stringParms = null;
        Report report = getReport(id);
        if (report != null) {
            stringParms = report.getParameters().getStringParm();
        }
        
        return stringParms;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public IntParm[] getIntParms(String id) {
        
        IntParm[] intParms = null;
        Report report = getReport(id);
        if (report != null) {
            intParms = report.getParameters().getIntParm();
        }
        
        return intParms;
        
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> translateConfig(OpennmsReports castorConfig) {
        return Collections.unmodifiableList(castorConfig.getReportCollection());
    }
    
}
