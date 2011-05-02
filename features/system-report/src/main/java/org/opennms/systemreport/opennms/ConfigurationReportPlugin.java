package org.opennms.systemreport.opennms;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class ConfigurationReportPlugin extends AbstractSystemReportPlugin {

    public String getName() {
        return "Configuration";
    }

    public String getDescription() {
        return "Append all OpenNMS configuration files (full output only)";
    }

    public int getPriority() {
        return 20;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        File f = new File(System.getProperty("opennms.home") + File.separator + "etc");
        processFile(f, map);
        return map;
    }

    public void processFile(final File file, final Map<String,Resource> map) {
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                processFile(f, map);
            }
        } else {
            String filename = file.getPath();
            filename = filename.replaceFirst("^" + System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "?", "");
            if ((!filename.contains(File.separator + "examples" + File.separator)) && file.length() > 0) {
                map.put(filename, new FileSystemResource(file));
            }
        }
    }
}
