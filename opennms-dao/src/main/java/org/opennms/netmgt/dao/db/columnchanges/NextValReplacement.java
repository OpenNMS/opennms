
/**
 * <p>NextValReplacement class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;
public class NextValReplacement implements ColumnChangeReplacement {
        private final String m_sequence;
        
        private final Connection m_connection;
        private final PreparedStatement m_statement;
        
        /**
         * <p>Constructor for NextValReplacement.</p>
         *
         * @param sequence a {@link java.lang.String} object.
         * @param dataSource a {@link javax.sql.DataSource} object.
         * @throws java.sql.SQLException if any.
         */
        public NextValReplacement(String sequence, DataSource dataSource) throws SQLException {
            m_sequence = sequence;
//            m_dataSource = dataSource;
            m_connection = dataSource.getConnection();
            m_statement = m_connection.prepareStatement("SELECT nextval('"
                                                        + m_sequence
                                                        + "')");
        }
        
        private PreparedStatement getStatement() {
            /*
            if (m_statement == null) {
                createStatement();
            }
            */
            return m_statement;
        }

        /*
        private void createStatement() throws SQLException {
            m_statement = getConnection().prepareStatement("SELECT nextval('" + m_sequence + "')");
        }
        
        private Connection getConnection() throws SQLException {
            if (m_connection == null) {
                createConnection();
            }
            
            return m_connection;
        }
        
        private void createConnection() throws SQLException {
            m_connection = m_dataSource.getConnection();
        }
        */

        /** {@inheritDoc} */
        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            ResultSet r = getStatement().executeQuery();
            
            if (!r.next()) {
                r.close();
                throw new SQLException("Query for next value of sequence did not return any rows.");
            }
            
            int i = r.getInt(1);
            r.close();
            return i;
        }
        
        /**
         * <p>addColumnIfColumnIsNew</p>
         *
         * @return a boolean.
         */
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        /**
         * <p>close</p>
         *
         * @throws java.sql.SQLException if any.
         */
        public void close() throws SQLException {
            finalize();
        }
        
        /**
         * <p>finalize</p>
         *
         * @throws java.sql.SQLException if any.
         */
        protected void finalize() throws SQLException {
            if (m_statement != null) {
                m_statement.close();
            }
            if (m_connection != null) {
                m_connection.close();
            }
        }
    }
