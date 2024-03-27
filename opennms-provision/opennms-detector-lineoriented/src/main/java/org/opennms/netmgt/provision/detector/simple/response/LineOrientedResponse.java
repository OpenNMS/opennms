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
