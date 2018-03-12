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

package org.opennms.plugins.elasticsearch.rest.bulk;

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
        final List<FailedItem<T>> failedItems = new ArrayList<>();
        for (int i = 0; i< rawResult.getItems().size(); i++) {
            final BulkResult.BulkResultItem bulkResultItem = rawResult.getItems().get(i);
            if (bulkResultItem.error != null && !bulkResultItem.error.isEmpty()) {
                final Exception cause = BulkUtils.convertToException(bulkResultItem.error);
                final T failedObject = documents.get(i);
                final FailedItem failedItem = new FailedItem(i, failedObject, cause);
                failedItems.add(failedItem);
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
