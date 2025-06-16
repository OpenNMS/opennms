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
package org.opennms.netmgt.collectd.vmware.vijava;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VmwarePerformanceValues {
    private Map<String, Map<String, Long>> multiValues = new HashMap<String, Map<String, Long>>();
    private Map<String, Long> singleValues = new HashMap<String, Long>();

    public VmwarePerformanceValues() {
    }

    public void addValue(String name, String instance, long value) {
        if (!multiValues.containsKey(name)) {
            multiValues.put(name, new HashMap<String, Long>());
        }

        Map<String, Long> map = multiValues.get(name);

        map.put(instance, Long.valueOf(value));
    }

    public void addValue(String name, long value) {
        singleValues.put(name, Long.valueOf(value));
    }

    public boolean hasInstances(String name) {
        return (multiValues.containsKey(name));
    }

    public boolean hasSingleValue(String name) {
        return (singleValues.containsKey(name));
    }

    public Set<String> getInstances(String name) {
        if (multiValues.containsKey(name)) {
            return multiValues.get(name).keySet();
        } else {
            return null;
        }
    }

    public Long getValue(String name) {
        return singleValues.get(name);
    }

    public Long getValue(String name, String instance) {
        if (multiValues.containsKey(name)) {
            return multiValues.get(name).get(instance);
        } else {
            return null;
        }
    }
}
