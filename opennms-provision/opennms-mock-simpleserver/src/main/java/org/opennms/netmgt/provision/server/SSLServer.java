/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SSLServer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SSLServer extends SimpleServer{
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLServer.class);
    
    /** Constant <code>DEFAULT_TESTING_PORT=7070</code> */
    public static final int DEFAULT_TESTING_PORT = 7070;
    /** Constant <code>DEFAULT_PASSWORD="123456"</code> */
    public static final String DEFAULT_PASSWORD = "123456";
    /** Constant <code>DEFAULT_PATH_TO_KEY_STORE="src/main/resources/org/opennms/netmgt/p"{trunked}</code> */
    public static final String DEFAULT_PATH_TO_KEY_STORE = "src/main/resources/org/opennms/netmgt/provision/server/mySrvKeystore";
    /** Constant <code>DEFAULT_KEY_MANAGER_ALGORITHM="SunX509"</code> */
    public static final String DEFAULT_KEY_MANAGER_ALGORITHM = "SunX509";
    /** Constant <code>DEFAULT_KEY_MANAGER_PROVIDER="SunJSSE"</code> */
    public static final String DEFAULT_KEY_MANAGER_PROVIDER = "SunJSSE";
    /** Constant <code>DEFAULT_SSL_CONTEXT_PROTOCOL="SSL"</code> */
    public static final String DEFAULT_SSL_CONTEXT_PROTOCOL = "SSL";
    
    private int m_port = DEFAULT_TESTING_PORT;
    private String m_password = DEFAULT_PASSWORD;
    private String m_pathToKeyStore = DEFAULT_PATH_TO_KEY_STORE;
    private String m_keyManagerAlgorithm = DEFAULT_KEY_MANAGER_ALGORITHM;
    private String m_keyManagerProvider = DEFAULT_KEY_MANAGER_PROVIDER;
    private String m_sslContextProtocol = DEFAULT_SSL_CONTEXT_PROTOCOL;    
    
    /**
     * <p>init</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void init() throws Exception {
        super.init();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(getKeyManagerAlgorithm(), getKeyManagerProvider());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = getPassword().toCharArray();
        
        java.io.FileInputStream fis = null;
        try {
        fis = new java.io.FileInputStream(getPathToKeyStore());
        ks.load(fis, password);
        } finally {
            if (fis != null) {
            fis.close();
            }
        }
        
        kmf.init(ks, password );
        KeyManager[] km = kmf.getKeyManagers();
        
        SSLContext sslContext = SSLContext.getInstance(getSslContextProtocol());
        sslContext.init(km, null, new SecureRandom());
        SSLServerSocketFactory serverFactory = sslContext.getServerSocketFactory();
        setServerSocket(serverFactory.createServerSocket(getPort()));
        onInit();
    }
    
    /**
     * <p>getRunnable</p>
     *
     * @return a {@link java.lang.Runnable} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    protected Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            @Override
            public void run(){
                try{
                    OutputStream out = null;
                    BufferedReader in = null;
                    try {
                        getServerSocket().setSoTimeout(getTimeout());
                        setSocket(getServerSocket().accept());
                        
                        if(getThreadSleepLength() > 0) { Thread.sleep(getThreadSleepLength()); }
                        getSocket().setSoTimeout(getTimeout());
                        
                        out = getSocket().getOutputStream();
                        if(getBanner() != null){sendBanner(out);};
                        
                        
                        in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                        attemptConversation(in, out);
                    } finally {
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(out);
                        getSocket().close();
                    }
                }catch(Throwable e){
                    throw new UndeclaredThrowableException(e);
                } finally {
                    try {
                        stopServer();
                    } catch (final IOException e) {
                        LOG.debug("unable to stop server", e);
                    }
                }
            }
            
        };
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * <p>setPathToKeyStore</p>
     *
     * @param pathToKeyStore a {@link java.lang.String} object.
     */
    public void setPathToKeyStore(String pathToKeyStore) {
        m_pathToKeyStore = pathToKeyStore;
    }

    /**
     * <p>getPathToKeyStore</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPathToKeyStore() {
        return m_pathToKeyStore;
    }

    /**
     * <p>setKeyManagerAlgorithm</p>
     *
     * @param keyManagerAlgorithm a {@link java.lang.String} object.
     */
    public void setKeyManagerAlgorithm(String keyManagerAlgorithm) {
        m_keyManagerAlgorithm = keyManagerAlgorithm;
    }

    /**
     * <p>getKeyManagerAlgorithm</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKeyManagerAlgorithm() {
        return m_keyManagerAlgorithm;
    }

    /**
     * <p>setKeyManagerProvider</p>
     *
     * @param keyManagerProvider a {@link java.lang.String} object.
     */
    public void setKeyManagerProvider(String keyManagerProvider) {
        m_keyManagerProvider = keyManagerProvider;
    }

    /**
     * <p>getKeyManagerProvider</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKeyManagerProvider() {
        return m_keyManagerProvider;
    }

    /**
     * <p>setSslContextProtocol</p>
     *
     * @param sslContextProtocol a {@link java.lang.String} object.
     */
    public void setSslContextProtocol(String sslContextProtocol) {
        m_sslContextProtocol = sslContextProtocol;
    }

    /**
     * <p>getSslContextProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslContextProtocol() {
        return m_sslContextProtocol;
    }
}
