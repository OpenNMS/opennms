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
package org.opennms.features.jest.client.template;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Ping;
import io.searchbox.indices.template.PutTemplate;

public class DefaultTemplateInitializer implements TemplateInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTemplateInitializer.class);

    private static final long[] COOL_DOWN_TIMES_IN_MS = { 250, 500, 1000, 5000, 10000, 60000 };

    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final JestClient client;
    private final TemplateLoader templateLoader;
    private final String templateLocation;
    private final String templateName;

    private final IndexSettings indexSettings;

    private boolean initialized;

    public DefaultTemplateInitializer(final BundleContext bundleContext, final JestClient client, final String templateLocation, final String templateName) {
        this(client, templateLocation, templateName, new CachingTemplateLoader(new OsgiTemplateLoader(bundleContext)), new IndexSettings());
    }

    public DefaultTemplateInitializer(final BundleContext bundleContext, JestClient client, String templateLocation, String templateName, IndexSettings indexSettings) {
        this(client, templateLocation, templateName, new CachingTemplateLoader(
                new MergingTemplateLoader(new OsgiTemplateLoader(bundleContext), indexSettings)), indexSettings);
    }

    protected DefaultTemplateInitializer(final JestClient client, final String templateLocation, final String templateName, final TemplateLoader templateLoader, final IndexSettings indexSettings) {
        this.client = Objects.requireNonNull(client);
        this.templateLocation = templateLocation;
        this.templateName = Objects.requireNonNull(templateName);
        this.templateLoader = Objects.requireNonNull(templateLoader);
        this.indexSettings = Objects.requireNonNull(indexSettings);
    }

    @Override
    public synchronized void initialize() {
        while(!initialized && !Thread.interrupted()) {
            try {
                LOG.debug("Template {} is not initialized. Initializing...", templateName);
                doInitialize();
                initialized = true;
            } catch (ConnectionPoolShutdownException ex) {
                throw ex; // We cannot recover from this
            } catch (Exception ex) {
                LOG.error("An error occurred while initializing template {}: {}.", templateName, ex.getMessage(), ex);
                long coolDownTimeInMs = COOL_DOWN_TIMES_IN_MS[retryCount.get()];
                LOG.debug("Retrying in {} ms", coolDownTimeInMs);
                waitBeforeRetrying(coolDownTimeInMs);
                if (retryCount.get() != COOL_DOWN_TIMES_IN_MS.length - 1) {
                    retryCount.incrementAndGet();
                }
            }
        }
    }

    @Override
    public synchronized boolean isInitialized() {
        return initialized;
    }

    private void waitBeforeRetrying(long cooldown) {
        try {
            Thread.sleep(cooldown);
        } catch (InterruptedException e) {
            LOG.warn("Sleep was interrupted", e);
        }
    }

    private void doInitialize() throws IOException {
        // Retrieve the server version
        final Version version = getServerVersion();
        // Load the appropriate template
        final String template = templateLoader.load(version, templateLocation);

        // Apply the index prefix to the template name as well so that templates from multiple instances
        // do not overwrite each other
        String effectiveTemplateName = templateName;
        if (indexSettings.getIndexPrefix() != null) {
            effectiveTemplateName = indexSettings.getIndexPrefix() + templateName;
        }

        // Post it to elastic
        final PutTemplate putTemplate = new PutTemplate.Builder(effectiveTemplateName, template).build();
        final JestResult result = client.execute(putTemplate);
        if (!result.isSucceeded()) {
            // In case the template could not be created, we bail
            throw new IllegalStateException("Template '" + templateName + "' could not be persisted. Reason: " + result.getErrorMessage());
        }
    }

    private Version getServerVersion() throws IOException {
        final Ping ping = new Ping.Builder().build();
        final JestResult result = client.execute(ping);
        if (!result.isSucceeded()) {
            throw new IllegalStateException("Ping failed. Template '" + templateName + "' will not be persisted. Reason: " + result.getErrorMessage());
        }

        final JsonObject responseJson = result.getJsonObject();
        final JsonObject versionDetails = responseJson.getAsJsonObject("version");
        if (versionDetails == null) {
            throw new IllegalStateException("Ping response does not contain version: " + responseJson);
        }
        final JsonElement versionEl = versionDetails.get("number");
        if (versionEl == null) {
            throw new IllegalStateException("Ping response does not contain version number: " + responseJson);
        }
        final String versionNumber = versionEl.getAsString();
        return Version.fromVersionString(versionNumber);
    }

    public IndexSettings getIndexSettings() {
        return indexSettings;
    }
}
