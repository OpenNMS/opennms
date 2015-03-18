/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

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
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcStoredProcedureDetectorTest implements InitializingBean {
    
    @Autowired
    public JdbcStoredProcedureDetector m_detector;
    
    @Autowired
    public DataSource m_dataSource;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws SQLException{
        MockLogAppender.setupLogging();

        String createSchema = "CREATE SCHEMA test";
        String createProcedure = "CREATE FUNCTION test.isRunning () RETURNS bit AS 'BEGIN RETURN 1; END;' LANGUAGE 'plpgsql';";
                            		
        String url = null;
        String username = null;
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
            conn.close();
        }
        
        m_detector.setDbDriver("org.postgresql.Driver");
        m_detector.setPort(5432);
        m_detector.setUrl(url);
        m_detector.setUser(username);
        m_detector.setPassword("");
        m_detector.setStoredProcedure("isRunning");
        
    }
    
    @After
    public void tearDown(){
        
    }
    
    @Test(timeout=90000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.init();
        assertTrue("JDBCStoredProcedureDetector should work", m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }
    
    @Test(timeout=90000)
    public void testStoredProcedureFail() throws UnknownHostException{
        m_detector.setStoredProcedure("bogus");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }
    
    @Test(timeout=90000)
    public void testWrongUserName() throws UnknownHostException{
        m_detector.setUser("wrongUserName");
        m_detector.init();
        
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")) );
    }
    

    @Test(timeout=90000)
    public void testWrongSchema() throws UnknownHostException{
        m_detector.setSchema("defaultSchema");
        m_detector.init();
        
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")) );
    }
}
