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
