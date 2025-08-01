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
package org.opennms.netmgt.flows.classification.internal.value;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.classification.internal.decision.Bound;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;

public class ProtocolValue {

    public static  ProtocolValue of(String string) {
        var protocols = new StringValue(string).splitBy(",")
                .stream()
                .map(p -> Protocols.getProtocol(p.getValue()))
                .filter(p -> p != null)
                .map(Protocol::getDecimal)
                .collect(Collectors.toSet());
        return new ProtocolValue(protocols);
    }

    private final Set<Integer> protocols;

    public ProtocolValue(Set<Integer> protocols) {
        this.protocols = protocols;
    }

    public Set<Integer> getProtocols() {
        return protocols;
    }

    public ProtocolValue shrink(Bound<Integer> bound) {
        Set<Integer> s = new HashSet<>(protocols.size());
        for (var i: protocols) {
            if (bound.canBeRestrictedBy(i)) {
                s.add(i);
            }
        }
        return s.isEmpty() ? null : s.size() == protocols.size() ? this : new ProtocolValue(s);
    }

}
