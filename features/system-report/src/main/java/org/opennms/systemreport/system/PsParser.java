package org.opennms.systemreport.system;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opennms.core.utils.LogUtils;

public final class PsParser extends Thread {
    private final Set<Integer> m_processes = Collections.synchronizedSet(new HashSet<Integer>());
    private DataInputStream m_input;
    private final String m_matchText;
    private final String m_skipText;
    private final int m_matchField;

    public PsParser(final DataInputStream input, final String matchText, String skipText, final int matchField) {
        m_input = input;
        m_matchText = matchText;
        m_skipText = skipText;
        m_matchField = matchField;
    }
    
    public void run() {
        final InputStreamReader isr = new InputStreamReader(m_input);
        final BufferedReader reader = new BufferedReader(isr);

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(m_matchText)) {
                    if (m_skipText != null && m_skipText.length() > 0 && line.contains(m_skipText)) {
                        LogUtils.debugf(this, "skipped match: %s", line);
                        continue;
                    } else {
                        LogUtils.debugf(this, "found match: %s", line);
                    }
                    final String[] entries = line.split(" +");
                    m_processes.add(Integer.valueOf(entries[m_matchField]));
                }
                if (this.isInterrupted()) {
                    break;
                }
            }
        } catch (final IOException e) {
            if (e.getMessage().contains("Write end dead")) {
                // ignore this, the stream is finished
            } else {
                LogUtils.debugf(this, e, "An error occurred matching '%s' for field '%d' in the input stream.", m_matchText, m_matchField);
            }
        } catch (final Exception e) {
            LogUtils.debugf(this, e, "An error occurred matching '%s' for field '%d' in the input stream.", m_matchText, m_matchField);
        }
    }

    public Set<Integer> getProcesses() {
        return m_processes;
    }
}