/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.camel;

import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.opennms.core.utils.AnyServerX509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHttpClientConfigurer implements HttpClientConfigurer {
    private static final KeyManager[] EMPTY_KEYMANAGER_ARRAY = new KeyManager[0];
    private static final Logger LOG = LoggerFactory.getLogger(CustomHttpClientConfigurer.class);

    private String m_username = "admin";
    private String m_password = "admin";

    @Override
    public void configureHttpClient(final HttpClient client) {
        try {
            final SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(EMPTY_KEYMANAGER_ARRAY, new TrustManager[] { new AnyServerX509TrustManager() }, new SecureRandom());
            SSLContext.setDefault(ctx);

            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getParams().setAuthenticationPreemptive(true);
            LOG.debug("Configuring HTTP client with modified trust manager, username={}, password=xxxxxxxx", getUsername());
        } catch (final Exception e) {
            throw new CustomConfigurerException(e);
        }
    }


    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }


}
