/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortValue {
    private final Set<Integer> ports = new HashSet<>();

    public PortValue(String input) {
        final StringValue portValue = new StringValue(input);
        if (portValue.hasWildcard()) {
            throw new IllegalArgumentException("Wildcards not supported");
        }
        final List<StringValue> portValues = portValue.splitBy(",");
        final List<StringValue> rangedPortValues = portValues.stream().filter(v -> v.isRanged()).collect(Collectors.toList());
        rangedPortValues.forEach(v -> portValues.remove(v));

        // Add the actual ports
        this.ports.addAll(portValues.stream().map(v -> new IntegerValue(v).getValue()).collect(Collectors.toList()));

        // Add ranged ports
        final Set<Integer> rangedPorts = rangedPortValues.stream()
                .flatMap(v -> {
                    final RangedValue rangedValue = new RangedValue(v);
                    return IntStream.range(rangedValue.getStart(), rangedValue.getEnd()).boxed();
                })
                .collect(Collectors.toSet());
        this.ports.addAll(rangedPorts);
    }

    public Set<Integer> getPorts() {
        return ports;
    }
}
