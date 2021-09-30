/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;

/**
 * This class supports experiments with TLS support.
 * <p>
 * It implements a server that can support different TLS protocols.
 * <p>
 * It implements a couple of test methods that check if a client can connect using a certain TLS protocol.
 * <p>
 * Some of the security related code is taken from the example code of the (great) book
 * "Java Cryptography: Tools and Techniques"
 * (cf. https://www.bouncycastle.org/java-crypto-tools-src.zip)
 */
public class TlsExperiment {

    static {
        System.out.println("vendor : " + System.getProperty("java.vendor"));
        System.out.println("version: " + System.getProperty("java.version"));
    }

    // starts an echo server that waits for connections
    // -> can be started using maven by using the command below
    // -> the `exec.args` command parameter contains a space separated list of supported protocols
    //
    // mvn -pl features/poller/monitors/core -Dexec.args="TLSv1.1 TLSv1.2 TLSv1.3" -Dexec.mainClass=org.opennms.netmgt.poller.monitors.TlsTest -Dexec.classpathScope=test test-compile exec:java
    public static void main(String[] args) throws Exception {
        String[] supportedProtocols;
        if (args.length != 0) {
            supportedProtocols = args;
        } else {
            supportedProtocols = new String[]{"TLSv1.3"};
        }
        TestServerSocketFactory.SUPPORTED_PROTOCOLS = supportedProtocols;
        ServerSocket serverSocket = TEST_SERVER_SOCKET_FACTORY.createServerSocket(PORT);
        System.out.println("server started");
        System.out.println("supported protocols: " + String.join(", ", supportedProtocols));
        while (true) {
            try {
                acceptAndHandle(serverSocket, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testKeyGenerationAndRestoration() throws Exception {
        KeyPair kp = generateECKeyPair();
        String encodedPub = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
        String encodedPriv = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
        System.out.println("public : " + encodedPub);
        System.out.println("private: " + encodedPriv);
        KeyPair restoredKp = keyPair(encodedPub, encodedPriv);
        assertEquals("restored public key differs", kp.getPublic(), restoredKp.getPublic());
        assertEquals("restored private key differs", kp.getPrivate(), restoredKp.getPrivate());
    }

    @Test
    public void testTls12() throws Exception {
        TestServerSocketFactory.SUPPORTED_PROTOCOLS = new String[]{"TLSv1.2"};
        startServerAndTestCommunication();
    }

    @Test
    public void testTls13() throws Exception {
        TestServerSocketFactory.SUPPORTED_PROTOCOLS = new String[]{"TLSv1.3"};
        startServerAndTestCommunication();
    }

    @Test
    public void testCommunication() throws Exception {
        // requires a separately started server (see the main method above)
        communicate();
    }

    void startServerAndTestCommunication() throws Exception {
        CountDownLatch latch = startServer();
        communicate();
        latch.await();
    }

    static CountDownLatch startServer() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        ServerSocket serverSocket = TEST_SERVER_SOCKET_FACTORY.createServerSocket(PORT);
        Runnable server = () -> {
            try {
                acceptAndHandle(serverSocket, false);
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(server).start();
        return latch;
    }

    private static void acceptAndHandle(ServerSocket serverSocket, boolean print) throws IOException {
        SSLSocket sslSock = (SSLSocket) serverSocket.accept();
        InputStream in = sslSock.getInputStream();
        OutputStream out = sslSock.getOutputStream();
        int ch = 0;
        do {
            ch = in.read();
            if (print) {
                System.out.print((char) ch);
            }
            out.write(ch);
        } while (ch != '!');
        System.out.println();
    }

    void communicate() throws Exception {
        SSLSocket clientSocket = (SSLSocket) TEST_CLIENT_SOCKER_FACTORY.createSocket("localhost", PORT);
        try {
            OutputStream out = clientSocket.getOutputStream();
            InputStream in = clientSocket.getInputStream();
            out.write("Hello World!".getBytes());
            int ch = 0;
            do {
                ch = in.read();
                System.out.print((char) ch);
            } while (ch != '!');
            System.out.println();
        } finally {
            clientSocket.close();
        }
    }

    static Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

    static String ALGORITHM = "EC";

    static Instant NOT_BEFORE = Instant.parse("2020-11-17T17:32:15.000Z");
    static Instant NOT_AFTER = NOT_BEFORE.plus(20 * 365, ChronoUnit.DAYS);

    static final char[] ID_STORE_PASSWORD = "passwd".toCharArray();

    static int PORT = 11111;

    // define a fixed key pair (generated by invoking the test `testKeyGenerationAndRestoration()` method)
    // -> when the GreenMail server is run in a separate JVM the test code running in another JVM must trust
    //    the self signed certificateof the GreenMail server
    // -> exactly the same self signed certificates are generated in both JVMs
    // -> the same keys must be used
    static String PRIVATE_KEY = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgGLeFc+fEA1ApICBnuVwxo9ki0ZxeWorbX27odOD08a6gCgYIKoZIzj0DAQehRANCAAQRCvuNNrpaYH4eVHKajIt4nJp2yGUIoYWPppW8A/nNd+S9DH9OYkkRJebY5PSHrU8nJJa1BGmIK32IoS0WB3EE";
    static String PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEQr7jTa6WmB+HlRymoyLeJyadshlCKGFj6aVvAP5zXfkvQx/TmJJESXm2OT0h61PJySWtQRpiCt9iKEtFgdxBA==";

    static final KeyStore IDENTITY_KEY_STORE;
    static final KeyStore TRUST_STORE;
    static final TestServerSocketFactory TEST_SERVER_SOCKET_FACTORY;
    static final TestClientSocketFactory TEST_CLIENT_SOCKER_FACTORY;

    static {
        try {
            IDENTITY_KEY_STORE = createIdentityKeyStore();
            TRUST_STORE = createTrustStore(IDENTITY_KEY_STORE);
            TEST_SERVER_SOCKET_FACTORY = new TestServerSocketFactory();
            TEST_CLIENT_SOCKER_FACTORY = new TestClientSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class TestServerSocketFactory extends SSLServerSocketFactory {

        // the supported protocols can be configured from the outside
        volatile static String[] SUPPORTED_PROTOCOLS = new String[]{"TLSv1.2"};

        private final SSLServerSocketFactory socketFactory;

        public TestServerSocketFactory() throws Exception {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyMgrFact = KeyManagerFactory.getInstance("PKIX");
            keyMgrFact.init(IDENTITY_KEY_STORE, ID_STORE_PASSWORD);
            sslContext.init(keyMgrFact.getKeyManagers(), null, null);
            socketFactory = sslContext.getServerSocketFactory();
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return socketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return socketFactory.getSupportedCipherSuites();
        }

        private ServerSocket restrictToTls13(ServerSocket socket) {
            ((SSLServerSocket) socket).setEnabledProtocols(SUPPORTED_PROTOCOLS);
            return socket;
        }

        @Override
        public ServerSocket createServerSocket(int i) throws IOException {
            return restrictToTls13(socketFactory.createServerSocket(i));
        }

        @Override
        public ServerSocket createServerSocket(int i, int i1) throws IOException {
            return restrictToTls13(socketFactory.createServerSocket(i, i1));
        }

        @Override
        public ServerSocket createServerSocket(int i, int i1, InetAddress inetAddress) throws IOException {
            return restrictToTls13(socketFactory.createServerSocket(i, i1, inetAddress));
        }

    }

    public static class TestClientSocketFactory extends SSLSocketFactory {

        private final SSLSocketFactory socketFactory;

        public TestClientSocketFactory() throws Exception {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustMgrFact = TrustManagerFactory.getInstance("PKIX");
            trustMgrFact.init(TRUST_STORE);
            sslContext.init(null, trustMgrFact.getTrustManagers(), null);
            socketFactory = sslContext.getSocketFactory();
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return socketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return socketFactory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
            return socketFactory.createSocket(socket, s, i, b);
        }

        @Override
        public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
            return socketFactory.createSocket(s, i);
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
            return socketFactory.createSocket(s, i, inetAddress, i1);
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            return socketFactory.createSocket(inetAddress, i);
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
            return socketFactory.createSocket(inetAddress, i, inetAddress1, i1);
        }
    }

    /**
     * Create a KeyStore containing a single key with a self-signed certificate.
     *
     * @return a KeyStore containing a single key with a self-signed certificate.
     */
    static KeyStore createIdentityKeyStore() throws Exception {
        PrivateCredential cred = createSelfSignedCredentials();

        KeyStore store = KeyStore.getInstance("JKS");

        store.load(null, null);

        store.setKeyEntry("identity", cred.getPrivateKey(), ID_STORE_PASSWORD, new Certificate[]{cred.getCertificate()});

        return store;
    }

    /**
     * Create a key store suitable for use as a trust store, containing only
     * the certificates associated with each alias in the passed in
     * credentialStore.
     *
     * @param credentialStore key store containing public/credentials.
     * @return a key store containing only certificates.
     */
    static KeyStore createTrustStore(KeyStore credentialStore) throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");

        store.load(null, null);

        for (Enumeration<String> en = credentialStore.aliases(); en.hasMoreElements(); ) {
            String alias = en.nextElement();

            store.setCertificateEntry(alias, credentialStore.getCertificate(alias));
        }

        return store;
    }

    /**
     * Create a private key with an associated self-signed certificate
     * returning them wrapped in an X500PrivateCredential
     * <p>
     * Note: We use generateECKeyPair() from chapter6.EcDsaUtils and
     * createTrustAnchor() from chapter8.JcaX509Certificate.
     *
     * @return an X500PrivateCredential containing the key and its certificate.
     */
    static PrivateCredential createSelfSignedCredentials() throws Exception {
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider());

        // always use the same key pair
        KeyPair selfSignedKp = fixedKeyPair();

        X509CertificateHolder selfSignedHldr = createTrustAnchor(selfSignedKp, "SHA256withECDSA");

        X509Certificate selfSignedCert = certConverter.getCertificate(selfSignedHldr);

        return new PrivateCredential(selfSignedCert, selfSignedKp.getPrivate());
    }

    /**
     * Build a sample self-signed V1 certificate to use as a trust anchor, or
     * root certificate.
     *
     * @param keyPair the key pair to use for signing and providing the
     *                public key.
     * @param sigAlg  the signature algorithm to sign the certificate with.
     * @return an X509CertificateHolder containing the V1 certificate.
     */
    static X509CertificateHolder createTrustAnchor(KeyPair keyPair, String sigAlg) throws Exception {
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.C, "AU")
                .addRDN(BCStyle.ST, "Victoria")
                .addRDN(BCStyle.L, "Melbourne")
                .addRDN(BCStyle.O, "The Legion of the Bouncy Castle")
                .addRDN(BCStyle.CN, "Demo Root Certificate");

        X500Name name = x500NameBld.build();

        X509v1CertificateBuilder certBldr = new JcaX509v1CertificateBuilder(
                name,
                new BigInteger("1", 10),
                new Date(NOT_BEFORE.toEpochMilli()),
                new Date(NOT_AFTER.toEpochMilli()),
                name,
                keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder(sigAlg)
                .setProvider(BOUNCY_CASTLE_PROVIDER).build(keyPair.getPrivate());

        return certBldr.build(signer);
    }

    /**
     * Generate a EC key pair on the passed in named curve.
     *
     * @param curveName the name of the curve to generate the key pair on.
     * @return a EC KeyPair
     */
    static KeyPair generateECKeyPair(String curveName) throws Exception {
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance(ALGORITHM, BOUNCY_CASTLE_PROVIDER);

        keyPair.initialize(new ECGenParameterSpec(curveName));

        return keyPair.generateKeyPair();
    }

    /**
     * Generate a EC key pair on the P-256 curve.
     *
     * @return a EC KeyPair
     */
    static KeyPair generateECKeyPair() throws Exception {
        return generateECKeyPair("P-256");
    }

    static KeyPair fixedKeyPair() throws Exception {
        return keyPair(PUBLIC_KEY, PRIVATE_KEY);
    }

    /**
     * Restore a key pair from the encoded representation of the keys
     */
    static KeyPair keyPair(String encodedPub, String encodedPriv) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM, BOUNCY_CASTLE_PROVIDER);
        PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(encodedPub)));
        PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encodedPriv)));
        return new KeyPair(pub, priv);
    }


    /**
     * Carrier class for a private key and its corresponding public key certificate.
     * <p>
     * The regular Java API also has javax.security.auth.x500.X500PrivateCredential,
     * this class is a basic replacement for that. There is also a slightly more
     * general class in the BC PKIX API - org.bouncycastle.pkix.PKIXIdentity.
     * </p>
     */
    static class PrivateCredential {
        private final X509Certificate certificate;
        private final PrivateKey privateKey;

        /**
         * Base constructor.
         *
         * @param certificate the public key certificate matching privateKey.
         * @param privateKey  the private key matching the certificate parameter.
         */
        public PrivateCredential(X509Certificate certificate, PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }
    }

}
