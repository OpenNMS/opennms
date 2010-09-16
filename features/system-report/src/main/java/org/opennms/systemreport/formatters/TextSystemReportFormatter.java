package org.opennms.systemreport.formatters;

import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;

public class TextSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {

    public String getName() {
        return "text";
    }

    public String getDescription() {
        return "simple human-readable indented text";
    }
    
    public boolean canStdout() {
        return true;
    }

    public void write(final SystemReportPlugin plugin) {
        if (!hasDisplayable(plugin)) return;
        LogUtils.debugf(this, "write(%s)", plugin.getName());
        try {
            final String title = plugin.getName() + " (" + plugin.getDescription() + "):" + "\n";
            getOutputStream().write(title.getBytes());
            for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
                final Resource value = entry.getValue();
                final boolean displayable = isDisplayable(value);
    
                final String text;
                if (displayable) {
                    text = "\t" + entry.getKey() + ": " + getResourceText(value) + "\n";
                } else {
                    text = "\t" + entry.getKey() + ": " + (value == null? "NULL" : value.getClass().getSimpleName() + " resource is not displayable.  Try using the 'zip' format.") + "\n";
                }
                getOutputStream().write(text.getBytes());
            }
        } catch (Exception e) {
            LogUtils.errorf(this, e, "Error writing plugin data.");
        }
    }
}
