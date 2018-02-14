/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
