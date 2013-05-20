/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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
