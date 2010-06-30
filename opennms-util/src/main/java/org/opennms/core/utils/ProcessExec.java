//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// The code in this file is Copyright (C) 2004 DJ Gregor.
//
// Based on install.pl which was Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * <p>ProcessExec class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ProcessExec {
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

        Thread t1 = new Thread(out);
        Thread t2 = new Thread(err);
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

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(m_inputStream));
                String line;

                while ((line = in.readLine()) != null) {
                    m_printStream.println(line);
                }

                m_inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    m_inputStream.close();
                } catch (IOException e2) {
                } // do nothing
            }
        }

    }
}
