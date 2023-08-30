/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.javamail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
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
    public static synchronized Properties getProperties() throws IOException {
        LOG.debug("JavaMailConfig: Loading javamail properties");
        Properties properties = new Properties();
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.JAVA_MAIL_CONFIG_FILE_NAME);
        InputStream in = new FileInputStream(configFile);
        properties.load(in);
        in.close();
        return interpolate(properties);
    }

    private static Properties interpolate(final Properties properties, final String key, final Scope scope) {
        final String value = properties.getProperty(key);

        if (value != null) {
            properties.put(key, Interpolator.interpolate(value, scope).output);
        }

        return properties;
    }

    private static Properties interpolate(final Properties properties) {
        final Scope scope = getSecureCredentialsScope();

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
