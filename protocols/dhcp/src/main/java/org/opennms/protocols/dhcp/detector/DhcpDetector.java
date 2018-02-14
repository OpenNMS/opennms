/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.dhcp.detector;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.protocols.dhcp.detector.client.DhcpClient;
import org.opennms.protocols.dhcp.detector.request.DhcpRequest;
import org.opennms.protocols.dhcp.detector.response.DhcpResponse;

/**
 * <p>DhcpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class DhcpDetector extends BasicDetector<DhcpRequest, DhcpResponse> {
    
    private static final int DEFAULT_RETRY = 0;
    private static final int DEFAULT_TIMEOUT = 3000;
    
    /**
     * <p>Constructor for DhcpDetector.</p>
     */
    public DhcpDetector() {
        super("DHCP", 0);
        setTimeout(DEFAULT_TIMEOUT);
        setRetries(DEFAULT_RETRY);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(responseTimeGreaterThan(-1));
    }

    private static ResponseValidator<DhcpResponse> responseTimeGreaterThan(final long num) {
        return new ResponseValidator<DhcpResponse>(){

            @Override
            public boolean validate(DhcpResponse response) {
                return response.validate(num);
            }
            
        };
    }

    /** {@inheritDoc} */
    @Override
    protected Client<DhcpRequest, DhcpResponse> getClient() {
        DhcpClient client = new DhcpClient();
        client.setRetries(1);
        return client;
    }

    
}
