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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
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
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.provision.detector.jdbc.JdbcStoredProcedureDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml",
                                    "classpath:/META-INF/opennms/applicationContext-dao.xml"})
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@JUnitTemporaryDatabase
public class JdbcStoredProcedureDetectorTest {
    
    @Autowired
    public JdbcStoredProcedureDetector m_detector;
    
    @Autowired
    public DataSource m_dataSource;
    
    @Before
    public void setUp() throws SQLException{
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
    
    @Test
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.init();
        assertTrue("JDBCStoredProcedureDetector should work", m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()));
    }
    
    @Test
    public void testStoredProcedureFail() throws UnknownHostException{
        m_detector.setStoredProcedure("bogus");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()));
    }
    
    @Test
    public void testWrongUserName() throws UnknownHostException{
        m_detector.setUser("wrongUserName");
        m_detector.init();
        
        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()) );
    }
    

    @Test
    public void testWrongSchema() throws UnknownHostException{
        m_detector.setSchema("defaultSchema");
        m_detector.init();
        
        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()) );
    }
}
