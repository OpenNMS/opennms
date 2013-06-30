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
