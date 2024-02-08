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
package org.opennms.reporting.core.svclayer;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>ReportStoreService interface.</p>
 */
@Transactional(readOnly = true)
public interface ReportStoreService {
    
    /**
     * <p>getAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ReportCatalogEntry> getAll();

    List<ReportCatalogEntry> getPage(int offset, int limit);

    /**
     * <p>getFormatMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getFormatMap();
    
    /**
     * <p>render</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @param format a {@link org.opennms.api.reporting.ReportFormat} object.
     * @param outputStream a {@link java.io.OutputStream} object.
     */
    public void render(Integer id, ReportFormat format, OutputStream outputStream);
    
    /**
     * <p>delete</p>
     *
     * @param ids an array of {@link java.lang.Integer} objects.
     */
    @Transactional(readOnly = false)
    public void delete(Integer[] ids);
    
    /**
     * <p>delete</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    @Transactional(readOnly = false)
    public void delete(Integer id);
    
    /**
     * <p>save</p>
     *
     * @param reportCatalogEntry a {@link org.opennms.netmgt.model.ReportCatalogEntry} object.
     */
    @Transactional(readOnly = false)
    public void save(ReportCatalogEntry reportCatalogEntry);
    
    /**
     * <p>setReportCatalogDao</p>
     *
     * @param reportCatalogDao a {@link org.opennms.netmgt.dao.api.ReportCatalogDao} object.
     */
    public void setReportCatalogDao(ReportCatalogDao reportCatalogDao);

    /**
     * <p>setReportServiceLocator</p>
     *
     * @param reportServiceLocator a {@link org.opennms.reporting.core.svclayer.ReportServiceLocator} object.
     */
    public void setReportServiceLocator(ReportServiceLocator reportServiceLocator);

    long countAll();
}
