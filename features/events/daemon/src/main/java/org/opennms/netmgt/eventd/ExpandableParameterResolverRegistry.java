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
