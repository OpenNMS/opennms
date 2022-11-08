/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vacuumd;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;

/**
 * Tests transactional capabilities of Vacuumd
 * 
 * @author <a href=mailto:brozow@opennms.org>Mathew Brozowski</a>
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 */
public class TransactionTest {

    Connection m_conn;

    Connection m_conn2;

    DataSource m_ds;

    DataSource m_ds2;

    @Before
    public void setUp() throws Exception {

        m_ds = mock(DataSource.class);
        m_ds2 = mock(DataSource.class);

        m_conn = mock(Connection.class);
        m_conn2 = mock(Connection.class);

        DataSourceFactory.setInstance("ds", m_ds);
        DataSourceFactory.setInstance("ds2", m_ds2);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_ds);
        verifyNoMoreInteractions(m_ds2);
        verifyNoMoreInteractions(m_conn);
        verifyNoMoreInteractions(m_conn2);
    }

    @Test
    public void testCommit() throws Exception {

        when(m_ds.getConnection()).thenReturn(m_conn);
        when(m_ds2.getConnection()).thenReturn(m_conn2);

        m_conn.setAutoCommit(false);
        m_conn.commit();
        m_conn.close();

        m_conn2.setAutoCommit(false);
        m_conn2.commit();
        m_conn2.close();

        Transaction.begin();
        Transaction.getConnection("ds");
        Transaction.getConnection("ds2");
        Transaction.end();

        verify(m_ds, atLeastOnce()).getConnection();
        verify(m_ds2, atLeastOnce()).getConnection();
        verify(m_conn, atLeastOnce()).close();
        verify(m_conn, atLeastOnce()).commit();
        verify(m_conn, atLeastOnce()).setAutoCommit(eq(false));
        verify(m_conn2, atLeastOnce()).close();
        verify(m_conn2, atLeastOnce()).commit();
        verify(m_conn2, atLeastOnce()).setAutoCommit(eq(false));
    }

    @Test
    public void testRollback() throws Exception {

        when(m_ds.getConnection()).thenReturn(m_conn);
        when(m_ds2.getConnection()).thenReturn(m_conn2);

        m_conn.setAutoCommit(false);
        m_conn.rollback();
        m_conn.close();

        m_conn2.setAutoCommit(false);
        m_conn2.rollback();
        m_conn2.close();

        Transaction.begin();
        Transaction.getConnection("ds");
        Transaction.getConnection("ds2");
        Transaction.rollbackOnly();
        Transaction.end();

        verify(m_ds, atLeastOnce()).getConnection();
        verify(m_ds2, atLeastOnce()).getConnection();
        verify(m_conn, atLeastOnce()).close();
        verify(m_conn, atLeastOnce()).rollback();
        verify(m_conn, atLeastOnce()).setAutoCommit(eq(false));
        verify(m_conn2, atLeastOnce()).close();
        verify(m_conn2, atLeastOnce()).rollback();
        verify(m_conn2, atLeastOnce()).setAutoCommit(eq(false));
    }

    @Test
    public void testReturnSameConnection() throws Exception {

        when(m_ds.getConnection()).thenReturn(m_conn);
        m_conn.setAutoCommit(false);

        m_conn.commit();
        m_conn.close();

        Transaction.begin();

        Connection c1 = Transaction.getConnection("ds");
        Connection c2 = Transaction.getConnection("ds");
        assertSame("Expected to get the same connection for both calls to getConnection",
                   c1, c2);

        Transaction.end();

        verify(m_ds, atLeastOnce()).getConnection();
        verify(m_conn, atLeastOnce()).close();
        verify(m_conn, atLeastOnce()).commit();
        verify(m_conn, atLeastOnce()).setAutoCommit(eq(false));
    }

    @Test
    public void testCloseResources() throws Exception {

        when(m_ds.getConnection()).thenReturn(m_conn);
        m_conn.setAutoCommit(false);

        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        rs.close();
        stmt.close();
        m_conn.close();
        m_conn.commit();

        Transaction.begin();
        Transaction.getConnection("ds");
        Transaction.register(stmt);
        Transaction.register(rs);
        Transaction.end();

        verify(m_ds, atLeastOnce()).getConnection();
        verify(m_conn, atLeastOnce()).close();
        verify(m_conn, atLeastOnce()).commit();
        verify(m_conn, atLeastOnce()).setAutoCommit(eq(false));
    }

}
