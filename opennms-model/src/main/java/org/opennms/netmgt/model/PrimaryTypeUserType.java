/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
