/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;

public class JavaReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(JavaReportPlugin.class);

    @Override
    public String getName() {
        return "Java";
    }

    @Override
    public String getDescription() {
        return "Java and JVM information";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        map.put("Class Version", getResourceFromProperty("java.class.version"));
        map.put("Compiler", getResourceFromProperty("java.compiler"));
        map.put("Home", getResourceFromProperty("java.home"));
        map.put("Version", getResourceFromProperty("java.version"));
        map.put("Vendor", getResourceFromProperty("java.vendor"));
        map.put("VM Version", getResourceFromProperty("java.vm.version"));
        map.put("VM Name", getResourceFromProperty("java.vm.name"));

        MemoryMXBean memoryBean = getBean(ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
        if (memoryBean == null) {
            LOG.info("falling back to local VM MemoryMXBean");
            memoryBean = ManagementFactory.getMemoryMXBean();
        }

        addGetters(memoryBean, map);

        RuntimeMXBean runtimeBean = getBean(ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        if (runtimeBean == null) {
            LOG.info("falling back to local VM RuntimeMXBean");
            runtimeBean = ManagementFactory.getRuntimeMXBean();
        }

        addGetters(runtimeBean, map);

        ClassLoadingMXBean classBean = getBean(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
        if (classBean == null) {
            LOG.info("falling back to local VM ClassLoadingMXBean");
            classBean = ManagementFactory.getClassLoadingMXBean();
        }

        addGetters(classBean, map);

        /* this stuff is really not giving us anything useful
        List<GarbageCollectorMXBean> beans = getBeans(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, GarbageCollectorMXBean.class);
        if (beans == null || beans.size() == 0) {
            LOG.info("falling back to local VM MemoryMXBean");
            beans = ManagementFactory.getGarbageCollectorMXBeans();
        }

        LOG.trace("beans = {}", beans);
        int collectorNum = 1;
        for (final GarbageCollectorMXBean bean : beans) {
            final Map<String,Resource> temp = new TreeMap<String,Resource>();
            addGetters(bean, map);

            StringBuilder sb = new StringBuilder();
            for (final String s : temp.keySet()) {
                sb.append(s).append(": ").append(temp.get(s)).append("\n");
            }
            if (sb.length() > 0) sb.deleteCharAt(sb.length());
            map.put("Garbage Collector " + collectorNum, getResource(sb.toString()));
        }
        */

        return map;
    }

}
