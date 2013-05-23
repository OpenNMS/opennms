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

package org.opennms.netmgt.provision.detector.simple.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * <p>LineOrientedResponse class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class LineOrientedResponse {
    
    private String m_response;
    
    /**
     * <p>Constructor for LineOrientedResponse.</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public LineOrientedResponse(final String response) {
        setResponse(response);
    }
    
    /**
     * <p>receive</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     * @throws java.io.IOException if any.
     */
    public void receive(final BufferedReader in) throws IOException {
        setResponse(in.readLine());
    }

    /**
     * <p>startsWith</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean startsWith(final String prefix) {
        return getResponse() != null && getResponse().startsWith(prefix);
    }
    
    /**
     * <p>contains</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean contains(final String pattern) {
        return getResponse() != null && getResponse().contains(pattern);
    }
    
    /**
     * <p>endsWith</p>
     *
     * @param suffix a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean endsWith(final String suffix) {
        return getResponse() != null && getResponse().endsWith(suffix);
    }
    
    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean matches(final String regex) {
        return getResponse() != null && getResponse().toString().trim().matches(regex);
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean find(final String regex) {
        return getResponse() != null && Pattern.compile(regex).matcher(getResponse()).find();
    }
    
    /**
     * <p>equals</p>
     *
     * @param response a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean equals(final String response) {
        return (response == null ? getResponse() == null : response.equals(getResponse()));
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return String.format("Response: %s", getResponse());
    }

    /**
     * <p>setResponse</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public void setResponse(final String response) {
        m_response = response;
    }

    /**
     * <p>getResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResponse() {
        return m_response;
    }

}
