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
package org.opennms.netmgt.provision.server.exchange;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>LineConversation class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class LineConversation {
    
    private String m_banner;
    private Map<String, String> m_responseMap = new HashMap<String, String>();
    private String m_closeRequest;
    private String m_closeResponse;
    
    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(String banner) {
        m_banner = banner;
    }
    
    /**
     * <p>getBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBanner() {
        return m_banner;
    }
    
    /**
     * <p>setExpectedClose</p>
     *
     * @param closeRequest a {@link java.lang.String} object.
     */
    public void setExpectedClose(String closeRequest) {
        m_closeRequest = closeRequest;
    }
    
    /**
     * <p>setExpectedClose</p>
     *
     * @param closeRequest a {@link java.lang.String} object.
     * @param closeResponse a {@link java.lang.String} object.
     */
    public void setExpectedClose(String closeRequest, String closeResponse) {
        m_closeRequest = closeRequest;
        m_closeResponse = closeResponse;
    }
    
    /**
     * <p>getExpectedClose</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExpectedClose() {
        return m_closeRequest;
    }
    
    /**
     * <p>getExpectedCloseResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExpectedCloseResponse() {
        return m_closeResponse;
    }
    
    /**
     * <p>addRequestHandler</p>
     *
     * @param request a {@link java.lang.String} object.
     * @param response a {@link java.lang.String} object.
     */
    public void addRequestHandler(String request, String response){
        m_responseMap.put(request, response);
    }
    
    /**
     * <p>getResponse</p>
     *
     * @param request a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getResponse(String request) {
        return m_responseMap.get(request);
    }

    /**
     * <p>hasBanner</p>
     *
     * @return a boolean.
     */
    public boolean hasBanner() {
        return m_banner != null;
    }
}
