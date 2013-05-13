/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opennms.netmgt.xmlrpcd;

import java.net.URL;

import org.apache.xmlrpc.DefaultXmlRpcTransport;
import org.apache.xmlrpc.DefaultXmlRpcTransportFactory;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcTransport;

/**
 * Derived from DefaultXmlRpcTransportFactory.
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeoutSecureXmlRpcTransportFactory extends DefaultXmlRpcTransportFactory 
{
    // Default properties for new http transports
    protected int timeout = 0;

    /**
     * <p>Constructor for TimeoutSecureXmlRpcTransportFactory.</p>
     *
     * @param url a {@link java.net.URL} object.
     * @param timeout a int.
     */
    public TimeoutSecureXmlRpcTransportFactory(final URL url, final int timeout) {
        super(url);
        this.timeout = timeout;
    }
    
    /**
     * Contructor taking a Base64 encoded Basic Authentication string.
     *
     * @deprecated use setBasicAuthentication method instead
     * @param url a {@link java.net.URL} object.
     * @param auth a {@link java.lang.String} object.
     * @param timeout a int.
     */
    public TimeoutSecureXmlRpcTransportFactory(final URL url, final String auth, final int timeout) {
        this(url, timeout);
        this.auth = auth;
    }
    
    /**
     * <p>createTransport</p>
     *
     * @return a {@link org.apache.xmlrpc.XmlRpcTransport} object.
     * @throws org.apache.xmlrpc.XmlRpcClientException if any.
     */
    @SuppressWarnings("deprecation")
    @Override
	public XmlRpcTransport createTransport() throws XmlRpcClientException {
        if ("https".equals(url.getProtocol())) {
            if (timeout > 0) 
            {
                throw new XmlRpcClientException("Timeouts not supported under https.", null);
            }
            return new DefaultXmlRpcTransport(url, auth);
        }
        
        return new TimeoutSecureXmlRpcTransport(url, auth, timeout);
    }
}
