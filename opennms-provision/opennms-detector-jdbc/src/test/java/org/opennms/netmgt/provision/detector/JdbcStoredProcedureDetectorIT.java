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
import java.sql.Statement;
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
import org.opennms.netmgt.provision.detector.jdbc.JdbcStoredProcedureDetector;
import org.opennms.netmgt.provision.detector.jdbc.JdbcStoredProcedureDetectorFactory;
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
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcStoredProcedureDetectorIT implements InitializingBean {
    @Autowired
    public JdbcStoredProcedureDetectorFactory m_detectorFactory;
    
    public JdbcStoredProcedureDetector m_detector;

    @Autowired
    public DataSource m_dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() throws SQLException{
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        String createSchema = "CREATE SCHEMA test";
        String createProcedure = "CREATE FUNCTION test.isRunning () RETURNS bit AS 'BEGIN RETURN 1; END;' LANGUAGE 'plpgsql';";

        String url = null;
        String username = null;
        String password = "postgres";
        Connection conn = null;
        try {
            conn = m_dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            url = metaData.getURL();
            username = metaData.getUserName();

            Statement createStmt = conn.createStatement();
            createStmt.executeUpdate(createSchema);
            createStmt.close();

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createProcedure);
            stmt.close();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                conn.close();
            }
        }

        m_detector.setDbDriver("org.postgresql.Driver");
        m_detector.setPort(5432);
        m_detector.setUrl(url);
        m_detector.setUser(username);
        m_detector.setPassword(password);
        m_detector.setStoredProcedure("isRunning");

    }

    @After
    public void tearDown(){

    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.init();
        assertTrue("JDBCStoredProcedureDetector should work", m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=20000)
    public void testStoredProcedureFail() throws UnknownHostException{
        m_detector.setStoredProcedure("bogus");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=20000)
    public void testWrongUserName() throws UnknownHostException{
        m_detector.setUser("wrongUserName");
        m_detector.init();

        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")) );
    }


    @Test(timeout=20000)
    public void testWrongSchema() throws UnknownHostException{
        m_detector.setSchema("defaultSchema");
        m_detector.init();

        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")) );
    }
}
