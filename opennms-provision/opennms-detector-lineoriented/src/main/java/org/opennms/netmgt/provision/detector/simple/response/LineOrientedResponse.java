/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author brozow
 *
 */
public class LineOrientedResponse {
    
    private String m_response;
    
    public LineOrientedResponse(String response) {
        setResponse(response);
    }
    
    public void receive(BufferedReader in) throws IOException {
        setResponse(in.readLine());
    }

    public boolean startsWith(String prefix) {
        return getResponse() != null && getResponse().startsWith(prefix);
    }
    
    public boolean contains(String pattern) {
        return getResponse() != null && getResponse().contains(pattern);
    }
    
    public boolean endsWith(String suffix) {
        return getResponse() != null && getResponse().endsWith(suffix);
    }
    
    public boolean matches(String regex) {
        return getResponse() != null && getResponse().matches(regex);
    }
    
    public boolean find(String regex) {
        return getResponse() != null && Pattern.compile(regex).matcher(getResponse()).find();
    }
    
    public boolean equals(String response) {
        return (response == null ? getResponse() == null : response.equals(getResponse()));
    }
    
    public String toString() {
        return String.format("Response: %s", getResponse());
    }

    public void setResponse(String response) {
        m_response = response;
    }

    public String getResponse() {
        return m_response;
    }

}