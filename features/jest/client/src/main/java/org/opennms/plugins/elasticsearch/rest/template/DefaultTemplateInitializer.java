/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.template;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    private boolean initialized;

    public DefaultTemplateInitializer(final BundleContext bundleContext, final JestClient client, final String templateLocation, final String templateName) {
        this(client, templateLocation, templateName, new CachingTemplateLoader(new OsgiTemplateLoader(bundleContext)));
    }

    public DefaultTemplateInitializer(final BundleContext bundleContext, JestClient client, String templateLocation, String templateName, IndexSettings indexSettings) {
        this(client, templateLocation, templateName, new CachingTemplateLoader(
                new MergingTemplateLoader(new OsgiTemplateLoader(bundleContext), indexSettings)));
    }

    protected DefaultTemplateInitializer(final JestClient client, final String templateLocation, final String templateName, final TemplateLoader templateLoader) {
        this.client = Objects.requireNonNull(client);
        this.templateLocation = templateLocation;
        this.templateName = Objects.requireNonNull(templateName);
        this.templateLoader = Objects.requireNonNull(templateLoader);
    }

    @Override
    public synchronized void initialize() {
        while(!initialized && !Thread.interrupted()) {
            try {
                LOG.debug("Template {} is not initialized. Initializing...", templateName);
                doInitialize();
                initialized = true;
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

        // Post it to elastic
        final PutTemplate putTemplate = new PutTemplate.Builder(templateName, template).build();
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

}
