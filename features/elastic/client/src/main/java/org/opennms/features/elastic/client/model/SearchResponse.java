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
package org.opennms.features.elastic.client.model;

import com.google.gson.JsonObject;
import java.util.List;

/**
 * Represents the response from a search operation.
 */
public class SearchResponse {
    
    private final long tookInMillis;
    private final boolean timedOut;
    private final JsonObject shards;
    private final SearchHits hits;
    private final JsonObject aggregations;
    private final String scrollId;
    
    public SearchResponse(long tookInMillis, boolean timedOut, JsonObject shards, 
                         SearchHits hits, JsonObject aggregations, String scrollId) {
        this.tookInMillis = tookInMillis;
        this.timedOut = timedOut;
        this.shards = shards;
        this.hits = hits;
        this.aggregations = aggregations;
        this.scrollId = scrollId;
    }
    
    public long getTookInMillis() {
        return tookInMillis;
    }
    
    public boolean isTimedOut() {
        return timedOut;
    }
    
    public JsonObject getShards() {
        return shards;
    }
    
    public SearchHits getHits() {
        return hits;
    }
    
    public JsonObject getAggregations() {
        return aggregations;
    }
    
    public String getScrollId() {
        return scrollId;
    }
    
    public static class SearchHits {
        private final long totalHits;
        private final String totalHitsRelation;
        private final double maxScore;
        private final List<SearchHit> hits;
        
        public SearchHits(long totalHits, String totalHitsRelation, double maxScore, List<SearchHit> hits) {
            this.totalHits = totalHits;
            this.totalHitsRelation = totalHitsRelation;
            this.maxScore = maxScore;
            this.hits = hits;
        }
        
        public long getTotalHits() {
            return totalHits;
        }
        
        public String getTotalHitsRelation() {
            return totalHitsRelation;
        }
        
        public double getMaxScore() {
            return maxScore;
        }
        
        public List<SearchHit> getHits() {
            return hits;
        }
    }
    
    public static class SearchHit {
        private final String index;
        private final String id;
        private final double score;
        private final JsonObject source;
        
        public SearchHit(String index, String id, double score, JsonObject source) {
            this.index = index;
            this.id = id;
            this.score = score;
            this.source = source;
        }
        
        public String getIndex() {
            return index;
        }
        
        public String getId() {
            return id;
        }
        
        public double getScore() {
            return score;
        }
        
        public JsonObject getSource() {
            return source;
        }
    }
}