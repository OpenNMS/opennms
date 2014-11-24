/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;

public class InetAddressUserType implements UserType {

    private static final int[] SQL_TYPES = new int[] { java.sql.Types.VARCHAR };

	/**
     * A public default constructor is required by Hibernate.
     */
    public InetAddressUserType() {}

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    /**
     * Since {@link java.net.InetAddress} is immutable, we just return the original
     * value without copying it.
     */
    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (value instanceof InetAddress) {
            // InetAddress is immutable so return the value without copying it
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
        // It's probably more consistent if we use our own comparator here
        // return ((InetAddress)x).equals((InetAddress)y);
        return new InetAddressComparator().compare((InetAddress)x, (InetAddress)y) == 0;
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
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session, final Object owner) throws HibernateException, SQLException {
        return InetAddressUtils.addr((String)StringType.INSTANCE.get(rs, names[0], session));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            StringType.INSTANCE.set(st, null, index, session);
        } else if (value instanceof InetAddress){
            // Format the IP address into a uniform format
            StringType.INSTANCE.set(st, InetAddressUtils.str((InetAddress)value), index, session);
        } else if (value instanceof String){
            try {
                // Format the IP address into a uniform format
                StringType.INSTANCE.set(st, InetAddressUtils.normalize((String)value), index, session);
            } catch (final IllegalArgumentException e) {
                // If the argument is not a valid IP address, then just pass it as-is. This
                // can occur of the query is performing a LIKE query (ie. '192.168.%').
                //
                // TODO: Add more validation of this string
                //
                StringType.INSTANCE.set(st, (String)value, index, session);
            }
        }
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Class<InetAddress> returnedClass() {
        return InetAddress.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

}
