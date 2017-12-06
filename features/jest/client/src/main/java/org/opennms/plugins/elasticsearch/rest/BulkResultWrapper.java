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

package org.opennms.plugins.elasticsearch.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.searchbox.core.BulkResult;

public class BulkResultWrapper {

    private final BulkResult result;

    public BulkResultWrapper(BulkResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public boolean isSucceeded() {
        return result.isSucceeded();
    }

    public String getErrorMessage() {
        return result.getErrorMessage();
    }

    public <T> List<FailedItem<T>> getFailedItems(List<T> items) {
        final List<FailedItem<T>> failedItems = new ArrayList<>();
        for (int i=0; i<result.getItems().size(); i++) {
            final BulkResult.BulkResultItem bulkResultItem = result.getItems().get(i);
            if (bulkResultItem.error != null && !bulkResultItem.error.isEmpty()) {
                final Exception cause = convertToException(bulkResultItem.error);
                final T failedObject = items.get(i);
                final FailedItem failedItem = new FailedItem(failedObject, cause);
                failedItems.add(failedItem);
            }
        }
        return failedItems;
    }

    protected static Exception convertToException(String error) {
        // Read error data
        final JsonObject errorObject = new JsonParser().parse(error).getAsJsonObject();
        final String errorType = errorObject.get("type").getAsString();
        final String errorReason = errorObject.get("reason").getAsString();
        final JsonElement errorCause = errorObject.get("caused_by");

        // Create Exception
        final String errorMessage = String.format("%s: %s", errorType, errorReason);
        if (errorCause != null) {
            return new Exception(errorMessage, convertToException(errorCause.toString()));
        }
        return new Exception(errorMessage);
    }
}
