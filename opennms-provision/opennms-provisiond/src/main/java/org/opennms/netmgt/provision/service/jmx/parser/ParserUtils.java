/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.jmx.parser;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.lang.ClassUtils;
import org.opennms.netmgt.provision.service.jmx.annotation.Composite;
import org.opennms.netmgt.provision.service.jmx.annotation.CompositeField;
import org.opennms.netmgt.provision.service.jmx.annotation.Description;

import com.google.common.base.Strings;

public class ParserUtils {

    // Supported simple types.
    private static final Map<String, OpenType> TYPE_MAP = new HashMap<>();
    static {
        TYPE_MAP.put("java.lang.Void", SimpleType.VOID);
        TYPE_MAP.put("java.lang.Boolean", SimpleType.BOOLEAN);
        TYPE_MAP.put("java.lang.Character", SimpleType.CHARACTER);
        TYPE_MAP.put("java.lang.Byte", SimpleType.BYTE);
        TYPE_MAP.put("java.lang.Short", SimpleType.SHORT);
        TYPE_MAP.put("java.lang.Integer", SimpleType.INTEGER);
        TYPE_MAP.put("java.lang.Long", SimpleType.LONG);
        TYPE_MAP.put("java.lang.Float", SimpleType.FLOAT);
        TYPE_MAP.put("java.lang.Double", SimpleType.DOUBLE);
        TYPE_MAP.put("java.lang.String", SimpleType.STRING);
        TYPE_MAP.put("java.math.BigDecimal", SimpleType.BIGDECIMAL);
        TYPE_MAP.put("java.math.BigInteger", SimpleType.BIGINTEGER);
        TYPE_MAP.put("java.util.Date", SimpleType.DATE);
        TYPE_MAP.put("javax.management.ObjectName", SimpleType.OBJECTNAME);
    }

    public static OpenType getType(Class<?> type) {
        if (!isSupportedType(type)) {
            throw new IllegalStateException("Cannot convert " + type + " to OpenType.");
        }
        return TYPE_MAP.get(ClassUtils.primitiveToWrapper(type).getName());
    }

    public static boolean isSupportedType(Class<?> type) {
        final Class<?> convertedClass = ClassUtils.primitiveToWrapper(type);
        boolean isSupported = TYPE_MAP.containsKey(convertedClass.getName());
        return isSupported;
    }

    public static String getDescription(Description description) {
        return getDescription(description, "No description defined");
    }

    public static String getDescription(Description description, String defaultValue) {
        if (description != null && !Strings.isNullOrEmpty(description.value())) {
            return description.value();
        }
        return defaultValue;
    }

    public static String getName(Composite composite, String defaultValue) {
        if (composite != null && !Strings.isNullOrEmpty(composite.name())) {
            return composite.name();
        }
        return defaultValue;
    }

    public static String getName(CompositeField compositeField, String defaultValue) {
        if (compositeField != null && !Strings.isNullOrEmpty(compositeField.name())) {
            return compositeField.name();
        }
        return defaultValue;
    }

    public static String getName(CompositeField compositeField) {
        return getName(compositeField, null);
    }
}
