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
package org.opennms.reporting.core.svclayer.support;


import org.opennms.api.reporting.ReportService;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportServiceLocatorException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>DefaultReportServiceLocator class.</p>
 */
public class DefaultReportServiceLocator implements ApplicationContextAware, ReportServiceLocator {

    private ApplicationContext m_applicationContext;

    private GlobalReportRepository m_globalReportRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportService getReportService(final String reportServiceName) throws ReportServiceLocatorException {
        try {
            return m_applicationContext.getBean(reportServiceName, ReportService.class);
        } catch (final BeansException e) {
            throw new ReportServiceLocatorException("cannot locate report service bean: " + reportServiceName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportService getReportServiceForId(String reportId)
            throws ReportServiceLocatorException {

        return getReportService(m_globalReportRepository.getReportService(reportId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        m_applicationContext = applicationContext;
    }

    public void setGlobalReportRepository(GlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}
