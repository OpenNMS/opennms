/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.jaxb;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.statsd.model.Report;
import org.opennms.netmgt.dao.jaxb.DefaultStatisticsDaemonConfigDao;
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
        assertTrue("at least two reports should be present but found " + reports.size(), reports.size() > 1);
        assertNotNull("first report should non-null", reports.get(0));
    }

}
