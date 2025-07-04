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
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class OnmsSeverityUserType implements UserType {

    /**
     * A public default constructor is required by Hibernate.
     */
    public OnmsSeverityUserType() {}

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (value instanceof OnmsSeverity) {
            // immutable, we can just return the value
            return value;
        } else {
            throw new IllegalArgumentException("Unexpected type that is mapped with " + this.getClass().getSimpleName() + ": " + value.getClass().getName());
        }
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable)deepCopy(value);
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws HibernateException, SQLException {
        return OnmsSeverity.get(rs.getInt(names[0]));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setInt(index, 1);
        } else if (value instanceof OnmsSeverity) {
            st.setInt(index,((OnmsSeverity)value).getId());
        } else if (value instanceof String) {
            try {
                st.setInt(index, Integer.parseInt((String)value));
            } catch (final IllegalArgumentException e) {
                throw new HibernateException("unable to set severity " + value, e);
            }
        }
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Class<OnmsSeverity> returnedClass() {
        return OnmsSeverity.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { java.sql.Types.INTEGER };
    }

}
