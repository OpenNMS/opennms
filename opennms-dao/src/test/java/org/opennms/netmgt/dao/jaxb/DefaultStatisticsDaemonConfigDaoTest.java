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
