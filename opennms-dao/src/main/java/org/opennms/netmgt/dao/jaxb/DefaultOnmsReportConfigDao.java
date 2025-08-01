/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
