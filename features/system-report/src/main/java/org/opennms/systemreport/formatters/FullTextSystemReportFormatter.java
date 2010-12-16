package org.opennms.systemreport.formatters;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;

public class FullTextSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    public String getName() {
        return "full";
    }

    public String getDescription() {
        return "human-readable text (including large resources like logs)";
    }

    public boolean canStdout() {
        return true;
    }

    public void write(final SystemReportPlugin plugin) {
        final OutputStream out = getOutputStream();

        try {
            out.write(String.format("= %s: %s =\n\n", plugin.getName(), plugin.getDescription()).getBytes());
            
            for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
                final Resource value = entry.getValue();

                out.write(String.format("== %s ==\n\n", entry.getKey()).getBytes());

                final InputStream is = value.getInputStream();
                int bytes;
                byte[] buffer = new byte[1024];
                
                while ((bytes = is.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes);
                }
                is.close();

                out.write("\n\n".getBytes());
            }

        } catch (final Exception e) {
            LogUtils.infof(this, e, "unable to write");
        }
    }

}
