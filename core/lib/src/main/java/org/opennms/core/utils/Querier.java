/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * <p>Querier class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Querier extends JDBCTemplate implements RowProcessor {
    private int m_count;
    private RowProcessor m_rowProcessor;
    /**
     * <p>Constructor for Querier.</p>
     *
     * @param db a {@link javax.sql.DataSource} object.
     * @param sql a {@link java.lang.String} object.
     * @param rowProcessor a {@link org.opennms.netmgt.utils.RowProcessor} object.
     */
    public Querier(DataSource db, String sql, RowProcessor rowProcessor) {
        super(db, sql);
        if (rowProcessor == null)
            m_rowProcessor = this;
        else 
            m_rowProcessor = rowProcessor;
        m_count = 0;
    }
     
    /**
     * <p>Constructor for Querier.</p>
     *
     * @param db a {@link javax.sql.DataSource} object.
     * @param sql a {@link java.lang.String} object.
     */
    public Querier(DataSource db, String sql) {
        this(db, sql, null);
    }
     
    /**
     * <p>getCount</p>
     *
     * @return a int.
     */
    public int getCount() {
        return m_count;
    }
     
    /** {@inheritDoc} */
    @Override
    protected void executeStmt(PreparedStatement stmt) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            m_count = 0;
            while (rs.next()) {
                m_rowProcessor.processRow(rs);
                m_count++;
            }
        } finally {
            d.cleanUp();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processRow(ResultSet rs) throws SQLException {
    }

 }
