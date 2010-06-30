/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.detector.loop;

import org.opennms.netmgt.provision.detector.loop.client.LoopClient;
import org.opennms.netmgt.provision.detector.loop.response.LoopResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>LoopDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class LoopDetector extends BasicDetector<LineOrientedRequest, LoopResponse> {
    private static final String DEFAULT_SERVICE_NAME = "LOOP";
    private static final int DEFAULT_PORT = 0;
    
    private String m_ipMatch;
    private boolean m_isSupported = true;
    
    /**
     * <p>Constructor for LoopDetector.</p>
     */
    public LoopDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, LoopResponse> getClient() {
        LoopClient loopClient = new LoopClient();
        loopClient.setSupported(isSupported());
        return loopClient;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(ipMatches(getIpMatch()));
    }

    private ResponseValidator<LoopResponse> ipMatches(final String ipAddr) {
        
        return new ResponseValidator<LoopResponse>(){

            public boolean validate(LoopResponse response) throws Exception {
                return response.validateIPMatch(ipAddr);
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
