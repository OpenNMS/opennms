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
package org.opennms.web.alarm.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.SQLType;

/**
 * OnmsSeverityType
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class OnmsSeveritySqlType implements SQLType<OnmsSeverity> {

    /**
     * <p>bindParam</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @param value a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void bindParam(final PreparedStatement ps, final int parameterIndex, final OnmsSeverity value) throws SQLException {
        ps.setInt(parameterIndex, value.getId());
    }

    /**
     * <p>createArray</p>
     *
     * @param value1 a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @param value2 a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return an array of {@link org.opennms.netmgt.model.OnmsSeverity} objects.
     */
    @Override
    public OnmsSeverity[] createArray(final OnmsSeverity value1, final OnmsSeverity value2) {
        return new OnmsSeverity[] { value1, value2 };
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String formatValue(final OnmsSeverity value) {
        return String.valueOf(value.getId());
    }

    /**
     * <p>getValueAsString</p>
     *
     * @param value a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getValueAsString(final OnmsSeverity value) {
        return String.valueOf(value.getId());
    }

}
