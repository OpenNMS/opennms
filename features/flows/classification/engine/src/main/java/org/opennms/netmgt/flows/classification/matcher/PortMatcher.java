/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.matcher;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.value.IntegerValue;
import org.opennms.netmgt.flows.classification.value.RangedValue;
import org.opennms.netmgt.flows.classification.value.StringValue;

public class PortMatcher implements Matcher {
    private final List<IntegerValue> concretePorts = new ArrayList<>();
    private final List<RangedValue> rangedPorts = new ArrayList<>();

    public PortMatcher(String ports) {
        final StringValue portValue = new StringValue(ports);
        if (portValue.isWildcard()) {
            throw new IllegalArgumentException("Wildcards not supported");
        }

        final List<StringValue> portValues = portValue.splitBy(",");
        final List<StringValue> rangedPortValues = portValues.stream().filter(v -> v.isRanged()).collect(Collectors.toList());
        rangedPortValues.forEach(v -> portValues.remove(v));

        final List<RangedValue> convertedRangedPortValues = rangedPortValues
                .stream()
                .map(v -> new RangedValue(v))
                .collect(Collectors.toList());

        this.concretePorts.addAll(portValues.stream().map(v -> new IntegerValue(v)).collect(Collectors.toList()));
        this.rangedPorts.addAll(convertedRangedPortValues);
    }

    @Override
    public boolean matches(ClassificationRequest request) {
        final Optional<IntegerValue> concreteValue = concretePorts.stream().filter(p -> !p.isNull() && p.getValue() == request.getPort()).findAny();
        if (concreteValue.isPresent()) {
            return true;
        }
        final Optional<RangedValue> rangedValue = rangedPorts.stream().filter(rp -> rp.isInRange(request.getPort())).findAny();
        return rangedValue.isPresent();
    }
}
