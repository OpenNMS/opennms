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

package org.opennms.features.elastic.client;

import java.io.IOException;

public class ElasticRestClientFactory {

    private final String elasticHosts;
    private final String elasticUsername;
    private final String elasticPassword;
    
    // Configuration settings with defaults
    private int connTimeout = 5000; // 5 seconds default
    private int readTimeout = 30000; // 30 seconds default
    private int bulkRetryCount = 5; // Default retry count
    private int retryCooldown = 500; // Default retry cooldown in ms

    private ElasticRestClient elasticRestClient;

    public ElasticRestClientFactory(String elasticHosts, String elasticUsername, String elasticPassword) {
        this.elasticHosts = elasticHosts;
        this.elasticUsername = elasticUsername;
        this.elasticPassword = elasticPassword;
    }

    public ElasticRestClient createClient() {
        if (this.elasticRestClient == null) {
            DefaultElasticRestClient client = new DefaultElasticRestClient(elasticHosts, elasticUsername, elasticPassword);
            
            // Apply configuration settings
            client.setBulkRetryCount(bulkRetryCount);
            client.setConnTimeout(connTimeout);
            client.setReadTimeout(readTimeout);
            client.setRetryCooldown(retryCooldown);
            
            this.elasticRestClient = client;
        }
        return elasticRestClient;
    }

    public void closeClient() throws IOException {
        if (elasticRestClient != null) {
            elasticRestClient.close();
        }
    }

    public ElasticRestClient getClient() {
        return elasticRestClient;
    }
    
    // Configuration setters for blueprint injection
    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public void setBulkRetryCount(int bulkRetryCount) {
        this.bulkRetryCount = bulkRetryCount;
    }
    
    public void setRetryCooldown(int retryCooldown) {
        this.retryCooldown = retryCooldown;
    }
}
