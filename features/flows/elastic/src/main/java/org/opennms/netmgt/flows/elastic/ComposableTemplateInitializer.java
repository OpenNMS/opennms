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

import org.opennms.features.elastic.client.ElasticRestClient;
import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.template.TemplateInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ComposableTemplateInitializer implements TemplateInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ComposableTemplateInitializer.class);

    private static final long[] COOL_DOWN_TIMES_IN_MS = { 250, 500, 1000, 5000, 10000, 60000 };

    private final AtomicInteger retryCount = new AtomicInteger(0);

    private final ElasticRestClient elasticRestClient;

    private boolean initialized = false;

    private final String templatesPath;

    private final boolean useComposableTemplates;

    public ComposableTemplateInitializer(ElasticRestClient elasticRestClient, String templatesPath, boolean useComposableTemplates) {
        this.elasticRestClient = elasticRestClient;
        this.templatesPath = templatesPath;
        this.useComposableTemplates = useComposableTemplates;
    }

    @Override
    public void initialize() {
        if (!useComposableTemplates) {
            return;
        }
        int count = 0;
        while (!initialized && !Thread.interrupted()) {
            try {
                LOG.debug("Composable Templates are not initialized. Initializing...");
                count = doInitialize();
            } catch (ConnectionPoolShutdownException ex) {
                throw ex; // We cannot recover from this
            } catch (Exception ex) {
                LOG.error("An error occurred while initializing composable templates {}", ex.getMessage(), ex);
            }
            if (count == 0) {
                long coolDownTimeInMs = COOL_DOWN_TIMES_IN_MS[retryCount.get()];
                LOG.debug("Retrying in {} ms", coolDownTimeInMs);
                waitBeforeRetrying(coolDownTimeInMs);
                if (retryCount.get() != COOL_DOWN_TIMES_IN_MS.length - 1) {
                    retryCount.incrementAndGet();
                }
            } else {
                initialized = true;
            }
        }
    }

    private void waitBeforeRetrying(long cooldown) {
        try {
            Thread.sleep(cooldown);
        } catch (InterruptedException e) {
            LOG.warn("Sleep was interrupted", e);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isComposableTemplate() {
        return true;
    }

    private int doInitialize() throws IOException {
        int count = 0;
        elasticRestClient.connect();
        //Path templateDirectory = Paths.get(System.getProperty("opennms.home"), "etc", "/flow/templates");
        File templatesDir = new File(templatesPath);

        if (templatesDir.exists() && templatesDir.isDirectory()) {

            try {
                // Apply all templates from the directory
                count = elasticRestClient.applyAllTemplatesFromDirectory(templatesPath);
                if (count > 0) {
                    LOG.info("Successfully applied {} composable templates from the dir: {}", count, templatesPath);
                }

            } catch (Exception e) {
                LOG.warn("Error while trying to apply composable templates: {} from the dir: {}", e.getMessage(), templatesPath, e);
            }
        }
        return count;
    }
}
