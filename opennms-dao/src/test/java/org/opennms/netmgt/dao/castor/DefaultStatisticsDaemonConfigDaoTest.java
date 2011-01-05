/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao.castor;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.castor.statsd.Report;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.InputStreamResource;

/**
 * Unit tests for DefaultStatisticsDaemonConfigDao.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see DefaultStatisticsDaemonConfigDao
 */
public class DefaultStatisticsDaemonConfigDaoTest extends TestCase {
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        DefaultStatisticsDaemonConfigDao dao = new DefaultStatisticsDaemonConfigDao();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("statsd-configuration.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
    }
    
    public void testGetReports() throws Exception {
        DefaultStatisticsDaemonConfigDao dao = new DefaultStatisticsDaemonConfigDao();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("statsd-configuration.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();

        List<Report> reports = dao.getReports();
        assertNotNull("reports list should not be null", reports);
        assertEquals("reports list size", 5, reports.size());
        
        Report report = reports.get(0);
        assertNotNull("first report should not be zero", report);
    }
    

}
