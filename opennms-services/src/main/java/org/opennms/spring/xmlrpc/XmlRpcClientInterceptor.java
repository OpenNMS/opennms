/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.spring.xmlrpc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.secure.SecureXmlRpcClient;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class XmlRpcClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor {

    SecureXmlRpcClient client;
    String serviceName;
    boolean secure = false;
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
    
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String methodName = methodInvocation.getMethod().getName();
        Object[] args = methodInvocation.getArguments();
        Vector parms = new Vector(args == null ? Collections.EMPTY_LIST : Arrays.asList(args));
        String serviceMethod = (serviceName == null ? methodName : serviceName+"."+methodName);
        try {
            return getClient().execute(serviceMethod, parms);
        } catch (XmlRpcException e) {
            if (e.code == XmlRpcConstants.FAULT_INVALID_DATA)
                throw new IllegalArgumentException(e.getMessage());
            else if (e.code == XmlRpcConstants.FAULT_INVALID_URL)
                throw new MalformedURLException(e.getMessage());
            else
                throw new RemoteAccessException(serviceMethod, e);
        } catch (IOException e) {
            throw new RemoteAccessException(serviceMethod, e);
        }
    }

    private XmlRpcClient getClient() {
        if (client == null) {
            try {
                client = new SecureXmlRpcClient(getServiceUrl());
                client.setup();
                
            } catch (Exception e) {
                throw new RemoteLookupFailureException("Invalid url ", e);
            }
        }
        
        return client;
    }


}
