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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public enum Format {
    PLAIN {
        private Stream<String> indent(final String first, final String other, final Stream<String> input) {
            final var state = new AtomicBoolean(true);
            return input.map(line -> String.format("%s%s",
                                                   state.compareAndSet(true, false)
                                                   ? first
                                                   : other,
                                                   line));
        }

        private Stream<String> object(final JsonObject data) {
            return data.entrySet().stream()
                       .flatMap(entry -> {
                           final var name = StringUtils.capitalize(
                                   StringUtils.join(
                                           StringUtils.splitByCharacterTypeCamelCase(entry.getKey()),
                                           StringUtils.SPACE));

                           if (entry.getValue().isJsonNull()) {
                               return Stream.empty();
                           }

                           if (entry.getValue().isJsonPrimitive()) {
                               return Stream.of(String.format("%s = %s", name, entry.getValue().getAsJsonPrimitive().getAsString()));
                           }

                           if (entry.getValue().isJsonArray()) {
                               return Stream.concat(
                                       Stream.of(String.format("%s:", name)),
                                       indent("  ", "  ", array(entry.getValue().getAsJsonArray())));
                           }

                           if (entry.getValue().isJsonObject()) {
                               return Stream.concat(
                                       Stream.of(String.format("%s:", name)),
                                       indent("  ", "  ", object(entry.getValue().getAsJsonObject())));
                           }

                           throw new IllegalArgumentException(entry.getValue().toString());
                       });

        }

        private Stream<String> array(final JsonArray data) {
            return StreamSupport.stream(data.spliterator(), false)
                                .flatMap(element -> {
                                    if (element.isJsonNull()) {
                                        return Stream.empty();
                                    }

                                    if (element.isJsonPrimitive()) {
                                        return Stream.of(String.format("- %s", element.getAsJsonPrimitive().getAsString()));
                                    }

                                    if (element.isJsonObject()) {
                                        return indent("- ", "  ", object(element.getAsJsonObject()));
                                    }

                                    if (element.isJsonArray()) {
                                        return indent("- ", "  ", array(element.getAsJsonArray()));
                                    }

                                    throw new IllegalArgumentException(element.toString());
                                });
        }

        @Override
        public void print(final List<JsonObject> data) {
            data.forEach(element -> {
                final JsonObject json = Utils.GSON.toJsonTree(element)
                                                  .getAsJsonObject();

                object(json).forEach(System.out::println);
                System.out.println();
            });
        }
    },

    JSON {
        @Override
        public void print(final List<JsonObject> data) {
            final String json = Utils.GSON.toJson(data);
            System.out.println(json);
        }
    },

    ;

    public abstract void print(final List<JsonObject> data);
}
