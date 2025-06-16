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
package org.opennms.core.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * <p>Querier class.</p>
 *
 * @deprecated Use Hibernate instead of plain JDBC
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
     * @param rowProcessor a {@link org.opennms.core.utils.RowProcessor} object.
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
