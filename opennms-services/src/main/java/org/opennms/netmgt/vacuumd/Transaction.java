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
package org.opennms.netmgt.vacuumd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Transaction class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Transaction {
	
	public static final Logger LOG = LoggerFactory.getLogger(Transaction.class);
	
	private static ThreadLocal<Transaction> s_threadTX = new ThreadLocal<>();

	private static Transaction getTX() {
		Transaction tx = s_threadTX.get();
		if (tx == null) {
			throw new IllegalStateException("No transaction has been started for this thread!");
		}
		return tx;
	}
    
    private static void clearTX() {
        s_threadTX.set(null);
    }
	
	/**
	 * <p>begin</p>
	 */
	public static void begin() {
        
        LOG.debug("About to begin Transaction for {}", Thread.currentThread());
		Transaction tx = s_threadTX.get();
		if (tx != null) {
			throw new IllegalStateException("Cannot begin a transaction.. one has already been begun");
		}
        LOG.debug("Began Transaction for {}", Thread.currentThread());
		s_threadTX.set(new Transaction());
		
	}
    
    /**
     * <p>getConnection</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public static Connection getConnection(String dsName) throws SQLException {
        return getTX().doGetConnection(dsName);
    }

    /**
     * <p>register</p>
     *
     * @param stmt a {@link java.sql.Statement} object.
     */
    public static void register(Statement stmt) {
        getTX().doRegister(stmt);
    }

    /**
     * <p>register</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     */
    public static void register(ResultSet rs) {
        getTX().doRegister(rs);
    }

    /**
     * <p>rollbackOnly</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public static void rollbackOnly() throws SQLException {
        getTX().doRollbackOnly();
    }

    /**
     * <p>end</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public static void end() throws SQLException {
        LOG.debug("Ending transaction for {}", Thread.currentThread());
        try {
            Transaction tx = getTX();
            tx.doEnd();
            LOG.debug("{} transaction for {}", (tx.m_rollbackOnly ? "Rolled Back" : "Committed"), Thread.currentThread());
        } finally {
            clearTX();
        }
	}
    
    private Map<String, Connection> m_connections = new HashMap<String, Connection>();
    private List<Statement> m_statements = new LinkedList<>();
    private List<ResultSet> m_resultSets = new LinkedList<>();
    private boolean m_rollbackOnly = false;
    private DBUtils m_dbUtils = new DBUtils(Transaction.class);

    private void doRegister(Statement stmt) {
        m_dbUtils.watch(stmt);
        m_statements.add(stmt);
    }

    private void doRegister(ResultSet rs) {
        m_dbUtils.watch(rs);
        m_resultSets.add(rs);
    }

    private void doClose() throws SQLException {
        m_dbUtils.cleanUp();
    }

    private void doEnd() throws SQLException {
        try {
            for(Connection conn : m_connections.values()) {
                if (m_rollbackOnly) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            }
        } finally {
            doClose();
        }
    }

    private void doRollbackOnly() throws SQLException {
        m_rollbackOnly = true;
    }

    private Connection doGetConnection(String dsName) throws SQLException {
        if (!m_connections.containsKey(dsName)) {
            DataSource ds = DataSourceFactory.getInstance(dsName);
            if (ds == null) {
                throw new IllegalArgumentException("Could not find this datasource by using the DataSourceFactory: " + dsName);
            }
            Connection conn = ds.getConnection();
            m_dbUtils.watch(conn);
            m_connections.put(dsName, conn);
            conn.setAutoCommit(false);
        } 
        
        return m_connections.get(dsName);
    }

    /**
     * <p>finalize</p>
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        m_dbUtils.cleanUp();
        super.finalize();
    }

}
