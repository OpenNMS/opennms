/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.template.PutTemplate;

public class ElasticFlowRepositoryInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepositoryInitializer.class);

    private static final String FLOW_TEMPLATE_NAME = "netflow";
    private static final String TEMPLATE_RESOURCE = "/netflow-template.json";
    private static final long[] COOL_DOWN_TIMES_IN_MS = {250, 500, 1000, 5000, 10000, 60000};

    private boolean initialized;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final JestClient client;
    private String template;

    protected ElasticFlowRepositoryInitializer(JestClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public void initialize() {
        while(!initialized && !Thread.interrupted()) {
            try {
                LOG.debug("ElasticFlowRepository is not initialized. Initializing...");
                doInitialize();
                initialized = true;
            } catch (Exception ex) {
                LOG.error("An error occurred while initializing the ElasticFlowRepository: {}.", ex.getMessage(), ex);
                long coolDownTimeInMs = COOL_DOWN_TIMES_IN_MS[retryCount.get()];
                LOG.debug("Retrying in {} ms", coolDownTimeInMs);
                waitBeforeRetrying(coolDownTimeInMs);
                if (retryCount.get() != COOL_DOWN_TIMES_IN_MS.length - 1) {
                    retryCount.incrementAndGet();
                }
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

    private void doInitialize() throws IOException {
        final String template = getTemplate();

        // Post it to elastic
        final PutTemplate putTemplate = new PutTemplate.Builder(FLOW_TEMPLATE_NAME, template).build();
        final JestResult result = client.execute(putTemplate);
        if (!result.isSucceeded()) {
            // In case the template could not be created, we bail
            throw new IllegalStateException("Template '" + FLOW_TEMPLATE_NAME + "' could not be persisted. Reason: " + result.getErrorMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    private String getTemplate() throws IOException {
        if (template == null) { // cache template, so it is only read once
            // Read template
            final InputStream inputStream = getClass().getResourceAsStream(TEMPLATE_RESOURCE);
            if (inputStream == null) {
                throw new NullPointerException("Template from '" + TEMPLATE_RESOURCE +  "' is null");
            }
            final byte[] bytes = new byte[inputStream.available()];
            ByteStreams.readFully(inputStream, bytes);
            template = new String(bytes);
        }
        return template;
    }
}
