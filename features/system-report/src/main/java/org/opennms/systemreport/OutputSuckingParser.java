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
package org.opennms.systemreport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputSuckingParser extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(OutputSuckingParser.class);
    private StringBuffer m_buffer = new StringBuffer();
    private DataInputStream m_input;

    public OutputSuckingParser(final DataInputStream input) {
        m_input = input;
    }
    
    @Override
    public void run() {
        final InputStreamReader isr = new InputStreamReader(m_input);
        final BufferedReader reader = new BufferedReader(isr);

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                m_buffer.append(line.replace("\\s*$", "")).append("\n");
                if (this.isInterrupted()) {
                    LOG.info("interrupted");
                    break;
                }
            }
        } catch (final IOException e) {
            if (e.getMessage().contains("Write end dead")) {
                // ignore this, the stream is finished
            } else {
                LOG.debug("An error occurred extracting top output.", e);
            }
        } catch (final Exception e) {
            LOG.debug("An error occurred extracting top output.", e);
        }
    }

    public String getOutput() {
        return m_buffer.toString();
    }
}
