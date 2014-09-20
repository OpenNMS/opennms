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

package org.opennms.netmgt.provision.detector.sms;

import org.opennms.netmgt.provision.detector.sms.client.SmsClient;
import org.opennms.netmgt.provision.detector.sms.response.SmsResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>SmsDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class SmsDetector extends BasicDetector<LineOrientedRequest, SmsResponse> {
    private static final String DEFAULT_SERVICE_NAME = "SMS";
    private static final int DEFAULT_PORT = 0;
    
    private String m_ipMatch;
    private boolean m_isSupported = true;
    
    /**
     * <p>Constructor for SmsDetector.</p>
     */
    public SmsDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, SmsResponse> getClient() {
        SmsClient loopClient = new SmsClient();
        loopClient.setSupported(isSupported());
        return loopClient;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(ipMatches(getIpMatch()));
    }

    private static ResponseValidator<SmsResponse> ipMatches(final String ipAddr) {
        
        return new ResponseValidator<SmsResponse>(){

            @Override
            public boolean validate(SmsResponse response) {
                return response.isSms(ipAddr);
            }
            
        };
    }

    /**
     * <p>setIpMatch</p>
     *
     * @param ipMatch a {@link java.lang.String} object.
     */
    public void setIpMatch(String ipMatch) {
        m_ipMatch = ipMatch;
    }

    /**
     * <p>getIpMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpMatch() {
        return m_ipMatch;
    }

    /**
     * <p>setSupported</p>
     *
     * @param isSupported a boolean.
     */
    public void setSupported(boolean isSupported) {
        m_isSupported = isSupported;
    }

    /**
     * <p>isSupported</p>
     *
     * @return a boolean.
     */
    public boolean isSupported() {
        return m_isSupported;
    }

}
