/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.persistence.converter;

import java.util.HashMap;
import java.util.Map;

public class ConverterService {

    private final Map<Class<?>, Converter<?>> converterRegistry = new HashMap<>();

        // TODO MVR we probably want import the converters via osgi?!
    public ConverterService() {
        converterRegistry.put(Boolean.class, (type, string) -> Boolean.valueOf(string));
        converterRegistry.put(Float.class, (type, string) -> Float.valueOf(string));
        converterRegistry.put(Integer.class, (type, string) -> Integer.valueOf(string));
        converterRegistry.put(Double.class, (type, string) -> Double.valueOf(string));
        converterRegistry.put(String.class, (type, string) -> string);
        converterRegistry.put(Short.class, (type, string) -> Short.valueOf(string));
        converterRegistry.put(Byte.class, (type, string) -> Byte.valueOf(string));
        converterRegistry.put(Enum.class, (Converter<Enum>) (type, string) -> Enum.valueOf(type, string));
    }

    public <T> Object toValue(Class<T> type, String stringRepresentation) {
        final Converter<T> converter = getConverter(type);
        final T value = converter.toValue(type, stringRepresentation);
        return value;
    }

    // TODO MVR logic to transform to string represantation should probably live in converter at some point
    public String toStringRepresentation(Class<?> type, Object value) throws IllegalStateException {
        return type.isEnum() ? ((Enum) value).name() : value.toString();
    }

    private Converter getConverter(Class type) throws IllegalStateException {
        final Converter<?> converter = type.isEnum()
                ? converterRegistry.get(Enum.class)
                : converterRegistry.get(type);
        if (converter == null) {
            throw new IllegalStateException("Missing converter for class '" + type + "'");
        }
        return converter;
    }

}
