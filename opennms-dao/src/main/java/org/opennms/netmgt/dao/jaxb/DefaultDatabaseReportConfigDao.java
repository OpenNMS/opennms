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
