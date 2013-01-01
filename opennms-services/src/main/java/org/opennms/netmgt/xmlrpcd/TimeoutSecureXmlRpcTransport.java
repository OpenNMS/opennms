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

/* package org.apache.xmlrpc; */
package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.xmlrpc.DefaultXmlRpcTransport;
import org.opennms.core.utils.LogUtils;

/**
 * TimeoutSecureXmlRpcTransport adds a read timeout to the url connection.
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeoutSecureXmlRpcTransport extends DefaultXmlRpcTransport {
    protected int m_timeout = 0;

    /**
     * Create a new TimeoutSecureXmlRpcTransport with the specified URL and
     * basic authorization string.
     *
     * @deprecated Use setBasicAuthentication instead of passing an encoded authentication String.
     * @param url the url to POST XML-RPC requests to.
     * @param auth the Base64 encoded HTTP Basic authentication value.
     * @param timeout a int.
     */
    public TimeoutSecureXmlRpcTransport(final URL url, final String auth, final int timeout) {
        super(url, auth);
        this.m_timeout = timeout;
    }

    /**
     * Create a new DefaultXmlRpcTransport with the specified URL.
     *
     * @param url the url to POST XML-RPC requests to.
     * @param timeout a int.
     */
    @SuppressWarnings("deprecation")
	public TimeoutSecureXmlRpcTransport(final URL url, final int timeout) {
        super(url, null);
        this.m_timeout = timeout;
    }

    /**
     * Sends the actual XMLRPC request.
     *
     * Taken from org.apache.xmlrpc.DefaultXmlRpcTransport with a socket
     * timeout added.
     *
     * @param request an array of byte.
     * @return a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public InputStream sendXmlRpc(final byte [] request) throws IOException {
        con = url.openConnection();
        LogUtils.debugf(this, "Setting read timeout to %d", m_timeout);
        con.setReadTimeout(m_timeout);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setAllowUserInteraction(false);
        con.setRequestProperty("Content-Length", Integer.toString(request.length));
        con.setRequestProperty("Content-Type", "text/xml");
        if (auth != null) {
            con.setRequestProperty("Authorization", "Basic " + auth);
        }
        OutputStream out = null;
        try {
        	out = con.getOutputStream();
            out.write(request);
            out.flush();
        } finally {
        	IOUtils.closeQuietly(out);
        	out = null;
        }
        return con.getInputStream();
    }

}
