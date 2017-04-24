/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.dao.jasper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>LegacyLocalJasperReportsDaoTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-reportingDaoTest.xml"})
public class LegacyLocalJasperReportsDaoTest implements InitializingBean {

    /**
     * Local report data access object to test
     */
    @Autowired
    private LocalJasperReportsDao m_localJasperReportsDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * <p>tearDown</p>
     * <p/>
     * Cleanup
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        m_localJasperReportsDao = null;
    }

    /**
     * <p>testJasperTemplateLocation</p>
     *
     * Tests to retrieve all jasper report template locations
     *
     * @throws Exception
     */
    @Test
    public void testJasperTemplateLocation() throws Exception {
        assertEquals("Test jasper sample-report template location", "sample-report.jrxml", m_localJasperReportsDao.getTemplateLocation("sample-report"));
        assertEquals("Test jasper online-sample-report template location", "sample-report.jrxml", m_localJasperReportsDao.getTemplateLocation("online-sample-report"));
        assertEquals("Test jasper not-online-sample-report template location","sample-report.jrxml", m_localJasperReportsDao.getTemplateLocation("not-online-sample-report"));
    }

    /**
     * <p>testJasperReportEngine</p>
     *
     * Tests to retrieve all jasper report engines
     *
     * @throws Exception
     */
    @Test
    public void testJasperReportEngine() throws Exception {
        assertEquals("Test jasper sample-report engine","jdbc", m_localJasperReportsDao.getEngine("sample-report"));
        assertEquals("Test jasper online-sample-report engine","jdbc", m_localJasperReportsDao.getEngine("online-sample-report"));
        assertEquals("Test jasper not-online-sample-report engine","jdbc", m_localJasperReportsDao.getEngine("not-online-sample-report"));
    }

    /**
     * <p>testTemplateStream</p>
     *
     * Tests to retrieve all jasper report template streams
     *
     * @throws Exception
     */
    @Test
    public void testTemplateStream() throws Exception {
        assertNotNull("Test to retrieve sample-report", m_localJasperReportsDao.getTemplateStream("sample-report"));
        assertNotNull("Test to retrieve online-sample-report", m_localJasperReportsDao.getTemplateStream("online-sample-report"));
        assertNotNull("Test to retrieve not-online-sample-report", m_localJasperReportsDao.getTemplateStream("not-online-sample-report"));
    }
}
