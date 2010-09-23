package org.opennms.systemreport.system;

import java.io.File;
import java.util.TreeMap;

import org.apache.commons.exec.CommandLine;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class TopReportPlugin extends AbstractSystemReportPlugin {
    public String getName() {
        return "Top";
    }

    public String getDescription() {
        return "Output of the 'top' Command";
    }

    public int getPriority() {
        return 5;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();

        final String top = findBinary("top");

        String topOutput = null;

        if (top != null) {
            topOutput = slurpOutput(CommandLine.parse(top + " -h"), true);

            if (topOutput.contains("-b") && topOutput.contains("-n")) {
                topOutput = slurpOutput(CommandLine.parse(top + " -n 1 -b"), false);
            } else if (topOutput.contains("-l")) {
                topOutput = slurpOutput(CommandLine.parse(top + " -l 1"), false);
            } else {
                topOutput = null;
            }
        }

        if (topOutput != null) {
            File tempFile = createTemporaryFileFromString(topOutput);
            if(tempFile != null) {
                map.put("Output", new FileSystemResource(tempFile));
            }
        }

        return map;
    }
}
