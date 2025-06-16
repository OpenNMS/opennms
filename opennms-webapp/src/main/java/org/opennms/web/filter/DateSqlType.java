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

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.events.api.EventConstants;

/**
 * <p>DateSqlType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DateSqlType implements SQLType<Date> {

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(PreparedStatement ps, int parameterIndex, Date value) throws SQLException {
        ps.setTimestamp(parameterIndex, new java.sql.Timestamp(value.getTime()));
    }
    
    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(Date value) {
        return value == null ? "Null" : String.valueOf(value.getTime());
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(Date value) {
        return "to_timestamp(\'" + StringUtils.toStringEfficiently(value) + "\', " + EventConstants.POSTGRES_DATE_FORMAT +")";
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link java.util.Date} object.
     * @param value2 a {@link java.util.Date} object.
     * @return an array of {@link java.util.Date} objects.
     */
    @Override
    public Date[] createArray(Date value1, Date value2) {
        return new Date[] { value1, value2 };
    }

}
