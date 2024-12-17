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
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.management.*;

public class JavaReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(JavaReportPlugin.class);
    private static final String JMX_OBJ_OS = "java.lang:type=OperatingSystem";
    private static final String JMX_ATTR_AVAILABLE_PROCESSORS = "AvailableProcessors";
    private static final MBeanServer M_BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
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

        Object availableProcessorsObj = getJmxAttribute(JMX_OBJ_OS, JMX_ATTR_AVAILABLE_PROCESSORS);
        int number = (int) availableProcessorsObj;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(number);
        byte[] data = buffer.array();
        Resource resource = new ByteArrayResource(data);
        map.put("System CPU count", resource);


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
    private Object getJmxAttribute(String objectName, String attributeName) {
        ObjectName objNameActual;
        try {
            objNameActual = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            LOG.warn("Failed to query from object name " + objectName, e);
            return null;
        }
        try {
            return M_BEAN_SERVER.getAttribute(objNameActual, attributeName);
        } catch (InstanceNotFoundException | AttributeNotFoundException
                 | ReflectionException | MBeanException e) {
            LOG.warn("Failed to query from attribute name " + attributeName + " on object " + objectName, e);
            return null;
        }
    }
}
