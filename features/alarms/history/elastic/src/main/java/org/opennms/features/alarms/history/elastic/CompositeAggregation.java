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
