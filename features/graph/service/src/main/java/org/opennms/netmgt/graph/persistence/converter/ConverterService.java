/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
