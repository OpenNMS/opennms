/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import org.opennms.netmgt.flows.api.ConversationKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for building the {@link ConversationKey} from
 * a {@link FlowDocument} and converting it to/from a string
 * so that it can be used in group-by statements when querying.
 */
public class ConversationKeyUtils {
    private static final Gson gson = new GsonBuilder().create();

    public static String toJsonString(ConversationKey key) {
        return gson.toJson(new Object[]{key.getLocation(), key.getProtocol(),
                key.getSrcIp(), key.getSrcPort(),
                key.getDstIp(), key.getDstPort()});
    }

    public static ConversationKey fromJsonString(String json) {
        final Object[] array = gson.fromJson(json, Object[].class);
        if (array.length != 6) {
            throw new IllegalArgumentException("Invalid conversation key string: " + json);
        }
        return new ConversationKey((String)array[0], ((Number)array[1]).intValue(),
                (String)array[2], ((Number)array[3]).intValue(),
                (String)array[4], ((Number)array[5]).intValue());
    }

    private static ConversationKey egressKeyFor(FlowDocument document) {
        return new ConversationKey(document.getLocation(), document.getProtocol(),
                document.getSrcAddr(), document.getSrcPort(),
                document.getDstAddr(), document.getDstPort());
    }

    private static ConversationKey ingressKeyFor(FlowDocument document) {
        return new ConversationKey(document.getLocation(), document.getProtocol(),
                document.getDstAddr(), document.getDstPort(),
                document.getSrcAddr(), document.getSrcPort());
    }

    public static String getConvoKeyAsJsonString(FlowDocument document) {
        final ConversationKey convoKey = document.isInitiator() ? egressKeyFor(document) : ingressKeyFor(document);
        return toJsonString(convoKey);
    }
}
