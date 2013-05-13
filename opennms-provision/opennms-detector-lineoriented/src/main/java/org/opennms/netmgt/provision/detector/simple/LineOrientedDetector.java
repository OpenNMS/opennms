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

package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.LineOrientedClient;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>Abstract LineOrientedDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class LineOrientedDetector extends BasicDetector<LineOrientedRequest, LineOrientedResponse> {

    /**
     * <p>Constructor for LineOrientedDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected LineOrientedDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>Constructor for LineOrientedDetector.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    protected LineOrientedDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);

    }

    /**
     * <p>startsWith</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> startsWith(final String pattern) {
        return new ResponseValidator<LineOrientedResponse>() {
            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.startsWith(pattern);
            }
            
        };
    }
    
    /**
     * <p>equals</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> equals(final String pattern) {
        return new ResponseValidator<LineOrientedResponse>() {
            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.equals(pattern);
            }
            
        };
    }
    
    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> matches(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.matches(regex);
            }
            
        };
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> find(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.find(regex);
            }
          
            
        };
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    public LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }
    
    /**
     * <p>expectClose</p>
     */
    public void expectClose() {
        send(LineOrientedRequest.Null, equals(null));
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, LineOrientedResponse> getClient() {
        return new LineOrientedClient();
    }

}
