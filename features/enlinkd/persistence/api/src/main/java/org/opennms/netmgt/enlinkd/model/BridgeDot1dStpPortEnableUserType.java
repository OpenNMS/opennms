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
package org.opennms.netmgt.enlinkd.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;
import org.hibernate.type.IntegerType;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink.BridgeDot1dStpPortEnable;

public class BridgeDot1dStpPortEnableUserType extends EnumType {

    private static final long serialVersionUID = 2935892942529340988L;

    private static final int[] SQL_TYPES = new int[] { java.sql.Types.INTEGER };

	/**
     * A public default constructor is required by Hibernate.
     */
    public BridgeDot1dStpPortEnableUserType() {}

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws HibernateException, SQLException {
        Integer c = IntegerType.INSTANCE.nullSafeGet(rs, names[0], session);
        if (c == null) {
            return null;
        }
        for (BridgeDot1dStpPortEnable type : BridgeDot1dStpPortEnable.values()) {
            if (type.getValue().intValue() == c.intValue()) {
                return type;
            }
        }
        throw new HibernateException("Invalid value for BridgeDot1dStpPortEnable: " + c);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            IntegerType.INSTANCE.nullSafeSet(st, null, index, session);
        } else if (value instanceof BridgeDot1dStpPortEnable){
            IntegerType.INSTANCE.nullSafeSet(st, ((BridgeDot1dStpPortEnable)value).getValue(), index, session);
        }
    }

    @Override
    public Class<BridgeDot1dStpPortEnable> returnedClass() {
        return BridgeDot1dStpPortEnable.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public void setParameterValues(Properties parameters) {
    }
}
