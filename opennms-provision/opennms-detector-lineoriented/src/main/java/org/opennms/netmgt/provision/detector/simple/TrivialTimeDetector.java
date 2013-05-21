/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.TrivialTimeClient;
import org.opennms.netmgt.provision.detector.simple.request.TrivialTimeRequest;
import org.opennms.netmgt.provision.detector.simple.response.TrivialTimeResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>TrivialTimeDetector class.</p>
 *
 * @author Alejandro Galue <agalue@sync.com.ve>
 * @version $Id: $
 */
@Component
@Scope("prototype")
public class TrivialTimeDetector extends BasicDetector<TrivialTimeRequest, TrivialTimeResponse> {

    private static final String DEFAULT_SERVICE_NAME = "TrivialTime";

    /**
     * Default layer-4 protocol to use
     */
    private static final String DEFAULT_PROTOCOL = "tcp"; // Use TCP by default

    /**
     * Default port.
     */
    private static final int DEFAULT_PORT = 37;

    /**
     * Default permissible skew between the remote and local clocks
     */
    private static final int DEFAULT_ALLOWED_SKEW = 30; // 30 second skew

    private String protocol = DEFAULT_PROTOCOL;
    private int allowedSkew = DEFAULT_ALLOWED_SKEW;

    /**
     * Default constructor
     */
    public TrivialTimeDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public TrivialTimeDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(request(), validate());
    }

    private TrivialTimeRequest request() {
        return new TrivialTimeRequest();
    }

    private static ResponseValidator<TrivialTimeResponse> validate() {
        return new ResponseValidator<TrivialTimeResponse>() {
            @Override
            public boolean validate(final TrivialTimeResponse response) {
                return response.isAvailable();
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected Client<TrivialTimeRequest, TrivialTimeResponse> getClient() {
        final TrivialTimeClient client = new TrivialTimeClient(getProtocol(), getAllowedSkew());
        client.setRetries(getRetries());
        return client;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getAllowedSkew() {
        return allowedSkew;
    }

    public void setAllowedSkew(int allowedSkew) {
        this.allowedSkew = allowedSkew;
    }

}
