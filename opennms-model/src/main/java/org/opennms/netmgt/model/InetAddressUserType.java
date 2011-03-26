package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.opennms.core.utils.InetAddressUtils;

public class InetAddressUserType implements UserType {

    private static final int[] SQL_TYPES = new int[] { java.sql.Types.VARCHAR };

	/**
     * A public default constructor is required by Hibernate.
     */
    public InetAddressUserType() {}

    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    public Object deepCopy(final Object value) throws HibernateException {
        if (value == null) return null;
        return InetAddressUtils.normalize((InetAddress)value);
    }

    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable)deepCopy(value);
    }

    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return ((InetAddress)x).equals((InetAddress)y);
    }

    public int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }

    public boolean isMutable() {
        return false;
    }

    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
        return InetAddressUtils.getInetAddress((String)Hibernate.STRING.nullSafeGet(rs, names[0]));
    }

    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
        if (value == null) {
            Hibernate.STRING.nullSafeSet(st, null, index);
        } else if (value instanceof InetAddress){
            // Format the IP address into a uniform format
            Hibernate.STRING.nullSafeSet(st, InetAddressUtils.toIpAddrString((InetAddress)value), index);
        } else if (value instanceof String){
            try {
                // Format the IP address into a uniform format
                Hibernate.STRING.nullSafeSet(st, InetAddressUtils.toIpAddrString(InetAddressUtils.getInetAddress((String)value)), index);
            } catch (final IllegalArgumentException e) {
                // If the argument is not a valid IP address, then just pass it as-is. This
                // can occur of the query is performing a LIKE query (ie. '192.168.%').
                //
                // TODO: Add more validation of this string
                //
                Hibernate.STRING.nullSafeSet(st, (String)value, index);
            }
        }
    }

    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    public Class<InetAddress> returnedClass() {
        return InetAddress.class;
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

}
