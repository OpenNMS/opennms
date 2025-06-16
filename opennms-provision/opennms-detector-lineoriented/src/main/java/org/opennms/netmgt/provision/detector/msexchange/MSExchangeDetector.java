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
package org.opennms.netmgt.provision.detector.msexchange;

import org.opennms.netmgt.provision.detector.msexchange.client.MSExchangeDetectorClient;
import org.opennms.netmgt.provision.detector.msexchange.response.MSExchangeResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;


/**
 * <p>MSExchangeDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class MSExchangeDetector extends BasicDetector<LineOrientedRequest, MSExchangeResponse> {
    
    private static String SERVICE_NAME = "MSExchange";
    private static String DEFAULT_BANNER = "Microsoft Exchange";
    
    private static int DEFAULT_POP3_PORT = 110;
    private static int DEFAULT_IMAP_PORT = 143;
    
    private int m_pop3Port;
    private int m_imapPort;
    
    /**
     * <p>Constructor for MSExchangeDetector.</p>
     */
    public MSExchangeDetector() {
        super(SERVICE_NAME, 0);
        setPop3Port(DEFAULT_POP3_PORT);
        setImapPort(DEFAULT_IMAP_PORT);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, MSExchangeResponse> getClient() {
        final MSExchangeDetectorClient client = new MSExchangeDetectorClient();
        client.setImapPort(getImapPort());
        client.setPop3Port(getPop3Port());
        return client;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(find(DEFAULT_BANNER));
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    protected static ResponseValidator<MSExchangeResponse> find(final String regex){
        return new ResponseValidator<MSExchangeResponse>() {

            @Override
            public boolean validate(final MSExchangeResponse response) {
                return response.contains(regex);
            }
          
            
        };
    }

    /**
     * <p>setPop3Port</p>
     *
     * @param pop3Port a int.
     */
    public void setPop3Port(final int pop3Port) {
        m_pop3Port = pop3Port;
    }

    /**
     * <p>getPop3Port</p>
     *
     * @return a int.
     */
    public int getPop3Port() {
        return m_pop3Port;
    }

    /**
     * <p>setImapPort</p>
     *
     * @param imapPort a int.
     */
    public void setImapPort(final int imapPort) {
        m_imapPort = imapPort;
    }

    /**
     * <p>getImapPort</p>
     *
     * @return a int.
     */
    public int getImapPort() {
        return m_imapPort;
    }

}
