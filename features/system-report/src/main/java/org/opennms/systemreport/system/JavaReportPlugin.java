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
package org.opennms.systemreport.system;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
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
    public Map<String, Resource> getEntries() {
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

            final StringBuilder sb = new StringBuilder();
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
