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
package org.opennms.core.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * <p>ProcessExec class.</p>
 */
public class ProcessExec {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProcessExec.class);
	
    private PrintStream m_out = null;

    private PrintStream m_err = null;

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

    public static class PrintInputStream implements Runnable {
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
