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
package org.opennms.netmgt.provision.detector.smb;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.smb.client.SmbClient;
import org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>SmbDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */

@Component
@Scope("prototype")
public class SmbDetector extends BasicDetector<LineOrientedRequest, NbtAddressResponse> {

    private static final String DEFAULT_SERVICE_NAME = "SMB";
    private static final int DEFAULT_RETRIES = 0;
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final int DEFAULT_PORT = 0;

    /**
     * Default constructor
     */
    public SmbDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Constructor for instantiating a non-default service name of this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public SmbDetector(String serviceName) {
        super(serviceName, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    
    /**
     * Constructor for overriding defaults
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param timeout a int.
     * @param retries a int.
     */
    public SmbDetector(String serviceName, int timeout, int retries) {
        super(serviceName, DEFAULT_PORT, timeout, retries);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(validateAddressIsNotSame());
        
    }
    
    private ResponseValidator<NbtAddressResponse> validateAddressIsNotSame(){
        return new ResponseValidator<NbtAddressResponse>() {

            public boolean validate(NbtAddressResponse response) throws Exception {
                return response.validateAddressIsNotSame();
            }
            
        };
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, NbtAddressResponse> getClient() {
        return new SmbClient();
    }


}
