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
package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>StringSqlType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class StringSqlType implements SQLType<String> {

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(PreparedStatement ps, int parameterIndex, String value) throws SQLException {
        ps.setString(parameterIndex, value);
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(String value) {
        return value;
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(String value) {
        return "'" + value + "'";
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link java.lang.String} object.
     * @param value2 a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    @Override
    public String[] createArray(String value1, String value2) {
        return new String[] { value1, value2 };
    }

}
