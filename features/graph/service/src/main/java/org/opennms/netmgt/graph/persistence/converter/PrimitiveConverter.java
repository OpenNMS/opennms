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
