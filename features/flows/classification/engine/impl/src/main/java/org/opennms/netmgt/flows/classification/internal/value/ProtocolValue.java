/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
