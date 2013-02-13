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
    private Map<String, Object> values = new HashMap<String, Object>();

    public VmwarePerformanceValues() {
    }

    public void addValue(String name, String instance, long value) {
        Object object = values.get(name);

        if (object == null && instance != null && !"".equals(instance)) {
            object = new HashMap<String, Long>();
        }

        if (object instanceof HashMap<?,?>) {
            ((HashMap<String, Long>) object).put(instance, Long.valueOf(value));
        } else {
            object = new Long(value);
        }

        values.put(name, object);
    }

    public void addValue(String name, long value) {
        values.put(name, Long.valueOf(value));
    }

    public boolean hasInstances(String name) {
        Object object = values.get(name);

        return (object instanceof HashMap<?,?>);
    }

    public Set<String> getInstances(String name) {
        Object object = values.get(name);

        if (object instanceof HashMap<?,?>) {
            return ((HashMap<String,?>) object).keySet();
        } else {
            return null;
        }
    }

    public Long getValue(String name) {
        Object object = values.get(name);

        if (object instanceof Long) {
            return (Long) object;
        } else {
            return null;
        }
    }

    public Long getValue(String name, String instance) {
        Object object = values.get(name);

        if (object instanceof HashMap<?,?>) {
            return ((HashMap<String,Long>) object).get(instance);
        } else {
            return null;
        }
    }
}
