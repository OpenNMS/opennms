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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConverterService {

    private final List<Converter<?>> converterRegistry = new ArrayList<>();

    public ConverterService() {
        converterRegistry.add(new PrimitiveConverter<>(Boolean.class, Boolean::valueOf));
        converterRegistry.add(new PrimitiveConverter<>(Float.class, Float::valueOf));
        converterRegistry.add(new PrimitiveConverter<>(Integer.class, Integer::valueOf));
        converterRegistry.add(new PrimitiveConverter<>(Double.class, Double::valueOf));
        converterRegistry.add(new PrimitiveConverter<>(String.class, (string) -> string));
        converterRegistry.add(new PrimitiveConverter<>(Short.class, Short::valueOf));
        converterRegistry.add(new PrimitiveConverter<>(Byte.class, Byte::valueOf));
        converterRegistry.add(new EnumConverter());
        converterRegistry.add(new CollectionConverter(this));
    }

    public <T> Object toValue(Class<T> type, String stringRepresentation) {
        final Converter<T> converter = getConverter(type);
        return converter.toValue(type, stringRepresentation);
    }

    public <T> String toStringRepresentation(Class<?> type, T object) throws IllegalStateException {
        final Converter<T> converter = getConverter(type);
        return converter.toStringRepresentation(object);
    }

    private Converter getConverter(Class<?> clazz) throws IllegalStateException {
        Optional<Converter<?>> converter = converterRegistry.stream().filter(con -> con.canConvert(clazz)).findFirst();
        if (!converter.isPresent()) {
            throw new IllegalStateException("Missing converter for class '" + clazz + "'");
        }
        return converter.get();
    }

}
