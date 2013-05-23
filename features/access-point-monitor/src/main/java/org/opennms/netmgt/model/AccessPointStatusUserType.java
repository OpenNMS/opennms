/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * <p>
 * AccessPointStatusUserType class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class AccessPointStatusUserType implements UserType {

    /**
     * A public default constructor is required by Hibernate.
     */
    public AccessPointStatusUserType() {
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) {
        return deepCopy(cached);
    }

    @Override
    public Object deepCopy(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof AccessPointStatus) {
            // immutable, we can just return the value
            return value;
        } else {
            throw new IllegalArgumentException("Unexpected type that is mapped with " + this.getClass().getSimpleName() + ": " + value.getClass().getName());
        }
    }

    @Override
    public Serializable disassemble(final Object value) {
        return (Serializable) deepCopy(value);
    }

    @Override
    public boolean equals(final Object x, final Object y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(final Object x) {
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws SQLException {
        return AccessPointStatus.get(rs.getInt(names[0]));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws SQLException {
        if (value == null) {
            st.setInt(index, 1);
        } else if (value instanceof AccessPointStatus) {
            st.setInt(index, ((AccessPointStatus) value).getId());
        } else if (value instanceof String) {
            try {
                st.setInt(index, AccessPointStatus.get((String)value).getId());
            } catch (final IllegalArgumentException e) {
                throw new HibernateException("unable to set status " + value, e);
            }
        }
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) {
        return original;
    }

    @Override
    public Class<AccessPointStatus> returnedClass() {
        return AccessPointStatus.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { java.sql.Types.INTEGER };
    }

}
