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
package org.opennms.netmgt.provision.detector.generic.response;

/**
 * <p>GpResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GpResponse {
    
    private String m_response;
    
    /**
     * <p>setExitStatus</p>
     *
     * @param exitStatus a int.
     */
    public void setExitStatus(final int exitStatus) {
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
     * <p>setError</p>
     *
     * @param error a {@link java.lang.String} object.
     */
    public void setError(final String error) {
    }

    /**
     * <p>validate</p>
     *
     * @param banner a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean validate(final String banner) {
        return m_response.matches(banner);
    }
}
