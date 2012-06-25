/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.vacuumd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.test.mock.EasyMockUtils;

import junit.framework.TestCase;

/**
 * Tests transactional capabilities of Vacuumd
 * 
 * @author <a href=mailto:brozow@opennms.org>Mathew Brozowski</a>
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 *
 */
public class TransactionTest extends TestCase {
	
	EasyMockUtils m_ezMock = new EasyMockUtils();
    Connection m_conn;
    Connection m_conn2;
    DataSource m_ds;
    DataSource m_ds2;

	protected void setUp() throws Exception {
		super.setUp();
        
        m_ds = m_ezMock.createMock(DataSource.class);
        m_ds2 = m_ezMock.createMock(DataSource.class);
		
        m_conn = m_ezMock.createMock(Connection.class);
        m_conn2 = m_ezMock.createMock(Connection.class);
        
        DataSourceFactory.setInstance("ds", m_ds);
        DataSourceFactory.setInstance("ds2", m_ds2);

		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCommit() throws Exception {
        
        EasyMock.expect(m_ds.getConnection()).andReturn(m_conn);
        EasyMock.expect(m_ds2.getConnection()).andReturn(m_conn2);
		
        m_conn.setAutoCommit(false);
		m_conn.commit();
		m_conn.close();
        
        m_conn2.setAutoCommit(false);
        m_conn2.commit();
        m_conn2.close();
		
		m_ezMock.replayAll();
		
		Transaction.begin();
        Transaction.getConnection("ds");
        Transaction.getConnection("ds2");
		Transaction.end();
		
		m_ezMock.verifyAll();
		
	}
    
    public void testRollback() throws Exception {
        
        EasyMock.expect(m_ds.getConnection()).andReturn(m_conn);
        EasyMock.expect(m_ds2.getConnection()).andReturn(m_conn2);

        m_conn.setAutoCommit(false);
        m_conn.rollback();
        m_conn.close();
        
        m_conn2.setAutoCommit(false);
        m_conn2.rollback();
        m_conn2.close();
        
        m_ezMock.replayAll();
        
        Transaction.begin();
        Transaction.getConnection("ds");
        Transaction.getConnection("ds2");
        Transaction.rollbackOnly();
        Transaction.end();
        
        m_ezMock.verifyAll();
        
    }
    
    public void testReturnSameConnection() throws Exception {
        
        EasyMock.expect(m_ds.getConnection()).andReturn(m_conn);
        m_conn.setAutoCommit(false);
        
        m_conn.commit();
        m_conn.close();
        
        m_ezMock.replayAll();
        
        Transaction.begin();
        
        Connection c1 = Transaction.getConnection("ds");
        Connection c2 = Transaction.getConnection("ds");
        assertSame("Expected to get the same connection for both calls to getConnection", c1, c2);
        
        Transaction.end();
        
        m_ezMock.verifyAll();
        
    }
    
    public void testCloseResources() throws Exception {

        EasyMock.expect(m_ds.getConnection()).andReturn(m_conn);
        m_conn.setAutoCommit(false);

        Statement stmt = m_ezMock.createMock(Statement.class);
        ResultSet rs = m_ezMock.createMock(ResultSet.class);
        
        
        rs.close();
        stmt.close();
        m_conn.close();
        m_conn.commit();

        m_ezMock.replayAll();
        
        Transaction.begin();
        Transaction.getConnection("ds");
        Transaction.register(stmt);
        Transaction.register(rs);
        Transaction.end();
        
        m_ezMock.verifyAll();

    }


}
