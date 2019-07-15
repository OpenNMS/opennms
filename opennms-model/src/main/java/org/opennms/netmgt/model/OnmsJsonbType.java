/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.google.gson.Gson;

/**
 * An implementation to be used when persisting postgreSQL jsonb type using the Hibernate ORM.
 * 
 * @author smith
 */
public class OnmsJsonbType implements UserType {

    private static final Gson gson = new Gson();

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.JAVA_OBJECT };
    }

    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        if (rs.wasNull()) {
            return null;
        }
        return rs.getString(names[0]);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }
        try {
            st.setObject(index, (String) value, Types.OTHER);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to convert document to String: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return gson.fromJson(gson.toJson(value), Object.class);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

}
