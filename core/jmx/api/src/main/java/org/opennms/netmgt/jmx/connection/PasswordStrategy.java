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
package org.opennms.netmgt.jmx.connection;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.opennms.core.utils.AnyServerX509TrustManager;
import org.slf4j.LoggerFactory;

public interface PasswordStrategy {

    void apply(Map<String, Object> env, JmxConnectionConfig config);

    // Insecure connection with plain text credentials if given
    PasswordStrategy PASSWORD_CLEAR = (env, config) -> {
        if (config.hasCredentials()) {
            // Provide the credentials required by the server to successfully perform user authentication
            env.put("jmx.remote.credentials", config.getCredentials());
        }
    };

    // Secure connection with credentials if given
    PasswordStrategy SASL = (env, config) -> {
        try {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            final AnyServerX509TrustManager tm = new AnyServerX509TrustManager();
            final SSLContext ctx = SSLContext.getInstance("TLSv1");
            ctx.init(null, new TrustManager[]{tm}, null);

            final SSLSocketFactory ssf = ctx.getSocketFactory();
            env.put("jmx.remote.tls.socket.factory", ssf);
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException(ex);
        }

        // We don't need to add this provider manually... it is included in the JVM
        // by default in Java5+
        //
        // @see $JAVA_HOME/jre/lib/security/java.security
        //
        // Security.addProvider(new com.sun.security.sasl.Provider());
        env.put("jmx.remote.profiles", "TLS SASL/PLAIN");
        if (config.hasCredentials()) {
            final String[] creds = config.getCredentials();
            env.put("jmx.remote.credentials", creds);
        }
    };

    // Default strategy
    PasswordStrategy STANDARD = (env, config) -> {
        // If we have credentials we will apply them even if strategy is STANDARD
        if (config.hasCredentials()) {
            LoggerFactory.getLogger(PasswordStrategy.class).warn("PasswordStrategy is STANDARD but credentials are provided. Using PASSWORD_CLEAR instead.");
            PASSWORD_CLEAR.apply(env, config);
        }
    };
}
