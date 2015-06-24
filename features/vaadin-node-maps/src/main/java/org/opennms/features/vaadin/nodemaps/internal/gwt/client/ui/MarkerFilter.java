/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;

public interface MarkerFilter {
    public static enum MatchType {
        // substring search
        SUBSTRING,
        // exact match
        EXACT,
        // search in a comma-separated list
        IN;

        public static final Logger LOG = Logger.getLogger(MatchType.class.getName());

        public static MatchType fromToken(final String token) {
            if ("in".equals(token) || " in ".equals(token)) {
                return MatchType.IN;
            } else if ("=".equals(token)) {
                return MatchType.EXACT;
            } else if (":".equals(token)) {
                return MatchType.SUBSTRING;
            } else {
                LOG.warning("Unknown match token: " + token + ", blowing things up!");
                return null;
            }
        }
    };

    public abstract boolean matches(final NodeMarker marker);
}
