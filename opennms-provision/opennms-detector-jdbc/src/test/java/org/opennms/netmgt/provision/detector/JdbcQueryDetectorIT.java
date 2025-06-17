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
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.jdbc.JdbcQueryDetector;
import org.opennms.netmgt.provision.detector.jdbc.JdbcQueryDetectorFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcQueryDetectorIT implements InitializingBean {

    @Autowired
    public JdbcQueryDetectorFactory m_detectorFactory;
    
    public JdbcQueryDetector m_detector;

    @Autowired
    public DataSource m_dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws SQLException {
        MockLogAppender.setupLogging();
        
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        String url = null;
        String username = null;
        Connection conn = null;
        try {
            conn = m_dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            url = metaData.getURL();
            username = metaData.getUserName();
            conn.close();
        } catch (final SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                conn.close();
            }
        }

        m_detector.setDbDriver("org.postgresql.Driver");
        m_detector.setPort(5432);
        m_detector.setUrl(url);
        m_detector.setUser(username);
        m_detector.setPassword("");
        m_detector.setSqlQuery("select count(nodeid) from node");
    }

    @After
    public void tearDown(){
        MockLogAppender.assertNoFatalOrGreater();
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.init();
        assertTrue("JDBCQueryDetector should work", m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=20000)
    public void testStoredProcedureFail() throws UnknownHostException{
        m_detector.setSqlQuery("bogus");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=20000)
    public void testWrongUserName() throws UnknownHostException{
        m_detector.setUser("wrongUserName");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")) );
    }
}
