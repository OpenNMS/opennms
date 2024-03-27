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
package org.opennms.netmgt.provision.detector.msexchange.response;


/**
 * <p>MSExchangeResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MSExchangeResponse {
    
    private String m_pop3Response;
    private String m_imapResponse;
    
    /**
     * <p>Constructor for MSExchangeResponse.</p>
     */
    public MSExchangeResponse() {}
    
    /**
     * <p>contains</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean contains(final String pattern) {
        return (getPop3Response()!= null && getPop3Response().indexOf(pattern) > -1) || (getImapResponse() != null &&getImapResponse().indexOf(pattern) > -1);
    }

    /**
     * <p>setPop3Response</p>
     *
     * @param ftpResponse a {@link java.lang.String} object.
     */
    public void setPop3Response(final String ftpResponse) {
        m_pop3Response = ftpResponse;
    }

    /**
     * <p>getPop3Response</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPop3Response() {
        return m_pop3Response;
    }

    /**
     * <p>setImapResponse</p>
     *
     * @param imapResponse a {@link java.lang.String} object.
     */
    public void setImapResponse(final String imapResponse) {
        m_imapResponse = imapResponse;
    }

    /**
     * <p>getImapResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getImapResponse() {
        return m_imapResponse;
    }

}
