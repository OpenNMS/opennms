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

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.network.IPPortRange;
import org.opennms.netmgt.flows.classification.internal.decision.Bound;

public class PortValue implements RuleValue<Integer, PortValue> {

    public static PortValue of(String input) {
            final StringValue portValue = new StringValue(input);
            if (portValue.hasWildcard()) {
                throw new IllegalArgumentException("Wildcards not supported");
            }
            final List<StringValue> portValues = portValue.splitBy(",");
            List<IPPortRange> ranges = new ArrayList<>();
            for (var pv: portValues) {
                if (pv.isRanged()) {
                    var rv = new RangedValue(pv);
                    ranges.add(new IPPortRange(rv.getStart(), rv.getEnd()));
                } else {
                    var iv = new IntegerValue(pv);
                    ranges.add(new IPPortRange(iv.getValue()));
                }
            }
            return new PortValue(ranges);
    }

    private final List<IPPortRange> ranges;

    public PortValue(List<IPPortRange> ranges) {
        this.ranges = ranges;
    }

    public List<IPPortRange> getPortRanges() {
        return ranges;
    }

    public boolean matches(int port) {
        for (var r: ranges) {
            if (r.contains(port)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PortValue shrink(Bound<Integer> bound) {
        List<IPPortRange> l = new ArrayList<>(ranges.size());
        for (var r: ranges) {
            if (bound.overlaps(r.getBegin(), r.getEnd())) {
                l.add(r);
            }
        }
        return l.isEmpty() ? null : ranges.size() == l.size() ? this : new PortValue(l);
    }

}
