/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemInfoUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SystemInfoUtils.class);

    public static final String OPENNMS_INSTANCE_ID_SYS_PROP = "org.opennms.instance.id";
    public static final String DEFAULT_INSTANCE_ID = "OpenNMS";

    private static final String s_instanceId;
    private static final String s_displayVersion;
    private static final String s_version;
    private static String s_packageName;
    private static String s_packageDescription;

    static {
        s_instanceId =  System.getProperty(OPENNMS_INSTANCE_ID_SYS_PROP, DEFAULT_INSTANCE_ID);

        s_displayVersion = System.getProperty("version.display", "");
        final Pattern versionPattern = Pattern.compile("^(\\d+\\.\\d+\\.\\d+).*?$");
        final Matcher m = versionPattern.matcher(s_displayVersion);
        if (m.matches()) {
            s_version = m.group(1);
        } else {
            s_version = s_displayVersion;
        }

        final InputStream installerProperties = SystemInfoUtils.class.getResourceAsStream("/installer.properties");
        if (installerProperties != null) {
            final Properties props = new Properties();
            try {
                props.load(installerProperties);
                installerProperties.close();
                s_packageName = (String)props.get("install.package.name");
                s_packageDescription = (String)props.get("install.package.description");
            } catch (final IOException e) {
                LOG.info("Unable to read from installer.properties in the classpath.", e);
            }
        }
    }

    /**
     * Retrieves the instance id of the current system.
     *
     * This value is used to identify OpenNMS instances in environments
     * where multiple systems share the same infrastructure.
     *
     * Defaults to "OpenNMS", but can be altered by setting the "org.opennms.instance.id" system property.
     *
     * @return instance id string
     */
    public static String getInstanceId() {
        return s_instanceId;
    }

    /**
     * Retrieves the version of the current system i.e:
     * <ul>
     * <li>17.0.0
     * <li>2015.1.1
     * </ul>
     * @return version string
     */
    public String getVersion() {
        return s_version;
    }

    /**
     * Retrieves the displaying version of the current system i.e:
     * <ul>
     * <li>17.0.0
     * <li>2015.1.1
     * </ul>
     * This is typically equal to the string returned by getVersion(), but may differ.
     * @return display version string
     */
    public String getDisplayVersion() {
        return s_displayVersion;
    }

    /**
     * Retrieves the package name of the current system i.e:
     * <ul>
     * <li>opennms
     * <li>meridian
     * </ul>
     * @return package name
     */
    public String getPackageName() {
        return s_packageName;
    }

    /**
     * Retrieves the package description of the current system i.e:
     * <ul>
     * <li>OpenNMS
     * <li>OpenNMS Meridian
     * </ul>
     * @return package name
     */
    public String getPackageDescription() {
        return s_packageDescription;
    }
}
