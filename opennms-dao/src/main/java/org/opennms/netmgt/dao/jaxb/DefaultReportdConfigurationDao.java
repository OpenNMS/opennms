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

import java.util.List;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.dao.api.ReportdConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>DefaultReportdConfigurationDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultReportdConfigurationDao extends AbstractJaxbConfigDao<ReportdConfiguration, ReportdConfiguration> implements ReportdConfigurationDao {

    /**
     * <p>Constructor for DefaultReportdConfigurationDao.</p>
     */
    public DefaultReportdConfigurationDao() {
        super(ReportdConfiguration.class, "Reportd Configuration");
    }
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     */
    @Override
    public ReportdConfiguration getConfig() {
        return getContainer().getObject();
    }
    
    //@Override
    /**
     * <p>translateConfig</p>
     *
     * @param config a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     * @return a {@link org.opennms.netmgt.config.reportd.ReportdConfiguration} object.
     */
    @Override
    public ReportdConfiguration translateConfig(ReportdConfiguration config) {
        return config;
    }
    
    
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
    }
    
    /** {@inheritDoc} */
    @Override
    public Report getReport(String reportName) {
        for (Report report : getReports()) {
            if (report.getReportName().equals(reportName)) {
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
        return getConfig().getReports();
    }

    /**
     * <p>getPersistFlag</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean getPersistFlag() {
        return getConfig().getPersistReports().booleanValue();
    }

    /**
     * <p>getStorageDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStorageDirectory() {
        return getConfig().getStorageLocation();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean deleteReport(String report){
        return getConfig().removeReport(getReport(report));
    }
    
        
}
