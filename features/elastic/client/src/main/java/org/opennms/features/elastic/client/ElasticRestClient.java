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
import java.util.Map;

/**
 * Interface for an Elasticsearch client that can connect to ElasticSearch
 * and apply composable templates dynamically.
 */
public interface ElasticRestClient {

    /**
     * Connects to the Elasticsearch cluster.
     *
     * @throws IOException if a connection error occurs
     */
    void connect() throws IOException;

    /**
     * Lists all templates currently registered in the Elasticsearch cluster.
     *
     * @return a map of template names to template definitions
     * @throws IOException if a connection error occurs
     */
    Map<String, String> listTemplates() throws IOException;

    /**
     * Checks if the client is connected to the Elasticsearch cluster.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();


    /**
     * Closes the client and releases any resources.
     *
     * @throws IOException if an error occurs during close
     */
    void close() throws IOException;

    /**
     * Applies an ILM policy to Elasticsearch.
     */
    boolean applyILMPolicy(String policyName, String policyBody) throws IOException;

    /**
     * Applies a component template to Elasticsearch.
     */
    boolean applyComponentTemplate(String componentName, String componentBody) throws IOException;

    /**
     * Applies a composable index template to Elasticsearch.
     */
    boolean applyComposableIndexTemplate(String templateName, String templateBody) throws IOException;

    /**
     * Loads and applies all templates from configured directories.
     */
    int applyAllTemplatesFromDirectory(String templateDirectory) throws IOException;
}