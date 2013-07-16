package org.opennms.core.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author <a mailto:seth@opennms.org>Seth Leger</a>
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
            TrustManager[] trustManagers = { new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException {
                    // Perform no checks
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException {
                    // Perform no checks
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }}
            };
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