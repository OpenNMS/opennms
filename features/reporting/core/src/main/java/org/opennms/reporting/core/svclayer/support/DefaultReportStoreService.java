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

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportService;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.core.logging.Logging;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * <p>DefaultReportStoreService class.</p>
 */
public class DefaultReportStoreService implements ReportStoreService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultReportStoreService.class);
    
    private ReportCatalogDao m_reportCatalogDao;
    private ReportServiceLocator m_reportServiceLocator;
    
    private GlobalReportRepository m_globalReportRepository;
    
    private static final String LOG4J_CATEGORY = "reports";
    
    /**
     * <p>Constructor for DefaultReportStoreService.</p>
     */
    public DefaultReportStoreService () {
    }

    /**
     * <p>delete</p>
     *
     * @param ids an array of {@link java.lang.Integer} objects.
     */
    @Override
    public void delete(final Integer[] ids) {
        for (final Integer id : ids) {
            delete(id); 
        }
    }
    
    /**
     * <p>delete</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    @Override
    public void delete(final Integer id) {
        Logging.withPrefix(LOG4J_CATEGORY, new Runnable() {
            @Override public void run() {
                final String deleteFile = m_reportCatalogDao.get(id).getLocation();
                final boolean success = (new File(deleteFile).delete());
                if (success) {
                    LOG.debug("deleted report XML file: {}", deleteFile);
                } else {
                    LOG.warn("unable to delete report XML file: {} will delete reportCatalogEntry anyway", deleteFile);
                }
                m_reportCatalogDao.delete(id);
            }
        });
    }

    /**
     * <p>getAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<ReportCatalogEntry> getAll() {
        final Criteria onmsCrit = new Criteria(ReportCatalogEntry.class);
        onmsCrit.setOrders(Arrays.asList(new Order[] {
            Order.desc("date")
        }));
        return m_reportCatalogDao.findMatching(onmsCrit);
    }

    @Override
    public long countAll() {
        return m_reportCatalogDao.countAll();
    }

    @Override
    public List<ReportCatalogEntry> getPage(int offset, int limit) {
        final Criteria criteria = new Criteria(ReportCatalogEntry.class);
        criteria.setOrders(Lists.newArrayList(Order.desc("date")));
        criteria.setOffset(offset);
        criteria.setLimit(limit);
        return m_reportCatalogDao.findMatching(criteria);
    }
    
    /**
     * <p>getFormatMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, Object> getFormatMap() {
        final HashMap <String, Object> formatMap = new HashMap<String, Object>();
        for (final BasicReportDefinition report : m_globalReportRepository.getAllReports()) {
            final List <ReportFormat> formats = m_reportServiceLocator.getReportService(report.getReportService()).getFormats(report.getId());
            formatMap.put(report.getId(), formats);
        }
        return formatMap;
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(final Integer id, final ReportFormat format, final OutputStream outputStream) {
        Logging.withPrefix(LOG4J_CATEGORY, new Runnable() {
            @Override public void run() {
                final ReportCatalogEntry catalogEntry = m_reportCatalogDao.get(id);
                final String reportServiceName = m_globalReportRepository.getReportService(catalogEntry.getReportId());
                final ReportService reportService = m_reportServiceLocator.getReportService(reportServiceName);
                LOG.debug("attempting to rended the report as {} using {}", reportServiceName, format);
                try {
                    reportService.render(catalogEntry.getReportId(), catalogEntry.getLocation(), format, outputStream);
                } catch (ReportException e) {
                    LOG.error("unable to render report " + id, e);
                }
            }
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public void save(final ReportCatalogEntry reportCatalogEntry) {
        m_reportCatalogDao.save(reportCatalogEntry);
        m_reportCatalogDao.flush();
    }

    /** {@inheritDoc} */
    @Override
    public void setReportCatalogDao(ReportCatalogDao reportCatalogDao) {
        m_reportCatalogDao = reportCatalogDao;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setReportServiceLocator(ReportServiceLocator reportServiceLocator) {
        m_reportServiceLocator = reportServiceLocator;
    }

    /**
     * <p>setGlobalReportRepository</p>
     * 
     * Set the global report repository which implements a local report for Community reports and remote 
     * OpenNMS CONNECT repositories
     * 
     * @param globalReportRepository a {@link org.opennms.features.reporting.repository.global.GlobalReportRepository} object
     */
    public void setGlobalReportRepository(GlobalReportRepository globalReportRepository) {
        this.m_globalReportRepository = globalReportRepository;
    }
}
