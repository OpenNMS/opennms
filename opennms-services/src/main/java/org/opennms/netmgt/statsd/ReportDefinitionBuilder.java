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
package org.opennms.netmgt.statsd;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.config.statsd.model.PackageReport;
import org.opennms.netmgt.config.statsd.model.Report;
import org.opennms.netmgt.config.statsd.model.StatsdPackage;
import org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>ReportDefinitionBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportDefinitionBuilder implements InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReportDefinitionBuilder.class);
    
    private StatisticsDaemonConfigDao m_statsdConfigDao;
    
    /**
     * <p>reload</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    public void reload() throws DataAccessResourceFailureException {
        m_statsdConfigDao.reloadConfiguration();
        
    }

    /**
     * Builds and schedules all reports enabled in the statsd-configuration.
     * This method has the capability to throw a ton of exceptions, just generically throwing <code>Exception</code>
     *
     * @return a <code>Collection</code> of enabled reports from the statsd-configuration.
     * @throws java.lang.Exception if any.
     */
    public Collection<ReportDefinition> buildReportDefinitions() throws Exception {
        Set<ReportDefinition> reportDefinitions = new HashSet<>();
        
        for (StatsdPackage pkg : m_statsdConfigDao.getPackages()) {
            for (PackageReport packageReport : pkg.getReports()) {
                Report report = packageReport.getReport();

                if (!packageReport.isEnabled()) {
                    LOG.debug("skipping report '{}' in package '{}' because the report is not enabled", report.getName(), pkg.getName());
                    continue;
                }
                
                Class<? extends AttributeStatisticVisitorWithResults> clazz;
                try {
                    clazz = createClassForReport(report);
                } catch (ClassNotFoundException e) {
                    throw new DataAccessResourceFailureException("Could not find class '" + report.getClassName() + "'; nested exception: " + e, e);
                }
                
                Assert.isAssignable(AttributeStatisticVisitorWithResults.class, clazz, "the class specified by class-name in the '" + report.getName() + "' report does not implement the interface " + AttributeStatisticVisitorWithResults.class.getName() + "; ");
                
                ReportDefinition reportDef = new ReportDefinition();
                reportDef.setReport(packageReport);
                reportDef.setReportClass(clazz);
                
                BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(reportDef);
                try {
                    bw.setPropertyValues(packageReport.getAggregateParameters());
                } catch (BeansException e) {
                    LOG.error("Could not set properties on report definition: {}", e.getMessage(), e);
                }
                
                reportDef.afterPropertiesSet();

                reportDefinitions.add(reportDef);
            }
        }
        
        return reportDefinitions;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AttributeStatisticVisitorWithResults> createClassForReport(Report report) throws ClassNotFoundException {
        return (Class<? extends AttributeStatisticVisitorWithResults>) Class.forName(report.getClassName());
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_statsdConfigDao != null, "property statsdConfigDao must be set to a non-null value");
    }

    /**
     * <p>getStatsdConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao} object.
     */
    public StatisticsDaemonConfigDao getStatsdConfigDao() {
        return m_statsdConfigDao;
    }

    /**
     * <p>setStatsdConfigDao</p>
     *
     * @param statsdConfigDao a {@link org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao} object.
     */
    public void setStatsdConfigDao(StatisticsDaemonConfigDao statsdConfigDao) {
        m_statsdConfigDao = statsdConfigDao;
    }
}
