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

import org.opennms.netmgt.xml.event.Event;

/**
 * The simplest {@link ExpandableToken} is a "not" expandable token, or {@link ExpandableConstant}.
 *
 */
public class ExpandableConstant implements ExpandableToken {

    private final String token;

    public ExpandableConstant(String token) {
        this.token = Objects.requireNonNull(token);
    }

    public ExpandableConstant(char c) {
        this(String.valueOf(c));
    }

    @Override
    public String expand(Event event, Map<String, Map<String, String>> decode) {
        return token;
    }

    @Override
    public boolean requiresTransaction() {
        return false; // no transaction for constants
    }
}
