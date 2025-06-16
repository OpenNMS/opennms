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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemInfoUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SystemInfoUtils.class);

    // Note: if you change the default instance ID, unit tests will fail unless you update
    // core/ipc/sink/kafka/server/src/main/resources/OSGI-INF/blueprint/blueprint-ipc-server.xml
    // as well
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
