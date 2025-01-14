package org.opennms.systemreport.system;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.TreeMap;

public class HardDriveReportPlugin  extends AbstractSystemReportPlugin {


    @Override
    public String getName() {
        return "Hard Drive Stats";
    }

    @Override
    public String getDescription() { return "Hard Drive Capacity and Performance Information"; }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new TreeMap<String,Resource>();

        String[] dfCommand = {"bash", "-c", "df -h"};
        final String dfOutput = slurpCommand(dfCommand);
        if (dfOutput != null) {
            map.put("Hard Drive Capacity", getResource("\n"+dfOutput));
        }

        String[] ioStatCommand = {"bash", "-c", "iostat -d"};
        final String iostatOutput = slurpCommand(ioStatCommand);
        if (iostatOutput != null) {
            map.put("Hard Drive Performance", getResource("\n"+iostatOutput));
        }

        return map;
    }

}
