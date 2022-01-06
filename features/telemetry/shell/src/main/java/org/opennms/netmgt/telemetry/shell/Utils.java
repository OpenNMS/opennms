/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.shell;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Utils {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .create();

    /**
     * Returns the values for all bean properties that are writable.
     *
     * @param bean the bean to get the properties for
     * @return a map from property name to property value
     */
    public static JsonObject getWritableProperties(final Object bean) {
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);

        final JsonObject result = new JsonObject();
        for (final PropertyDescriptor desc : wrapper.getPropertyDescriptors()) {
            if (desc.getReadMethod() == null || desc.getWriteMethod() == null) {
                continue;
            }

            try {
                result.add(desc.getDisplayName(),
                           GSON.toJsonTree(desc.getReadMethod().invoke(bean)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw Throwables.propagate(e);
            }
        }

        return result;
    }

    public static class DurationTypeAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(final JsonWriter out, final Duration value) throws IOException {
            out.value(value.getSeconds());
        }

        @Override
        public Duration read(final JsonReader in) throws IOException {
            return Duration.ofSeconds(in.nextLong());
        }
    }
}
