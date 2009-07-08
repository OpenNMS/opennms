/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.simple.request;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author brozow
 *
 */
public class LineOrientedRequest {
    
    public static final LineOrientedRequest Null = new LineOrientedRequest(null) {
        
    };
    
    private String m_command;
    
    public LineOrientedRequest(String command) {
        m_command = command;
    }

    /**
     * @param socket
     * @throws IOException 
     */
    public void send(OutputStream out) throws IOException {
        out.write(String.format("%s\r\n", m_command).getBytes());
    }
    
    public String getRequest() {
        return String.format("%s\r\n", m_command);
    }
    
    public String toString() {
        return String.format("Request: %s", m_command);
    }

}