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
package org.opennms.netmgt.telemetry.protocols.common.utils;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
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
            value = value.isDocument() ? value.asDocument().get(p) : null;
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

    public static Optional<Boolean> getBool(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asBoolean).map(BsonBoolean::getValue);
    }

    public static Optional<Iterable<BsonValue>> getArray(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asArray).map(BsonArray::getValues);
    }

    public static Optional<BsonDocument> getDocument(final BsonDocument doc, final String... path) {
        return get(doc, path).map(BsonValue::asDocument);
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
