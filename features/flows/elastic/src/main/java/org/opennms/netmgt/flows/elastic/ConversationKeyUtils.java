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
        if (array.length != 6) {
            throw new IllegalArgumentException("Invalid conversation key string: " + json);
        }
        return new ConversationKey((String)array[0], ((Number)array[1]).intValue(),
                (String)array[2], ((Number)array[3]).intValue(),
                (String)array[4], ((Number)array[5]).intValue());
    }

    public static String getConvoKeyAsJsonString(FlowDocument document) {
        // Only generate the key if all of the required fields are set
        if (document.getLocation() != null
                && document.getProtocol() != null
                && document.getSrcAddr() != null
                && document.getSrcPort() != null
                && document.getDstAddr() != null
                && document.getDstPort() != null) {
            // Build the JSON string manually
            // This is faster than creating some new object on which we can use gson.toJson or similar
            final StringWriter writer = new StringWriter();
            writer.write("[");
            // Use GSON to encode the location, since this may contain characters that need to be escape
            writer.write(gson.toJson(document.getLocation()));
            writer.write(",");
            writer.write(Integer.toString(document.getProtocol()));
            writer.write(",\"");
            if (Direction.INGRESS.equals(document.getDirection())) {
                writer.write(document.getSrcAddr());
                writer.write("\",");
                writer.write(Integer.toString(document.getSrcPort()));
                writer.write(",\"");
                writer.write(document.getDstAddr());
                writer.write("\",");
                writer.write(Integer.toString(document.getDstPort()));
            } else {
                writer.write(document.getDstAddr());
                writer.write("\",");
                writer.write(Integer.toString(document.getDstPort()));
                writer.write(",\"");
                writer.write(document.getSrcAddr());
                writer.write("\",");
                writer.write(Integer.toString(document.getSrcPort()));
            }
            writer.write("]");
            return writer.toString();
        }
        return null;
    }
}
