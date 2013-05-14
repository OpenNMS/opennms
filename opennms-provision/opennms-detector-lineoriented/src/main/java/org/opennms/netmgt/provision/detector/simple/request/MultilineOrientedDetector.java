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

package org.opennms.netmgt.provision.detector.simple.request;

import org.opennms.netmgt.provision.detector.simple.client.MultilineOrientedClient;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>Abstract MultilineOrientedDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class MultilineOrientedDetector extends BasicDetector<LineOrientedRequest, MultilineOrientedResponse> {

    /**
     * <p>Constructor for MultilineOrientedDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected MultilineOrientedDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>Constructor for MultilineOrientedDetector.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    protected MultilineOrientedDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    protected LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }
    
    /**
     * <p>expectClose</p>
     */
    protected void expectClose() {
        send(LineOrientedRequest.Null, equals(null));
        
    }
    
    /**
     * <p>equals</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<MultilineOrientedResponse> equals(final String pattern) {
        return new ResponseValidator<MultilineOrientedResponse>() {
            
            @Override
            public boolean validate(final MultilineOrientedResponse response) {
                return response.equals(pattern);
            }
            
        };
    }
    
    /**
     * <p>startsWith</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<MultilineOrientedResponse> startsWith(final String pattern){
        return new ResponseValidator<MultilineOrientedResponse>(){

            @Override
            public boolean validate(final MultilineOrientedResponse response) {
                return response.startsWith(pattern);
            }
            
        };
    }

    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, MultilineOrientedResponse> getClient() {
        return new MultilineOrientedClient();
    }

}
