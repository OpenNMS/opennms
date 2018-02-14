/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import org.opennms.netmgt.eventd.processor.expandable.ExpandableParameterResolver;
import org.opennms.netmgt.xml.event.Event;

/**
 * Knows about all existing {@link ExpandableParameterResolver}s.
 *
 * This is the entry point to add, e.g. OSGi-aware {@link ExpandableParameterResolver}s in the future.
 */
public class ExpandableParameterResolverRegistry {

    private static ExpandableParameterResolver NULL_RESOLVER = new ExpandableParameterResolver() {
        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return null;
        }

        @Override
        public String parse(String parm) {
            return null;
        }

        @Override
        public boolean matches(String parm) {
            return false;
        }

        @Override
        public boolean requiresTransaction() {
            return false;
        }
    };

    public ExpandableParameterResolver getResolver(String token) {
        for (StandardExpandableParameterResolvers parameters : StandardExpandableParameterResolvers.values()) {
            if (parameters.matches(token)) {
                return parameters;
            }
        }
        return NULL_RESOLVER;
    }
}
