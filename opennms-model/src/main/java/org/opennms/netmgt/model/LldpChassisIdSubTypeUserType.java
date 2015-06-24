/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.EnumType;
import org.hibernate.type.IntegerType;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;

public class LldpChassisIdSubTypeUserType extends EnumType {

    private static final long serialVersionUID = 2935892942529340988L;

    private static final int[] SQL_TYPES = new int[] { java.sql.Types.INTEGER };

	/**
     * A public default constructor is required by Hibernate.
     */
    public LldpChassisIdSubTypeUserType() {}

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
        Integer c = IntegerType.INSTANCE.nullSafeGet(rs, names[0]);
        if (c == null) {
            return null;
        }
        for (LldpChassisIdSubType type : LldpChassisIdSubType.values()) {
            if (type.getValue().intValue() == c.intValue()) {
                return type;
            }
        }
        throw new HibernateException("Invalid value for NodeUserType: " + c);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
        if (value == null) {
            IntegerType.INSTANCE.nullSafeSet(st, null, index);
        } else if (value instanceof LldpChassisIdSubType){
            IntegerType.INSTANCE.nullSafeSet(st, ((LldpChassisIdSubType)value).getValue(), index);
        }
    }

    @Override
    public Class<LldpChassisIdSubType> returnedClass() {
        return LldpChassisIdSubType.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public void setParameterValues(Properties parameters) {
    }
}
