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
package org.opennms.netmgt.flows.processing;

import java.io.StringWriter;
import java.util.Objects;

import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Flow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for building the {@link ConversationKey} and
 * converting it to/from a string so that it can be used in
 * group-by statements when querying.
 *
 * These methods are optimized for speed when generating the key,
 * with the constraint that we must also be able to decode the key.
 *
 * @author jwhite
 */
public class ConversationKeyUtils {
    private static final Gson gson = new GsonBuilder().create();

    public static ConversationKey fromJsonString(String json) {
        final Object[] array = gson.fromJson(json, Object[].class);
        if (array.length != 5) {
            throw new IllegalArgumentException("Invalid conversation key string: " + json);
        }
        return new ConversationKey((String)array[0], ((Number)array[1]).intValue(),
                (String)array[2], (String)array[3], (String)array[4]);
    }
    
    public static String getConvoKeyAsJsonString(final String location,
                                                 final Integer protocol,
                                                 final String srcAddr,
                                                 final String dstAddr,
                                                 final String application) {
        // Only generate the key if all of the required fields are set
        if (location != null
                && protocol != null
                && srcAddr != null
                && dstAddr != null) {
            // Build the JSON string manually
            // This is faster than creating some new object on which we can use gson.toJson or similar
            final StringWriter writer = new StringWriter();
            writer.write("[");

            // Use GSON to encode the location, since this may contain characters that need to be escape
            writer.write(gson.toJson(location));
            writer.write(",");
            writer.write(Integer.toString(protocol));
            writer.write(",");

            // Write out addresses in canonical format (lower one first)
            if (Objects.compare(srcAddr, dstAddr, String::compareTo) < 0) {
                writer.write(gson.toJson(srcAddr));
                writer.write(",");
                writer.write(gson.toJson(dstAddr));
            } else {
                writer.write(gson.toJson(dstAddr));
                writer.write(",");
                writer.write(gson.toJson(srcAddr));
            }
            writer.write(",");

            if (application != null) {
                writer.write(gson.toJson(application));
            } else {
                writer.write("null");
            }

            writer.write("]");
            return writer.toString();
        }
        return null;
    }
}
