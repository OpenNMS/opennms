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
package org.opennms.features.jmxconfiggenerator.log;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The STDOUT/STERR implementation of the {@ink LogAdapter}.
 */
public class ConsoleLogAdapter implements LogAdapter {

    private boolean debug;

    @Override
    public void warn(String warnMessage, Object... args) {
        System.out.println(String.format("WARNING: %s", format(warnMessage, args)));
    }

    @Override
    public void error(String message, Object... args) {
        System.err.println(format(message, args));
    }

    @Override
    public void debug(String message, Object... args) {
        if (debug) {
            System.out.println(format(message, args));
        }
    }

    @Override
    public void info(String message, Object... args) {
        System.out.println(format(message, args));
    }

    @Override
    public void info(InputStream inputStream) {
        try {
            ByteStreams.copy(inputStream, System.out);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return debug;
    }

    public OutputStream getErrorOutputStream() {
        return System.err;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public OutputStream getOutputStream() {
        return System.out;
    }
}
