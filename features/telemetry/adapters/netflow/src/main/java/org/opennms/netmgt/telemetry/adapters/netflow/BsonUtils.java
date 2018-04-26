/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;

public class BsonUtils {

    private BsonUtils() {
    }

    public static Optional<BsonValue> get(final BsonDocument doc, final String... path) {
        BsonValue value = doc;
        for (final String p : path) {
            value = value.asDocument().get(p);
            if (value == null) {
                return Optional.empty();
            }
        }

        return Optional.of(value);
    }

    public static Optional<Long> getInt64(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asInt64).map(BsonInt64::getValue);
    }

    public static Optional<Double> getDouble(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asDouble).map(BsonDouble::getValue);
    }

    public static Optional<Integer> getInt32(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asInt32).map(BsonInt32::getValue);
    }

    public static Optional<String> getString(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asString).map(BsonString::getValue);
    }

    public static <V> Optional<V> first(final Optional<V>... values) {
        return Stream.of(values)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public static Optional<Instant> getTime(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asDocument)
                .map(ts -> {
                    final long epoch = getInt64(ts, "epoch").get();
                    final long nanos = getInt64(ts, "nanos").orElse(0L);

                    return Instant.ofEpochSecond(epoch, nanos);
                });
    }
}
