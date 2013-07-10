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
}
