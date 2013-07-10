/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.systemreport.system;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PsParser extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(PsParser.class);
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
    
    @Override
    public void run() {
        final InputStreamReader isr = new InputStreamReader(m_input);
        final BufferedReader reader = new BufferedReader(isr);

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(m_matchText)) {
                    if (m_skipText != null && m_skipText.length() > 0 && line.contains(m_skipText)) {
                        LOG.debug("skipped match: {}", line);
                        continue;
                    } else {
                        LOG.debug("found match: {}", line);
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
                LOG.debug("An error occurred matching '{}' for field '{}' in the input stream.", m_matchText, m_matchField, e);
            }
        } catch (final Exception e) {
            LOG.debug("An error occurred matching '{}' for field '{}' in the input stream.", m_matchText, m_matchField, e);
        }
    }

    public Set<Integer> getProcesses() {
        return m_processes;
    }
}
