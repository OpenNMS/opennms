package org.opennms.systemreport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.opennms.core.utils.LogUtils;

public class OutputSuckingParser extends Thread {
    private StringBuffer m_buffer = new StringBuffer();
    private DataInputStream m_input;

    public OutputSuckingParser(final DataInputStream input) {
        m_input = input;
    }
    
    public void run() {
        final InputStreamReader isr = new InputStreamReader(m_input);
        final BufferedReader reader = new BufferedReader(isr);

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                m_buffer.append(line.replace("\\s*$", "")).append("\n");
                if (this.isInterrupted()) {
                    LogUtils.infof(this, "interrupted");
                    break;
                }
            }
        } catch (final IOException e) {
            if (e.getMessage().contains("Write end dead")) {
                // ignore this, the stream is finished
            } else {
                LogUtils.debugf(this, e, "An error occurred extracting top output.");
            }
        } catch (final Exception e) {
            LogUtils.debugf(this, e, "An error occurred extracting top output.");
        }
    }

    public String getOutput() {
        return m_buffer.toString();
    }
}
