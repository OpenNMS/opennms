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
 * <p>IntegerSqlType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class IntegerSqlType implements SQLType<Integer> {

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(Integer value) {
        return value.toString();
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link java.lang.Integer} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(Integer value) {
        return String.valueOf(value);
    }

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link java.lang.Integer} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(PreparedStatement ps, int parameterIndex, Integer value) throws SQLException {
        ps.setInt(parameterIndex, value);
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link java.lang.Integer} object.
     * @param value2 a {@link java.lang.Integer} object.
     * @return an array of {@link java.lang.Integer} objects.
     */
    @Override
    public Integer[] createArray(Integer value1, Integer value2) {
        return new Integer[] { value1, value2 };
    }

}
