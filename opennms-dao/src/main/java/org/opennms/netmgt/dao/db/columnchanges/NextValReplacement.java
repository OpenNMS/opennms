/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

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
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public void close() throws SQLException {
            finalize();
        }
        
        protected void finalize() throws SQLException {
            if (m_statement != null) {
                m_statement.close();
            }
            if (m_connection != null) {
                m_connection.close();
            }
        }
    }
