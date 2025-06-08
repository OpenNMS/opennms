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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.RootAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.opennms.features.elastic.client.model.SearchResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help parse aggregations from native Elasticsearch SearchResponse.
 * This provides similar functionality to Jest's aggregation parsing.
 */
public class AggregationUtils {
    
    /**
     * Convert SearchResponse to MetricAggregation format expected by existing code.
     */
    public static MetricAggregation toMetricAggregation(SearchResponse response) {
        if (response.getAggregations() == null) {
            return null;
        }
        
        // Create a RootAggregation from the JSON
        RootAggregation root = new RootAggregation("root", response.getAggregations());
        return root;
    }
    
    /**
     * Extract TermsAggregation from aggregations JSON.
     */
    public static TermsAggregation getTermsAggregation(JsonObject aggregations, String name) {
        if (aggregations == null || !aggregations.has(name)) {
            return null;
        }
        
        JsonObject termsAgg = aggregations.getAsJsonObject(name);
        return new TermsAggregation(name, termsAgg);
    }
    
    /**
     * Extract ProportionalSumAggregation from aggregations JSON.
     */
    public static ProportionalSumAggregation getProportionalSumAggregation(JsonObject aggregations, String name) {
        if (aggregations == null || !aggregations.has(name)) {
            return null;
        }
        
        JsonObject sumAgg = aggregations.getAsJsonObject(name);
        return new ProportionalSumAggregation(name, sumAgg);
    }
    
    /**
     * Get the first hit source from SearchResponse.
     */
    public static JsonObject getFirstHitSource(SearchResponse response) {
        if (response.getHits() != null && 
            response.getHits().getHits() != null && 
            !response.getHits().getHits().isEmpty()) {
            return response.getHits().getHits().get(0).getSource();
        }
        return null;
    }
    
    /**
     * Extract bucket list from terms aggregation.
     */
    public static List<JsonObject> getTermsBuckets(JsonObject aggregations, String aggName) {
        List<JsonObject> buckets = new ArrayList<>();
        
        if (aggregations != null && aggregations.has(aggName)) {
            JsonObject termsAgg = aggregations.getAsJsonObject(aggName);
            if (termsAgg.has("buckets") && termsAgg.get("buckets").isJsonArray()) {
                JsonArray bucketsArray = termsAgg.getAsJsonArray("buckets");
                for (JsonElement bucket : bucketsArray) {
                    if (bucket.isJsonObject()) {
                        buckets.add(bucket.getAsJsonObject());
                    }
                }
            }
        }
        
        return buckets;
    }
}