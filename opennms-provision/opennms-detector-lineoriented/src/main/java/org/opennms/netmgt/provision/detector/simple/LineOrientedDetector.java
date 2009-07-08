/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.LineOrientedClient;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;

/**
 * @author Donald Desloge
 *
 */
public abstract class LineOrientedDetector extends BasicDetector<LineOrientedRequest, LineOrientedResponse> {

    protected LineOrientedDetector(String serviceName, int port) {
        super(serviceName, port);
    }
    
    /**
     * @param port
     * @param timeout
     * @param retries
     */
    protected LineOrientedDetector(String serviceName, int port, int timeout, int retries) {
        super(serviceName, port, timeout, retries);

    }

    public ResponseValidator<LineOrientedResponse> startsWith(final String pattern) {
        return new ResponseValidator<LineOrientedResponse>() {
            public boolean validate(LineOrientedResponse response) {
                return response.startsWith(pattern);
            }
            
        };
    }
    
    public ResponseValidator<LineOrientedResponse> equals(final String pattern) {
        return new ResponseValidator<LineOrientedResponse>() {
            public boolean validate(LineOrientedResponse response) {
                return response.equals(pattern);
            }
            
        };
    }
    
    public ResponseValidator<LineOrientedResponse> matches(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            public boolean validate(LineOrientedResponse response) {
                return response.matches(regex);
            }
            
        };
    }
    
    public ResponseValidator<LineOrientedResponse> find(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            public boolean validate(LineOrientedResponse response) {
                return response.find(regex);
            }
          
            
        };
    }
    
    public LineOrientedRequest request(String command) {
        return new LineOrientedRequest(command);
    }
    
    public void expectClose() {
        send(LineOrientedRequest.Null, equals(null));
    }
    
    @Override
    protected Client<LineOrientedRequest, LineOrientedResponse> getClient() {
        return new LineOrientedClient();
    }

}
