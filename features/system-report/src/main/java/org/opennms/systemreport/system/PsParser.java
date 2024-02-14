/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
