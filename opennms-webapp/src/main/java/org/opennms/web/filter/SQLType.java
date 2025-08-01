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
import java.util.Date;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.filter.OnmsSeveritySqlType;

/**
 * <p>SQLType interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface SQLType<T> {
    
    public static final SQLType<Integer> INT = new IntegerSqlType();
    public static final SQLType<String> STRING = new StringSqlType();
    public static final SQLType<Date> DATE = new DateSqlType();
    public static final SQLType<OnmsSeverity> SEVERITY = new OnmsSeveritySqlType();
    public static final SQLType<Boolean> BOOLEAN = new BooleanSqlType();
    public static final SQLType<Long> BIGINT = new BigIntSqlType();

    /**
     * <p>getValueAsString</p>
     *
     * @param value a T object.
     * @param <T> a T object.
     * @return a {@link java.lang.String} object.
     */
    public String getValueAsString(T value);
    
    /**
     * <p>formatValue</p>
     *
     * @param value a T object.
     * @return a {@link java.lang.String} object.
     */
    public String formatValue(T value);

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a T object.
     * @throws java.sql.SQLException if any.
     */
    public void bindParam(PreparedStatement ps, int parameterIndex, T value) throws SQLException;
    
    /**
     * <p>createArray</p>
     *
     * @param value1 a T object.
     * @param value2 a T object.
     * @return an array of T objects.
     */
    public T[] createArray(T value1, T value2);
    
}
