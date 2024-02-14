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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * <p>SingleResultQuerier class.</p>
 *
 * @deprecated Use Hibernate instead of plain JDBC
 *
 * @author ranger
 * @version $Id: $
 */
public class SingleResultQuerier extends Querier {
    /**
     * <p>Constructor for SingleResultQuerier.</p>
     *
     * @param db a {@link javax.sql.DataSource} object.
     * @param sql a {@link java.lang.String} object.
     */
    public SingleResultQuerier(DataSource db, String sql) {
        super(db, sql);
    }
    
    private Object m_result;
    
    /**
     * <p>getResult</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getResult() { return m_result; }
    
    /** {@inheritDoc} */
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        m_result = rs.getObject(1);
    }
    
}
