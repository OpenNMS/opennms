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
