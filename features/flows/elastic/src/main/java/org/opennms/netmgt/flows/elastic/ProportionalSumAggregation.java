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
package org.opennms.netmgt.flows.elastic;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.KEY;
import static io.searchbox.core.search.aggregation.AggregationField.KEY_AS_STRING;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.searchbox.core.search.aggregation.Aggregation;
import io.searchbox.core.search.aggregation.AggregationField;
import io.searchbox.core.search.aggregation.Bucket;

public class ProportionalSumAggregation extends Aggregation {

    public static final String TYPE = "partial_sum";

    private List<ProportionalSumAggregation.DateHistogram> dateHistograms = new LinkedList<ProportionalSumAggregation.DateHistogram>();

    public ProportionalSumAggregation(String name, JsonObject PartialSumAggregation) {
        super(name, PartialSumAggregation);
        if (PartialSumAggregation.has(String.valueOf(BUCKETS)) && PartialSumAggregation.get(String.valueOf(BUCKETS)).isJsonArray()) {
            parseBuckets(PartialSumAggregation.get(String.valueOf(BUCKETS)).getAsJsonArray());
        }
    }

    private void parseBuckets(JsonArray bucketsSource) {
        for (JsonElement bucket : bucketsSource) {
            Long time = bucket.getAsJsonObject().get(String.valueOf(KEY)).getAsLong();
            String timeAsString = bucket.getAsJsonObject().get(String.valueOf(KEY_AS_STRING)).getAsString();
            Long count = bucket.getAsJsonObject().get(String.valueOf(DOC_COUNT)).getAsLong();
            Double value = bucket.getAsJsonObject().get(String.valueOf(AggregationField.VALUE)).getAsDouble();

            dateHistograms.add(new ProportionalSumAggregation.DateHistogram(bucket.getAsJsonObject(), time, timeAsString, value, count));
        }
    }

    /**
     * @return List of DateHistogram objects if found, or empty list otherwise
     */
    public List<ProportionalSumAggregation.DateHistogram> getBuckets() {
        return dateHistograms;
    }

    public class DateHistogram extends Histogram {

        private String timeAsString;
        private Double value;

        DateHistogram(JsonObject bucket, Long time, String timeAsString, Double value, Long count) {
            super(bucket, time, count);
            this.timeAsString = timeAsString;
            this.value = value;
        }

        public Long getTime() {
            return getKey();
        }

        public String getTimeAsString() {
            return timeAsString;
        }

        public Double getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }

            ProportionalSumAggregation.DateHistogram rhs = (ProportionalSumAggregation.DateHistogram) obj;
            return super.equals(obj) && Objects.equals(timeAsString, rhs.timeAsString) && Objects.equals(value, rhs.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), timeAsString);
        }

        @Override
        public String toString() {
            return "DateHistogram{" +
                    "timeAsString='" + timeAsString + '\'' +
                    ", value=" + value +
                    ", count=" + count +
                    '}';
        }
    }

    public static class Histogram extends Bucket {

        private Long key;

        Histogram(JsonObject bucket, Long key, Long count) {
            super(bucket, count);
            this.key = key;
        }

        public Long getKey() {
            return key;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }

            Histogram rhs = (Histogram) obj;
            return super.equals(obj) && Objects.equals(key, rhs.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), key);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        ProportionalSumAggregation rhs = (ProportionalSumAggregation) obj;
        return super.equals(obj) && Objects.equals(dateHistograms, rhs.dateHistograms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dateHistograms);
    }
}
