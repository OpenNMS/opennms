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
package org.opennms.core.web;

import static org.opennms.core.web.HttpClientWrapperConfigHelper.PARAMETER_KEYS.useSystemProxy;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientWrapperConfigHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientWrapperConfigHelper.class);

    public enum PARAMETER_KEYS {
        useSystemProxy("use-system-proxy"),
        keyStore("key-store"),
        keyStoreType("key-store-type"),
        keyStorePassword("key-store-password"),
        keyPassword("key-password"),
        trustStore("trust-store"),
        trustStoreType("trust-store-type"),
        trustStorePassword("trust-store-password"),
        hostnameVerification("hostname-verification");

        private String key;

        PARAMETER_KEYS(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static void setUseSystemProxyIfDefined(HttpClientWrapper httpClientWrapper, Map<String, Object> keyedParameters) {
        if (getKeyedBoolean(keyedParameters, useSystemProxy.getKey(), false)) {
            httpClientWrapper.useSystemProxySettings();
            LOG.debug("setting useSystemProxySettings() on HttpClientWrapper");
        }
    }

    /**
     * Configure a custom SSLContext for the "https" scheme on the given wrapper when a
     * keystore (client certificate, for mutual TLS) and/or truststore (custom trust
     * anchors) is present in the parameter map. Does nothing when neither is set.
     * Hostname verification is on by default and may be disabled with the
     * hostname-verification parameter.
     */
    public static void setSSLContextIfConfigured(HttpClientWrapper httpClientWrapper, Map<String, Object> keyedParameters) throws GeneralSecurityException, IOException {
        final String keyStorePath = getKeyedString(keyedParameters, PARAMETER_KEYS.keyStore.getKey());
        final String trustStorePath = getKeyedString(keyedParameters, PARAMETER_KEYS.trustStore.getKey());
        if (keyStorePath == null && trustStorePath == null) {
            return;
        }
        final SSLContext sslContext = SslContextFactory.buildSslContext(
                keyStorePath,
                getKeyedString(keyedParameters, PARAMETER_KEYS.keyStoreType.getKey()),
                getKeyedString(keyedParameters, PARAMETER_KEYS.keyStorePassword.getKey()),
                getKeyedString(keyedParameters, PARAMETER_KEYS.keyPassword.getKey()),
                trustStorePath,
                getKeyedString(keyedParameters, PARAMETER_KEYS.trustStoreType.getKey()),
                getKeyedString(keyedParameters, PARAMETER_KEYS.trustStorePassword.getKey()));
        final boolean verifyHostname = getKeyedBoolean(keyedParameters, PARAMETER_KEYS.hostnameVerification.getKey(), true);
        httpClientWrapper.setSSLContext("https", sslContext, verifyHostname);
        LOG.debug("setting SSLContext on HttpClientWrapper: keyStore={}, trustStore={}, verifyHostname={}", keyStorePath, trustStorePath, verifyHostname);
    }

    private static String getKeyedString(final Map<String, Object> map, final String key) {
        if (map == null) return null;
        final Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }

    // TODO: silly to pull in org.opennms.core.lib just for this, refactor org.opennms.core.utils.ParameterMap someday
    private static boolean getKeyedBoolean(final Map<String, Object> map, final String key, final boolean defaultValue) {
        if (map == null) return defaultValue;

        final Object value = map.get(key);
        if (value == null) return defaultValue;

        if (value instanceof String) {
            return Boolean.valueOf((String)value);
        }
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }

        return defaultValue;
    }

}
