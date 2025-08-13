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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a search request to Elasticsearch.
 */
public class SearchRequest {
    
    private final List<String> indices;
    private final String query;
    private final Map<String, String> parameters;
    
    public SearchRequest(List<String> indices, String query) {
        this(indices, query, new HashMap<>());
    }
    
    public SearchRequest(List<String> indices, String query, Map<String, String> parameters) {
        this.indices = indices;
        this.query = query;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
    }
    
    public static SearchRequest forIndex(String index, String query) {
        return new SearchRequest(List.of(index), query);
    }
    
    public static SearchRequest forIndices(List<String> indices, String query) {
        return new SearchRequest(indices, query);
    }
    
    public List<String> getIndices() {
        return indices;
    }
    
    public String getQuery() {
        return query;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public SearchRequest addParameter(String key, String value) {
        this.parameters.put(key, value);
        return this;
    }
    
    public SearchRequest size(int size) {
        return addParameter("size", String.valueOf(size));
    }
    
    public SearchRequest from(int from) {
        return addParameter("from", String.valueOf(from));
    }
    
    public SearchRequest timeout(String timeout) {
        return addParameter("timeout", timeout);
    }
    
    public SearchRequest ignoreUnavailable(boolean ignore) {
        return addParameter("ignore_unavailable", String.valueOf(ignore));
    }
    
    public SearchRequest allowNoIndices(boolean allow) {
        return addParameter("allow_no_indices", String.valueOf(allow));
    }
}