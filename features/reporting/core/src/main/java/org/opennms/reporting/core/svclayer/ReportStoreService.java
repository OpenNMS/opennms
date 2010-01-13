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
package org.opennms.reporting.core.svclayer;

import java.io.OutputStream;
import java.util.List;

import org.opennms.netmgt.dao.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ReportStoreService {
    
    public List<ReportCatalogEntry> getAll();
    
    public void render(Integer id, String format, OutputStream outputStream);
    
    @Transactional(readOnly = false)
    public void delete(Integer[] ids);
    
    @Transactional(readOnly = false)
    public void delete(Integer id);
    
    @Transactional(readOnly = false)
    public void save(ReportCatalogEntry reportCatalogEntry);
    
    public void setReportCatalogDao(ReportCatalogDao reportCatalogDao);

    public void setReportServiceLocator(ReportServiceLocator reportServiceLocator);
}
