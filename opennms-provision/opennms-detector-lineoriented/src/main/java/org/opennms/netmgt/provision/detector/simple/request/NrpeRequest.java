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
package org.opennms.netmgt.provision.detector.simple.request;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * <p>NrpeRequest class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class NrpeRequest {
    
    /** Constant <code>Null</code> */
    public static final NrpeRequest Null = new NrpeRequest(null) {
        @Override
        public void send(final OutputStream out) throws IOException {
        }
    };
    
    private final byte[] m_command;
    
    /**
     * <p>Constructor for NrpeRequest.</p>
     *
     * @param command an array of byte.
     */
    public NrpeRequest(final byte[] command) {
        if (command != null) {
            m_command = command.clone();
        } else {
            m_command = null;
        }
    }

    /**
     * <p>send</p>
     *
     * @throws java.io.IOException if any.
     * @param out a {@link java.io.OutputStream} object.
     */
    public void send(final OutputStream out) throws IOException {
        out.write( m_command);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return String.format("Request: %s", Arrays.toString(m_command));
    }
}
