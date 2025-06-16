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
package org.opennms.netmgt.eventd.processor.expandable;

import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.WebSecurityUtils;
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
                final StringBuilder ret = new StringBuilder();
                ret.append(decode.get(token).get(value));
                ret.append("(");
                ret.append(value);
                ret.append(")");
                return ret.toString();
            } else {
                return WebSecurityUtils.sanitizeString(value);
            }
        }
        return "";
    }

    @Override
    public boolean requiresTransaction() {
        return resolver.requiresTransaction();
    }
}
