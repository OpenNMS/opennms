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
 * Created: January 10th 2010 jonathan@opennms.org
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
package org.opennms.netmgt.dao.hibernate;

import java.util.Date;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.model.ReportCatalogEntry;

public class ReportCatalogDaoHibernateTest extends AbstractTransactionalDaoTestCase {
    
    public void testSave() {
        
        Date date = new Date();
        ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
        catalogEntry.setReportId("reportId_1");
        catalogEntry.setLocation("location_1");
        catalogEntry.setTitle("title_1");
        catalogEntry.setDate(date);
        getReportCatalogDao().save(catalogEntry);
        
        Integer id = catalogEntry.getId();
        
        ReportCatalogEntry retrievedEntry = getReportCatalogDao().get(id);
        
        assertEquals(catalogEntry.getReportId(),retrievedEntry.getReportId());
        assertEquals(catalogEntry.getTitle(), retrievedEntry.getTitle());
        assertEquals(catalogEntry.getLocation(), retrievedEntry.getLocation());
        assertEquals(0,catalogEntry.getDate().compareTo(retrievedEntry.getDate()));
        
        
    }
    
    public void testDelete() {
        
        Date date = new Date();
        ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
        catalogEntry.setReportId("reportId_2");
        catalogEntry.setLocation("location_2");
        catalogEntry.setTitle("title_2");
        catalogEntry.setDate(date);
        getReportCatalogDao().save(catalogEntry);
        
        Integer id = catalogEntry.getId();
        assertNotNull(getReportCatalogDao().get(id));
        getReportCatalogDao().delete(id);
        assertNull(getReportCatalogDao().get(id));
        
    }

    

}
