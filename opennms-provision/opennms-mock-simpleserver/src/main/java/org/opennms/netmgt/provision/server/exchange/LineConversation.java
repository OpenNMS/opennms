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
package org.opennms.netmgt.provision.server.exchange;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Donald Desloge
 *
 */
public class LineConversation {
    
    private String m_banner;
    private Map<String, String> m_responseMap = new HashMap<String, String>();
    private String m_closeRequest;
    private String m_closeResponse;
    
    public void setBanner(String banner) {
        m_banner = banner;
    }
    
    public String getBanner() {
        return m_banner;
    }
    
    public void setExpectedClose(String closeRequest) {
        m_closeRequest = closeRequest;
    }
    
    public void setExpectedClose(String closeRequest, String closeResponse) {
        m_closeRequest = closeRequest;
        m_closeResponse = closeResponse;
    }
    
    public String getExpectedClose() {
        return m_closeRequest;
    }
    
    public String getExpectedCloseResponse() {
        return m_closeResponse;
    }
    
    public void addRequestHandler(String request, String response){
        m_responseMap.put(request, response);
    }
    
    public String getResponse(String request) {
        return m_responseMap.get(request);
    }

    /**
     * @return
     */
    public boolean hasBanner() {
        return m_banner != null;
    }
}
