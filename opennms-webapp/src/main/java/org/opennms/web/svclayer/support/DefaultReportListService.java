/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 21, 2007
 *
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
package org.opennms.web.svclayer.support;

import java.io.File;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;
import org.opennms.web.svclayer.ReportListService;

/**
 * 
 * @author <a href="mailto:sartin@opennms.org">Jonathan Sartin</a>
 */
public class DefaultReportListService implements ReportListService {

    AvailabilityReportLocatorDao m_reportLocatorDao;

    public List<AvailabilityReportLocator> getAllReports() {
        return m_reportLocatorDao.findAll();
    }
    
    public void deleteReports(Integer[] ids) {
        for (Integer id : ids) {
            String deleteFile = new String(m_reportLocatorDao.get(id).getLocation());
            boolean success = (new File(deleteFile).delete());
            if (success) {
                log().debug("deleted report XML file: " + deleteFile);
            } else {
                log().warn("unable to delete report XML file: " + deleteFile + " will delete locator anyway");
            }
            m_reportLocatorDao.deleteById(id);
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public AvailabilityReportLocatorDao getReportLocatorDao() {
        return m_reportLocatorDao;
    }

    public void setReportLocatorDao(AvailabilityReportLocatorDao dao) {
        m_reportLocatorDao = dao;
    }

}
