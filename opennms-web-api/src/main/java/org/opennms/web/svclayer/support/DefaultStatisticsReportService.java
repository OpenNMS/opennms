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
package org.opennms.web.svclayer.support;

import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.web.svclayer.StatisticsReportService;
import org.opennms.web.svclayer.model.StatisticsReportCommand;
import org.opennms.web.svclayer.model.StatisticsReportModel;
import org.opennms.web.svclayer.model.StatisticsReportModel.Datum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;

/**
 * Web service layer implementation for statistics reports.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultStatisticsReportService implements StatisticsReportService, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultStatisticsReportService.class);

    private StatisticsReportDao m_statisticsReportDao;
    private ResourceDao m_resourceDao;

    /**
     * <p>getStatisticsReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<StatisticsReport> getStatisticsReports() {
        return m_statisticsReportDao.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public StatisticsReportModel getReport(StatisticsReportCommand command, BindingResult errors) {
        StatisticsReportModel model = new StatisticsReportModel();
        model.setErrors(errors);
        
        if (errors.hasErrors()) {
            return model;
        }
        
        Assert.notNull(command.getId(), "id property on command object cannot be null");
        
        StatisticsReport report = m_statisticsReportDao.load(command.getId());
        model.setReport(report);
        
        m_statisticsReportDao.initialize(report);
        final Set<StatisticsReportData> data = report.getData();
        m_statisticsReportDao.initialize(data);
        
        for (StatisticsReportData reportDatum : data) {
            Datum d = new Datum();
            d.setValue(reportDatum.getValue());
            OnmsResource resource = m_resourceDao.getResourceById(ResourceId.fromString(reportDatum.getResourceId()));
            if (resource == null) {
                LOG.warn("Could not find resource for statistics report: {}", reportDatum.getResourceId());
            } else {
                d.setResource(resource);
            }
            model.addData(d);
        }
        
        return model;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_statisticsReportDao != null, "property statisticsReportDao must be set to a non-null value");
        Assert.state(m_resourceDao != null, "property resourceDao must be set to a non-null value");
    }

    /**
     * <p>getStatisticsReportDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.StatisticsReportDao} object.
     */
    public StatisticsReportDao getStatisticsReportDao() {
        return m_statisticsReportDao;
    }

    /**
     * <p>setStatisticsReportDao</p>
     *
     * @param statisticsReportDao a {@link org.opennms.netmgt.dao.api.StatisticsReportDao} object.
     */
    public void setStatisticsReportDao(StatisticsReportDao statisticsReportDao) {
        m_statisticsReportDao = statisticsReportDao;
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
}
