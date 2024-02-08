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
package org.opennms.netmgt.flows.classification.internal.matcher;

import java.util.Set;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.internal.value.ProtocolValue;

public class ProtocolMatcher implements Matcher {

    private final Set<Integer> protocols;

    public ProtocolMatcher(ProtocolValue protocols) {
        this.protocols = protocols.getProtocols();
    }

    public ProtocolMatcher(String protocols) {
        this(ProtocolValue.of(protocols));
    }

    @Override
    public boolean matches(ClassificationRequest request) {
        return protocols.contains(request.getProtocol().getDecimal());
    }
}
