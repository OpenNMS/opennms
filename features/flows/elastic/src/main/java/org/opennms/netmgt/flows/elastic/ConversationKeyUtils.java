/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.io.StringWriter;
import java.util.Objects;

import org.opennms.netmgt.flows.api.ConversationKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for building the {@link ConversationKey} from
 * a {@link FlowDocument} and converting it to/from a string
 * so that it can be used in group-by statements when querying.
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
    
    public static String getConvoKeyAsJsonString(FlowDocument document) {
        // Only generate the key if all of the required fields are set
        if (document.getLocation() != null
                && document.getProtocol() != null
                && document.getSrcAddr() != null
                && document.getDstAddr() != null) {
            // Build the JSON string manually
            // This is faster than creating some new object on which we can use gson.toJson or similar
            final StringWriter writer = new StringWriter();
            writer.write("[");

            // Use GSON to encode the location, since this may contain characters that need to be escape
            writer.write(gson.toJson(document.getLocation()));
            writer.write(",");
            writer.write(Integer.toString(document.getProtocol()));
            writer.write(",");

            // Write out addresses in canonical format (lower one first)
            final String srcAddr = document.getSrcAddr();
            final String dstAddr = document.getDstAddr();
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

            if (document.getApplication() != null) {
                writer.write(gson.toJson(document.getApplication()));
            } else {
                writer.write("null");
            }

            writer.write("]");
            return writer.toString();
        }
        return null;
    }
}
