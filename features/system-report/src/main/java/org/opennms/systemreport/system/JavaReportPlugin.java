package org.opennms.systemreport.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;
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

        MemoryMXBean bean = (MemoryMXBean)getBean(
            ManagementFactory.MEMORY_MXBEAN_NAME,
            Arrays.asList(new Class<?>[] { java.lang.management.MemoryMXBean.class })
        );
        if (bean == null) {
            LogUtils.infof(this, "falling back to local VM MemoryMXBean");
            bean = ManagementFactory.getMemoryMXBean();
        }

        LogUtils.tracef(this, "bean = %s", bean.toString());
        addGetters(bean, map);

        return map;
    }

}
