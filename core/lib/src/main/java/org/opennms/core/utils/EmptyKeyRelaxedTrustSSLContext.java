/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:seth@opennms.org">Seth Leger</a>
 */
public final class EmptyKeyRelaxedTrustSSLContext extends SSLContextSpi {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmptyKeyRelaxedTrustSSLContext.class);
	
    public static final String ALGORITHM = "EmptyKeyRelaxedTrust";

    private final SSLContext m_delegate;

    public EmptyKeyRelaxedTrustSSLContext() {
        SSLContext customContext = null;

        try {
            // Use a blank list of key managers so no SSL keys will be available
            KeyManager[] keyManager = null;
            TrustManager[] trustManagers = { new AnyServerX509TrustManager() };
            customContext = SSLContext.getInstance("SSL");
            customContext.init(keyManager, trustManagers, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            // Should never happen
        	LOG.error("Could not find SSL algorithm in JVM", e);
        } catch (KeyManagementException e) {
            // Should never happen
        	LOG.error("Could not find SSL algorithm in JVM", e);
        }
        m_delegate = customContext;
    }

    @Override
    protected SSLEngine engineCreateSSLEngine() {
        return m_delegate.createSSLEngine();
    }

    @Override
    protected SSLEngine engineCreateSSLEngine(String arg0, int arg1) {
        return m_delegate.createSSLEngine(arg0, arg1);
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        return m_delegate.getClientSessionContext();
    }

    @Override
    protected SSLSessionContext engineGetServerSessionContext() {
        return m_delegate.getServerSessionContext();
    }

    @Override
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        return m_delegate.getServerSocketFactory();
    }

    @Override
    protected javax.net.ssl.SSLSocketFactory engineGetSocketFactory() {
        return m_delegate.getSocketFactory();
    }

    @Override
    protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom arg2) throws KeyManagementException {
        // Don't do anything, we've already initialized everything in the constructor
    }
}