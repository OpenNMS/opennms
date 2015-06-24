/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.web.request;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>WebRequest class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class WebRequest {

    private Map<String,String> m_headers = new HashMap<String,String>();

    private String m_responseText;

    private String m_responseRange;

    public Map<String, String> getHeaders() {
        return m_headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.m_headers = headers;
    }

    public String getResponseText() {
        return m_responseText;
    }

    public void setResponseText(String responseText) {
        this.m_responseText = responseText;
    }

    public String getResponseRange() {
        return m_responseRange;
    }

    public void setResponseRange(String responseRange) {
        this.m_responseRange = responseRange;
    }

    public void parseHeaders(String headersUrl) {
        if (headersUrl == null || !headersUrl.contains("="))
            return;
        String[] pairs = headersUrl.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            m_headers.put(parts[0], parts[1]);
        }
    }

}
