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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class BulkUtils {

    private BulkUtils() {}

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
