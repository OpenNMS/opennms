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
package org.opennms.features.jest.client.bulk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.core.BulkResult;

public class BulkRequest<T> {

    public static long[] SLEEP_TIME = new long[]{ 500, 1000, 5000, 10000, 30000, 60000 };

    private static final Logger LOG = LoggerFactory.getLogger(BulkRequest.class);
    private final JestClientWithCircuitBreaker client;
    private final List<T> documents;
    private final Function<List<T>, BulkWrapper> transformer;
    private final int retryCount;
    private int retries = 0;
    private BulkWrapper bulkAction;

    public BulkRequest(final JestClientWithCircuitBreaker client, final List<T> documents, final Function<List<T>, BulkWrapper> documentToBulkTransformer, int retryCount) {
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
        return retries < retryCount - 1;
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
