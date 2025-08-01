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

import java.io.BufferedReader;
import java.io.Writer;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.StringBuilder;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:seth@opennms.org">Seth</a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public abstract class SocketUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(SocketUtils.class);

    public static Socket wrapSocketInSslContext(Socket socket) throws IOException {
        return wrapSocketInSslContext(socket, null, null);
    }

    public static SSLSocket wrapSocketInSslContext(Socket socket, String protocol) throws IOException {
        return wrapSocketInSslContext(socket, protocol, null);
    }

    public static SSLSocket wrapSocketInSslContext(Socket socket, String protocol, String[] cipherSuites) throws IOException {
        TrustManager[] tm = { new RelaxedX509ExtendedTrustManager() };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance(protocol == null ? "SSL" : protocol);
            sslContext.init(null, tm, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
        	LOG.error("wrapSocket: Error wrapping socket, throwing runtime exception", e);
            throw new IllegalStateException("No such algorithm in SSLSocketFactory: " + e);
        } catch (KeyManagementException e) {
        	LOG.error("wrapSocket: Error wrapping socket, throwing runtime exception", e);
            throw new IllegalStateException("Key management exception in SSLSocketFactory: " + e);
        }
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        InetAddress inetAddress = socket.getInetAddress();
        String hostAddress = InetAddressUtils.str(inetAddress);
        SSLSocket wrappedSocket = (SSLSocket) socketFactory.createSocket(socket, hostAddress, socket.getPort(), true);
        if (cipherSuites != null && cipherSuites.length > 0) {
            wrappedSocket.setEnabledCipherSuites(cipherSuites);
        }
        return wrappedSocket;
    }

    /**
     * <p>Writes {@code request} to {@code wr} then reads the response from {@code r} and validates that it matches the pattern provided in {@code responsePattern}.</p>
     * <p>To obtain {@code r} from a {@code java.util.Socket socket}:<br/> {@code new BufferedReader(new InputStreamReader(socket.getInputStream()));}</p>
     * <p>To obtain {@code wr} from a {@code java.util.Socket socket}:<br/> {@code new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));}</p>
     *
     * @param request the String to write to wr
     * @param responsePattern a String representing a regular expression pattern you expect to match the response received from {@code r} after writing request to wr
     * @param r the BufferedReader from which to get the response
     * @param wr the Writer on which the request is written
     */
    public static boolean validResponse(String request, String responsePattern, BufferedReader r, Writer wr) throws IOException {
        boolean validResponse = true;
        if (!Strings.isNullOrEmpty(request) && !Strings.isNullOrEmpty(responsePattern)) {
            String l = null;
            validResponse = false;
            LOG.debug("writing {}, hoping response matches /{}/", request, responsePattern);
            wr.write(request);
            wr.flush();
            Pattern p = Pattern.compile(responsePattern);
            StringBuilder sb = new StringBuilder();
            int i;
            try {
                while((i = r.read()) != -1) {
                    sb.append((char)i);
                }
            } catch (InterruptedIOException e) {
                LOG.debug("response was: {}", sb.toString());
            }
            validResponse = p.matcher(sb.toString()).matches();
        }
        return validResponse;
    }
}
