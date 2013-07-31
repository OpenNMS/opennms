/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ProcessExec class.</p>
 */
public class ProcessExec {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProcessExec.class);
	
    PrintStream m_out = null;

    PrintStream m_err = null;

    /**
     * <p>Constructor for ProcessExec.</p>
     *
     * @param out a {@link java.io.PrintStream} object.
     * @param err a {@link java.io.PrintStream} object.
     */
    public ProcessExec(PrintStream out, PrintStream err) {
        m_out = out;
        m_err = err;
    }

    /**
     * <p>exec</p>
     *
     * @param cmd an array of {@link java.lang.String} objects.
     * @return a int.
     * @throws java.io.IOException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public int exec(String[] cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);

        p.getOutputStream().close();
        PrintInputStream out = new PrintInputStream(p.getInputStream(), m_out);
        PrintInputStream err = new PrintInputStream(p.getErrorStream(), m_err);

        Thread t1 = new Thread(out, this.getClass().getSimpleName() + "-stdout");
        Thread t2 = new Thread(err, this.getClass().getSimpleName() + "-stderr");
        t1.start();
        t2.start();

        int exitVal = p.waitFor();

        t1.join();
        t2.join();

        return exitVal;
    }

    public class PrintInputStream implements Runnable {
        private InputStream m_inputStream;

        private PrintStream m_printStream;

        public PrintInputStream(InputStream inputStream, PrintStream printStream) {
            m_inputStream = inputStream;
            m_printStream = printStream;
        }

        @Override
        public void run() {
            InputStreamReader isr = null;
            BufferedReader in = null;

            try {
                isr = new InputStreamReader(m_inputStream);
                in = new BufferedReader(isr);
                String line;

                while ((line = in.readLine()) != null) {
                    m_printStream.println(line);
                }
            } catch (final Exception e) {
            	LOG.warn("an error occurred while reading the input stream", e);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(isr);
                IOUtils.closeQuietly(m_inputStream);
            }
        }

    }
}
