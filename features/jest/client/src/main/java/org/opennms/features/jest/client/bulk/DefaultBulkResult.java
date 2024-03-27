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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.searchbox.core.BulkResult;

public class DefaultBulkResult<T> implements BulkResultWrapper {

    private final BulkResult rawResult;

    private final List<T> documents;

    public DefaultBulkResult(BulkResult raw, List<T> documents) {
        this.rawResult = Objects.requireNonNull(raw);
        this.documents = new ArrayList<>(Objects.requireNonNull(documents));
    }

    @Override
    public boolean isSucceeded() {
        return rawResult.isSucceeded();
    }

    @Override
    public String getErrorMessage() {
        return rawResult.getErrorMessage();
    }

    @Override
    public List<FailedItem<T>> getFailedItems() {
        int j = 0;
        final List<FailedItem<T>> failedItems = new ArrayList<>();
        for (int i = 0; i< rawResult.getItems().size(); i++) {
            final BulkResult.BulkResultItem bulkResultItem = rawResult.getItems().get(i);
            if (bulkResultItem.error != null && !bulkResultItem.error.isEmpty()) {
                final Exception cause = BulkUtils.convertToException(bulkResultItem.error);
                final T failedObject = documents.get(j);
                final FailedItem failedItem = new FailedItem(j, failedObject, cause);
                failedItems.add(failedItem);
                j++;
            }
        }
        return failedItems;
    }

    @Override
    public BulkResult getRawResult() {
        return rawResult;
    }

    @Override
    public List<T> getFailedDocuments() {
        return getFailedItems().stream().map(item -> item.getItem()).collect(Collectors.toList());
    }
}
