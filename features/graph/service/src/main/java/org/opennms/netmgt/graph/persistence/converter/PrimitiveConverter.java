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

package org.opennms.netmgt.graph.persistence.converter;

import java.util.Objects;
import java.util.function.Function;

public class PrimitiveConverter<T> implements Converter<T> {

    private Class <T> clazz;
    private Function<String, T> toValue;

    PrimitiveConverter(Class <T> clazz, Function<String, T> toValue) {
        this.clazz = Objects.requireNonNull(clazz);
        this.toValue = toValue;
    }

    @Override
    public T toValue(Class<T> type, String string) {
        return toValue.apply(string);
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return clazz.isAssignableFrom(type);
    }
}
