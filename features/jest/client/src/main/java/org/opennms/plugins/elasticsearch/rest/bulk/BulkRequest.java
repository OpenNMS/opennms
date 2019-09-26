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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.core.BulkResult;

public class BulkRequest<T> {

    public static long[] SLEEP_TIME = new long[]{ 500, 1000, 5000, 10000, 30000, 60000 };

    private static final Logger LOG = LoggerFactory.getLogger(BulkRequest.class);
    private final JestClient client;
    private final List<T> documents;
    private final Function<List<T>, BulkWrapper> transformer;
    private final int retryCount;
    private int retries = 0;
    private BulkWrapper bulkAction;

    public BulkRequest(final JestClient client, final List<T> documents, final Function<List<T>, BulkWrapper> documentToBulkTransformer, int retryCount) {
        this.client = Objects.requireNonNull(client);
        this.transformer = Objects.requireNonNull(documentToBulkTransformer);
        this.documents = new ArrayList<>(Objects.requireNonNull(documents));
        this.retryCount = retryCount;
    }

    public BulkResultWrapper execute() throws IOException {
        do {
            try {
                final BulkResultWrapper bulkResultWrapper = executeRequest();
                if (bulkResultWrapper.isSucceeded()) {
                    return bulkResultWrapper;
                }
                // Handle errors
                final List<T> failedDocuments = bulkResultWrapper.getFailedDocuments();
                logError(bulkResultWrapper.getErrorMessage());

                // bail if retry is not possible
                if (!canRetry()) {
                    throw new BulkException(bulkResultWrapper);
                }

                // Update documents if only some failed
                if (!failedDocuments.isEmpty() && failedDocuments.size() != documents.size()) {
                    documents.clear();
                    documents.addAll(failedDocuments);
                }
            } catch (IOException ex) {
                // Prevent wrapping the BulkException
                // in an IOException and log twice
                if (ex instanceof BulkException) {
                    throw ex;
                }
                // Probably ConnectionTimeout, log and bail if no retries are left
                logError(ex.getMessage());
                if (!canRetry()) {
                    throw new BulkException(ex);
                }
            }
            retries++;
            waitBeforeRetrying(retries);
            LOG.info("Retrying now ...");
        } while(retries != retryCount);
        throw new IllegalStateException("The execution of the bulk request should have failed.");
    }

    private boolean canRetry() {
        return retries < retryCount -1;
    }


    private BulkResultWrapper executeRequest() throws IOException {
        // Create bulk action
        bulkAction = createBulk(bulkAction, documents);

        // The bulk action list may be empty.
        // In this case, we do not send any request to elastic, as this would raise an exception
        // Instead we fake an EMPTY / SUCCESS result
        if (bulkAction.isEmpty()) {
            return new EmptyResult();
        }

        // Handle bulk execute
        final BulkResult bulkResult = client.execute(bulkAction);
        final BulkResultWrapper bulkResultWrapper = new DefaultBulkResult<>(bulkResult, documents);
        return bulkResultWrapper;
    }

    // This creates a new (smaller) bulk action if the new document list is smaller than the bulk.actions
    // This can only be the case if less than all documents failed.
    private BulkWrapper createBulk(BulkWrapper bulk, List<T> documents) {
        if (bulk == null || bulk.size() != documents.size()) {
            return transformer.apply(documents);
        }
        return bulk;
    }

    private static void waitBeforeRetrying(int retries) {
        // Wait a bit, before actually retrying
        try {
            long sleepTime = getSleepTime(retries);
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

    public static long getSleepTime(int retry) {
        int index = Math.min(retry, SLEEP_TIME.length - 1);
        return SLEEP_TIME[index];
    }
}
