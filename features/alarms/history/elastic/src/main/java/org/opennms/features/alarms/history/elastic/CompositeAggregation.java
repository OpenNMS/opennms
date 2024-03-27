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
package org.opennms.features.alarms.history.elastic;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.KEY;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.searchbox.core.search.aggregation.Aggregation;
import io.searchbox.core.search.aggregation.Bucket;

/**
 * Used to parse the result of 'composite' aggregation queries since no
 * support for these exists in Jest yet.
 */
public class CompositeAggregation extends Aggregation {
    private static final String AFTER_KEY = "after_key";
    private JsonObject after_key;
    private List<Entry> buckets = new LinkedList<>();

    public CompositeAggregation(String name, JsonObject compositeAggregation) {
        super(name, compositeAggregation);
        after_key = compositeAggregation.getAsJsonObject(AFTER_KEY);
        parseBuckets(compositeAggregation.get(String.valueOf(BUCKETS)).getAsJsonArray());
    }

    private void parseBuckets(JsonArray bucketsSource) {
        for(JsonElement bucketElement : bucketsSource) {
            JsonObject bucket = (JsonObject) bucketElement;
            buckets.add(new Entry(bucket, bucket.get(String.valueOf(KEY)), bucket.get(String.valueOf(DOC_COUNT)).getAsLong()));
        }
    }

    public boolean hasAfterKey() {
        return after_key != null;
    }

    public JsonObject getAfterKey() {
        return after_key;
    }

    public List<Entry> getBuckets() {
        return buckets;
    }

    public class Entry extends Bucket {
        private final Object key;

        public Entry(JsonObject bucket, Object key, Long count) {
            super(bucket, count);
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Entry entry = (Entry) o;
            return Objects.equals(key, entry.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), key);
        }
    }
}
