/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.smb;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.smb.client.SmbClient;
import org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
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
    
    private static ResponseValidator<NbtAddressResponse> validateAddressIsNotSame(){
        return new ResponseValidator<NbtAddressResponse>() {

            @Override
            public boolean validate(NbtAddressResponse response) {
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
