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
package org.opennms.javamail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;

/**
 * Provides access to the default javamail configuration data.
 */
public abstract class JavaMailerConfig {
	
	private static final Logger LOG = LoggerFactory.getLogger(JavaMailerConfig.class);

    private static Scope secureCredentialsVaultScope;

    private static synchronized Scope getSecureCredentialsScope() {
        if (secureCredentialsVaultScope == null) {
            try {
                final EntityScopeProvider entityScopeProvider = BeanUtils.getBean("daoContext", "entityScopeProvider", EntityScopeProvider.class);

                if (entityScopeProvider != null) {
                    secureCredentialsVaultScope = entityScopeProvider.getScopeForScv();
                } else {
                    LOG.warn("JavaMailConfig: EntityScopeProvider is null, SecureCredentialsVault not available for metadata interpolation");
                }
            } catch (FatalBeanException e) {
                e.printStackTrace();
                LOG.warn("JavaMailConfig: Error retrieving EntityScopeProvider bean");
                secureCredentialsVaultScope = EmptyScope.EMPTY;
            }
        }

        return secureCredentialsVaultScope;
    }

    public static void setSecureCredentialsVaultScope(final Scope secureCredentialsVaultScope) {
        JavaMailerConfig.secureCredentialsVaultScope = secureCredentialsVaultScope;
    }

    /**
     * This loads the configuration file.
     *
     * @return a Properties object representing the configuration properties
     * @throws java.io.IOException if any.
     */
    public static synchronized Properties getProperties(final Scope scope) throws IOException {
        LOG.debug("JavaMailConfig: Loading javamail properties");
        Properties properties = new Properties();
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.JAVA_MAIL_CONFIG_FILE_NAME);
        InputStream in = new FileInputStream(configFile);
        properties.load(in);
        in.close();
        return interpolate(properties, scope);
    }

    public static synchronized Properties getProperties() throws IOException {
        return getProperties(getSecureCredentialsScope());
    }

    private static Properties interpolate(final Properties properties, final String key, final Scope scope) {
        final String value = properties.getProperty(key);

        if (value != null) {
            properties.put(key, Interpolator.interpolate(value, scope).output);
        }

        return properties;
    }

    private static Properties interpolate(final Properties properties, final Scope scope) {
        if (scope == null) {
            LOG.warn("JavaMailConfig: Scope is null, cannot interpolate metadata of properties");
            return properties;
        }

        interpolate(properties, "org.opennms.core.utils.authenticateUser", scope);
        interpolate(properties, "org.opennms.core.utils.authenticatePassword", scope);

        return properties;
    }

    public static String interpolate(final String string) {
        final Scope scope = getSecureCredentialsScope();

        if (scope == null) {
            LOG.warn("JavaMailConfig: Scope is null, cannot interpolate metadata of string");
            return string;
        }

        return Interpolator.interpolate(string, scope).output;
    }
}
