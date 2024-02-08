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
