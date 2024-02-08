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
