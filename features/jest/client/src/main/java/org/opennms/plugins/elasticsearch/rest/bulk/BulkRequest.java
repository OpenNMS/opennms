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

package org.opennms.plugins.elasticsearch.rest.bulk;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.plugins.elasticsearch.rest.BulkResultWrapper;
import org.opennms.plugins.elasticsearch.rest.FailedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.BulkResult;

public class BulkRequest<T> {

    private static int[] SLEEP_TIME = new int[]{ 1000, 5000, 10000, 30000, 60000 };

    private static final Logger LOG = LoggerFactory.getLogger(BulkRequest.class);
    private final JestClient client;
    private final List<T> documents;
    private final Function<List<T>, BulkWrapper> transformer;
    private int retryCount;

    public BulkRequest(final JestClient client, final List<T> documents, final Function<List<T>, BulkWrapper> documentToBulkTransformer, int retryCount) {
        this.client = Objects.requireNonNull(client);
        this.transformer = Objects.requireNonNull(documentToBulkTransformer);
        this.documents = Objects.requireNonNull(documents);
        this.retryCount = retryCount;
    }

    public BulkResultWrapper execute() throws IOException {
        IOException exception = null;
        do {
            try {
                final BulkResultWrapper bulkResultWrapper = executeRequest();
                if (!bulkResultWrapper.isSucceeded()) {
                    final List<FailedItem<T>> failedItems = bulkResultWrapper.getFailedItems(documents);
                    final List<T> failedDocuments = getFailedPages(failedItems);

                    // Log error
                    logError(bulkResultWrapper.getErrorMessage());

                    // Update documents if only some failed
                    if (failedDocuments.size() != documents.size()) {
                        documents.clear();
                        documents.addAll(failedDocuments);
                    }
                }
                return bulkResultWrapper;
            } catch (IOException ex) {
                logError(ex.getMessage());
                exception = ex;
            }
            retryCount--;
            waitBeforeRetrying(retryCount);
            LOG.info("Retrying now ...");
        } while(retryCount > 0);
        throw new IOException("Could not perform bulk operation.", exception);
    }


    private BulkResultWrapper executeRequest() throws IOException {
        final BulkWrapper bulk = transformer.apply(documents);

        // Handle single execution
        if (bulk.size() == 1) {
            final JestResult result = client.execute(bulk.getActions().get(0));
            return new BulkResultWrapper(new BulkResult(result));
        }

        // Handle bulk execute
        final BulkResult bulkResult = client.execute(bulk);
        final BulkResultWrapper bulkResultWrapper = new BulkResultWrapper(bulkResult);
        return bulkResultWrapper;
    }

    private List<T> getFailedPages(List<FailedItem<T>> failedItems) {
        final List<T> failedPages = failedItems.stream().map(item -> item.getItem()).collect(Collectors.toList());
        return failedPages;
    }


    private static void waitBeforeRetrying(int retryCount) {
        // Wait a bit, before actually retrying
        try {
            long sleepTime = getSleepTime(retryCount);
            if (sleepTime > 0) {
                LOG.info("Waiting {} ms before retrying", sleepTime);
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void logError(String errorMessage) {
        LOG.info("An error occurred while executing the bulk request: {}.", errorMessage);
    }

    private static long getSleepTime(int retry) {
        if (retry == 0) return 0;
        if ((retry - 1) > SLEEP_TIME.length - 1) {
            return SLEEP_TIME[SLEEP_TIME.length - 1];
        }
        return SLEEP_TIME[retry - 1];
    }
}
