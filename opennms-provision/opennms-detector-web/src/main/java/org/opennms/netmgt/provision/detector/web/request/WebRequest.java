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
