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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common implementation for initializing Elasticsearch composable templates.
 * This class provides a unified way to initialize templates for all OpenNMS modules
 * that use Elasticsearch, replacing the legacy template system.
 * 
 * Templates are expected to be organized under ${karaf.etc}/elastic/{module}/ directory structure.
 */
public class ComposableTemplateInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ComposableTemplateInitializer.class);

    private static final long[] COOL_DOWN_TIMES_IN_MS = { 250, 500, 1000, 5000, 10000, 60000 };

    private final AtomicInteger retryCount = new AtomicInteger(0);

    private final ElasticRestClient elasticRestClient;

    private boolean initialized = false;

    private final String templatesPath;

    private final String moduleName;

    /**
     * Creates a ComposableTemplateInitializer for a specific module.
     * 
     * @param elasticRestClient the Elasticsearch REST client
     * @param moduleName the name of the module (e.g., "flows", "alarms", "feedback", "events")
     */
    public ComposableTemplateInitializer(ElasticRestClient elasticRestClient, String moduleName) {
        this(elasticRestClient, moduleName, null);
    }

    /**
     * Creates a ComposableTemplateInitializer with a custom templates path.
     * 
     * @param elasticRestClient the Elasticsearch REST client
     * @param moduleName the name of the module (e.g., "flows", "alarms", "feedback", "events")
     * @param customTemplatesPath custom path to templates directory (optional)
     */
    public ComposableTemplateInitializer(ElasticRestClient elasticRestClient, String moduleName, String customTemplatesPath) {
        this.elasticRestClient = elasticRestClient;
        this.moduleName = moduleName;
        this.templatesPath = resolveTemplatesPath(customTemplatesPath);
    }

    public void initialize() {
        int count = 0;
        while (!initialized && !Thread.interrupted()) {
            try {
                LOG.debug("Composable templates for module '{}' are not initialized. Initializing from: {}", moduleName, templatesPath);
                count = doInitialize();
            } catch (RuntimeException ex) {
                // Check if this is an unrecoverable error
                if (ex.getMessage() != null && ex.getMessage().contains("shutdown")) {
                    throw ex; // We cannot recover from shutdown
                }
            } catch (Exception ex) {
                LOG.error("An error occurred while initializing composable templates for module '{}': {}", moduleName, ex.getMessage(), ex);
            }
            if (count == 0) {
                long coolDownTimeInMs = COOL_DOWN_TIMES_IN_MS[retryCount.get()];
                LOG.debug("Retrying template initialization for module '{}' in {} ms", moduleName, coolDownTimeInMs);
                waitBeforeRetrying(coolDownTimeInMs);
                if (retryCount.get() != COOL_DOWN_TIMES_IN_MS.length - 1) {
                    retryCount.incrementAndGet();
                }
            } else {
                initialized = true;
                LOG.info("Successfully initialized composable templates for module '{}'", moduleName);
            }
        }
    }

    /**
     * Resolves the templates path for the module.
     * If customTemplatesPath is provided, uses that path.
     * Otherwise, defaults to ${karaf.etc}/elastic/{moduleName}/
     * 
     * @param customTemplatesPath optional custom path
     * @return resolved templates path
     */
    private String resolveTemplatesPath(String customTemplatesPath) {
        if (customTemplatesPath != null && !customTemplatesPath.isBlank() && !customTemplatesPath.contains("${")) {
            return customTemplatesPath;
        }
        
        // Default path: ${karaf.etc}/elastic/{moduleName}/
        Path pathForTemplates = Paths.get(System.getProperty("karaf.etc"), "elastic", moduleName);
        return pathForTemplates.toString();
    }

    private void waitBeforeRetrying(long cooldown) {
        try {
            Thread.sleep(cooldown);
        } catch (InterruptedException e) {
            LOG.warn("Sleep was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isComposableTemplate() {
        return true;
    }

    /**
     * Performs the actual template initialization by applying all templates from the templates directory.
     * 
     * @return number of templates successfully applied
     * @throws IOException if an I/O error occurs
     */
    private int doInitialize() throws IOException {
        int count = 0;
        File templatesDir = new File(templatesPath);

        if (!templatesDir.exists()) {
            LOG.warn("Templates directory does not exist for module '{}': {}", moduleName, templatesPath);
            return count;
        }

        if (!templatesDir.isDirectory()) {
            LOG.warn("Templates path is not a directory for module '{}': {}", moduleName, templatesPath);
            return count;
        }

        try {
            // Apply all templates from the directory
            // ElasticRestClient will handle the proper ordering: ILM policies -> Component templates -> Index templates
            count = elasticRestClient.applyAllTemplatesFromDirectory(templatesPath);
            if (count > 0) {
                LOG.info("Successfully applied {} composable templates for module '{}' from directory: {}", count, moduleName, templatesPath);
            } else {
                LOG.warn("No templates found in directory for module '{}': {}", moduleName, templatesPath);
            }
        } catch (Exception e) {
            LOG.warn("Error while applying composable templates for module '{}': {} from directory: {}", moduleName, e.getMessage(), templatesPath, e);
            throw e;
        }
        
        return count;
    }

    /**
     * Gets the module name this initializer is configured for.
     * 
     * @return the module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Gets the resolved templates path.
     * 
     * @return the templates path
     */
    public String getTemplatesPath() {
        return templatesPath;
    }
}