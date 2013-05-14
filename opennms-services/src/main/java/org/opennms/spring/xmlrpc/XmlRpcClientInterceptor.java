/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
 * <p>XmlRpcClientInterceptor class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class XmlRpcClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor {

    SecureXmlRpcClient client;
    String serviceName;
    boolean secure = false;
    
    /**
     * <p>Setter for the field <code>serviceName</code>.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /**
     * <p>Getter for the field <code>serviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return this.serviceName;
    }
    
    /** {@inheritDoc} */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String methodName = methodInvocation.getMethod().getName();
        Object[] args = methodInvocation.getArguments();
        Vector<Object> parms = new Vector<Object>(args == null ? Collections.emptyList() : Arrays.asList(args));
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
                
            } catch (Throwable e) {
                throw new RemoteLookupFailureException("Invalid url ", e);
            }
        }
        
        return client;
    }


}
