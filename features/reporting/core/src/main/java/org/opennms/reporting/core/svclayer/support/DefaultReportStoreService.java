/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 12th 2010 jonathan@opennms.org
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.reporting.core.svclayer.support;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.api.integration.reporting.ReportService;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.netmgt.dao.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;

public class DefaultReportStoreService implements ReportStoreService {
    
    private ReportCatalogDao m_reportCatalogDao;
    private ReportServiceLocator m_reportServiceLocator;
    private DatabaseReportConfigDao m_databaseReportConfigDao;

    public void delete(Integer[] ids) {
        for (Integer id : ids) {
            delete(id); 
        }
    }
    
    public void delete(Integer id) {
        String deleteFile = new String(m_reportCatalogDao.get(id).getLocation());
        boolean success = (new File(deleteFile).delete());
        if (success) {
            log().debug("deleted report XML file: " + deleteFile);
        } else {
            log().warn("unable to delete report XML file: " + deleteFile + " will delete reportCatalogEntry anyway");
        }
        m_reportCatalogDao.delete(id);
    }

    public List<ReportCatalogEntry> getAll() {
        return m_reportCatalogDao.findAll();
    }
    
    public void render(Integer id, String format, OutputStream outputStream) {
        ReportCatalogEntry catalogEntry = m_reportCatalogDao.get(id);
        String reportServiceName = m_databaseReportConfigDao.getReportService(catalogEntry.getReportId());
        ReportService reportService = m_reportServiceLocator.getReportService(reportServiceName);
        reportService.render(catalogEntry.getReportId(), catalogEntry.getLocation(), format, outputStream);
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void save(ReportCatalogEntry reportCatalogEntry) {
        m_reportCatalogDao.save(reportCatalogEntry);
    }

    public void setReportCatalogDao(ReportCatalogDao reportCatalogDao) {
        m_reportCatalogDao = reportCatalogDao;
    }
    
    public void setDatabaseReportConfigDao(DatabaseReportConfigDao databaseReportConfigDao) {
        m_databaseReportConfigDao = databaseReportConfigDao;
    }
    
    public void setReportServiceLocator(ReportServiceLocator reportServiceLocator) {
        m_reportServiceLocator = reportServiceLocator;
    }

}
