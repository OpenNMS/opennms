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
