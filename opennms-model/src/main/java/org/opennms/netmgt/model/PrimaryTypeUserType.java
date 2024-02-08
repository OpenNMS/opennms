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
import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.type.CharacterType;
import org.hibernate.usertype.UserType;

public class PrimaryTypeUserType implements UserType {
    private static final int[] SQL_TYPES = new int[] { java.sql.Types.CHAR };

    /**
     * A public default constructor is required by Hibernate.
     */
    public PrimaryTypeUserType() {}

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<PrimaryType> returnedClass() {
        return PrimaryType.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null? 0 : x.hashCode();
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
        final Character c = CharacterType.INSTANCE.nullSafeGet(rs, names[0]);
        return PrimaryType.get(c);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
        if (value == null) {
            CharacterType.INSTANCE.nullSafeSet(st, null, index);
        } else if (value instanceof PrimaryType) {
            CharacterType.INSTANCE.nullSafeSet(st, ((PrimaryType)value).getCharCode(), index);
        } else if (value instanceof Character) {
            CharacterType.INSTANCE.nullSafeSet(st, (Character)value, index);
        } else if (value instanceof String) {
            // let PrimaryType validate it as a "good" value
            CharacterType.INSTANCE.nullSafeSet(st, PrimaryType.get((String)value).getCharCode(), index);
        }
    }

    /**
     * Since {@link PrimaryType} is immutable, we just return the original
     * value without copying it.
     */
    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (value instanceof PrimaryType) {
            // PrimaryType is immutable so return the value without copying it
            return value;
        } else {
            throw new IllegalArgumentException("Unexpected type that is mapped with " + this.getClass().getSimpleName() + ": " + value.getClass().getName());
        }
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)deepCopy(value);
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

}
