package org.opennms.systemreport.opennms;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class LogReportPlugin extends AbstractSystemReportPlugin {

    public String getName() {
        return "Logs";
    }

    public String getDescription() {
        return "OpenNMS Log Files";
    }

    public int getPriority() {
        return 21;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        File f = new File(System.getProperty("opennms.home") + "/logs");
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
            filename = filename.replaceFirst("^" + System.getProperty("opennms.home") + "/logs/?", "");
            if (filename.endsWith(".log") && file.length() > 0) {
                map.put(filename, new FileSystemResource(file));
            }
        }
    }
}
