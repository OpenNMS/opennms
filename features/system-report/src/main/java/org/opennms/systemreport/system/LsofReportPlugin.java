package org.opennms.systemreport.system;

import java.io.File;
import java.util.TreeMap;

import org.apache.commons.exec.CommandLine;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class LsofReportPlugin extends AbstractSystemReportPlugin {
    public String getName() {
        return "lsof";
    }

    public String getDescription() {
        return "Output of the 'lsof' Command";
    }

    public int getPriority() {
        return 6;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        String lsofOutput = null;

        final String lsof = findBinary("lsof");

        if (lsof != null) {
            lsofOutput = slurpOutput(CommandLine.parse(lsof), false);
        }

        if (lsofOutput != null) {
            File tempFile = createTemporaryFileFromString(lsofOutput);
            if(tempFile != null) {
                map.put("Output", new FileSystemResource(tempFile));
            }
        }

        return map;
    }
}
