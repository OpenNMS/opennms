/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
