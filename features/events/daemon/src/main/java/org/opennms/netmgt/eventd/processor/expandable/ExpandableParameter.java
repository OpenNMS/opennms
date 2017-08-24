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

package org.opennms.netmgt.eventd.processor.expandable;

import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.eventd.ExpandableParameterResolverRegistry;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link ExpandableParameter} requires the {@link #ExpandableParameter#parameter} to be replaced with an appropriate value.
 *
 * Each {@link ExpandableParameter#token} may be mapped to a {@link ExpandableParameterResolver} resolving the token to a value.
 * If no resolver is found, null is returned.
 *
 * @see ExpandableParameterResolverRegistry
 */
public class ExpandableParameter implements ExpandableToken {

    private static final Logger LOG = LoggerFactory.getLogger(ExpandableParameter.class);

    private final String token;
    private final String parsedToken;
    private final ExpandableParameterResolver resolver;
    private final EventUtil eventUtil;


    public ExpandableParameter(String token, EventUtil eventUtil) {
        this.token = Objects.requireNonNull(token);
        this.resolver = Objects.requireNonNull(eventUtil.getResolver(token));
        this.parsedToken = resolver.parse(token);
        this.eventUtil = Objects.requireNonNull(eventUtil);
    }

    @Override
    public String expand(Event event, Map<String, Map<String, String>> decode) {
        String value = resolver.getValue(token, parsedToken, event, eventUtil);
        LOG.debug("Value of token {}={}", token, value);

        if (value != null) {
            if (decode != null && decode.containsKey(token) && decode.get(token).containsKey(value)) {
                StringBuilder ret = new StringBuilder();
                ret.append(decode.get(token).get(value));
                ret.append("(");
                ret.append(value);
                ret.append(")");
                return ret.toString();
            } else {
                return value;
            }
        }
        return "";
    }

    @Override
    public boolean requiresTransaction() {
        return resolver.requiresTransaction();
    }
}
