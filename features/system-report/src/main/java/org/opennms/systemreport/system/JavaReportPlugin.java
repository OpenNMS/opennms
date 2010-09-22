package org.opennms.systemreport.system;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.TreeMap;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;

public class JavaReportPlugin extends AbstractSystemReportPlugin {

    public String getName() {
        return "Java";
    }

    public String getDescription() {
        return "Java and JVM";
    }

    public int getPriority() {
        return 1;
    }

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
            LogUtils.infof(this, "falling back to local VM MemoryMXBean");
            memoryBean = ManagementFactory.getMemoryMXBean();
        }

        LogUtils.tracef(this, "bean = %s", memoryBean.toString());
        addGetters(memoryBean, map);

        RuntimeMXBean runtimeBean = getBean(ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        if (runtimeBean == null) {
            LogUtils.infof(this, "falling back to local VM RuntimeMXBean");
            runtimeBean = ManagementFactory.getRuntimeMXBean();
        }

        LogUtils.tracef(this, "bean = %s", runtimeBean.toString());
        addGetters(runtimeBean, map);

        ClassLoadingMXBean classBean = getBean(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
        if (classBean == null) {
            LogUtils.infof(this, "falling back to local VM ClassLoadingMXBean");
            classBean = ManagementFactory.getClassLoadingMXBean();
        }

        LogUtils.tracef(this, "bean = %s", classBean.toString());
        addGetters(classBean, map);

        /* this stuff is really not giving us anything useful
        List<GarbageCollectorMXBean> beans = getBeans(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, GarbageCollectorMXBean.class);
        if (beans == null || beans.size() == 0) {
            LogUtils.infof(this, "falling back to local VM MemoryMXBean");
            beans = ManagementFactory.getGarbageCollectorMXBeans();
        }

        LogUtils.tracef(this, "beans = %s", beans.toString());
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
