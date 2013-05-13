/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
