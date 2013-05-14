/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.msexchange;

import org.opennms.netmgt.provision.detector.msexchange.client.MSExchangeDetectorClient;
import org.opennms.netmgt.provision.detector.msexchange.response.MSExchangeResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>MSExchangeDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
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
    protected MSExchangeDetector() {
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
