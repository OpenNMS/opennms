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

public class SSLServer extends SimpleServer{
    
    public static final int DEFAULT_TESTING_PORT = 7070;
    public static final String DEFAULT_PASSWORD = "123456";
    public static final String DEFAULT_PATH_TO_KEY_STORE = "src/main/resources/org/opennms/netmgt/provision/server/mySrvKeystore";
    public static final String DEFAULT_KEY_MANAGER_ALGORITHM = "SunX509";
    public static final String DEFAULT_KEY_MANAGER_PROVIDER = "SunJSSE";
    public static final String DEFAULT_SSL_CONTEXT_PROTOCOL = "SSL";
    
    private int m_port = DEFAULT_TESTING_PORT;
    private String m_password = DEFAULT_PASSWORD;
    private String m_pathToKeyStore = DEFAULT_PATH_TO_KEY_STORE;
    private String m_keyManagerAlgorithm = DEFAULT_KEY_MANAGER_ALGORITHM;
    private String m_keyManagerProvider = DEFAULT_KEY_MANAGER_PROVIDER;
    private String m_sslContextProtocol = DEFAULT_SSL_CONTEXT_PROTOCOL;    
    
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
    
    protected Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            public void run(){
                try{
                    getServerSocket().setSoTimeout(getTimeout());
                    setSocket(getServerSocket().accept());
                    
                    if(getThreadSleepLength() > 0) { Thread.sleep(getThreadSleepLength()); }
                    getSocket().setSoTimeout(getTimeout());
                    
                    OutputStream out = getSocket().getOutputStream();
                    if(getBanner() != null){sendBanner(out);};
                    
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                    attemptConversation(in, out);

                }catch(Exception e){
                    throw new UndeclaredThrowableException(e);
                } finally {
                    try {
                        stopServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        };
    }

    public void setPort(int port) {
        m_port = port;
    }

    public int getPort() {
        return m_port;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPathToKeyStore(String pathToKeyStore) {
        m_pathToKeyStore = pathToKeyStore;
    }

    public String getPathToKeyStore() {
        return m_pathToKeyStore;
    }

    public void setKeyManagerAlgorithm(String keyManagerAlgorithm) {
        m_keyManagerAlgorithm = keyManagerAlgorithm;
    }

    public String getKeyManagerAlgorithm() {
        return m_keyManagerAlgorithm;
    }

    public void setKeyManagerProvider(String keyManagerProvider) {
        m_keyManagerProvider = keyManagerProvider;
    }

    public String getKeyManagerProvider() {
        return m_keyManagerProvider;
    }

    public void setSslContextProtocol(String sslContextProtocol) {
        m_sslContextProtocol = sslContextProtocol;
    }

    public String getSslContextProtocol() {
        return m_sslContextProtocol;
    }
}
