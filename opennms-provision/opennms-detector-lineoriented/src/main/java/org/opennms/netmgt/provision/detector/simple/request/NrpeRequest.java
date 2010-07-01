/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
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
        public void send(OutputStream out) throws IOException {
        }
    };
    
    private final byte[] m_command;
    
    /**
     * <p>Constructor for NrpeRequest.</p>
     *
     * @param command an array of byte.
     */
    public NrpeRequest(byte[] command) {
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
    public void send(OutputStream out) throws IOException {
        out.write( m_command);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return String.format("Request: %s", Arrays.toString(m_command));
    }
}
